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
 * The persistent class for the ARO_UPD_COMP_UNITA_DOC database table.
 */
@Entity
@Table(name = "ARO_UPD_COMP_UNITA_DOC")

public class AroUpdCompUnitaDoc extends SoftDelete implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "ID_UPD_COMP_UNITA_DOC")
    private Long idUpdCompUnitaDoc;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_COMP_DOC")
    private AroCompDoc aroCompDoc;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_UPD_DOC_UNITA_DOC")
    private AroUpdDocUnitaDoc aroUpdDocUnitaDoc;

    @OneToMany(mappedBy = "aroUpdCompUnitaDoc")
    private List<AroUpdDatiSpecUnitaDoc> aroUpdDatiSpecUnitaDocs = new ArrayList<>();

    public AroUpdCompUnitaDoc() {/* Hibernate */
    }

    public Long getIdUpdCompUnitaDoc() {
        return this.idUpdCompUnitaDoc;
    }

    public AroCompDoc getAroCompDoc() {
        return this.aroCompDoc;
    }

    public AroUpdDocUnitaDoc getAroUpdDocUnitaDoc() {
        return this.aroUpdDocUnitaDoc;
    }

    public List<AroUpdDatiSpecUnitaDoc> getAroUpdDatiSpecUnitaDocs() {
        return this.aroUpdDatiSpecUnitaDocs;
    }

}
