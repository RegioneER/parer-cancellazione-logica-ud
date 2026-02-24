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

/*
 */
package it.eng.parer.soft.delete.beans.utils;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

/**
 *
 * @author DiLorenzo_F
 */
public class AvanzamentoWs implements java.io.Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger log = LoggerFactory.getLogger(AvanzamentoWs.class);

    public enum Funzioni {

        CANCELLAZIONE_LOGICA
    }

    public enum CheckPoints {

        INIZIO, VERIFICA_STRUTTURA_CHIAMATA_WS, VERIFICA_XML, VERIFICA_SEMANTICA,
        SALVATAGGIO_DATI_VERSATI, SALVATAGGIO_SESSIONE_WS, CREAZIONE_RISPOSTA,
        TRASFERIMENTO_PAYLOAD_IN, LETTURA_IP_VERSANTE, INVIO_RISPOSTA, FINE
    }

    //
    private String instanceName;
    private Funzioni funzione;
    //
    private CheckPoints checkPoint;
    private String fase;
    //
    private String chNumero;
    private String chAnno;
    private String chRegistro;
    //
    private String vrsAmbiente;
    private String vrsEnte;
    private String vrsStruttura;
    private String vrsUser;
    //
    private String documento;
    private String componente;
    private String indirizzoIp;
    //
    private long totalTime;

    // costruttore privato, la classe non è direttamente istanziabile
    private AvanzamentoWs() {
        this.reset();
    }

    // factory, che assegna il session Id
    public static AvanzamentoWs nuovoAvanzamentoWS(String instance, Funzioni funz) {
        AvanzamentoWs tmpAvanzamentoWS = null;

        tmpAvanzamentoWS = new AvanzamentoWs();
        tmpAvanzamentoWS.instanceName = instance;
        tmpAvanzamentoWS.funzione = funz;

        return tmpAvanzamentoWS;
    }

    public AvanzamentoWs logAvanzamento() {
        return logAvanzamento(false);
    }

    public AvanzamentoWs logAvanzamento(boolean needToVisual) {

        StringBuilder tmpBuilder = new StringBuilder();

        tmpBuilder.append(String.format("I: %s ; ", this.getInstanceName()));
        tmpBuilder.append(String.format("F: %s ; ", this.getFunzione().toString()));
        tmpBuilder.append(String.format("CP: %s ; ", this.getCheckPoint().toString()));

        if (!this.getFase().isEmpty()) {
            tmpBuilder.append(String.format("F: %s ; ", this.getFase()));
        }

        if (!this.getVrsAmbiente().isEmpty()) {
            tmpBuilder.append(String.format("Amb: %s ; ", this.getVrsAmbiente()));
            tmpBuilder.append(String.format("Ente: %s ; ", this.getVrsEnte()));
            tmpBuilder.append(String.format("Strutt: %s ; ", this.getVrsStruttura()));
        }

        if (!this.getIndirizzoIp().isEmpty()) {
            tmpBuilder.append(String.format("IP: %s ; ", this.getIndirizzoIp()));
        }

        if (!this.getVrsUser().isEmpty()) {
            tmpBuilder.append(String.format("U: %s ; ", this.getVrsUser()));
        }

        if (!this.getChAnno().isEmpty()) {
            tmpBuilder.append(String.format("Anno: %s ; ", this.getChAnno()));
            tmpBuilder.append(String.format("Num: %s ; ", this.getChNumero()));
            tmpBuilder.append(String.format("Reg: %s ; ", this.getChRegistro()));
        }

        if (!this.getDocumento().isEmpty()) {
            tmpBuilder.append(String.format("Doc: %s ; ", this.getDocumento()));
        }

        if (!this.getComponente().isEmpty()) {
            tmpBuilder.append(String.format("Comp: %s ; ", this.getComponente()));
        }

        if (this.getTotalTime() != 0) {
            tmpBuilder.append(String.format("Tempo Impiegato: %s ms; ", this.getTotalTime()));
        }

        log.atLevel(needToVisual ? Level.INFO : Level.DEBUG).log(tmpBuilder.toString());
        return this;
    }

    public AvanzamentoWs reset() {
        checkPoint = CheckPoints.INIZIO;
        chNumero = StringUtils.EMPTY;
        chAnno = StringUtils.EMPTY;
        chRegistro = StringUtils.EMPTY;
        vrsAmbiente = StringUtils.EMPTY;
        vrsEnte = StringUtils.EMPTY;
        vrsStruttura = StringUtils.EMPTY;
        vrsUser = StringUtils.EMPTY;
        indirizzoIp = StringUtils.EMPTY;
        this.resetFase();
        return this;
    }

    public AvanzamentoWs resetFase() {
        fase = StringUtils.EMPTY;
        documento = StringUtils.EMPTY;
        componente = StringUtils.EMPTY;
        return this;
    }

    /*
     * getter e setter (i setter sono coerenti con il modello Fluent di Martin Fowler
     * http://en.wikipedia.org/wiki/Fluent_interface http://en.wikipedia.org/wiki/Method_chaining
     *
     * )
     */
    public String getInstanceName() {
        return instanceName;
    }

    public AvanzamentoWs setInstanceName(String instanceName) {
        this.instanceName = instanceName;
        return this;
    }

    public Funzioni getFunzione() {
        return funzione;
    }

    public AvanzamentoWs setFunzione(Funzioni funzione) {
        this.funzione = funzione;
        return this;
    }

    public CheckPoints getCheckPoint() {
        return checkPoint;
    }

    public AvanzamentoWs setCheckPoint(CheckPoints checkPoint) {
        this.checkPoint = checkPoint;
        return this;
    }

    public String getFase() {
        return fase;
    }

    public AvanzamentoWs setFase(String fase) {
        this.fase = fase;
        return this;
    }

    public String getChNumero() {
        return chNumero;
    }

    public AvanzamentoWs setChNumero(String chNumero) {
        this.chNumero = chNumero;
        return this;
    }

    public String getChAnno() {
        return chAnno;
    }

    public AvanzamentoWs setChAnno(String chAnno) {
        this.chAnno = chAnno;
        return this;
    }

    public String getChRegistro() {
        return chRegistro;
    }

    public AvanzamentoWs setChRegistro(String chRegistro) {
        this.chRegistro = chRegistro;
        return this;
    }

    public String getVrsAmbiente() {
        return vrsAmbiente;
    }

    public AvanzamentoWs setVrsAmbiente(String vrsAmbiente) {
        this.vrsAmbiente = vrsAmbiente;
        return this;
    }

    public String getVrsEnte() {
        return vrsEnte;
    }

    public AvanzamentoWs setVrsEnte(String vrsEnte) {
        this.vrsEnte = vrsEnte;
        return this;
    }

    public String getVrsStruttura() {
        return vrsStruttura;
    }

    public AvanzamentoWs setVrsStruttura(String vrsStruttura) {
        this.vrsStruttura = vrsStruttura;
        return this;
    }

    public String getVrsUser() {
        return vrsUser;
    }

    public AvanzamentoWs setVrsUser(String vrsUser) {
        this.vrsUser = vrsUser;
        return this;
    }

    public String getDocumento() {
        return documento;
    }

    public AvanzamentoWs setDocumento(String documento) {
        this.documento = documento;
        return this;
    }

    public String getComponente() {
        return componente;
    }

    public AvanzamentoWs setComponente(String componente) {
        this.componente = componente;
        return this;
    }

    public long getTotalTime() {
        return totalTime;
    }

    public AvanzamentoWs setTotalTime(long totalTime) {
        this.totalTime = totalTime;
        return this;
    }

    public String getIndirizzoIp() {
        return indirizzoIp;
    }

    public AvanzamentoWs setIndirizzoIp(String indirizzoIp) {
        this.indirizzoIp = indirizzoIp;
        return this;
    }
}
