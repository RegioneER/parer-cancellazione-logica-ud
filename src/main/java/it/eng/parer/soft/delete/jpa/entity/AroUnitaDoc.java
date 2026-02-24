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

package it.eng.parer.soft.delete.jpa.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import it.eng.parer.soft.delete.jpa.inheritance.SoftDelete;
import it.eng.parer.soft.delete.jpa.sequence.NonMonotonicSequence;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

/**
 * The persistent class for the ARO_UNITA_DOC database table.
 */
@Entity
@Table(name = "ARO_UNITA_DOC")
public class AroUnitaDoc extends SoftDelete implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @NonMonotonicSequence(sequenceName = "SARO_UNITA_DOC", incrementBy = 1)
    @Column(name = "ID_UNITA_DOC")
    private Long idUnitaDoc;

    @Column(name = "ID_REGISTRO_UNITA_DOC", insertable = false, updatable = false)
    private Long idDecRegistroUnitaDoc;

    @Column(name = "TI_STATO_CONSERVAZIONE")
    private String tiStatoConservazione;

    @Column(name = "CD_KEY_UNITA_DOC_NORMALIZ")
    private String cdKeyUnitaDocNormaliz;

    @Column(name = "CD_REGISTRO_KEY_UNITA_DOC")
    private String cdRegistroKeyUnitaDoc;

    @Column(name = "AA_KEY_UNITA_DOC")
    private BigDecimal aaKeyUnitaDoc;

    @Column(name = "CD_KEY_UNITA_DOC")
    private String cdKeyUnitaDoc;

    @Column(name = "DT_ANNUL")
    private LocalDateTime dtAnnul;

    // bi-directional many-to-one association to DecTipoUnitaDoc
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_TIPO_UNITA_DOC")
    private DecTipoUnitaDoc decTipoUnitaDoc;

    // bi-directional many-to-one association to OrgStrut
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_STRUT")
    private OrgStrut orgStrut;

    // bi-directional many-to-one association to DecRegistroUnitaDoc
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_REGISTRO_UNITA_DOC")
    private DecRegistroUnitaDoc decRegistroUnitaDoc;

    @OneToMany(mappedBy = "aroUnitaDoc")
    private List<AroArchivSec> aroArchivSecs = new ArrayList<>();

    @OneToMany(mappedBy = "aroUnitaDoc")
    private List<AroCompDoc> aroCompDocs = new ArrayList<>();

    @OneToMany(mappedBy = "aroUnitaDoc")
    private List<AroDoc> aroDocs = new ArrayList<>();

    @OneToMany(mappedBy = "aroUnitaDoc")
    private List<AroIndiceAipUd> aroIndiceAipUds = new ArrayList<>();

    @OneToMany(mappedBy = "aroUnitaDoc")
    private List<AroIndiceAipUdDaElab> aroIndiceAipUdDaElabs = new ArrayList<>();

    @OneToMany(mappedBy = "aroUnitaDocLink")
    private List<AroLinkUnitaDoc> aroLinkUnitaDocLinks = new ArrayList<>();

    @OneToMany(mappedBy = "aroUnitaDoc")
    private List<AroLinkUnitaDoc> aroLinkUnitaDocs = new ArrayList<>();

    @OneToMany(mappedBy = "aroUnitaDoc")
    private List<AroNotaUnitaDoc> aroNotaUnitaDocs = new ArrayList<>();

    @OneToMany(mappedBy = "aroUnitaDocFirst")
    private List<FasFascicolo> fasFascicoloFirsts = new ArrayList<>();

    @OneToMany(mappedBy = "aroUnitaDocLast")
    private List<FasFascicolo> fasFascicoloLasts = new ArrayList<>();

    @OneToMany(mappedBy = "aroUnitaDoc")
    private List<AroUdAppartVerSerie> aroUdAppartVerSeries = new ArrayList<>();

    @OneToMany(mappedBy = "aroUnitaDoc")
    private List<AroUsoXsdDatiSpec> aroUsoXsdDatiSpecs = new ArrayList<>();

    @OneToMany(mappedBy = "aroUnitaDoc")
    private List<RecUnitaDocRecup> recUnitaDocRecups = new ArrayList<>();

    @OneToMany(mappedBy = "aroUnitaDoc")
    private List<SerUdErrFileInput> serUdErrFileInputs = new ArrayList<>();

    @OneToMany(mappedBy = "aroUnitaDoc")
    private List<VolAppartUnitaDocVolume> volAppartUnitaDocVolumes = new ArrayList<>();

    @OneToMany(mappedBy = "aroUnitaDoc")
    private List<VrsSessioneVers> vrsSessioneVers = new ArrayList<>();

    @OneToMany(mappedBy = "aroUnitaDoc")
    private List<VrsSessioneVersKo> vrsSessioneVersKos = new ArrayList<>();

    @OneToMany(mappedBy = "aroUnitaDoc")
    private List<AroWarnUnitaDoc> aroWarnUnitaDocs = new ArrayList<>();

    @OneToOne(cascade = CascadeType.ALL)
    @PrimaryKeyJoinColumn
    private AroXmlUnitaDocObjectStorage aroXmlUnitaDocObjectStorage;

    @OneToMany(mappedBy = "aroUnitaDoc")
    private List<ElvUdVersDaElabElenco> elvUdVersDaElabElencos = new ArrayList<>();

    @OneToMany(mappedBy = "aroUnitaDoc")
    private List<AroItemRichAnnulVers> aroItemRichAnnulVers = new ArrayList<>();

    @OneToMany(mappedBy = "aroUnitaDoc")
    private List<ElvElencoVersUdAnnul> elvElencoVersUdAnnuls = new ArrayList<>();

    @OneToMany(mappedBy = "aroUnitaDoc")
    private List<VolVolumeVersUdAnnul> volVolumeVersUdAnnuls = new ArrayList<>();

    @OneToMany(mappedBy = "aroUnitaDoc")
    private List<FasUnitaDocFascicolo> fasUnitaDocFascicolos = new ArrayList<>();

    @OneToMany(mappedBy = "aroUnitaDoc")
    private List<AroVersIniUnitaDoc> aroVersIniUnitaDocs = new ArrayList<>();

    @OneToMany(mappedBy = "aroUnitaDoc")
    private List<AroUpdUnitaDoc> aroUpdUnitaDocs = new ArrayList<>();

    @OneToMany(mappedBy = "aroUnitaDoc")
    private List<AroLogStatoConservUd> aroLogStatoConservUds = new ArrayList<>();

    @OneToOne(cascade = CascadeType.ALL)
    @PrimaryKeyJoinColumn
    private DmUdDel dmUdDel;

    public AroUnitaDoc() {/* Hibernate */
    }

    public Long getIdUnitaDoc() {
        return this.idUnitaDoc;
    }

    public void setIdUnitaDoc(Long idUnitaDoc) {
        this.idUnitaDoc = idUnitaDoc;
    }

    public Long getIdDecRegistroUnitaDoc() {
        return idDecRegistroUnitaDoc;
    }

    public String getTiStatoConservazione() {
        return this.tiStatoConservazione;
    }

    public void setTiStatoConservazione(String tiStatoConservazione) {
        this.tiStatoConservazione = tiStatoConservazione;
    }

    public String getCdKeyUnitaDocNormaliz() {
        return this.cdKeyUnitaDocNormaliz;
    }

    public void setCdRegistroKeyUnitaDoc(String cdRegistroKeyUnitaDoc) {
        this.cdRegistroKeyUnitaDoc = cdRegistroKeyUnitaDoc;
    }

    public String getCdRegistroKeyUnitaDoc() {
        return this.cdRegistroKeyUnitaDoc;
    }

    public BigDecimal getAaKeyUnitaDoc() {
        return this.aaKeyUnitaDoc;
    }

    public void setAaKeyUnitaDoc(BigDecimal aaKeyUnitaDoc) {
        this.aaKeyUnitaDoc = aaKeyUnitaDoc;
    }

    public String getCdKeyUnitaDoc() {
        return this.cdKeyUnitaDoc;
    }

    public void setCdKeyUnitaDoc(String cdKeyUnitaDoc) {
        this.cdKeyUnitaDoc = cdKeyUnitaDoc;
    }

    public LocalDateTime getDtAnnul() {
        return this.dtAnnul;
    }

    public DecTipoUnitaDoc getDecTipoUnitaDoc() {
        return this.decTipoUnitaDoc;
    }

    public OrgStrut getOrgStrut() {
        return this.orgStrut;
    }

    public DecRegistroUnitaDoc getDecRegistroUnitaDoc() {
        return this.decRegistroUnitaDoc;
    }

    public List<AroArchivSec> getAroArchivSecs() {
        return this.aroArchivSecs;
    }

    public List<AroCompDoc> getAroCompDocs() {
        return this.aroCompDocs;
    }

    public List<AroDoc> getAroDocs() {
        return this.aroDocs;
    }

    public List<AroIndiceAipUd> getAroIndiceAipUds() {
        return this.aroIndiceAipUds;
    }

    public List<AroIndiceAipUdDaElab> getAroIndiceAipUdDaElabs() {
        return this.aroIndiceAipUdDaElabs;
    }

    public List<AroLinkUnitaDoc> getAroLinkUnitaDocLinks() {
        return this.aroLinkUnitaDocLinks;
    }

    public List<AroLinkUnitaDoc> getAroLinkUnitaDocs() {
        return this.aroLinkUnitaDocs;
    }

    public List<AroNotaUnitaDoc> getAroNotaUnitaDocs() {
        return this.aroNotaUnitaDocs;
    }

    public List<FasFascicolo> getFasFascicoloFirsts() {
        return this.fasFascicoloFirsts;
    }

    public List<FasFascicolo> getFasFascicoloLasts() {
        return this.fasFascicoloLasts;
    }

    public List<AroUdAppartVerSerie> getAroUdAppartVerSeries() {
        return this.aroUdAppartVerSeries;
    }

    public List<VolAppartUnitaDocVolume> getVolAppartUnitaDocVolumes() {
        return this.volAppartUnitaDocVolumes;
    }

    public List<VrsSessioneVers> getVrsSessioneVers() {
        return this.vrsSessioneVers;
    }

    public List<VrsSessioneVersKo> getVrsSessioneVersKos() {
        return this.vrsSessioneVersKos;
    }

    public List<AroWarnUnitaDoc> getAroWarnUnitaDocs() {
        return this.aroWarnUnitaDocs;
    }

    public List<AroUsoXsdDatiSpec> getAroUsoXsdDatiSpecs() {
        return this.aroUsoXsdDatiSpecs;
    }

    public List<RecUnitaDocRecup> getRecUnitaDocRecups() {
        return this.recUnitaDocRecups;
    }

    public List<SerUdErrFileInput> getSerUdErrFileInputs() {
        return this.serUdErrFileInputs;
    }

    public List<ElvUdVersDaElabElenco> getElvUdVersDaElabElencos() {
        return this.elvUdVersDaElabElencos;
    }

    public List<AroItemRichAnnulVers> getAroItemRichAnnulVers() {
        return this.aroItemRichAnnulVers;
    }

    public List<ElvElencoVersUdAnnul> getElvElencoVersUdAnnuls() {
        return this.elvElencoVersUdAnnuls;
    }

    public List<VolVolumeVersUdAnnul> getVolVolumeVersUdAnnuls() {
        return this.volVolumeVersUdAnnuls;
    }

    public List<FasUnitaDocFascicolo> getFasUnitaDocFascicolos() {
        return this.fasUnitaDocFascicolos;
    }

    public List<AroVersIniUnitaDoc> getAroVersIniUnitaDocs() {
        return this.aroVersIniUnitaDocs;
    }

    public List<AroUpdUnitaDoc> getAroUpdUnitaDocs() {
        return this.aroUpdUnitaDocs;
    }

    public List<AroLogStatoConservUd> getAroLogStatoConservUds() {
        return aroLogStatoConservUds;
    }

    public AroXmlUnitaDocObjectStorage getAroXmlUnitaDocObjectStorage() {
        return aroXmlUnitaDocObjectStorage;
    }

    public DmUdDel getDmUdDel() {
        return dmUdDel;
    }

}
