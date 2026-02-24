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
 * The persistent class for the ARO_STATO_RICH_SOFT_DELETE database table.
 */
@Entity
@Table(name = "ARO_STATO_RICH_SOFT_DELETE")

public class AroStatoRichSoftDelete implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idStatoRichSoftDelete;

    private LocalDateTime dtRegStatoRichSoftDelete;

    private BigDecimal pgStatoRichSoftDelete;

    private String tiStatoRichSoftDelete;

    private AroRichSoftDelete aroRichSoftDelete;

    private IamUser iamUser;

    public AroStatoRichSoftDelete() {/* Hibernate */
    }

    @Id
    @NonMonotonicSequence(sequenceName = "SARO_STATO_RICH_SOFT_DELETE", incrementBy = 1)
    @Column(name = "ID_STATO_RICH_SOFT_DELETE")
    public Long getIdStatoRichSoftDelete() {
        return this.idStatoRichSoftDelete;
    }

    public void setIdStatoRichSoftDelete(Long idStatoRichSoftDelete) {
        this.idStatoRichSoftDelete = idStatoRichSoftDelete;
    }

    @Column(name = "DT_REG_STATO_RICH_SOFT_DELETE")
    public LocalDateTime getDtRegStatoRichSoftDelete() {
        return this.dtRegStatoRichSoftDelete;
    }

    public void setDtRegStatoRichSoftDelete(LocalDateTime dtRegStatoRichSoftDelete) {
        this.dtRegStatoRichSoftDelete = dtRegStatoRichSoftDelete;
    }

    @Column(name = "PG_STATO_RICH_SOFT_DELETE")
    public BigDecimal getPgStatoRichSoftDelete() {
        return this.pgStatoRichSoftDelete;
    }

    public void setPgStatoRichSoftDelete(BigDecimal pgStatoRichSoftDelete) {
        this.pgStatoRichSoftDelete = pgStatoRichSoftDelete;
    }

    @Column(name = "TI_STATO_RICH_SOFT_DELETE")
    public String getTiStatoRichSoftDelete() {
        return this.tiStatoRichSoftDelete;
    }

    public void setTiStatoRichSoftDelete(String tiStatoRichSoftDelete) {
        this.tiStatoRichSoftDelete = tiStatoRichSoftDelete;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_RICH_SOFT_DELETE")
    public AroRichSoftDelete getAroRichSoftDelete() {
        return this.aroRichSoftDelete;
    }

    public void setAroRichSoftDelete(AroRichSoftDelete aroRichSoftDelete) {
        this.aroRichSoftDelete = aroRichSoftDelete;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_USER_IAM")
    public IamUser getIamUser() {
        return this.iamUser;
    }

    public void setIamUser(IamUser iamUser) {
        this.iamUser = iamUser;
    }

}
