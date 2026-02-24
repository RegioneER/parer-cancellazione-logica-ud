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

import it.eng.parer.soft.delete.beans.annotations.RelationQuery;
import it.eng.parer.soft.delete.jpa.inheritance.SoftDelete;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * The persistent class for the ARO_COMP_VER_INDICE_AIP_UD database table.
 */
@Entity
@Table(name = "ARO_COMP_VER_INDICE_AIP_UD")
@RelationQuery(parentClass = AroCompDoc.class, query = "SELECT c.idCompVerIndiceAipUd, c.aroCompDoc.idCompDoc "
        + "FROM AroCompVerIndiceAipUd c " + "JOIN c.aroCompDoc p "
        + "WHERE p.idCompDoc IN :parentIds " + "AND c.aroVerIndiceAipUd.idVerIndiceAip IN "
        + "(SELECT v.idVerIndiceAip FROM AroVerIndiceAipUd v "
        + "JOIN v.aroIndiceAipUd i JOIN i.aroUnitaDoc u WHERE u.idUnitaDoc = p.idUnitaDocRif)", levels = {
                2 }, parentIdParam = "parentIds", parentIdsParam = "parentIds")
// @RelationQuery(parentClass = AroCompDoc.class, query = "SELECT c.idCompVerIndiceAipUd,
// c.aroCompDoc.idCompDoc "
// + "FROM AroCompVerIndiceAipUd c " + "JOIN c.aroCompDoc p " + "JOIN p.aroStrutDoc s " + "JOIN
// s.aroDoc d "
// + "JOIN d.aroUnitaDoc ud " + "WHERE p.idCompDoc IN :parentIds " + "AND ud.idUnitaDoc = :rootId "
// + "AND c.aroVerIndiceAipUd.idVerIndiceAip IN " + "(SELECT v.idVerIndiceAip FROM AroVerIndiceAipUd
// v "
// + "JOIN v.aroIndiceAipUd i JOIN i.aroUnitaDoc u WHERE u.idUnitaDoc = ud.idUnitaDoc)", levels = {
// 4 }, parentIdsParam
// = "parentIds")
@RelationQuery(parentClass = AroVerIndiceAipUd.class, query = "SELECT c.idCompVerIndiceAipUd, c.aroVerIndiceAipUd.idVerIndiceAip "
        + "FROM AroCompVerIndiceAipUd c " + "JOIN c.aroVerIndiceAipUd v "
        + "JOIN v.aroIndiceAipUd d "
        + "WHERE v.idVerIndiceAip IN :parentIds AND d.aroUnitaDoc.idUnitaDoc = :rootId", levels = {
                3 }, parentIdParam = "parentIds", parentIdsParam = "parentIds")
public class AroCompVerIndiceAipUd extends SoftDelete implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "ID_COMP_VER_INDICE_AIP_UD")
    private Long idCompVerIndiceAipUd;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_COMP_DOC")
    private AroCompDoc aroCompDoc;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_VER_INDICE_AIP")
    private AroVerIndiceAipUd aroVerIndiceAipUd;

    public AroCompVerIndiceAipUd() {/* Hibernate */
    }

    public Long getIdCompVerIndiceAipUd() {
        return this.idCompVerIndiceAipUd;
    }

    public AroCompDoc getAroCompDoc() {
        return this.aroCompDoc;
    }

    public AroVerIndiceAipUd getAroVerIndiceAipUd() {
        return this.aroVerIndiceAipUd;
    }

}
