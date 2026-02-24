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
import jakarta.persistence.Table;

/**
 * The persistent class for the IAM_ABIL_TIPO_DATO database table.
 *
 */
@Entity
@Table(name = "IAM_ABIL_TIPO_DATO")
public class IamAbilTipoDato implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long idAbilTipoDato;
    private BigDecimal idTipoDatoApplic;
    private String nmClasseTipoDato;
    private IamAbilOrganiz iamAbilOrganiz;

    public IamAbilTipoDato() {
        // hibernate constructor
    }

    @Id
    @NonMonotonicSequence(sequenceName = "SIAM_ABIL_TIPO_DATO", incrementBy = 1)
    @Column(name = "ID_ABIL_TIPO_DATO")
    public Long getIdAbilTipoDato() {
        return this.idAbilTipoDato;
    }

    public void setIdAbilTipoDato(Long idAbilTipoDato) {
        this.idAbilTipoDato = idAbilTipoDato;
    }

    @Column(name = "ID_TIPO_DATO_APPLIC")
    public BigDecimal getIdTipoDatoApplic() {
        return this.idTipoDatoApplic;
    }

    public void setIdTipoDatoApplic(BigDecimal idTipoDatoApplic) {
        this.idTipoDatoApplic = idTipoDatoApplic;
    }

    @Column(name = "NM_CLASSE_TIPO_DATO")
    public String getNmClasseTipoDato() {
        return this.nmClasseTipoDato;
    }

    public void setNmClasseTipoDato(String nmClasseTipoDato) {
        this.nmClasseTipoDato = nmClasseTipoDato;
    }

    // bi-directional many-to-one association to IamAbilOrganiz
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_ABIL_ORGANIZ")
    public IamAbilOrganiz getIamAbilOrganiz() {
        return this.iamAbilOrganiz;
    }

    public void setIamAbilOrganiz(IamAbilOrganiz iamAbilOrganiz) {
        this.iamAbilOrganiz = iamAbilOrganiz;
    }

}
