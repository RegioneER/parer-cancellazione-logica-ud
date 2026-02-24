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

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import jakarta.persistence.Table;

import it.eng.parer.soft.delete.jpa.sequence.NonMonotonicSequence;

/**
 * The persistent class for the ARO_STATO_RICH_ANNUL_VERS database table.
 */
@Entity
@Table(name = "ARO_STATO_RICH_ANNUL_VERS")

public class AroStatoRichAnnulVers implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idStatoRichAnnulVers;

    private String dsNotaRichAnnulVers;

    private LocalDateTime dtRegStatoRichAnnulVers;

    private BigDecimal pgStatoRichAnnulVers;

    private String tiStatoRichAnnulVers;

    private AroRichAnnulVers aroRichAnnulVers;

    private IamUser iamUser;

    public AroStatoRichAnnulVers() {/* Hibernate */
    }

    @Id
    @Column(name = "ID_STATO_RICH_ANNUL_VERS")
    @NonMonotonicSequence(sequenceName = "SARO_STATO_RICH_ANNUL_VERS", incrementBy = 1)
    public Long getIdStatoRichAnnulVers() {
        return this.idStatoRichAnnulVers;
    }

    public void setIdStatoRichAnnulVers(Long idStatoRichAnnulVers) {
        this.idStatoRichAnnulVers = idStatoRichAnnulVers;
    }

    @Column(name = "DS_NOTA_RICH_ANNUL_VERS")
    public String getDsNotaRichAnnulVers() {
        return this.dsNotaRichAnnulVers;
    }

    public void setDsNotaRichAnnulVers(String dsNotaRichAnnulVers) {
        this.dsNotaRichAnnulVers = dsNotaRichAnnulVers;
    }

    @Column(name = "DT_REG_STATO_RICH_ANNUL_VERS")
    public LocalDateTime getDtRegStatoRichAnnulVers() {
        return this.dtRegStatoRichAnnulVers;
    }

    public void setDtRegStatoRichAnnulVers(LocalDateTime dtRegStatoRichAnnulVers) {
        this.dtRegStatoRichAnnulVers = dtRegStatoRichAnnulVers;
    }

    @Column(name = "PG_STATO_RICH_ANNUL_VERS")
    public BigDecimal getPgStatoRichAnnulVers() {
        return this.pgStatoRichAnnulVers;
    }

    public void setPgStatoRichAnnulVers(BigDecimal pgStatoRichAnnulVers) {
        this.pgStatoRichAnnulVers = pgStatoRichAnnulVers;
    }

    @Column(name = "TI_STATO_RICH_ANNUL_VERS")
    public String getTiStatoRichAnnulVers() {
        return this.tiStatoRichAnnulVers;
    }

    public void setTiStatoRichAnnulVers(String tiStatoRichAnnulVers) {
        this.tiStatoRichAnnulVers = tiStatoRichAnnulVers;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_RICH_ANNUL_VERS")
    public AroRichAnnulVers getAroRichAnnulVers() {
        return this.aroRichAnnulVers;
    }

    public void setAroRichAnnulVers(AroRichAnnulVers aroRichAnnulVers) {
        this.aroRichAnnulVers = aroRichAnnulVers;
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
