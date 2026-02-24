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
 * The persistent class for the APL_VAL_PARAM_APPLIC_MULTI database table.
 *
 */
@Entity
@Table(name = "APL_VAL_PARAM_APPLIC_MULTI")
@NamedQuery(name = "AplValParamApplicMulti.findAll", query = "SELECT a FROM AplValParamApplicMulti a")
public class AplValParamApplicMulti implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long idValParamApplicMulti;
    private String dsValoreParamApplic;
    private AplParamApplic aplParamApplic;
    private OrgAmbiente orgAmbiente;

    public AplValParamApplicMulti() {
        // hibernate constructor
    }

    @Id
    @NonMonotonicSequence(sequenceName = "SAPL_VAL_PARAM_APPLIC_MULTI", incrementBy = 1)
    @Column(name = "ID_VAL_PARAM_APPLIC_MULTI")
    public Long getIdValParamApplicMulti() {
        return this.idValParamApplicMulti;
    }

    public void setIdValParamApplicMulti(Long idValParamApplicMulti) {
        this.idValParamApplicMulti = idValParamApplicMulti;
    }

    @Column(name = "DS_VALORE_PARAM_APPLIC")
    public String getDsValoreParamApplic() {
        return this.dsValoreParamApplic;
    }

    public void setDsValoreParamApplic(String dsValoreParamApplic) {
        this.dsValoreParamApplic = dsValoreParamApplic;
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

    // bi-directional many-to-one association to OrgAmbiente
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_AMBIENTE")
    public OrgAmbiente getOrgAmbiente() {
        return this.orgAmbiente;
    }

    public void setOrgAmbiente(OrgAmbiente orgAmbiente) {
        this.orgAmbiente = orgAmbiente;
    }

}
