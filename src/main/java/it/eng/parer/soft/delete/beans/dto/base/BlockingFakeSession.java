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
 * To change this template, choose Tools | Templates and open the template in the editor.
 */
package it.eng.parer.soft.delete.beans.dto.base;

import java.time.ZonedDateTime;

import it.eng.parer.ws.xml.richSoftDelete.RichiestaCancellazioneLogica;

public class BlockingFakeSession implements java.io.Serializable {

    private static final String CONTROL_CHARACTERS_REGEX = "[\\u0000-\\u001F]";

    /**
     *
     */
    private static final long serialVersionUID = 8890894113138332183L;
    private boolean salvaSessione = true;
    private String tipoDatiSessioneVers;
    private String ipChiamante;
    private String loginName;
    private String password;
    private long idUser = 0;
    private String versioneWS;
    //
    private String datiRequestXml;
    private String urnRequestXml;
    private String hashRequestXml;
    private String datiDaSalvareRequest;
    //
    private String datiPackInfoRequestXml;
    private String urnPackInfoRequestXml;
    private String hashPackInfoRequestXml;
    private String datiC14NPackInfoRequestXml;
    //
    private String datiResponse;
    private String urnResponse;
    private String hashResponse;
    //
    private boolean xmlOk;
    private ZonedDateTime tmApertura;
    private ZonedDateTime tmChiusura;
    //
    private transient RichiestaCancellazioneLogica richiestaCancellazioneLogica;
    private long idRichiestaCancellazioneLogica;

    /**
     * Costruttore
     */
    public BlockingFakeSession() {
        xmlOk = false;
    }

    /*
     *
     */
    /**
     * @return the salvaSessione
     */
    public boolean isSalvaSessione() {
        return salvaSessione;
    }

    /**
     * @param salvaSessione the salvaSessione to set
     */
    public void setSalvaSessione(boolean salvaSessione) {
        this.salvaSessione = salvaSessione;
    }

    /**
     * @return the tipoDatiSessioneVers
     */
    public String getTipoDatiSessioneVers() {
        return tipoDatiSessioneVers;
    }

    /**
     * @param tipoDatiSessioneVers the tipoDatiSessioneVers to set
     */
    public void setTipoDatiSessioneVers(String tipoDatiSessioneVers) {
        this.tipoDatiSessioneVers = tipoDatiSessioneVers;
    }

    public String getIpChiamante() {
        return ipChiamante;
    }

    public void setIpChiamante(String ipChiamante) {
        this.ipChiamante = ipChiamante;
    }

    /**
     * @return the loginName
     */
    public String getLoginName() {
        return loginName;
    }

    /**
     * Nota: la variabile inserita viene pulita di tutti gli eventuali caratteri di controllo
     *
     * @param loginName the loginName to set
     */
    public void setLoginName(String loginName) {
        this.loginName = loginName.replaceAll(CONTROL_CHARACTERS_REGEX, "");
    }

    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Nota: la variabile inserita viene pulita di tutti gli eventuali caratteri di controllo
     *
     * @param password the password to set
     */
    public void setPassword(String password) {
        this.password = password.replaceAll(CONTROL_CHARACTERS_REGEX, "");
    }

    /**
     * @return the idUser
     */
    public long getIdUser() {
        return idUser;
    }

    /**
     * @param idUser the idUser to set
     */
    public void setIdUser(long idUser) {
        this.idUser = idUser;
    }

    /**
     * @return the versioneWS
     */
    public String getVersioneWS() {
        return versioneWS;
    }

    /**
     * Nota: la variabile inserita viene pulita di tutti gli eventuali caratteri di controllo
     *
     * @param versioneWS the versioneWS to set
     */
    public void setVersioneWS(String versioneWS) {
        this.versioneWS = versioneWS.replaceAll(CONTROL_CHARACTERS_REGEX, "");
    }

    /**
     * @return the datiRequestXml
     */
    public String getDatiRequestXml() {
        return datiRequestXml;
    }

