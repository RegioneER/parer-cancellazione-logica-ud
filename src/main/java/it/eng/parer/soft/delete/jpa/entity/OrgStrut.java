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

import jakarta.persistence.Cacheable;
import jakarta.persistence.CascadeType;
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
 * The persistent class for the ORG_STRUT database table.
 *
 */
@Entity
@Cacheable(true)
@Table(name = "ORG_STRUT")
public class OrgStrut implements Serializable {

    private static final long serialVersionUID = 1L;
    private Long idStrut;
    private String cdIpa;
    private String dlNoteStrut;
    private String dsStrut;
    private String cdStrutNormaliz;
    private LocalDateTime dtIniVal;
    private LocalDateTime dtFineVal;
    private LocalDateTime dtIniValStrut;
    private LocalDateTime dtFineValStrut;
    private String flTemplate;
    private BigDecimal idEnteConvenz;
    private String nmStrut;
    // private List<AroUnitaDoc> aroUnitaDocs = new ArrayList<>();
    private List<DecRegistroUnitaDoc> decRegistroUnitaDocs = new ArrayList<>();
    private List<DecTipoUnitaDoc> decTipoUnitaDocs = new ArrayList<>();
    private List<DecTitol> decTitols = new ArrayList<>();
    private BigDecimal orgCategStrut;
    private OrgEnte orgEnte;
    private List<DecTipoFascicolo> decTipoFascicolos = new ArrayList<>();
    private List<OrgUsoSistemaMigraz> orgUsoSistemaMigrazs = new ArrayList<>();
    private List<AplValoreParamApplic> aplValoreParamApplics = new ArrayList<>();
    private String flCessato;
    private String flArchivioRestituito;

    public OrgStrut() {
        // hibernate constructor
    }

    @Id
    @Column(name = "ID_STRUT")
    public Long getIdStrut() {
        return this.idStrut;
    }

    public void setIdStrut(Long idStrut) {
        this.idStrut = idStrut;
    }

    @Column(name = "CD_IPA")
    public String getCdIpa() {
        return cdIpa;
    }

    public void setCdIpa(String cdIpa) {
        this.cdIpa = cdIpa;
    }

    @Column(name = "DL_NOTE_STRUT")
    public String getDlNoteStrut() {
        return this.dlNoteStrut;
    }

    public void setDlNoteStrut(String dlNoteStrut) {
        this.dlNoteStrut = dlNoteStrut;
    }

    @Column(name = "DS_STRUT")
    public String getDsStrut() {
        return this.dsStrut;
    }

    public void setDsStrut(String dsStrut) {
        this.dsStrut = dsStrut;
    }

    @Column(name = "CD_STRUT_NORMALIZ")
    public String getCdStrutNormaliz() {
        return this.cdStrutNormaliz;
    }

    public void setCdStrutNormaliz(String cdStrutNormaliz) {
        this.cdStrutNormaliz = cdStrutNormaliz;
    }

    @Column(name = "DT_INI_VAL")
    public LocalDateTime getDtIniVal() {
        return dtIniVal;
    }

    public void setDtIniVal(LocalDateTime dtIniVal) {
        this.dtIniVal = dtIniVal;
    }

    @Column(name = "DT_FINE_VAL")
    public LocalDateTime getDtFineVal() {
        return dtFineVal;
    }

    public void setDtFineVal(LocalDateTime dtFineVal) {
        this.dtFineVal = dtFineVal;
    }

    @Column(name = "DT_INI_VAL_STRUT")
    public LocalDateTime getDtIniValStrut() {
        return dtIniValStrut;
    }

    public void setDtIniValStrut(LocalDateTime dtIniValStrut) {
        this.dtIniValStrut = dtIniValStrut;
    }

    @Column(name = "DT_FINE_VAL_STRUT")
    public LocalDateTime getDtFineValStrut() {
        return dtFineValStrut;
    }

    public void setDtFineValStrut(LocalDateTime dtFineValStrut) {
        this.dtFineValStrut = dtFineValStrut;
    }

    @Column(name = "FL_TEMPLATE", columnDefinition = "CHAR")
    public String getFlTemplate() {
        return this.flTemplate;
    }

    public void setFlTemplate(String flTemplate) {
        this.flTemplate = flTemplate;
    }

    @Column(name = "ID_ENTE_CONVENZ")
    public BigDecimal getIdEnteConvenz() {
        return idEnteConvenz;
    }

    public void setIdEnteConvenz(BigDecimal idEnteConvenz) {
        this.idEnteConvenz = idEnteConvenz;
    }

    @Column(name = "NM_STRUT")
    public String getNmStrut() {
        return this.nmStrut;
    }

    public void setNmStrut(String nmStrut) {
        this.nmStrut = nmStrut;
    }

    @Column(name = "FL_CESSATO", columnDefinition = "CHAR")
    public String getFlCessato() {
        return this.flCessato;
    }

    public void setFlCessato(String flCessato) {
        this.flCessato = flCessato;
    }

    @Column(name = "FL_ARCHIVIO_RESTITUITO", columnDefinition = "CHAR")
    public String getFlArchivioRestituito() {
        return this.flArchivioRestituito;
    }

