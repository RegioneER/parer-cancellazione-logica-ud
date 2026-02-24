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
import jakarta.persistence.*;

/**
 * The persistent class for the ARO_UPD_UNITA_DOC database table.
 */
@Entity
@Table(name = "ARO_UPD_UNITA_DOC")

public class AroUpdUnitaDoc extends SoftDelete implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "ID_UPD_UNITA_DOC")
    private Long idUpdUnitaDoc;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_UNITA_DOC")
    private AroUnitaDoc aroUnitaDoc;

    @OneToMany(mappedBy = "aroUpdUnitaDoc")
    private List<AroUpdDatiSpecUnitaDoc> aroUpdDatiSpecUnitaDocs = new ArrayList<>();

    @OneToMany(mappedBy = "aroUpdUnitaDoc")
    private List<AroUpdDatiSpecUdObjectStorage> aroUpdDatiSpecUdObjectStorages = new ArrayList<>();

    @OneToMany(mappedBy = "aroUpdUnitaDoc")
    private List<AroUpdDocUnitaDoc> aroUpdDocUnitaDocs = new ArrayList<>();

    @OneToMany(mappedBy = "aroUpdUnitaDoc")
    private List<AroUpdLinkUnitaDoc> aroUpdLinkUnitaDocs = new ArrayList<>();

    @OneToMany(mappedBy = "aroUpdUnitaDoc")
    private List<AroWarnUpdUnitaDoc> aroWarnUpdUnitaDocs = new ArrayList<>();

    @OneToMany(mappedBy = "aroUpdUnitaDoc")
    private List<AroXmlUpdUnitaDoc> aroXmlUpdUnitaDocs = new ArrayList<>();

    @OneToMany(mappedBy = "aroUpdUnitaDoc")
    private List<AroUpdArchivSec> aroUpdArchivSecs = new ArrayList<>();

    @OneToMany(mappedBy = "aroUpdUnitaDoc")
    private List<ElvUpdUdDaElabElenco> elvUpdUdDaElabElencos = new ArrayList<>();

    @OneToMany(mappedBy = "aroUpdUnitaDoc")
    private List<AroUpdUdVerIndiceAipUd> aroUpdUdVerIndiceAipUds = new ArrayList<>();

    @OneToMany(mappedBy = "aroUpdUnitaDoc")
    private List<AroUpdUdIndiceAipDaElab> aroUpdUdIndiceAipDaElabs = new ArrayList<>();

    @OneToMany(mappedBy = "aroUpdUnitaDoc")
    private List<VrsSesUpdUnitaDocKo> vrsSesUpdUnitaDocKos = new ArrayList<>();

    @OneToOne(cascade = CascadeType.ALL)
    @PrimaryKeyJoinColumn
    private AroXmlUpdUdObjectStorage aroXmlUpdUdObjectStorage;

    public AroUpdUnitaDoc() {/* Hibernate */
    }

    public Long getIdUpdUnitaDoc() {
        return this.idUpdUnitaDoc;
    }

    public AroUnitaDoc getAroUnitaDoc() {
        return this.aroUnitaDoc;
    }

    public List<AroUpdDatiSpecUnitaDoc> getAroUpdDatiSpecUnitaDocs() {
        return this.aroUpdDatiSpecUnitaDocs;
    }

    public List<AroUpdDatiSpecUdObjectStorage> getAroUpdDatiSpecUdObjectStorages() {
        return this.aroUpdDatiSpecUdObjectStorages;
    }

    public List<AroUpdDocUnitaDoc> getAroUpdDocUnitaDocs() {
        return this.aroUpdDocUnitaDocs;
    }

    public List<AroUpdLinkUnitaDoc> getAroUpdLinkUnitaDocs() {
        return this.aroUpdLinkUnitaDocs;
    }

    public List<AroWarnUpdUnitaDoc> getAroWarnUpdUnitaDocs() {
        return this.aroWarnUpdUnitaDocs;
    }

    public List<AroXmlUpdUnitaDoc> getAroXmlUpdUnitaDocs() {
        return this.aroXmlUpdUnitaDocs;
    }

    public List<AroUpdArchivSec> getAroUpdArchivSecs() {
        return this.aroUpdArchivSecs;
    }

    public List<ElvUpdUdDaElabElenco> getElvUpdUdDaElabElencos() {
        return this.elvUpdUdDaElabElencos;
    }

    public List<AroUpdUdVerIndiceAipUd> getAroUpdUdVerIndiceAipUds() {
        return this.aroUpdUdVerIndiceAipUds;
    }

    public List<AroUpdUdIndiceAipDaElab> getAroUpdUdIndiceAipDaElabs() {
        return this.aroUpdUdIndiceAipDaElabs;
    }

    public List<VrsSesUpdUnitaDocKo> getVrsSesUpdUnitaDocKos() {
        return this.vrsSesUpdUnitaDocKos;
    }

    public AroXmlUpdUdObjectStorage getAroXmlUpdUdObjectStorage() {
        return aroXmlUpdUdObjectStorage;
    }

}