    /**
     * Nota: la variabile inserita viene pulita di tutti gli eventuali caratteri di controllo
     *
     * @param datiRequestXml the datiRequestXml to set
     */
    public void setDatiRequestXml(String datiRequestXml) {
        this.datiRequestXml = datiRequestXml.replaceAll(CONTROL_CHARACTERS_REGEX, "");
    }

    public String getUrnRequestXml() {
        return urnRequestXml;
    }

    public void setUrnRequestXml(String urnRequestXml) {
        this.urnRequestXml = urnRequestXml;
    }

    public String getHashRequestXml() {
        return hashRequestXml;
    }

    public void setHashRequestXml(String hashRequestXml) {
        this.hashRequestXml = hashRequestXml;
    }

    public String getDatiDaSalvareRequest() {
        return datiDaSalvareRequest;
    }

    public void setDatiDaSalvareRequest(String datiDaSalvareRequest) {
        this.datiDaSalvareRequest = datiDaSalvareRequest;
    }

    public String getDatiPackInfoRequestXml() {
        return datiPackInfoRequestXml;
    }

    /**
     * Nota: la variabile inserita viene pulita di tutti gli eventuali caratteri di controllo
     *
     * @param datiPackInfoRequestXml the datiIndiceRequestXml to set
     */
    public void setDatiPackInfoRequestXml(String datiPackInfoRequestXml) {
        this.datiPackInfoRequestXml = datiPackInfoRequestXml.replaceAll(CONTROL_CHARACTERS_REGEX,
                "");
    }

    public String getUrnPackInfoRequestXml() {
        return urnPackInfoRequestXml;
    }

    public void setUrnPackInfoRequestXml(String urnPackInfoRequestXml) {
        this.urnPackInfoRequestXml = urnPackInfoRequestXml;
    }

    public String getHashPackInfoRequestXml() {
        return hashPackInfoRequestXml;
    }

    public void setHashPackInfoRequestXml(String hashPackInfoRequestXml) {
        this.hashPackInfoRequestXml = hashPackInfoRequestXml;
    }

    public String getDatiResponse() {
        return datiResponse;
    }

    public void setDatiResponse(String datiResponse) {
        this.datiResponse = datiResponse;
    }

    public String getUrnResponse() {
        return urnResponse;
    }

    public void setUrnResponse(String urnResponse) {
        this.urnResponse = urnResponse;
    }

    public String getHashResponse() {
        return hashResponse;
    }

    public void setHashResponse(String hashResponse) {
        this.hashResponse = hashResponse;
    }

    public boolean isXmlOk() {
        return xmlOk;
    }

    public void setXmlOk(boolean xmlOk) {
        this.xmlOk = xmlOk;
    }

    public ZonedDateTime getTmApertura() {
        return tmApertura;
    }

    public void setTmApertura(ZonedDateTime tmApertura) {
        this.tmApertura = tmApertura;
    }

    public ZonedDateTime getTmChiusura() {
        return tmChiusura;
    }

    public void setTmChiusura(ZonedDateTime tmChiusura) {
        this.tmChiusura = tmChiusura;
    }

    public String getDatiC14NPackInfoRequestXml() {
        return datiC14NPackInfoRequestXml;
    }

    public void setDatiC14NPackInfoRequestXml(String datiC14NPackInfoRequestXml) {
        this.datiC14NPackInfoRequestXml = datiC14NPackInfoRequestXml;
    }

    public RichiestaCancellazioneLogica getRichiestaCancellazioneLogica() {
        return richiestaCancellazioneLogica;
    }

    public void setRichiestaCancellazioneLogica(
            RichiestaCancellazioneLogica richiestaCancellazioneLogica) {
        this.richiestaCancellazioneLogica = richiestaCancellazioneLogica;
    }

    public long getIdRichiestaCancellazioneLogica() {
        return idRichiestaCancellazioneLogica;
    }

    public void setIdRichiestaCancellazioneLogica(long idRichiestaCancellazioneLogica) {
        this.idRichiestaCancellazioneLogica = idRichiestaCancellazioneLogica;
    }
}