    public void setFlArchivioRestituito(String flArchivioRestituito) {
        this.flArchivioRestituito = flArchivioRestituito;
    }

    // bi-directional many-to-one association to AroUnitaDoc
    // @OneToMany(mappedBy = "orgStrut", fetch = FetchType.LAZY)
    // public List<AroUnitaDoc> getAroUnitaDocs() {
    // return this.aroUnitaDocs;
    // }

    // public void setAroUnitaDocs(List<AroUnitaDoc> aroUnitaDocs) {
    // this.aroUnitaDocs = aroUnitaDocs;
    // }

    // bi-directional many-to-one association to DecRegistroUnitaDoc
    @OneToMany(mappedBy = "orgStrut", cascade = {
            CascadeType.PERSIST, CascadeType.DETACH })
    public List<DecRegistroUnitaDoc> getDecRegistroUnitaDocs() {
        return this.decRegistroUnitaDocs;
    }

    public void setDecRegistroUnitaDocs(List<DecRegistroUnitaDoc> decRegistroUnitaDocs) {
        this.decRegistroUnitaDocs = decRegistroUnitaDocs;
    }

    // bi-directional many-to-one association to DecTipoUnitaDoc
    @OneToMany(mappedBy = "orgStrut", cascade = CascadeType.PERSIST)
    public List<DecTipoUnitaDoc> getDecTipoUnitaDocs() {
        return this.decTipoUnitaDocs;
    }

    public void setDecTipoUnitaDocs(List<DecTipoUnitaDoc> decTipoUnitaDocs) {
        this.decTipoUnitaDocs = decTipoUnitaDocs;
    }

    // bi-directional many-to-one association to DecTitol
    @OneToMany(mappedBy = "orgStrut", cascade = {
            CascadeType.PERSIST, CascadeType.REMOVE })

    public List<DecTitol> getDecTitols() {
        return this.decTitols;
    }

    public void setDecTitols(List<DecTitol> decTitols) {
        this.decTitols = decTitols;
    }

    // bi-directional many-to-one association to OrgCategStrut
    // @ManyToOne(fetch = FetchType.LAZY)
    @Column(name = "ID_CATEG_STRUT")
    public BigDecimal getOrgCategStrut() {
        return this.orgCategStrut;
    }

    public void setOrgCategStrut(BigDecimal orgCategStrut) {
        this.orgCategStrut = orgCategStrut;
    }

    // bi-directional many-to-one association to OrgEnte
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_ENTE")
    public OrgEnte getOrgEnte() {
        return this.orgEnte;
    }

    public void setOrgEnte(OrgEnte orgEnte) {
        this.orgEnte = orgEnte;
    }

    // bi-directional many-to-one association to DecTipoFascicolo
    @OneToMany(mappedBy = "orgStrut", cascade = CascadeType.PERSIST)
    public List<DecTipoFascicolo> getDecTipoFascicolos() {
        return this.decTipoFascicolos;
    }

    public void setDecTipoFascicolos(List<DecTipoFascicolo> decTipoFascicolos) {
        this.decTipoFascicolos = decTipoFascicolos;
    }

    // bi-directional many-to-one association to OrgUsoSistemaMigraz
    @OneToMany(mappedBy = "orgStrut")

    public List<OrgUsoSistemaMigraz> getOrgUsoSistemaMigrazs() {
        return this.orgUsoSistemaMigrazs;
    }

    public void setOrgUsoSistemaMigrazs(List<OrgUsoSistemaMigraz> orgUsoSistemaMigrazs) {
        this.orgUsoSistemaMigrazs = orgUsoSistemaMigrazs;
    }

    // bi-directional many-to-one association to AplValoreParamApplic
    @OneToMany(mappedBy = "orgStrut")
    public List<AplValoreParamApplic> getAplValoreParamApplics() {
        return this.aplValoreParamApplics;
    }

    public void setAplValoreParamApplics(List<AplValoreParamApplic> aplValoreParamApplics) {
        this.aplValoreParamApplics = aplValoreParamApplics;
    }

    public AplValoreParamApplic addAplValoreParamApplic(AplValoreParamApplic aplValoreParamApplic) {
        getAplValoreParamApplics().add(aplValoreParamApplic);
        aplValoreParamApplic.setOrgStrut(this);

        return aplValoreParamApplic;
    }

    public AplValoreParamApplic removeAplValoreParamApplic(
            AplValoreParamApplic aplValoreParamApplic) {
        getAplValoreParamApplics().remove(aplValoreParamApplic);
        aplValoreParamApplic.setOrgStrut(null);

        return aplValoreParamApplic;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 43 * hash + (int) (this.idStrut ^ (this.idStrut >>> 32));
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final OrgStrut other = (OrgStrut) obj;
        return this.idStrut == other.idStrut;
    }

    @PrePersist
    void preInsert() {
        if (this.flCessato == null) {
            this.flCessato = "0";
        }
        if (this.flArchivioRestituito == null) {
            this.flArchivioRestituito = "0";
        }
    }
}
