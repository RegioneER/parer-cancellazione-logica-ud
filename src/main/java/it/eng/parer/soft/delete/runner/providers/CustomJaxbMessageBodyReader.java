/*
 * Engineering Ingegneria Informatica S.p.A.
 *
 * Copyright (C) 2023 Regione Emilia-Romagna
 * <p/>
 * This program is free software: you can redistribute it and/or modify it under the terms of
 * the GNU Affero General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 * <p/>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <https://www.gnu.org/licenses/>.
 */

/**
 *
 */
package it.eng.parer.soft.delete.runner.providers;

import static it.eng.parer.soft.delete.beans.utils.Costanti.TMP_FILE_SUFFIX;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;
import javax.xml.validation.Schema;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.jboss.resteasy.reactive.common.util.StreamUtil;
import org.jboss.resteasy.reactive.server.spi.ServerRequestContext;

import io.quarkus.resteasy.reactive.jaxb.runtime.serialisers.ServerJaxbMessageBodyReader;
import it.eng.parer.soft.delete.beans.XmlSoftDeleteCache;
import it.eng.parer.soft.delete.beans.exceptions.AppGenericRuntimeException;
import it.eng.parer.soft.delete.beans.exceptions.xml.XmlReqNotWellFormedException;
import it.eng.parer.soft.delete.beans.exceptions.xml.XmlReqUnmarshalException;
import it.eng.parer.soft.delete.beans.utils.Costanti.ErrorCategory;
import it.eng.parer.soft.delete.beans.utils.UUIDMdcLogUtil;
import it.eng.parer.soft.delete.beans.utils.xml.WsXmlValidationEventHandler;
import it.eng.parer.soft.delete.beans.utils.xml.XmlUtils;
import it.eng.parer.ws.xml.richSoftDelete.RichiestaCancellazioneLogica;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.ext.Provider;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;

/*
 * Provider che estende JaxbMessageBodyReader per ampliarne la logica, aggiungendo : validazione con
 * xsd, gestione avanzata dell'encoding del body del messaggio (nell'XML in caso specificio).
 */
@Provider
public class CustomJaxbMessageBodyReader extends ServerJaxbMessageBodyReader {

    XmlSoftDeleteCache xmlSoftDeleteCache;

    @Inject
    public CustomJaxbMessageBodyReader(XmlSoftDeleteCache xmlSoftDeleteCache) {
        this.xmlSoftDeleteCache = xmlSoftDeleteCache;
    }

    private static final FileAttribute<Set<PosixFilePermission>> attr = PosixFilePermissions
            .asFileAttribute(PosixFilePermissions.fromString("rw-------"));

    @Override
    public RichiestaCancellazioneLogica readFrom(Class<Object> type, Type genericType,
            MediaType mediaType, ServerRequestContext context)
            throws WebApplicationException, IOException {
        final Path tmpFile = Files.createTempFile(UUIDMdcLogUtil.getUuid(), TMP_FILE_SUFFIX, attr);
        try (InputStream is = new FileInputStream(tmpFile.toFile())) {
            // manage encoding
            byte[] istobyte = IOUtils.toByteArray(context.getInputStream());
            String xml = new String(istobyte);
            String encoding = XmlUtils.getXmlEcondingDeclaration(xml).name();
            //
            FileUtils.writeByteArrayToFile(tmpFile.toFile(), xml.getBytes(encoding));

            // well formed -> XmlReqNotWellFormedException
            XmlUtils.validateXml(xml, encoding);
            // unmarshal
            return doReadFromWithValidation(is, xml);
        } catch (XMLStreamException | FactoryConfigurationError | XmlReqNotWellFormedException
                | XmlReqUnmarshalException e) {
            throw new AppGenericRuntimeException(e, ErrorCategory.VALIDATION_ERROR);
        } finally {
            Files.delete(tmpFile);
        }
    }

    private RichiestaCancellazioneLogica doReadFromWithValidation(InputStream entityStream,
            String xml) throws IOException, XmlReqUnmarshalException {
        RichiestaCancellazioneLogica entity = null;
        WsXmlValidationEventHandler ve = new WsXmlValidationEventHandler();
        if (checkInputStreamEmpty(entityStream)) {
            return null;
        }
        try {
            // validate and unmarshal
            JAXBContext jaxbContext = xmlSoftDeleteCache.getRichSoftDeleteCtx();
            Schema schema = xmlSoftDeleteCache.getRichSoftDeleteSchema();
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            unmarshaller.setSchema(schema);
            unmarshaller.setEventHandler(ve);
            entity = (RichiestaCancellazioneLogica) unmarshaller.unmarshal(entityStream);
        } catch (JAXBException e) {
            throw new XmlReqUnmarshalException(ve.getMessaggio(), e, xml);
        }
        // unmarshal
        return entity;
    }

    private boolean checkInputStreamEmpty(InputStream entityStream) throws IOException {
        return StreamUtil.isEmpty(entityStream) || entityStream.available() == 0;
    }
}
