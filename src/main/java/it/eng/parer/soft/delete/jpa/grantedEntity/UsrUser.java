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

package it.eng.parer.soft.delete.jpa.grantedEntity;

import java.io.Serializable;

import org.hibernate.annotations.Immutable;

import it.eng.parer.soft.delete.jpa.entity.AplSistemaVersante;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * The persistent class for the USR_USER database table.
 *
 */
@Entity
@Immutable
@Table(name = "USR_USER", schema = "SACER_IAM")
public class UsrUser implements Serializable {

    private static final long serialVersionUID = 1L;
    private Long idUserIam;
    private AplSistemaVersante aplSistemaVersante;

    public UsrUser() {
        // hibernate constructor
    }

    @Id
    @Column(name = "ID_USER_IAM")
    public Long getIdUserIam() {
        return this.idUserIam;
    }

    public void setIdUserIam(Long idUserIam) {
        this.idUserIam = idUserIam;
    }

    // bi-directional many-to-one association to AplSistemaVersante
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_SISTEMA_VERSANTE")
    public AplSistemaVersante getAplSistemaVersante() {
        return this.aplSistemaVersante;
    }

    public void setAplSistemaVersante(AplSistemaVersante aplSistemaVersante) {
        this.aplSistemaVersante = aplSistemaVersante;
    }
}
