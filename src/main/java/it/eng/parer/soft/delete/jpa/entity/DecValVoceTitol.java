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
 * The persistent class for the DEC_VAL_VOCE_TITOL database table.
 *
 */
@Entity
@Table(name = "DEC_VAL_VOCE_TITOL")
public class DecValVoceTitol implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long idValVoceTitol;
    private String dlNote;
    private String dsVoceTitol;
    private LocalDateTime dtFinVal;
    private LocalDateTime dtIniVal;
    private String flUsoClassif;
    private BigDecimal niAnniConserv;
    private DecVoceTitol decVoceTitol;

    public DecValVoceTitol() {
        // hibernate constructor
    }

    @Id
    @NonMonotonicSequence(sequenceName = "SDEC_VAL_VOCE_TITOL", incrementBy = 1)
    @Column(name = "ID_VAL_VOCE_TITOL")
    public Long getIdValVoceTitol() {
        return this.idValVoceTitol;
    }

    public void setIdValVoceTitol(Long idValVoceTitol) {
        this.idValVoceTitol = idValVoceTitol;
    }

    @Column(name = "DL_NOTE")
    public String getDlNote() {
        return this.dlNote;
    }

    public void setDlNote(String dlNote) {
        this.dlNote = dlNote;
    }

    @Column(name = "DS_VOCE_TITOL")
    public String getDsVoceTitol() {
        return this.dsVoceTitol;
    }

    public void setDsVoceTitol(String dsVoceTitol) {
        this.dsVoceTitol = dsVoceTitol;
    }

    @Column(name = "DT_FIN_VAL")
    public LocalDateTime getDtFinVal() {
        return this.dtFinVal;
    }

    public void setDtFinVal(LocalDateTime dtFinVal) {
        this.dtFinVal = dtFinVal;
    }

    @Column(name = "DT_INI_VAL")
    public LocalDateTime getDtIniVal() {
        return this.dtIniVal;
    }

    public void setDtIniVal(LocalDateTime dtIniVal) {
        this.dtIniVal = dtIniVal;
    }

    @Column(name = "FL_USO_CLASSIF", columnDefinition = "CHAR")
    public String getFlUsoClassif() {
        return this.flUsoClassif;
    }

    public void setFlUsoClassif(String flUsoClassif) {
        this.flUsoClassif = flUsoClassif;
    }

    @Column(name = "NI_ANNI_CONSERV")
    public BigDecimal getNiAnniConserv() {
        return this.niAnniConserv;
    }

    public void setNiAnniConserv(BigDecimal niAnniConserv) {
        this.niAnniConserv = niAnniConserv;
    }

    // bi-directional many-to-one association to DecVoceTitol
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_VOCE_TITOL")
    public DecVoceTitol getDecVoceTitol() {
        return this.decVoceTitol;
    }

    public void setDecVoceTitol(DecVoceTitol decVoceTitol) {
        this.decVoceTitol = decVoceTitol;
    }

}
