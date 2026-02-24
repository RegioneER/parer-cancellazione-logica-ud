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
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import it.eng.parer.soft.delete.jpa.sequence.NonMonotonicSequence;
import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

/**
 * The persistent class for the ORG_ENTE database table.
 *
 */
@Entity
@Cacheable(true)
@Table(name = "ORG_ENTE")
public class OrgEnte implements Serializable {

    private static final long serialVersionUID = 1L;
    private Long idEnte;
    private String dsEnte;
    private String nmEnte;
    private String cdEnteNormaliz;
    private OrgAmbiente orgAmbiente;
    private List<OrgStrut> orgStruts = new ArrayList<>();
    private BigDecimal orgCategEnte;
    private String tipoDefTemplateEnte;
    private LocalDateTime dtFinValAppartAmbiente;
    private LocalDateTime dtIniValAppartAmbiente;
    private LocalDateTime dtFineVal;
    private LocalDateTime dtIniVal;
    private String flCessato;

    public OrgEnte() {
        // hibernate constructor
    }

    @Id
    @NonMonotonicSequence(sequenceName = "SORG_ENTE", incrementBy = 1)
    @Column(name = "ID_ENTE")
    public Long getIdEnte() {
        return this.idEnte;
    }

    public void setIdEnte(Long idEnte) {
        this.idEnte = idEnte;
    }

    @Column(name = "DS_ENTE")
    public String getDsEnte() {
        return this.dsEnte;
    }

    public void setDsEnte(String dsEnte) {
        this.dsEnte = dsEnte;
    }

    @Column(name = "NM_ENTE")
    public String getNmEnte() {
        return this.nmEnte;
    }

    public void setNmEnte(String nmEnte) {
        this.nmEnte = nmEnte;
    }

    @Column(name = "CD_ENTE_NORMALIZ")
    public String getCdEnteNormaliz() {
        return this.cdEnteNormaliz;
    }

    public void setCdEnteNormaliz(String cdEnteNormaliz) {
        this.cdEnteNormaliz = cdEnteNormaliz;
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
    @OneToMany(mappedBy = "orgEnte")
    public List<OrgStrut> getOrgStruts() {
        return this.orgStruts;
    }

    public void setOrgStruts(List<OrgStrut> orgStruts) {
        this.orgStruts = orgStruts;
    }

    // bi-directional many-to-one association to OrgCategEnte
    // @ManyToOne(fetch = FetchType.LAZY)
    @Column(name = "ID_CATEG_ENTE")
    public BigDecimal getOrgCategEnte() {
        return this.orgCategEnte;
    }

    public void setOrgCategEnte(BigDecimal orgCategEnte) {
        this.orgCategEnte = orgCategEnte;
    }

    @Column(name = "TIPO_DEF_TEMPLATE_ENTE")
    public String getTipoDefTemplateEnte() {
        return this.tipoDefTemplateEnte;
    }

    public void setTipoDefTemplateEnte(String tipoDefTemplateEnte) {
        this.tipoDefTemplateEnte = tipoDefTemplateEnte;
    }

    @Column(name = "DT_FIN_VAL_APPART_AMBIENTE")
    public LocalDateTime getDtFinValAppartAmbiente() {
        return this.dtFinValAppartAmbiente;
    }

    public void setDtFinValAppartAmbiente(LocalDateTime dtFinValAppartAmbiente) {
        this.dtFinValAppartAmbiente = dtFinValAppartAmbiente;
    }

    @Column(name = "DT_INI_VAL_APPART_AMBIENTE")
    public LocalDateTime getDtIniValAppartAmbiente() {
        return this.dtIniValAppartAmbiente;
    }

    public void setDtIniValAppartAmbiente(LocalDateTime dtIniValAppartAmbiente) {
        this.dtIniValAppartAmbiente = dtIniValAppartAmbiente;
    }

    @Column(name = "DT_FINE_VAL")
    public LocalDateTime getDtFineVal() {
        return this.dtFineVal;
    }

    public void setDtFineVal(LocalDateTime dtFineVal) {
        this.dtFineVal = dtFineVal;
    }

    @Column(name = "DT_INI_VAL")
    public LocalDateTime getDtIniVal() {
        return this.dtIniVal;
    }

    public void setDtIniVal(LocalDateTime dtIniVal) {
        this.dtIniVal = dtIniVal;
    }

    @Column(name = "FL_CESSATO", columnDefinition = "CHAR")
    public String getFlCessato() {
        return this.flCessato;
    }

    public void setFlCessato(String flCessato) {
        this.flCessato = flCessato;
    }

    @PrePersist
    void preInsert() {
        if (this.flCessato == null) {
            this.flCessato = "0";
        }
    }
}
