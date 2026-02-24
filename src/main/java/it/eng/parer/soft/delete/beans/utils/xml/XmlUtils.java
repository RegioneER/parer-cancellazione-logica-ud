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

package it.eng.parer.soft.delete.beans.utils.xml;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.xml.security.c14n.Canonicalizer;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import it.eng.parer.soft.delete.beans.exceptions.xml.XmlReqNotWellFormedException;
import it.eng.parer.soft.delete.beans.utils.Costanti.ErrorCategory;
import it.eng.parer.soft.delete.beans.exceptions.AppGenericRuntimeException;

public class XmlUtils {

    private XmlUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static Charset getXmlEcondingDeclaration(String xmlSip)
            throws XMLStreamException, FactoryConfigurationError {
        // prevent XXE
        XMLInputFactory factory = XMLInputFactory.newDefaultFactory(); // default instance
        // (jre)
        factory.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, ""); // Compliant
        factory.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, ""); // compliant
        //
        XMLStreamReader xmlStreamReader = factory.createXMLStreamReader(new StringReader(xmlSip));
        String encodingFromXMLDeclaration = xmlStreamReader.getCharacterEncodingScheme();
        return StringUtils.isNotBlank(encodingFromXMLDeclaration)
                ? Charset.forName(encodingFromXMLDeclaration)
                : StandardCharsets.UTF_8;
    }

    public static String unPrettyPrint(final String xmlSip) throws IOException, DocumentException {
        final StringWriter sw;
        final OutputFormat format = OutputFormat.createCompactFormat();
        // format
        format.setNewLineAfterDeclaration(false);
        final org.dom4j.Document document = DocumentHelper.parseText(xmlSip);
        sw = new StringWriter();
        final XMLWriter writer = new XMLWriter(sw, format);
        writer.write(document);

        return sw.toString();
    }

    public static String doCanonicalizzazioneXml(final String xml, final boolean unPrettyPrint)
            throws AppGenericRuntimeException {
        String xmlOut = null;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            // needed
            org.apache.xml.security.Init.init();
            //
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newDefaultInstance(); // default
            // instance
            // (jre)
            // to be compliant, completely disable DOCTYPE declaration:
            dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            // or completely disable external entities declarations:
            dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
            dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);

            dbf.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, ""); // Compliant
            dbf.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, ""); // Compliant

            // MAC#14681 fix per gestione encoding XML
            String encodingFromXMLDeclaration = XmlUtils.getXmlEcondingDeclaration(xml).name();
            InputSource in = new InputSource(
                    new ByteArrayInputStream(xml.getBytes(encodingFromXMLDeclaration)));
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(in);

            org.apache.xml.security.c14n.Canonicalizer canonicalizer = Canonicalizer
                    .getInstance(Canonicalizer.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);
            canonicalizer.canonicalizeSubtree(doc, baos);
            xmlOut = new String(baos.toByteArray(), StandardCharsets.UTF_8);
            if (unPrettyPrint) {
                xmlOut = XmlUtils.unPrettyPrint(xmlOut);
            }
        } catch (Exception e) {
            throw new AppGenericRuntimeException("Errore in fase di canonicalizzazione XML", e,
                    ErrorCategory.INTERNAL_ERROR);
        }
        return xmlOut;
    }

    public static void validateXml(final String xmlSip, final String encoding)
            throws XmlReqNotWellFormedException {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newDefaultInstance();
            // to be compliant, completely disable DOCTYPE declaration:
            dbFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            // or completely disable external entities declarations:
            dbFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            dbFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);

            dbFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            dbFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            dBuilder.parse(IOUtils.toInputStream(xmlSip, encoding));
        } catch (IllegalArgumentException | ParserConfigurationException | SAXException
                | IOException e) {
            throw new XmlReqNotWellFormedException(e, xmlSip);
        }
    }
}
