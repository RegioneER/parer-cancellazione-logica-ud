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
 * The persistent class for the ARO_XML_UPD_UNITA_DOC database table.
 */
@Entity
@Table(name = "ARO_XML_UPD_UNITA_DOC")

public class AroXmlUpdUnitaDoc extends SoftDelete implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "ID_XML_UPD_UNITA_DOC")
    private Long idXmlUpdUnitaDoc;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_UPD_UNITA_DOC")
    private AroUpdUnitaDoc aroUpdUnitaDoc;

    public AroXmlUpdUnitaDoc() {/* Hibernate */
    }

    public Long getIdXmlUpdUnitaDoc() {
        return this.idXmlUpdUnitaDoc;
    }

    public AroUpdUnitaDoc getAroUpdUnitaDoc() {
        return this.aroUpdUnitaDoc;
    }

}
