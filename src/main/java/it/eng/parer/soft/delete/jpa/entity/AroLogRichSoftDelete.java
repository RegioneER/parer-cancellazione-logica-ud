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

import it.eng.parer.soft.delete.jpa.sequence.NonMonotonicSequence;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * The persistent class for the ARO_LOG_RICH_SOFT_DELETE database table.
 */
@Entity
@Table(name = "ARO_LOG_RICH_SOFT_DELETE")

public class AroLogRichSoftDelete implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idLogRichSoftDelete;

    private BigDecimal idUnitaDocRef;

    private String nmParentTable;

    private String nmChildTable;

    private BigDecimal updatedRowCount;

    private BigDecimal niLevel;

    private AroItemRichSoftDelete aroItemRichSoftDelete;

    public AroLogRichSoftDelete() {/* Hibernate */
    }

    @Id
    @NonMonotonicSequence(sequenceName = "SARO_LOG_RICH_SOFT_DELETE", incrementBy = 1)
    @Column(name = "ID_LOG_RICH_SOFT_DELETE")
    public Long getIdLogRichSoftDelete() {
        return this.idLogRichSoftDelete;
    }

    public void setIdLogRichSoftDelete(Long idLogRichSoftDelete) {
        this.idLogRichSoftDelete = idLogRichSoftDelete;
    }

    @Column(name = "ID_UNITA_DOC_REF")
    public BigDecimal getIdUnitaDocRef() {
        return this.idUnitaDocRef;
    }

    public void setIdUnitaDocRef(BigDecimal idUnitaDocRef) {
        this.idUnitaDocRef = idUnitaDocRef;
    }

    @Column(name = "NM_PARENT_TABLE")
    public String getNmParentTable() {
        return this.nmParentTable;
    }

    public void setNmParentTable(String nmParentTable) {
        this.nmParentTable = nmParentTable;
    }

    @Column(name = "NM_CHILD_TABLE")
    public String getNmChildTable() {
        return this.nmChildTable;
    }

    public void setNmChildTable(String nmChildTable) {
        this.nmChildTable = nmChildTable;
    }

    @Column(name = "UPDATED_ROW_COUNT")
    public BigDecimal getUpdatedRowCount() {
        return this.updatedRowCount;
    }

    public void setUpdatedRowCount(BigDecimal updatedRowCount) {
        this.updatedRowCount = updatedRowCount;
    }

    @Column(name = "NI_LEVEL")
    public BigDecimal getNiLevel() {
        return this.niLevel;
    }

    public void setNiLevel(BigDecimal niLevel) {
        this.niLevel = niLevel;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_ITEM_RICH_SOFT_DELETE")
    public AroItemRichSoftDelete getAroItemRichSoftDelete() {
        return this.aroItemRichSoftDelete;
    }

    public void setAroItemRichSoftDelete(AroItemRichSoftDelete aroItemRichSoftDelete) {
        this.aroItemRichSoftDelete = aroItemRichSoftDelete;
    }

}
