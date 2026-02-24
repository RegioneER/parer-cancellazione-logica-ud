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

import it.eng.parer.soft.delete.jpa.inheritance.SoftDelete;

/**
 * The persistent class for the ARO_LOG_STATO_CONSERV_UD database table.
 *
 */
@Entity
@Table(name = "ARO_LOG_STATO_CONSERV_UD")
@NamedQuery(name = "AroLogStatoConservUd.findAll", query = "SELECT a FROM AroLogStatoConservUd a")
public class AroLogStatoConservUd extends SoftDelete implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_LOG_STATO_CONSERV_UD")
    private long idLogStatoConservUd;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_UNITA_DOC")
    private AroUnitaDoc aroUnitaDoc;

    public AroLogStatoConservUd() {/* Hibernate */

    }

    public long getIdLogStatoConservUd() {
        return this.idLogStatoConservUd;
    }

    public AroUnitaDoc getAroUnitaDoc() {
        return this.aroUnitaDoc;
    }

}
