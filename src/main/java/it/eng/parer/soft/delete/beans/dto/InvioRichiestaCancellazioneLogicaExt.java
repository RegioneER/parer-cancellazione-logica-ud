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

import java.util.Date;
import java.util.EnumSet;
import java.util.Set;

import it.eng.parer.soft.delete.beans.dto.base.IWSDesc;
import it.eng.parer.soft.delete.beans.utils.Costanti;
import it.eng.parer.ws.xml.richSoftDelete.RichiestaCancellazioneLogica;

/**
 *
 * @author DiLorenzo_F
 */
public class InvioRichiestaCancellazioneLogicaExt extends AbsCancellazioneLogicaExt {

    private transient IWSDesc descrizione;
    private String xmlRichiesta;
    private Long idStrut;
    private Date dataElaborazione;

    private transient RichiestaCancellazioneLogica richiestaCancellazioneLogica;

    private Set<Costanti.ModificatoriWS> modificatoriWS = EnumSet
            .noneOf(Costanti.ModificatoriWS.class);

    @Override
    public IWSDesc getDescrizione() {
        return descrizione;
    }

    @Override
    public void setDescrizione(IWSDesc descrizione) {
        this.descrizione = descrizione;
    }

    @Override
    public String getXmlRichiesta() {
        return xmlRichiesta;
    }

    @Override
    public void setXmlRichiesta(String xmlRichiesta) {
        this.xmlRichiesta = xmlRichiesta;
    }

    public Long getIdStrut() {
        return idStrut;
    }

    public void setIdStrut(Long idStrut) {
        this.idStrut = idStrut;
    }

    public Date getDataElaborazione() {
        return dataElaborazione;
    }

    public void setDataElaborazione(Date dataElaborazione) {
        this.dataElaborazione = dataElaborazione;
    }

    public RichiestaCancellazioneLogica getRichiestaCancellazioneLogica() {
        return richiestaCancellazioneLogica;
    }

    public void setRichiestaCancellazioneLogica(
            RichiestaCancellazioneLogica richiestaCancellazioneLogica) {
        this.richiestaCancellazioneLogica = richiestaCancellazioneLogica;
    }

    @Override
    public Set<Costanti.ModificatoriWS> getModificatoriWSCalc() {
        return this.modificatoriWS;
    }

    public Set<Costanti.ModificatoriWS> getModificatoriWS() {
        return modificatoriWS;
    }

    public void setModificatoriWS(Set<Costanti.ModificatoriWS> modificatoriWS) {
        this.modificatoriWS = modificatoriWS;
    }

}
