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

import org.hibernate.annotations.Immutable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * The persistent class for the APL_SISTEMA_VERSANTE database table.
 *
 */
@Entity
@Immutable
@Table(schema = "SACER_IAM", name = "APL_SISTEMA_VERSANTE")
public class AplSistemaVersante implements Serializable {

    private static final long serialVersionUID = 1L;
    private Long idSistemaVersante;
    private String nmSistemaVersante;

    public AplSistemaVersante() {
        // hibernate constructor
    }

    @Id
    @Column(name = "ID_SISTEMA_VERSANTE")
    public Long getIdSistemaVersante() {
        return this.idSistemaVersante;
    }

    public void setIdSistemaVersante(Long idSistemaVersante) {
        this.idSistemaVersante = idSistemaVersante;
    }

    @Column(name = "NM_SISTEMA_VERSANTE")
    public String getNmSistemaVersante() {
        return this.nmSistemaVersante;
    }

    public void setNmSistemaVersante(String nmSistemaVersante) {
        this.nmSistemaVersante = nmSistemaVersante;
    }

}
