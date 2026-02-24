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
import java.util.ArrayList;
import java.util.List;

import it.eng.parer.soft.delete.jpa.inheritance.SoftDelete;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/**
 * The persistent class for the ARO_COMP_DOC database table.
 */
@Entity
@Table(name = "ARO_COMP_DOC")
public class AroCompDoc extends SoftDelete implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "ID_COMP_DOC")
    private Long idCompDoc;

    @Column(name = "ID_STRUT")
    private BigDecimal idStrut;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_COMP_DOC_PADRE")
    private AroCompDoc aroCompDoc;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_STRUT_DOC")
    private AroStrutDoc aroStrutDoc;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_UNITA_DOC_RIF")
    private AroUnitaDoc aroUnitaDoc;

    @OneToMany(mappedBy = "aroCompDoc")
    private List<AroCompDoc> aroCompDocs = new ArrayList<>();

    /*
     * @OneToMany(mappedBy = "aroCompDoc") private List<AroContenutoComp> aroContenutoComps = new
     * ArrayList<>();
     */

    @OneToMany(mappedBy = "aroCompDoc")
    private List<AroBustaCrittog> aroBustaCrittogs = new ArrayList<>();

    @OneToMany(mappedBy = "aroCompDoc")
    private List<AroFirmaComp> aroFirmaComps = new ArrayList<>();

    @OneToMany(mappedBy = "aroCompDoc")
    private List<AroMarcaComp> aroMarcaComps = new ArrayList<>();

    @OneToMany(mappedBy = "aroCompDoc")
    private List<AroUsoXsdDatiSpec> aroUsoXsdDatiSpecs = new ArrayList<>();

    @OneToMany(mappedBy = "aroCompDoc")
    private List<VolAppartCompVolume> volAppartCompVolumes = new ArrayList<>();

    @OneToMany(mappedBy = "aroCompDoc")
    private List<RecSessioneRecup> recSessioneRecups = new ArrayList<>();

    @OneToMany(mappedBy = "aroCompDoc")
    private List<AroCompHashCalc> aroCompHashCalcs = new ArrayList<>();

    @OneToMany(mappedBy = "aroCompDoc")
    private List<AroCompObjectStorage> aroCompObjectStorages = new ArrayList<>();

    @OneToMany(mappedBy = "aroCompDoc")
    private List<AroCompIndiceAipDaElab> aroCompIndiceAipDaElabs = new ArrayList<>();

    @OneToMany(mappedBy = "aroCompDoc")
    private List<AroCompVerIndiceAipUd> aroCompVerIndiceAipUds = new ArrayList<>();

    @OneToMany(mappedBy = "aroCompDoc")
    private List<FirReport> firReport = new ArrayList<>();

    @OneToMany(mappedBy = "aroCompDoc")
    private List<AroVersIniComp> aroVersIniComps = new ArrayList<>();

    @OneToMany(mappedBy = "aroCompDoc")
    private List<AroUpdCompUnitaDoc> aroUpdCompUnitaDocs = new ArrayList<>();

    @OneToMany(mappedBy = "aroCompDoc")
    private List<AroCompUrnCalc> aroAroCompUrnCalcs = new ArrayList<>();

    public AroCompDoc() {
        // hibernate
    }

    public Long getIdCompDoc() {
        return this.idCompDoc;
    }

    public BigDecimal getIdStrut() {
        return this.idStrut;
    }

    public AroCompDoc getAroCompDoc() {
        return this.aroCompDoc;
    }

    public AroStrutDoc getAroStrutDoc() {
        return this.aroStrutDoc;
    }

    public AroUnitaDoc getAroUnitaDoc() {
        return this.aroUnitaDoc;
    }

    public List<AroCompDoc> getAroCompDocs() {
        return this.aroCompDocs;
    }

    /*
     * public List<AroContenutoComp> getAroContenutoComps() { return this.aroContenutoComps; }
     */

    public List<AroBustaCrittog> getAroBustaCrittogs() {
        return this.aroBustaCrittogs;
    }

    public List<AroFirmaComp> getAroFirmaComps() {
        return this.aroFirmaComps;
    }

    public List<AroMarcaComp> getAroMarcaComps() {
        return this.aroMarcaComps;
    }

    public List<AroUsoXsdDatiSpec> getAroUsoXsdDatiSpecs() {
        return this.aroUsoXsdDatiSpecs;
    }

    public List<VolAppartCompVolume> getVolAppartCompVolumes() {
        return this.volAppartCompVolumes;
    }

    public List<RecSessioneRecup> getRecSessioneRecups() {
        return this.recSessioneRecups;
    }

    public List<AroCompHashCalc> getAroCompHashCalcs() {
        return this.aroCompHashCalcs;
    }

    public List<AroCompObjectStorage> getAroCompObjectStorages() {
        return this.aroCompObjectStorages;
    }

    public List<AroCompIndiceAipDaElab> getAroCompIndiceAipDaElabs() {
        return this.aroCompIndiceAipDaElabs;
    }

    public List<AroCompVerIndiceAipUd> getAroCompVerIndiceAipUds() {
        return this.aroCompVerIndiceAipUds;
    }

    public List<FirReport> getFirReport() {
        return firReport;
    }

    public List<AroVersIniComp> getAroVersIniComps() {
        return this.aroVersIniComps;
    }

    public List<AroUpdCompUnitaDoc> getAroUpdCompUnitaDocs() {
        return this.aroUpdCompUnitaDocs;
    }

    public List<AroCompUrnCalc> getAroAroCompUrnCalcs() {
        return this.aroAroCompUrnCalcs;
    }

}
