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

package it.eng.parer.soft.delete.beans.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import it.eng.parer.soft.delete.beans.IConfigurationDao;
import it.eng.parer.soft.delete.beans.IControlliSemanticiService;
import it.eng.parer.soft.delete.beans.IControlliWsService;
import it.eng.parer.soft.delete.beans.XmlSoftDeleteCache;
import it.eng.parer.soft.delete.beans.dto.InvioRichiestaCancellazioneLogicaExt;
import it.eng.parer.soft.delete.beans.dto.RispostaWSInvioRichiestaCancellazioneLogica;
import it.eng.parer.soft.delete.beans.dto.WSDescCancellazioneLogica;
import it.eng.parer.soft.delete.beans.dto.base.CSVersatore;
import it.eng.parer.soft.delete.beans.dto.base.IRispostaWS;
import it.eng.parer.soft.delete.beans.dto.base.RispostaControlli;
import it.eng.parer.soft.delete.beans.security.User;
import it.eng.parer.ws.xml.esitoRichSoftDelete.EsitoRichiestaCancellazioneLogica;
import it.eng.parer.ws.xml.esitoRichSoftDelete.EsitoRichiestaType;
import it.eng.parer.ws.xml.richSoftDelete.RichiestaCancellazioneLogica;
import it.eng.parer.ws.xml.richSoftDelete.RichiestaDiCancellazioneType;
import it.eng.parer.ws.xml.richSoftDelete.RichiestaType;
import it.eng.parer.ws.xml.richSoftDelete.TipoCancellazioneType;
import it.eng.parer.ws.xml.richSoftDelete.TipoRichiestaType;
import it.eng.parer.ws.xml.richSoftDelete.VersatoreType;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Unmarshaller;

@ExtendWith(MockitoExtension.class)
class CancellazioneLogicaParserServiceTest {

    @Mock
    private IControlliSemanticiService controlliSemanticiService;

    @Mock
    private IControlliWsService controlliWsService;

    @Mock
    private XmlSoftDeleteCache xmlSoftDeleteCache;

    @Mock
    private IConfigurationDao configurationDao;

    @Mock
    private JAXBContext jaxbContext;

    @Mock
    private Unmarshaller unmarshaller;

    @InjectMocks
    private CancellazioneLogicaParserService service;

    private RispostaWSInvioRichiestaCancellazioneLogica rispostaWs;
    private InvioRichiestaCancellazioneLogicaExt cancellazioneLogicaExt;
    private User mockUser;

    @BeforeEach
    void setup() {
        rispostaWs = new RispostaWSInvioRichiestaCancellazioneLogica();
        cancellazioneLogicaExt = new InvioRichiestaCancellazioneLogicaExt();

        mockUser = new User();
        mockUser.setIdUtente(10L);
        mockUser.setUsername("testuser");

        cancellazioneLogicaExt.setUtente(mockUser);
        cancellazioneLogicaExt.setVersioneWsChiamata("1.0");
        cancellazioneLogicaExt.setLoginName("testuser");
        cancellazioneLogicaExt.setDescrizione(new WSDescCancellazioneLogica());

        // Inizializza l'esito
        EsitoRichiestaCancellazioneLogica esito = new EsitoRichiestaCancellazioneLogica();
        esito.setEsitoRichiesta(new EsitoRichiestaType());
        rispostaWs.setEsitoRichiestaCancellazioneLogica(esito);
    }

    // ==================== parseXML_success ====================

    @Test
    void parseXML_success() throws Exception {
        // Crea XML valido
        String validXml = createValidXml();

        // Crea oggetto richiesta mock
        RichiestaCancellazioneLogica richiesta = createValidRichiesta();

        // Mock JAXB
        when(xmlSoftDeleteCache.getRichSoftDeleteCtx()).thenReturn(jaxbContext);
        when(jaxbContext.createUnmarshaller()).thenReturn(unmarshaller);
        when(xmlSoftDeleteCache.getRichSoftDeleteSchema()).thenReturn(null);
        when(unmarshaller.unmarshal(any(java.io.StringReader.class))).thenReturn(richiesta);

        // Mock controlli
        RispostaControlli rcStruttura = new RispostaControlli();
        rcStruttura.setrBoolean(true);
        rcStruttura.setrLong(100L);
        when(controlliSemanticiService.checkIdStrut(any(CSVersatore.class)))
                .thenReturn(rcStruttura);

        RispostaControlli rcAuth = new RispostaControlli();
        rcAuth.setrBoolean(true);
        when(controlliWsService.checkAuthWS(any(), any())).thenReturn(rcAuth);

        // Esegui
        service.parseXML(validXml, cancellazioneLogicaExt, rispostaWs);

        // Verifica
        assertEquals(IRispostaWS.SeverityEnum.OK, rispostaWs.getSeverity());
        assertNotNull(cancellazioneLogicaExt.getRichiestaCancellazioneLogica());
        assertEquals(100L, cancellazioneLogicaExt.getIdStrut());
    }

