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
import java.util.ArrayList;
import java.util.List;

import it.eng.parer.soft.delete.jpa.sequence.NonMonotonicSequence;
import jakarta.persistence.Cacheable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/**
 * The persistent class for the APL_PARAM_APPLIC database table.
 *
 */
@Entity
@Cacheable(true)
@Table(name = "APL_PARAM_APPLIC")
@NamedQuery(name = "AplParamApplic.findAll", query = "SELECT a FROM AplParamApplic a")
public class AplParamApplic implements Serializable {

    private static final long serialVersionUID = 1L;
    private Long idParamApplic;
    private String dmParamApplic;
    private String dsParamApplic;
    private String dsListaValoriAmmessi;
    private String flAppartAaTipoFascicolo;
    private String flAppartAmbiente;
    private String flAppartApplic;
    private String flAppartStrut;
    private String flAppartTipoUnitaDoc;
    private String flMulti;
    private String nmParamApplic;
    private String tiGestioneParam;
    private String tiParamApplic;
    private List<AplValoreParamApplic> aplValoreParamApplics = new ArrayList<>();
    private List<AplValParamApplicMulti> aplValParamApplicMultis = new ArrayList<>();

    public AplParamApplic() {
        // hibernate constructor
    }

    @Id
    @NonMonotonicSequence(sequenceName = "SAPL_PARAM_APPLIC", incrementBy = 1)
    @Column(name = "ID_PARAM_APPLIC")
    public Long getIdParamApplic() {
        return this.idParamApplic;
    }

    public void setIdParamApplic(Long idParamApplic) {
        this.idParamApplic = idParamApplic;
    }

    @Column(name = "DM_PARAM_APPLIC")
    public String getDmParamApplic() {
        return this.dmParamApplic;
    }

    public void setDmParamApplic(String dmParamApplic) {
        this.dmParamApplic = dmParamApplic;
    }

    @Column(name = "DS_PARAM_APPLIC")
    public String getDsParamApplic() {
        return this.dsParamApplic;
    }

    public void setDsParamApplic(String dsParamApplic) {
        this.dsParamApplic = dsParamApplic;
    }

    @Column(name = "DS_LISTA_VALORI_AMMESSI")
    public String getDsListaValoriAmmessi() {
        return this.dsListaValoriAmmessi;
    }

    public void setDsListaValoriAmmessi(String dsListaValoriAmmessi) {
        this.dsListaValoriAmmessi = dsListaValoriAmmessi;
    }

    @Column(name = "FL_APPART_AA_TIPO_FASCICOLO", columnDefinition = "CHAR")
    public String getFlAppartAaTipoFascicolo() {
        return this.flAppartAaTipoFascicolo;
    }

    public void setFlAppartAaTipoFascicolo(String flAppartAaTipoFascicolo) {
        this.flAppartAaTipoFascicolo = flAppartAaTipoFascicolo;
    }

    @Column(name = "FL_APPART_AMBIENTE", columnDefinition = "CHAR")
    public String getFlAppartAmbiente() {
        return this.flAppartAmbiente;
    }

    public void setFlAppartAmbiente(String flAppartAmbiente) {
        this.flAppartAmbiente = flAppartAmbiente;
    }

    @Column(name = "FL_APPART_APPLIC", columnDefinition = "CHAR")
    public String getFlAppartApplic() {
        return this.flAppartApplic;
    }

    public void setFlAppartApplic(String flAppartApplic) {
        this.flAppartApplic = flAppartApplic;
    }

    @Column(name = "FL_APPART_STRUT", columnDefinition = "CHAR")
    public String getFlAppartStrut() {
        return this.flAppartStrut;
    }

    public void setFlAppartStrut(String flAppartStrut) {
        this.flAppartStrut = flAppartStrut;
    }

    @Column(name = "FL_APPART_TIPO_UNITA_DOC", columnDefinition = "CHAR")
    public String getFlAppartTipoUnitaDoc() {
        return this.flAppartTipoUnitaDoc;
    }

    public void setFlAppartTipoUnitaDoc(String flAppartTipoUnitaDoc) {
        this.flAppartTipoUnitaDoc = flAppartTipoUnitaDoc;
    }

    @Column(name = "FL_MULTI", columnDefinition = "CHAR")
    public String getFlMulti() {
        return this.flMulti;
    }

    public void setFlMulti(String flMulti) {
        this.flMulti = flMulti;
    }

    @Column(name = "NM_PARAM_APPLIC")
    public String getNmParamApplic() {
        return this.nmParamApplic;
    }

    public void setNmParamApplic(String nmParamApplic) {
        this.nmParamApplic = nmParamApplic;
    }

    @Column(name = "TI_GESTIONE_PARAM")
    public String getTiGestioneParam() {
        return this.tiGestioneParam;
    }

    public void setTiGestioneParam(String tiGestioneParam) {
        this.tiGestioneParam = tiGestioneParam;
    }

    @Column(name = "TI_PARAM_APPLIC")
    public String getTiParamApplic() {
        return this.tiParamApplic;
    }

    public void setTiParamApplic(String tiParamApplic) {
        this.tiParamApplic = tiParamApplic;
    }

    // bi-directional many-to-one association to AplValoreParamApplic
    @OneToMany(mappedBy = "aplParamApplic", cascade = {
            CascadeType.REMOVE })
    public List<AplValoreParamApplic> getAplValoreParamApplics() {
        return this.aplValoreParamApplics;
    }

    public void setAplValoreParamApplics(List<AplValoreParamApplic> aplValoreParamApplics) {
        this.aplValoreParamApplics = aplValoreParamApplics;
    }

    public AplValoreParamApplic addAplValoreParamApplic(AplValoreParamApplic aplValoreParamApplic) {
        getAplValoreParamApplics().add(aplValoreParamApplic);
        aplValoreParamApplic.setAplParamApplic(this);

        return aplValoreParamApplic;
    }

    public AplValoreParamApplic removeAplValoreParamApplic(
            AplValoreParamApplic aplValoreParamApplic) {
        getAplValoreParamApplics().remove(aplValoreParamApplic);
        aplValoreParamApplic.setAplParamApplic(null);

        return aplValoreParamApplic;
    }

    // bi-directional many-to-one association to AplValParamApplicMulti
    @OneToMany(mappedBy = "aplParamApplic", cascade = {
            CascadeType.REMOVE })
    public List<AplValParamApplicMulti> getAplValParamApplicMultis() {
        return this.aplValParamApplicMultis;
    }

    public void setAplValParamApplicMultis(List<AplValParamApplicMulti> aplValParamApplicMultis) {
        this.aplValParamApplicMultis = aplValParamApplicMultis;
    }

    public AplValParamApplicMulti addAplValParamApplicMulti(
            AplValParamApplicMulti aplValParamApplicMulti) {
        getAplValParamApplicMultis().add(aplValParamApplicMulti);
        aplValParamApplicMulti.setAplParamApplic(this);

        return aplValParamApplicMulti;
    }

    public AplValParamApplicMulti removeAplValParamApplicMulti(
            AplValParamApplicMulti aplValParamApplicMulti) {
        getAplValParamApplicMultis().remove(aplValParamApplicMulti);
        aplValParamApplicMulti.setAplParamApplic(null);

        return aplValParamApplicMulti;
    }

}
