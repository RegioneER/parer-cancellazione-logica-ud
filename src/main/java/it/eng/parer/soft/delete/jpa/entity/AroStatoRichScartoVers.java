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
 * The persistent class for the ARO_STATO_RICH_SCARTO_VERS database table.
 */
@Entity
@Table(name = "ARO_STATO_RICH_SCARTO_VERS")

public class AroStatoRichScartoVers implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idStatoRichScartoVers;

    private String dsNotaRichScartoVers;

    private LocalDateTime dtRegStatoRichScartoVers;

    private BigDecimal pgStatoRichScartoVers;

    private String tiStatoRichScartoVers;

    private AroRichScartoVers aroRichScartoVers;

    private IamUser iamUser;

    public AroStatoRichScartoVers() {/* Hibernate */
    }

    @Id
    @Column(name = "ID_STATO_RICH_SCARTO_VERS")
    @NonMonotonicSequence(sequenceName = "SARO_STATO_RICH_SCARTO_VERS", incrementBy = 1)
    public Long getIdStatoRichScartoVers() {
        return this.idStatoRichScartoVers;
    }

    public void setIdStatoRichScartoVers(Long idStatoRichScartoVers) {
        this.idStatoRichScartoVers = idStatoRichScartoVers;
    }

    @Column(name = "DS_NOTA_RICH_SCARTO_VERS")
    public String getDsNotaRichScartoVers() {
        return this.dsNotaRichScartoVers;
    }

    public void setDsNotaRichScartoVers(String dsNotaRichScartoVers) {
        this.dsNotaRichScartoVers = dsNotaRichScartoVers;
    }

    @Column(name = "DT_REG_STATO_RICH_SCARTO_VERS")
    public LocalDateTime getDtRegStatoRichScartoVers() {
        return this.dtRegStatoRichScartoVers;
    }

    public void setDtRegStatoRichScartoVers(LocalDateTime dtRegStatoRichScartoVers) {
        this.dtRegStatoRichScartoVers = dtRegStatoRichScartoVers;
    }

    @Column(name = "PG_STATO_RICH_SCARTO_VERS")
    public BigDecimal getPgStatoRichScartoVers() {
        return this.pgStatoRichScartoVers;
    }

    public void setPgStatoRichScartoVers(BigDecimal pgStatoRichScartoVers) {
        this.pgStatoRichScartoVers = pgStatoRichScartoVers;
    }

    @Column(name = "TI_STATO_RICH_SCARTO_VERS")
    public String getTiStatoRichScartoVers() {
        return this.tiStatoRichScartoVers;
    }

    public void setTiStatoRichScartoVers(String tiStatoRichScartoVers) {
        this.tiStatoRichScartoVers = tiStatoRichScartoVers;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_RICH_SCARTO_VERS")
    public AroRichScartoVers getAroRichScartoVers() {
        return this.aroRichScartoVers;
    }

    public void setAroRichScartoVers(AroRichScartoVers aroRichScartoVers) {
        this.aroRichScartoVers = aroRichScartoVers;
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
