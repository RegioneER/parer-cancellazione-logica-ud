/*
 * Engineering Ingegneria Informatica S.p.A.
 *
 * Copyright (C) 2023 Regione Emilia-Romagna
 * <p/>
 * This program is free software: you can redistribute it and/or modify it under the terms of
 * the GNU Affero General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 * <p/>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <https://www.gnu.org/licenses/>.
 */

package it.eng.parer.soft.delete.jpa.entity;

import java.io.Serializable;
import java.math.BigDecimal;

import it.eng.parer.soft.delete.jpa.sequence.NonMonotonicSequence;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

/**
 *
 * @author fioravanti_f
 */
@Entity
@Table(name = "DEC_ERR_SACER")
@NamedQuery(name = "DecErrSacer.findAll", query = "SELECT d FROM DecErrSacer d")
public class DecErrSacer implements Serializable {

    private static final long serialVersionUID = 1L;
    private Long idErrSacer;
    private String cdErr;
    private String dsErr;
    private String dsErrFiltro;
    private String tiErrSacer;
    private BigDecimal decClasseErrSacer;

    public DecErrSacer() {
        // hibernate constructor
    }

    @Id
    @NonMonotonicSequence(sequenceName = "SDEC_ERR_SACER", incrementBy = 1)
    @Column(name = "ID_ERR_SACER")
    public Long getIdErrSacer() {
        return idErrSacer;
    }

    public void setIdErrSacer(Long idErrSacer) {
        this.idErrSacer = idErrSacer;
    }

    @Column(name = "CD_ERR")
    public String getCdErr() {
        return cdErr;
    }

    public void setCdErr(String cdErr) {
        this.cdErr = cdErr;
    }

    @Column(name = "DS_ERR")
    public String getDsErr() {
        return dsErr;
    }

    public void setDsErr(String dsErr) {
        this.dsErr = dsErr;
    }

    @Column(name = "DS_ERR_FILTRO")
    public String getDsErrFiltro() {
        return dsErrFiltro;
    }

    public void setDsErrFiltro(String dsErrFiltro) {
        this.dsErrFiltro = dsErrFiltro;
    }

    @Column(name = "TI_ERR_SACER")
    public String getTiErrSacer() {
        return tiErrSacer;
    }

    public void setTiErrSacer(String tiErrSacer) {
        this.tiErrSacer = tiErrSacer;
    }

    // bi-directional many-to-one association to DecAaTipoFascicolo
    // @ManyToOne(fetch = FetchType.LAZY)
    @Column(name = "ID_CLASSE_ERR_SACER")
    public BigDecimal getDecClasseErrSacer() {
        return decClasseErrSacer;
    }

    public void setDecClasseErrSacer(BigDecimal decClasseErrSacer) {
        this.decClasseErrSacer = decClasseErrSacer;
    }

}
