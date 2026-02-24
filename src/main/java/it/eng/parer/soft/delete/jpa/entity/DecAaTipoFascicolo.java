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
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

/**
 * The persistent class for the DEC_AA_TIPO_FASCICOLO database table.
 *
 */
@Entity
@Table(name = "DEC_AA_TIPO_FASCICOLO")
@NamedQuery(name = "DecAaTipoFascicolo.findAll", query = "SELECT d FROM DecAaTipoFascicolo d")
public class DecAaTipoFascicolo implements Serializable {

    private static final long serialVersionUID = 1L;
    private Long idAaTipoFascicolo;
    private BigDecimal aaFinTipoFascicolo;
    private BigDecimal aaIniTipoFascicolo;
    private String flUpdFmtNumero;
    private BigDecimal niCharPadParteClassif;
    private DecTipoFascicolo decTipoFascicolo;

    public DecAaTipoFascicolo() {
        // hibernate constructor
    }

    @Id
    @NonMonotonicSequence(sequenceName = "SDEC_AA_TIPO_FASCICOLO", incrementBy = 1)
    @Column(name = "ID_AA_TIPO_FASCICOLO")
    public Long getIdAaTipoFascicolo() {
        return this.idAaTipoFascicolo;
    }

    public void setIdAaTipoFascicolo(Long idAaTipoFascicolo) {
        this.idAaTipoFascicolo = idAaTipoFascicolo;
    }

    @Column(name = "AA_FIN_TIPO_FASCICOLO")
    public BigDecimal getAaFinTipoFascicolo() {
        return this.aaFinTipoFascicolo;
    }

    public void setAaFinTipoFascicolo(BigDecimal aaFinTipoFascicolo) {
        this.aaFinTipoFascicolo = aaFinTipoFascicolo;
    }

    @Column(name = "AA_INI_TIPO_FASCICOLO")
    public BigDecimal getAaIniTipoFascicolo() {
        return this.aaIniTipoFascicolo;
    }

    public void setAaIniTipoFascicolo(BigDecimal aaIniTipoFascicolo) {
        this.aaIniTipoFascicolo = aaIniTipoFascicolo;
    }

    @Column(name = "FL_UPD_FMT_NUMERO", columnDefinition = "CHAR")
    public String getFlUpdFmtNumero() {
        return this.flUpdFmtNumero;
    }

    public void setFlUpdFmtNumero(String flUpdFmtNumero) {
        this.flUpdFmtNumero = flUpdFmtNumero;
    }

    @Column(name = "NI_CHAR_PAD_PARTE_CLASSIF")
    public BigDecimal getNiCharPadParteClassif() {
        return this.niCharPadParteClassif;
    }

    public void setNiCharPadParteClassif(BigDecimal niCharPadParteClassif) {
        this.niCharPadParteClassif = niCharPadParteClassif;
    }

    // bi-directional many-to-one association to DecTipoFascicolo
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_TIPO_FASCICOLO")
    public DecTipoFascicolo getDecTipoFascicolo() {
        return this.decTipoFascicolo;
    }

    public void setDecTipoFascicolo(DecTipoFascicolo decTipoFascicolo) {
        this.decTipoFascicolo = decTipoFascicolo;
    }

}
