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
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

import it.eng.parer.soft.delete.jpa.inheritance.SoftDelete;

/**
 * The persistent class for the VRS_FILE_SESSIONE_KO database table.
 */
@Entity
@Table(name = "VRS_FILE_SESSIONE_KO")
public class VrsFileSessioneKo extends SoftDelete implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "ID_FILE_SESSIONE_KO")
    private Long idFileSessioneKo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_DATI_SESSIONE_VERS_KO")
    private VrsDatiSessioneVersKo vrsDatiSessioneVersKo;

    @OneToMany(mappedBy = "vrsFileSessioneKo")
    private List<VrsContenutoFileKo> vrsContenutoFileKos = new ArrayList<>();

    @OneToMany(mappedBy = "fileSessioneKo")
    private List<VrsFileSesObjectStorageKo> fileSesObjectStorageKos = new ArrayList<>();

    public VrsFileSessioneKo() {/* Hibernate */
    }

    public Long getIdFileSessioneKo() {
        return this.idFileSessioneKo;
    }

    public List<VrsContenutoFileKo> getVrsContenutoFileKos() {
        return this.vrsContenutoFileKos;
    }

    public VrsDatiSessioneVersKo getVrsDatiSessioneVersKo() {
        return this.vrsDatiSessioneVersKo;
    }

    public List<VrsFileSesObjectStorageKo> getFileSesObjectStorageKos() {
        return fileSesObjectStorageKos;
    }

}
