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

package it.eng.parer.soft.delete.jpa.entity;

import java.io.Serializable;

import it.eng.parer.soft.delete.jpa.sequence.NonMonotonicSequence;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

/**
 * The persistent class for the APL_VALORE_PARAM_APPLIC database table.
 *
 */
@Entity
@Table(name = "APL_VALORE_PARAM_APPLIC")
@NamedQuery(name = "AplValoreParamApplic.findAll", query = "SELECT a FROM AplValoreParamApplic a")
public class AplValoreParamApplic implements Serializable {

    private static final long serialVersionUID = 1L;
    private Long idValoreParamApplic;
    private String dsValoreParamApplic;
    private String tiAppart;
    private AplParamApplic aplParamApplic;
    private DecAaTipoFascicolo decAaTipoFascicolo;
    private DecTipoUnitaDoc decTipoUnitaDoc;
    private OrgAmbiente orgAmbiente;
    private OrgStrut orgStrut;

    public AplValoreParamApplic() {
        // hibernate constructor
    }

    @Id
    @NonMonotonicSequence(sequenceName = "SAPL_VALORE_PARAM_APPLIC", incrementBy = 1)
    @Column(name = "ID_VALORE_PARAM_APPLIC")
    public Long getIdValoreParamApplic() {
        return this.idValoreParamApplic;
    }

    public void setIdValoreParamApplic(Long idValoreParamApplic) {
        this.idValoreParamApplic = idValoreParamApplic;
    }

    @Column(name = "DS_VALORE_PARAM_APPLIC")
    public String getDsValoreParamApplic() {
        return this.dsValoreParamApplic;
    }

    public void setDsValoreParamApplic(String dsValoreParamApplic) {
        this.dsValoreParamApplic = dsValoreParamApplic;
    }

    @Column(name = "TI_APPART")
    public String getTiAppart() {
        return this.tiAppart;
    }

    public void setTiAppart(String tiAppart) {
        this.tiAppart = tiAppart;
    }

    // bi-directional many-to-one association to AplParamApplic
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_PARAM_APPLIC")
    public AplParamApplic getAplParamApplic() {
        return this.aplParamApplic;
    }

    public void setAplParamApplic(AplParamApplic aplParamApplic) {
        this.aplParamApplic = aplParamApplic;
    }

    // bi-directional many-to-one association to DecAaTipoFascicolo
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_AA_TIPO_FASCICOLO")
    public DecAaTipoFascicolo getDecAaTipoFascicolo() {
        return this.decAaTipoFascicolo;
    }

    public void setDecAaTipoFascicolo(DecAaTipoFascicolo decAaTipoFascicolo) {
        this.decAaTipoFascicolo = decAaTipoFascicolo;
    }

    // bi-directional many-to-one association to DecTipoUnitaDoc
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_TIPO_UNITA_DOC")
    public DecTipoUnitaDoc getDecTipoUnitaDoc() {
        return this.decTipoUnitaDoc;
    }

    public void setDecTipoUnitaDoc(DecTipoUnitaDoc decTipoUnitaDoc) {
        this.decTipoUnitaDoc = decTipoUnitaDoc;
    }

    // bi-directional many-to-one association to OrgAmbiente
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_AMBIENTE")
    public OrgAmbiente getOrgAmbiente() {
        return this.orgAmbiente;
    }

    public void setOrgAmbiente(OrgAmbiente orgAmbiente) {
        this.orgAmbiente = orgAmbiente;
    }

    // bi-directional many-to-one association to OrgStrut
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_STRUT")

    public OrgStrut getOrgStrut() {
        return this.orgStrut;
    }

    public void setOrgStrut(OrgStrut orgStrut) {
        this.orgStrut = orgStrut;
    }

}
