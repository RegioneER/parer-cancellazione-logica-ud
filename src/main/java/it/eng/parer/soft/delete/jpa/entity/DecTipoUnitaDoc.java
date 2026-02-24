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
 * The persistent class for the DEC_TIPO_UNITA_DOC database table.
 *
 */
@Entity
@Cacheable(true)
@Table(name = "DEC_TIPO_UNITA_DOC")
public class DecTipoUnitaDoc implements Serializable {

    private static final long serialVersionUID = 1L;
    private Long idTipoUnitaDoc;
    private String cdSerie;
    private String cdSerieDaCreare;
    private String dlNoteTipoUd;
    private String dsSerieDaCreare;
    private String dsTipoSerieDaCreare;
    private String dsTipoUnitaDoc;
    private LocalDateTime dtIstituz;
    private LocalDateTime dtSoppres;
    private String flCreaTipoSerieStandard;
    private String flVersManuale;
    private String nmTipoSerieDaCreare;
    private String nmTipoUnitaDoc;
    private String tiSaveFile;
    private List<AroUnitaDoc> aroUnitaDocs = new ArrayList<>();
    private BigDecimal decCategTipoUnitaDoc;
    private OrgStrut orgStrut;
    private BigDecimal decModelloTipoSerie;
    private BigDecimal orgTipoServizio;
    private BigDecimal orgTipoServizioAttiv;
    private BigDecimal orgTipoServAttivTipoUd;
    private BigDecimal orgTipoServConservTipoUd;
    private List<AplValoreParamApplic> aplValoreParamApplics = new ArrayList<>();

    public DecTipoUnitaDoc() {
        // hibernate constructor
    }

    @Id
    @NonMonotonicSequence(sequenceName = "SDEC_TIPO_UNITA_DOC", incrementBy = 1)
    @Column(name = "ID_TIPO_UNITA_DOC")
    public Long getIdTipoUnitaDoc() {
        return this.idTipoUnitaDoc;
    }

    public void setIdTipoUnitaDoc(Long idTipoUnitaDoc) {
        this.idTipoUnitaDoc = idTipoUnitaDoc;
    }

    @Column(name = "CD_SERIE")
    public String getCdSerie() {
        return this.cdSerie;
    }

    public void setCdSerie(String cdSerie) {
        this.cdSerie = cdSerie;
    }

    @Column(name = "CD_SERIE_DA_CREARE")
    public String getCdSerieDaCreare() {
        return this.cdSerieDaCreare;
    }

    public void setCdSerieDaCreare(String cdSerieDaCreare) {
        this.cdSerieDaCreare = cdSerieDaCreare;
    }

    @Column(name = "DL_NOTE_TIPO_UD")
    public String getDlNoteTipoUd() {
        return this.dlNoteTipoUd;
    }

    public void setDlNoteTipoUd(String dlNoteTipoUd) {
        this.dlNoteTipoUd = dlNoteTipoUd;
    }

    @Column(name = "DS_SERIE_DA_CREARE")
    public String getDsSerieDaCreare() {
        return this.dsSerieDaCreare;
    }

    public void setDsSerieDaCreare(String dsSerieDaCreare) {
        this.dsSerieDaCreare = dsSerieDaCreare;
    }

    @Column(name = "DS_TIPO_SERIE_DA_CREARE")
    public String getDsTipoSerieDaCreare() {
        return this.dsTipoSerieDaCreare;
    }

    public void setDsTipoSerieDaCreare(String dsTipoSerieDaCreare) {
        this.dsTipoSerieDaCreare = dsTipoSerieDaCreare;
    }

    @Column(name = "DS_TIPO_UNITA_DOC")
    public String getDsTipoUnitaDoc() {
        return this.dsTipoUnitaDoc;
    }

    public void setDsTipoUnitaDoc(String dsTipoUnitaDoc) {
        this.dsTipoUnitaDoc = dsTipoUnitaDoc;
    }

    @Column(name = "DT_ISTITUZ")
    public LocalDateTime getDtIstituz() {
        return this.dtIstituz;
    }

    public void setDtIstituz(LocalDateTime dtIstituz) {
        this.dtIstituz = dtIstituz;
    }

    @Column(name = "DT_SOPPRES")
    public LocalDateTime getDtSoppres() {
        return this.dtSoppres;
    }

    public void setDtSoppres(LocalDateTime dtSoppres) {
        this.dtSoppres = dtSoppres;
    }

    @Column(name = "FL_CREA_TIPO_SERIE_STANDARD", columnDefinition = "CHAR")
    public String getFlCreaTipoSerieStandard() {
        return this.flCreaTipoSerieStandard;
    }

    public void setFlCreaTipoSerieStandard(String flCreaTipoSerieStandard) {
        this.flCreaTipoSerieStandard = flCreaTipoSerieStandard;
    }

    @Column(name = "FL_VERS_MANUALE", columnDefinition = "CHAR")
    public String getFlVersManuale() {
        return this.flVersManuale;
    }

    public void setFlVersManuale(String flVersManuale) {
        this.flVersManuale = flVersManuale;
    }

    @Column(name = "NM_TIPO_SERIE_DA_CREARE")
    public String getNmTipoSerieDaCreare() {
        return this.nmTipoSerieDaCreare;
    }

    public void setNmTipoSerieDaCreare(String nmTipoSerieDaCreare) {
        this.nmTipoSerieDaCreare = nmTipoSerieDaCreare;
    }