    // ==================== parseXML_invalidVersatore ====================

    @Test
    void parseXML_invalidVersatore() throws Exception {
        String validXml = createValidXml();
        RichiestaCancellazioneLogica richiesta = createValidRichiesta();

        // Mock JAXB
        when(xmlSoftDeleteCache.getRichSoftDeleteCtx()).thenReturn(jaxbContext);
        when(jaxbContext.createUnmarshaller()).thenReturn(unmarshaller);
        when(xmlSoftDeleteCache.getRichSoftDeleteSchema()).thenReturn(null);
        when(unmarshaller.unmarshal(any(java.io.StringReader.class))).thenReturn(richiesta);

        // Mock controllo struttura fallito
        RispostaControlli rcStruttura = new RispostaControlli();
        rcStruttura.setrBoolean(false);
        rcStruttura.setrLong(0L);
        rcStruttura.setCodErr("STRUT-001");
        rcStruttura.setDsErr("Struttura non valida");
        when(controlliSemanticiService.checkIdStrut(any(CSVersatore.class)))
                .thenReturn(rcStruttura);

        // Esegui
        service.parseXML(validXml, cancellazioneLogicaExt, rispostaWs);

        // Verifica
        assertEquals(IRispostaWS.SeverityEnum.ERROR, rispostaWs.getSeverity());
        assertEquals("STRUT-001", rispostaWs.getErrorCode());
    }

    // ==================== Helper Methods ====================

    private String createValidXml() {
        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <RichiestaCancellazioneLogica xmlns="http://parer.eng.it/richSoftDelete">
                    <VersioneXmlRichiesta>1.0</VersioneXmlRichiesta>
                    <Versatore>
                        <Ambiente>AMB_TEST</Ambiente>
                        <Ente>ENTE_TEST</Ente>
                        <Struttura>STRUT_TEST</Struttura>
                        <UserID>testuser</UserID>
                    </Versatore>
                    <Richiesta>
                        <Descrizione>Test richiesta</Descrizione>
                        <Motivazione>Test motivazione</Motivazione>
                        <TipoCancellazione>CAMPIONE</TipoCancellazione>
                    </Richiesta>
                    <RichiesteDiCancellazione>
                        <RichiestaDiCancellazione>
                            <TipoRichiesta>UNITA_DOCUMENTARIA</TipoRichiesta>
                            <TipoRegistro>REG_TEST</TipoRegistro>
                            <Anno>2024</Anno>
                            <Numero>1</Numero>
                        </RichiestaDiCancellazione>
                    </RichiesteDiCancellazione>
                </RichiestaCancellazioneLogica>
                """;
    }

    private RichiestaCancellazioneLogica createValidRichiesta() {
        RichiestaCancellazioneLogica richiesta = new RichiestaCancellazioneLogica();
        richiesta.setVersioneXmlRichiesta("1.0");

        VersatoreType versatore = new VersatoreType();
        versatore.setAmbiente("AMB_TEST");
        versatore.setEnte("ENTE_TEST");
        versatore.setStruttura("STRUT_TEST");
        versatore.setUserID("testuser");
        richiesta.setVersatore(versatore);

        RichiestaType richiestaType = new RichiestaType();
        richiestaType.setDescrizione("Test richiesta");
        richiestaType.setMotivazione("Test motivazione");
        richiestaType.setTipoCancellazione(TipoCancellazioneType.CAMPIONE);
        richiesta.setRichiesta(richiestaType);

        RichiestaCancellazioneLogica.RichiesteDiCancellazione richiesteDiCanc = new RichiestaCancellazioneLogica.RichiesteDiCancellazione();

        RichiestaDiCancellazioneType richDiCanc = new RichiestaDiCancellazioneType();
        richDiCanc.setTipoRichiesta(TipoRichiestaType.UNITA_DOCUMENTARIA);
        richDiCanc.setTipoRegistro("REG_TEST");
        richDiCanc.setAnno(2024);
        richDiCanc.setNumero("1");

        richiesteDiCanc.getRichiestaDiCancellazione().add(richDiCanc);
        richiesta.setRichiesteDiCancellazione(richiesteDiCanc);

        return richiesta;
    }
}
