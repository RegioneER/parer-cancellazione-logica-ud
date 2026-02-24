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

import java.util.ArrayList;
import java.io.Serializable;
import jakarta.persistence.*;
import java.util.List;

import it.eng.parer.soft.delete.jpa.inheritance.SoftDelete;

/**
 * The persistent class for the VRS_DATI_SESSIONE_VERS_KO database table.
 */
@Entity
@Table(name = "VRS_DATI_SESSIONE_VERS_KO")
public class VrsDatiSessioneVersKo extends SoftDelete implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "ID_DATI_SESSIONE_VERS_KO")
    private Long idDatiSessioneVersKo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_SESSIONE_VERS_KO")
    private VrsSessioneVersKo vrsSessioneVersKo;

    @OneToMany(mappedBy = "vrsDatiSessioneVersKo")
    private List<VrsErrSessioneVersKo> vrsErrSessioneVersKos = new ArrayList<>();

    @OneToMany(mappedBy = "vrsDatiSessioneVersKo")
    private List<VrsFileSessioneKo> vrsFileSessioneKos = new ArrayList<>();

    @OneToMany(mappedBy = "vrsDatiSessioneVersKo")
    private List<VrsXmlDatiSessioneVersKo> vrsXmlDatiSessioneVersKos = new ArrayList<>();

    @OneToMany(mappedBy = "datiSessioneVersKo")
    private List<VrsXmlDatiSesObjectStorageKo> xmlDatiSesObjectStorageKos = new ArrayList<>();

    public VrsDatiSessioneVersKo() {/* Hibernate */
    }

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long getIdDatiSessioneVersKo() {
        return this.idDatiSessioneVersKo;
    }

    public VrsSessioneVersKo getVrsSessioneVersKo() {
        return this.vrsSessioneVersKo;
    }

    public List<VrsErrSessioneVersKo> getVrsErrSessioneVersKos() {
        return this.vrsErrSessioneVersKos;
    }

    public List<VrsFileSessioneKo> getVrsFileSessioneKos() {
        return this.vrsFileSessioneKos;
    }

    public List<VrsXmlDatiSessioneVersKo> getVrsXmlDatiSessioneVersKos() {
        return this.vrsXmlDatiSessioneVersKos;
    }

    public List<VrsXmlDatiSesObjectStorageKo> getXmlDatiSesObjectStorageKos() {
        return xmlDatiSesObjectStorageKos;
    }

}
