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
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/**
 * The persistent class for the ARO_VER_INDICE_AIP_UD database table.
 */
@Entity
@Table(name = "ARO_VER_INDICE_AIP_UD")
public class AroVerIndiceAipUd extends SoftDelete implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "ID_VER_INDICE_AIP")
    private Long idVerIndiceAip;

    @OneToMany(mappedBy = "aroVerIndiceAipUd")
    private List<AroCompVerIndiceAipUd> aroCompVerIndiceAipUds = new ArrayList<>();

    @OneToMany(mappedBy = "aroVerIndiceAipUd")
    private List<AroFileVerIndiceAipUd> aroFileVerIndiceAipUds = new ArrayList<>();

    @OneToMany(mappedBy = "aroVerIndiceAipUd")
    private List<AroNotaUnitaDoc> aroNotaUnitaDocs = new ArrayList<>();

    @OneToMany(mappedBy = "aroVerIndiceAipUd")
    private List<FasContenVerAipFascicolo> fasContenVerAipFascicolos = new ArrayList<>();

    @OneToMany(mappedBy = "aroVerIndiceAipUd")
    private List<FasUdAipFascicoloDaElab> fasUdAipFascicoloDaElabs = new ArrayList<>();

    @OneToMany(mappedBy = "aroVerIndiceAipUd")
    private List<AroVerIndiceAipUdObjectStorage> aroVerIndiceAipUdObjectStorages = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_INDICE_AIP")
    private AroIndiceAipUd aroIndiceAipUd;

    @OneToMany(mappedBy = "aroVerIndiceAipUd")
    private List<AroUpdUdVerIndiceAipUd> aroUpdUdVerIndiceAipUds = new ArrayList<>();

    @OneToMany(mappedBy = "aroVerIndiceAipUd")
    private List<AroUrnVerIndiceAipUd> aroUrnVerIndiceAipUds = new ArrayList<>();

    public AroVerIndiceAipUd() {/* Hibernate */
    }

    public Long getIdVerIndiceAip() {
        return this.idVerIndiceAip;
    }

    public List<AroCompVerIndiceAipUd> getAroCompVerIndiceAipUds() {
        return this.aroCompVerIndiceAipUds;
    }

    public List<AroFileVerIndiceAipUd> getAroFileVerIndiceAipUds() {
        return this.aroFileVerIndiceAipUds;
    }

    public List<AroNotaUnitaDoc> getAroNotaUnitaDocs() {
        return this.aroNotaUnitaDocs;
    }

    public List<FasContenVerAipFascicolo> getFasContenVerAipFascicolos() {
        return this.fasContenVerAipFascicolos;
    }

    public List<FasUdAipFascicoloDaElab> getFasUdAipFascicoloDaElabs() {
        return this.fasUdAipFascicoloDaElabs;
    }

    public List<AroVerIndiceAipUdObjectStorage> getAroVerIndiceAipUdObjectStorages() {
        return this.aroVerIndiceAipUdObjectStorages;
    }

    public AroIndiceAipUd getAroIndiceAipUd() {
        return this.aroIndiceAipUd;
    }

    public List<AroUpdUdVerIndiceAipUd> getAroUpdUdVerIndiceAipUds() {
        return this.aroUpdUdVerIndiceAipUds;
    }

    public List<AroUrnVerIndiceAipUd> getAroUrnVerIndiceAipUds() {
        return this.aroUrnVerIndiceAipUds;
    }

}
