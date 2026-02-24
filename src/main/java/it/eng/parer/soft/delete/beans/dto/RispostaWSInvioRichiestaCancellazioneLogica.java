/*
 * Engineering Ingegneria Informatica S.p.A.
 *
 * Copyright (C) 2023 Regione Emilia-Romagna <p/> This program is free software: you can
 * redistribute it and/or modify it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the License, or (at your option)
 * any later version. <p/> This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Affero General Public License for more details. <p/> You should
 * have received a copy of the GNU Affero General Public License along with this program. If not,
 * see <https://www.gnu.org/licenses/>.
 */

package it.eng.parer.soft.delete.beans.dto;

import it.eng.parer.soft.delete.beans.dto.base.IRispostaWS;
import it.eng.parer.soft.delete.beans.utils.AvanzamentoWs;
import it.eng.parer.soft.delete.beans.utils.messages.MessaggiWSBundle;
import it.eng.parer.ws.xml.esitoRichSoftDelete.CodiceEsitoType;
import it.eng.parer.ws.xml.esitoRichSoftDelete.EsitoRichiestaCancellazioneLogica;

/**
 *
 * @author DiLorenzo_F
 */
public class RispostaWSInvioRichiestaCancellazioneLogica implements IRispostaWS {
    private static final long serialVersionUID = 1L;
    private SeverityEnum severity = SeverityEnum.OK;
    private ErrorTypeEnum errorType = ErrorTypeEnum.NOERROR;
    private String errorMessage;
    private String errorCode;
    private AvanzamentoWs avanzamento;
    private transient EsitoRichiestaCancellazioneLogica esitoRichiestaCancellazioneLogica;

    public RispostaWSInvioRichiestaCancellazioneLogica() {
        // Inizializza l'esito con valori di default
    }

    public EsitoRichiestaCancellazioneLogica getEsitoRichiestaCancellazioneLogica() {
        return esitoRichiestaCancellazioneLogica;
    }

    public void setEsitoRichiestaCancellazioneLogica(
            EsitoRichiestaCancellazioneLogica esitoRichiestaCancellazioneLogica) {
        this.esitoRichiestaCancellazioneLogica = esitoRichiestaCancellazioneLogica;
    }

    @Override
    public SeverityEnum getSeverity() {
        return severity;
    }

    @Override
    public void setSeverity(SeverityEnum severity) {
        this.severity = severity;
    }

    @Override
    public ErrorTypeEnum getErrorType() {
        return errorType;
    }

    @Override
    public void setErrorType(ErrorTypeEnum errorType) {
        this.errorType = errorType;
    }

    @Override
    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @Override
    public String getErrorCode() {
        return errorCode;
    }

    @Override
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    @Override
    public AvanzamentoWs getAvanzamento() {
        return avanzamento;
    }

    @Override
    public void setAvanzamento(AvanzamentoWs avanzamento) {
        this.avanzamento = avanzamento;
    }

    @Override
    public void setEsitoWsErrBundle(String errCode, Object... params) {
        esitoRichiestaCancellazioneLogica.getEsitoRichiesta()
                .setCodiceEsito(CodiceEsitoType.NEGATIVO);
        esitoRichiestaCancellazioneLogica.getEsitoRichiesta().setCodiceErrore(errCode);
        esitoRichiestaCancellazioneLogica.getEsitoRichiesta()
                .setMessaggioErrore(MessaggiWSBundle.getString(errCode, params));
        this.setRispostaWsError();
    }

    @Override
    public void setEsitoWsErrBundle(String errCode) {
        esitoRichiestaCancellazioneLogica.getEsitoRichiesta()
                .setCodiceEsito(CodiceEsitoType.NEGATIVO);
        esitoRichiestaCancellazioneLogica.getEsitoRichiesta().setCodiceErrore(errCode);
        esitoRichiestaCancellazioneLogica.getEsitoRichiesta()
                .setMessaggioErrore(MessaggiWSBundle.getString(errCode));
        this.setRispostaWsError();
    }

    @Override
    public void setEsitoWsWarnBundle(String errCode, Object... params) {
        esitoRichiestaCancellazioneLogica.getEsitoRichiesta()
                .setCodiceEsito(CodiceEsitoType.WARNING);
        esitoRichiestaCancellazioneLogica.getEsitoRichiesta().setCodiceErrore(errCode);
        esitoRichiestaCancellazioneLogica.getEsitoRichiesta()
                .setMessaggioErrore(MessaggiWSBundle.getString(errCode, params));
        this.setRispostaWsWarning();
    }

    @Override
    public void setEsitoWsWarnBundle(String errCode) {
        esitoRichiestaCancellazioneLogica.getEsitoRichiesta()
                .setCodiceEsito(CodiceEsitoType.WARNING);
        esitoRichiestaCancellazioneLogica.getEsitoRichiesta().setCodiceErrore(errCode);
        esitoRichiestaCancellazioneLogica.getEsitoRichiesta()
                .setMessaggioErrore(MessaggiWSBundle.getString(errCode));
        this.setRispostaWsWarning();
    }

    @Override
    public void setEsitoWsError(String errCode, String errMessage) {
        esitoRichiestaCancellazioneLogica.getEsitoRichiesta()
                .setCodiceEsito(CodiceEsitoType.NEGATIVO);
        esitoRichiestaCancellazioneLogica.getEsitoRichiesta().setCodiceErrore(errCode);
        esitoRichiestaCancellazioneLogica.getEsitoRichiesta().setMessaggioErrore(errMessage);
        this.setRispostaWsError();
    }

    @Override
    public void setEsitoWsWarning(String errCode, String errMessage) {
        esitoRichiestaCancellazioneLogica.getEsitoRichiesta()
                .setCodiceEsito(CodiceEsitoType.WARNING);
        esitoRichiestaCancellazioneLogica.getEsitoRichiesta().setCodiceErrore(errCode);
        esitoRichiestaCancellazioneLogica.getEsitoRichiesta().setMessaggioErrore(errMessage);
        this.setRispostaWsWarning();
    }

    private void setRispostaWsError() {
        this.severity = IRispostaWS.SeverityEnum.ERROR;
        this.errorCode = esitoRichiestaCancellazioneLogica.getEsitoRichiesta().getCodiceErrore();
        this.errorMessage = esitoRichiestaCancellazioneLogica.getEsitoRichiesta()
                .getMessaggioErrore();
    }

    private void setRispostaWsWarning() {
        this.severity = IRispostaWS.SeverityEnum.WARNING;
        this.errorCode = esitoRichiestaCancellazioneLogica.getEsitoRichiesta().getCodiceErrore();
        this.errorMessage = esitoRichiestaCancellazioneLogica.getEsitoRichiesta()
                .getMessaggioErrore();
    }

}
