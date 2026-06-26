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
import java.math.BigDecimal;

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
 * The persistent class for the ARO_VALORE_ATTRIB_DATI_SPEC_RIC_DS database table.
 */
@Entity
@Table(name = "ARO_VALORE_ATTRIB_DATI_SPEC_RIC_DS")
public class AroValoreAttribDatiSpecRicDs extends SoftDelete implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "ID_VALORE_ATTRIB_DATI_SPEC")
    private Long idValoreAttribDatiSpec;

    // FK scalare read-only — usata nella custom query; owner della colonna è aroUsoXsdDatiSpec
    @Column(name = "ID_USO_XSD_DATI_SPEC", insertable = false, updatable = false)
    private Long idUsoXsdDatiSpec;

    // partition key read-only — pruning su ID_STRUT; Long allineato a OrgStrut.idStrut (@Id Long)
    @Column(name = "ID_STRUT", insertable = false, updatable = false)
    private Long idStrut;

    // partition key read-only — pruning su AA_KEY_UNITA_DOC; BigDecimal allineato a
    // AroUnitaDoc.aaKeyUnitaDoc
    @Column(name = "AA_KEY_UNITA_DOC", insertable = false, updatable = false)
    private BigDecimal aaKeyUnitaDoc;

    // parentClass inferita da AroUsoXsdDatiSpec (tipo del field); levels={} → tutti i livelli
    @RelationQuery(query = "SELECT v.idValoreAttribDatiSpec, v.idUsoXsdDatiSpec "
            + "FROM AroValoreAttribDatiSpecRicDs v " + "WHERE v.idUsoXsdDatiSpec IN :parentIds "
            + "AND v.idStrut = (SELECT ud.orgStrut.idStrut FROM AroUnitaDoc ud WHERE ud.idUnitaDoc = :rootId) "
            + "AND v.aaKeyUnitaDoc = (SELECT ud.aaKeyUnitaDoc FROM AroUnitaDoc ud WHERE ud.idUnitaDoc = :rootId)", levels = {}, parentIdParam = "parentIds", parentIdsParam = "parentIds")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_USO_XSD_DATI_SPEC")
    private AroUsoXsdDatiSpec aroUsoXsdDatiSpec;

    public AroValoreAttribDatiSpecRicDs() {/* Hibernate */
    }

    public Long getIdValoreAttribDatiSpec() {
        return this.idValoreAttribDatiSpec;
    }

    public AroUsoXsdDatiSpec getAroUsoXsdDatiSpec() {
        return this.aroUsoXsdDatiSpec;
    }

}
