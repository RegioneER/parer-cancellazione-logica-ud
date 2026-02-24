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

import it.eng.parer.soft.delete.jpa.inheritance.SoftDelete;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;

import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * The persistent class for the ARO_FILE_VER_INDICE_AIP_UD database table.
 */
@Entity
@Table(name = "ARO_FILE_VER_INDICE_AIP_UD")
public class AroFileVerIndiceAipUd extends SoftDelete implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "ID_FILE_VER_INDICE_AIP")
    private Long idFileVerIndiceAip;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_VER_INDICE_AIP")
    private AroVerIndiceAipUd aroVerIndiceAipUd;

    public AroFileVerIndiceAipUd() {/* Hibernate */
    }

    public Long getIdFileVerIndiceAip() {
        return this.idFileVerIndiceAip;
    }

    public AroVerIndiceAipUd getAroVerIndiceAipUd() {
        return this.aroVerIndiceAipUd;
    }

}
