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

/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package it.eng.parer.soft.delete.beans.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import it.eng.parer.soft.delete.beans.dto.base.IErroriMultipli;
import it.eng.parer.soft.delete.beans.dto.base.IRispostaWS;
import it.eng.parer.soft.delete.beans.dto.base.VoceDiErrore;
import it.eng.parer.soft.delete.beans.security.User;
import it.eng.parer.soft.delete.beans.utils.messages.MessaggiWSBundle;
import it.eng.parer.ws.xml.esitoRichSoftDelete.EsitoRichiestaType;

/**
 *
 * @author DiLorenzo_F
 */
public abstract class AbsCancellazioneLogicaExt
        implements IInvioRichiestaCancellazioneLogicaExt, IErroriMultipli {

    /**
     *
     */
    private static final long serialVersionUID = 4394458854053057889L;
    private User utente;
    private String versioneWsChiamata;
    private String loginName;

    private List<VoceDiErrore> erroriTrovati = new ArrayList<>();
    //
    private int numErrori = 0;
    private int numWarning = 0;
    //
    private Map<String, String> xmlDefaults;
    //
    private Map<String, String> wsVersions;

    @Override
    public String getLoginName() {
        return loginName;
    }

    @Override
    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    @Override
    public User getUtente() {
        return utente;
    }

    @Override
    public void setUtente(User utente) {
        this.utente = utente;
    }

    @Override
    public String getVersioneWsChiamata() {
        return versioneWsChiamata;
    }

    @Override
    public void setVersioneWsChiamata(String versioneWsChiamata) {
        this.versioneWsChiamata = versioneWsChiamata;
    }

    public Map<String, String> getXmlDefaults() {
        return xmlDefaults;
    }

    public void setXmlDefaults(Map<String, String> xmlDefaults) {
        this.xmlDefaults = xmlDefaults;
    }

    @Override
    public List<VoceDiErrore> getErroriTrovati() {
        return erroriTrovati;
    }

    @Override
    public boolean isTrovatiErrori() {
        return numErrori > 0;
    }

    @Override
    public boolean isTrovatiWarning() {
        return numWarning > 0;
    }

    @Override
    public VoceDiErrore calcolaErrorePrincipale() {
        numErrori = 0;
        numWarning = 0;
        VoceDiErrore tmpVdEErr = null;
        VoceDiErrore tmpVdEWarn = null;
        for (VoceDiErrore tmpVdE : erroriTrovati) {
            if (tmpVdE.getCodiceEsito() == VoceDiErrore.TipiEsitoErrore.NEGATIVO) {
                tmpVdEErr = tmpVdE;
                numErrori++;
            }
            if (tmpVdE.getCodiceEsito() == VoceDiErrore.TipiEsitoErrore.WARNING) {
                tmpVdEWarn = tmpVdE;
                numWarning++;
            }
        }
        if (tmpVdEErr != null) {
            this.impostaErrorePrincipale(tmpVdEErr);
            return tmpVdEErr;
        } else if (tmpVdEWarn != null) {
            this.impostaErrorePrincipale(tmpVdEWarn);
            return tmpVdEWarn;
        } else {
            return null;
        }
    }

    public void aggiungErroreFatale(EsitoRichiestaType esito) {
        VoceDiErrore tmpErrore;
        tmpErrore = this.addError("", esito.getCodiceErrore(), esito.getMessaggioErrore());
        this.impostaErrorePrincipale(tmpErrore);
    }

    @Override
    public void aggiungErroreFatale(String errCode, String errMessage) {
        VoceDiErrore tmpErrore;
        tmpErrore = this.addError("", errCode, errMessage);
        this.impostaErrorePrincipale(tmpErrore);
    }

    @Override
    public void listErrAddWarning(String descElemento, String errCode, Object... params) {
        this.addWarning(descElemento, errCode, MessaggiWSBundle.getString(errCode, params));
    }

    @Override
    public void listErrAddWarning(String descElemento, String errCode) {
        this.addWarning(descElemento, errCode, MessaggiWSBundle.getString(errCode));
    }

    @Override
    public void listErrAddError(String descElemento, String errCode, Object... params) {
        this.addError(descElemento, errCode, MessaggiWSBundle.getString(errCode, params));
    }

    @Override
    public void listErrAddError(String descElemento, String errCode) {
        this.addError(descElemento, errCode, MessaggiWSBundle.getString(errCode));
    }

    @Override
    public VoceDiErrore addWarning(String descElemento, String errCode, String errMessage) {
        VoceDiErrore tmpErrore = new VoceDiErrore();
        tmpErrore.setSeverity(IRispostaWS.SeverityEnum.WARNING);
        tmpErrore.setCodiceEsito(VoceDiErrore.TipiEsitoErrore.WARNING);
        tmpErrore.setElementoResponsabile(VoceDiErrore.ResponsabilitaErrore.UNI_DOC);
        tmpErrore.setErrorCode(errCode);
        tmpErrore.setErrorMessage(errMessage);
        tmpErrore.setDescElementoErr(descElemento);
        erroriTrovati.add(tmpErrore);
        numWarning++;
        return tmpErrore;
    }

    @Override
    public VoceDiErrore addError(String descElemento, String errCode, String errMessage) {
        VoceDiErrore tmpErrore = new VoceDiErrore();
        tmpErrore.setSeverity(IRispostaWS.SeverityEnum.ERROR);
        tmpErrore.setCodiceEsito(VoceDiErrore.TipiEsitoErrore.NEGATIVO);
        tmpErrore.setElementoResponsabile(VoceDiErrore.ResponsabilitaErrore.UNI_DOC);
        tmpErrore.setErrorCode(errCode);
        tmpErrore.setErrorMessage(errMessage);
        tmpErrore.setDescElementoErr(descElemento);
        erroriTrovati.add(tmpErrore);
        numErrori++;
        return tmpErrore;
    }

    private void impostaErrorePrincipale(VoceDiErrore vde) {
        for (VoceDiErrore tmpVdE : erroriTrovati) {
            tmpVdE.setElementoPrincipale(false);
        }
        vde.setElementoPrincipale(true);
    }

    public Map<String, String> getWsVersions() {
        return wsVersions;
    }

    public void setWsVersions(Map<String, String> wsVersions) {
        this.wsVersions = wsVersions;
    }
}
