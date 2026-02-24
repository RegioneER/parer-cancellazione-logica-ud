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
 * The persistent class for the ARO_ERR_RICH_SOFT_DELETE database table.
 */
@Entity
@Table(name = "ARO_ERR_RICH_SOFT_DELETE")

public class AroErrRichSoftDelete implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idErrRichSoftDelete;

    private String dsErr;

    private BigDecimal pgErr;

    private String tiErr;

    private String tiGravita;

    private AroItemRichSoftDelete aroItemRichSoftDelete;

    public AroErrRichSoftDelete() {/* Hibernate */
    }

    @Id
    @NonMonotonicSequence(sequenceName = "SARO_ERR_RICH_SOFT_DELETE", incrementBy = 1)
    @Column(name = "ID_ERR_RICH_SOFT_DELETE")
    public Long getIdErrRichSoftDelete() {
        return this.idErrRichSoftDelete;
    }

    public void setIdErrRichSoftDelete(Long idErrRichSoftDelete) {
        this.idErrRichSoftDelete = idErrRichSoftDelete;
    }

    @Column(name = "DS_ERR")
    public String getDsErr() {
        return this.dsErr;
    }

    public void setDsErr(String dsErr) {
        this.dsErr = dsErr;
    }

    @Column(name = "PG_ERR")
    public BigDecimal getPgErr() {
        return this.pgErr;
    }

    public void setPgErr(BigDecimal pgErr) {
        this.pgErr = pgErr;
    }

    @Column(name = "TI_ERR")
    public String getTiErr() {
        return this.tiErr;
    }

    public void setTiErr(String tiErr) {
        this.tiErr = tiErr;
    }

    @Column(name = "TI_GRAVITA")
    public String getTiGravita() {
        return this.tiGravita;
    }

    public void setTiGravita(String tiGravita) {
        this.tiGravita = tiGravita;
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
