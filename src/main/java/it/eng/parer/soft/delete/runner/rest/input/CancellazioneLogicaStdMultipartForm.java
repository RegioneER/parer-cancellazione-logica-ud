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
package it.eng.parer.soft.delete.runner.rest.input;

import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.media.SchemaProperty;
import org.jboss.resteasy.reactive.PartType;

import it.eng.parer.soft.delete.runner.util.ICancellazioneLogicaMultipartForm;
import it.eng.parer.ws.xml.richSoftDelete.RichiestaCancellazioneLogica;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.core.MediaType;

/*
 * Wrapper multipart-form con dati inviati da client. Presenti meccanismi di validazione da
 * javax.validation che verificano la presenza o meno dei campi all'interno del multipart scambiato.
 *
 * Campi: VERSIONE -> campo testo con versione ws/sip LOGINNAME -> campo testo con login utente
 * PASSWORD -> campo testo con password utente XMLSIP -> campo testo contenente l'xml (questo campo
 * viene sia portato su tipo String che su oggetto serializzato via JAXB)
 *
 *
 */
public class CancellazioneLogicaStdMultipartForm implements ICancellazioneLogicaMultipartForm {

    public CancellazioneLogicaStdMultipartForm() {
        super();
    }

    @Schema(type = SchemaType.STRING, required = true, example = "1.0", description = "Versione dell'xml versato (versione REQ)")
    @FormParam("VERSIONE")
    @PartType(MediaType.TEXT_PLAIN)
    @NotEmpty(message = "Necessario indicare il campo VERSIONE")
    public String VERSIONE;

    @Schema(type = SchemaType.STRING, required = true, description = "Userid dell'utente versatore")
    @FormParam("LOGINNAME")
    @PartType(MediaType.TEXT_PLAIN)
    @NotEmpty(message = "Necessario indicare il campo LOGINNAME")
    public String LOGINNAME;

    @Schema(type = SchemaType.STRING, format = "password", required = true, description = "Password dell'utente versatore")
    @FormParam("PASSWORD")
    @PartType(MediaType.TEXT_PLAIN)
    @NotEmpty(message = "Necessario indicare il campo PASSWORD")
    public String PASSWORD;

    @Schema(hidden = true)
    @FormParam("XMLREQ")
    @PartType(MediaType.APPLICATION_XML)
    public RichiestaCancellazioneLogica richiestaCancellazioneLogica;

    @Schema(type = SchemaType.OBJECT, properties = {
            @SchemaProperty(name = "RichiestaCancellazioneLogica", minLength = 1, examples = "<RichiestaCancellazioneLogica></RichiestaCancellazioneLogica>") }, required = true, description = "Xml versato")
    @FormParam("XMLREQ")
    @PartType(MediaType.TEXT_XML)
    @NotNull(message = "Necessario indicare il campo XMLREQ")
    public String XMLREQ;

}
