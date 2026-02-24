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
import java.time.LocalDateTime;

import it.eng.parer.soft.delete.jpa.sequence.NonMonotonicSequence;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * The persistent class for the ARO_DUP_RICH_SOFT_DELETE database table.
 */
@Entity
@Table(name = "ARO_DUP_RICH_SOFT_DELETE")

public class AroDupRichSoftDelete implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idDupRichSoftDelete;

    private BigDecimal idUnitaDocRef;

    private String nmChildTable;

    private LocalDateTime tsSoftDelete;

    private String dmSoftDelete;

    private AroItemRichSoftDelete aroItemRichSoftDelete;

    public AroDupRichSoftDelete() {/* Hibernate */
    }

    @Id
    @NonMonotonicSequence(sequenceName = "SARO_DUP_RICH_SOFT_DELETE", incrementBy = 1)
    @Column(name = "ID_DUP_RICH_SOFT_DELETE")
    public Long getIdDupRichSoftDelete() {
        return this.idDupRichSoftDelete;
    }

    public void setIdDupRichSoftDelete(Long idDupRichSoftDelete) {
        this.idDupRichSoftDelete = idDupRichSoftDelete;
    }

    @Column(name = "ID_UNITA_DOC_REF")
    public BigDecimal getIdUnitaDocRef() {
        return this.idUnitaDocRef;
    }

    public void setIdUnitaDocRef(BigDecimal idUnitaDocRef) {
        this.idUnitaDocRef = idUnitaDocRef;
    }

    @Column(name = "NM_CHILD_TABLE")
    public String getNmChildTable() {
        return this.nmChildTable;
    }

    public void setNmChildTable(String nmChildTable) {
        this.nmChildTable = nmChildTable;
    }

    @Column(name = "TS_SOFT_DELETE")
    public LocalDateTime getTsSoftDelete() {
        return tsSoftDelete;
    }

    public void setTsSoftDelete(LocalDateTime tsSoftDelete) {
        this.tsSoftDelete = tsSoftDelete;
    }

    @Column(name = "DM_SOFT_DELETE")
    public String getDmSoftDelete() {
        return this.dmSoftDelete;
    }

    public void setDmSoftDelete(String dmSoftDelete) {
        this.dmSoftDelete = dmSoftDelete;
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
