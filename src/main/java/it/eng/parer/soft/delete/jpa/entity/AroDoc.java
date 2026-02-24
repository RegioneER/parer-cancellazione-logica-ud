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
import java.util.ArrayList;
import java.util.List;

import it.eng.parer.soft.delete.jpa.inheritance.SoftDelete;
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
 * The persistent class for the ARO_DOC database table.
 *
 */
@Entity
@Table(name = "ARO_DOC")
public class AroDoc extends SoftDelete implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "ID_DOC")
    private Long idDoc;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_UNITA_DOC")
    private AroUnitaDoc aroUnitaDoc;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_TIPO_DOC")
    private DecTipoDoc decTipoDoc;

    @Column(name = "TI_DOC")
    private String tiDoc;

    @OneToOne(cascade = CascadeType.ALL)
    @PrimaryKeyJoinColumn
    private AroXmlDocObjectStorage aroXmlDocObjectStorage;

    @OneToMany(mappedBy = "aroDoc")
    private List<AroStrutDoc> aroStrutDocs = new ArrayList<>();

    @OneToMany(mappedBy = "aroDoc")
    private List<AroUsoXsdDatiSpec> aroUsoXsdDatiSpecs = new ArrayList<>();

    @OneToMany(mappedBy = "aroDoc")
    private List<RecSessioneRecup> recSessioneRecups = new ArrayList<>();

    @OneToMany(mappedBy = "aroDoc")
    private List<VolAppartDocVolume> volAppartDocVolumes = new ArrayList<>();

    @OneToMany(mappedBy = "aroDoc")
    private List<AroWarnUnitaDoc> aroWarnUnitaDocs = new ArrayList<>();

    @OneToMany(mappedBy = "aroDoc")
    private List<VrsSessioneVers> vrsSessioneVers = new ArrayList<>();

    @OneToMany(mappedBy = "aroDoc")
    private List<VrsSessioneVersKo> vrsSessioneVersKos = new ArrayList<>();

    @OneToMany(mappedBy = "aroDoc")
    private List<ElvDocAggDaElabElenco> elvDocAggDaElabElencos = new ArrayList<>();

    @OneToMany(mappedBy = "aroDoc")
    private List<AroVersIniDoc> aroVersIniDocs = new ArrayList<>();

    @OneToMany(mappedBy = "aroDoc")
    private List<AroUpdDocUnitaDoc> aroUpdDocUnitaDocs = new ArrayList<>();

    public AroDoc() {
        // hibernate
    }

    public Long getIdDoc() {
        return this.idDoc;
    }

    public AroUnitaDoc getAroUnitaDoc() {
        return this.aroUnitaDoc;
    }

    public DecTipoDoc getDecTipoDoc() {
        return this.decTipoDoc;
    }

    public String getTiDoc() {
        return this.tiDoc;
    }

    public List<AroStrutDoc> getAroStrutDocs() {
        return this.aroStrutDocs;
    }

    public List<AroUsoXsdDatiSpec> getAroUsoXsdDatiSpecs() {
        return this.aroUsoXsdDatiSpecs;
    }

    public List<VolAppartDocVolume> getVolAppartDocVolumes() {
        return this.volAppartDocVolumes;
    }

    public List<AroWarnUnitaDoc> getAroWarnUnitaDocs() {
        return this.aroWarnUnitaDocs;
    }

    public List<VrsSessioneVers> getVrsSessioneVers() {
        return this.vrsSessioneVers;
    }

    public List<VrsSessioneVersKo> getVrsSessioneVersKos() {
        return this.vrsSessioneVersKos;
    }

    public List<RecSessioneRecup> getRecSessioneRecups() {
        return this.recSessioneRecups;
    }

    public List<ElvDocAggDaElabElenco> getElvDocAggDaElabElencos() {
        return this.elvDocAggDaElabElencos;
    }

    public List<AroVersIniDoc> getAroVersIniDocs() {
        return this.aroVersIniDocs;
    }

    public List<AroUpdDocUnitaDoc> getAroUpdDocUnitaDocs() {
        return this.aroUpdDocUnitaDocs;
    }

    public AroXmlDocObjectStorage getAroXmlDocObjectStorage() {
        return aroXmlDocObjectStorage;
    }

}