    @Column(name = "NM_TIPO_UNITA_DOC")
    public String getNmTipoUnitaDoc() {
        return this.nmTipoUnitaDoc;
    }

    public void setNmTipoUnitaDoc(String nmTipoUnitaDoc) {
        this.nmTipoUnitaDoc = nmTipoUnitaDoc;
    }

    @Column(name = "TI_SAVE_FILE")
    public String getTiSaveFile() {
        return this.tiSaveFile;
    }

    public void setTiSaveFile(String tiSaveFile) {
        this.tiSaveFile = tiSaveFile;
    }

    // bi-directional many-to-one association to AroUnitaDoc
    @OneToMany(mappedBy = "decTipoUnitaDoc")
    public List<AroUnitaDoc> getAroUnitaDocs() {
        return this.aroUnitaDocs;
    }

    public void setAroUnitaDocs(List<AroUnitaDoc> aroUnitaDocs) {
        this.aroUnitaDocs = aroUnitaDocs;
    }

    // bi-directional many-to-one association to DecCategTipoUnitaDoc
    // @ManyToOne(fetch = FetchType.LAZY)
    @Column(name = "ID_CATEG_TIPO_UNITA_DOC")
    public BigDecimal getDecCategTipoUnitaDoc() {
        return this.decCategTipoUnitaDoc;
    }

    public void setDecCategTipoUnitaDoc(BigDecimal decCategTipoUnitaDoc) {
        this.decCategTipoUnitaDoc = decCategTipoUnitaDoc;
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

    // bi-directional many-to-one association to DecModelloTipoSerie
    // @ManyToOne(fetch = FetchType.LAZY)
    @Column(name = "ID_MODELLO_TIPO_SERIE")
    public BigDecimal getDecModelloTipoSerie() {
        return this.decModelloTipoSerie;
    }

    public void setDecModelloTipoSerie(BigDecimal decModelloTipoSerie) {
        this.decModelloTipoSerie = decModelloTipoSerie;
    }

    // bi-directional many-to-one association to OrgTipoServizio
    // @ManyToOne(fetch = FetchType.LAZY)
    @Column(name = "ID_TIPO_SERVIZIO")
    public BigDecimal getOrgTipoServizio() {
        return this.orgTipoServizio;
    }

    public void setOrgTipoServizio(BigDecimal orgTipoServizio) {
        this.orgTipoServizio = orgTipoServizio;
    }

    // bi-directional many-to-one association to OrgTipoServizio
    // @ManyToOne(fetch = FetchType.LAZY)
    @Column(name = "ID_TIPO_SERVIZIO_ATTIV")
    public BigDecimal getOrgTipoServizioAttiv() {
        return orgTipoServizioAttiv;
    }

    public void setOrgTipoServizioAttiv(BigDecimal orgTipoServizioAttiv) {
        this.orgTipoServizioAttiv = orgTipoServizioAttiv;
    }

    // @ManyToOne(fetch = FetchType.LAZY)
    @Column(name = "ID_TIPO_SERV_ATTIV_TIPO_UD")
    public BigDecimal getOrgTipoServAttivTipoUd() {
        return this.orgTipoServAttivTipoUd;
    }

    public void setOrgTipoServAttivTipoUd(BigDecimal orgTipoServAttivTipoUd) {
        this.orgTipoServAttivTipoUd = orgTipoServAttivTipoUd;
    }

    // bi-directional many-to-one association to OrgTipoServizio

    // @ManyToOne(fetch = FetchType.LAZY)
    @Column(name = "ID_TIPO_SERV_CONSERV_TIPO_UD")
    public BigDecimal getOrgTipoServConservTipoUd() {
        return orgTipoServConservTipoUd;
    }

    public void setOrgTipoServConservTipoUd(BigDecimal orgTipoServConservTipoUd) {
        this.orgTipoServConservTipoUd = orgTipoServConservTipoUd;
    }

    // bi-directional many-to-one association to AplValoreParamApplic
    //
    @OneToMany(mappedBy = "decTipoUnitaDoc")
    public List<AplValoreParamApplic> getAplValoreParamApplics() {
        return this.aplValoreParamApplics;
    }

    public void setAplValoreParamApplics(List<AplValoreParamApplic> aplValoreParamApplics) {
        this.aplValoreParamApplics = aplValoreParamApplics;
    }

    public AplValoreParamApplic addAplValoreParamApplic(AplValoreParamApplic aplValoreParamApplic) {
        getAplValoreParamApplics().add(aplValoreParamApplic);
        aplValoreParamApplic.setDecTipoUnitaDoc(this);

        return aplValoreParamApplic;
    }

    public AplValoreParamApplic removeAplValoreParamApplic(
            AplValoreParamApplic aplValoreParamApplic) {
        getAplValoreParamApplics().remove(aplValoreParamApplic);
        aplValoreParamApplic.setDecTipoUnitaDoc(null);

        return aplValoreParamApplic;
    }

    /**
     * Gestione dei default. Risulta la migliore pratica in quanto è indipendente dal db utilizzato
     * e sfrutta diretta JPA quindi calabile sotto ogni contesto in termini di ORM
     *
     * ref. https://stackoverflow.com/a/13432234
     */
    @PrePersist
    void preInsert() {
        if (this.flVersManuale == null) {
            this.flVersManuale = "0";
        }
    }

}
