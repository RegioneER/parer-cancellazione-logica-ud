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
import jakarta.persistence.*;

/**
 * The persistent class for the FAS_AMMIN_PARTEC database table.
 */
@Entity
@Table(name = "FAS_AMMIN_PARTEC")
public class FasAmminPartec extends SoftDelete implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "ID_AMMIN_PARTEC")
    private Long idAmminPartec;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_FASCICOLO")
    private FasFascicolo fasFascicolo;

    public FasAmminPartec() {/* Hibernate */
    }

    public Long getIdAmminPartec() {
        return this.idAmminPartec;
    }

    public FasFascicolo getFasFascicolo() {
        return this.fasFascicolo;
    }

}
