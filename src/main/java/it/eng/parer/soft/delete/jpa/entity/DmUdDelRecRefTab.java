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

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * The persistent class for the DM_UD_DEL database table.
 */
@Entity
@Table(name = "DM_UD_DEL_REC_REF_TAB")

public class DmUdDelRecRefTab implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idUdDelRecRefTab;

    private String nmTab;

    private BigDecimal idPkRecTab;

    private BigDecimal idFkRecTab;

    private String nmColumnPk;

    private String nmColumnFk;

    private BigDecimal niLivello;

    private DmUdDel dmUdDel;

    public DmUdDelRecRefTab() {/* Hibernate */
    }

    @Id
    @Column(name = "ID_UD_DEL_REC_REF_TAB")
    public Long getIdUdDelRecRefTab() {
        return idUdDelRecRefTab;
    }

    // bi-directional many-to-one association to DmUdDel
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_UNITA_DOC")
    public DmUdDel getDmUdDel() {
        return dmUdDel;
    }

    @Column(name = "NM_TAB")
    public String getNmTab() {
        return this.nmTab;
    }

    @Column(name = "ID_PK_REC_TAB")
    public BigDecimal getIdPkRecTab() {
        return this.idPkRecTab;
    }

    @Column(name = "ID_FK_REC_TAB")
    public BigDecimal getIdFkRecTab() {
        return this.idFkRecTab;
    }

    @Column(name = "NM_COLUMN_PK")
    public String getNmColumnPk() {
        return this.nmColumnPk;
    }

    @Column(name = "NM_COLUMN_FK")
    public String getNmColumnFk() {
        return this.nmColumnFk;
    }

    @Column(name = "NI_LIVELLO")
    public BigDecimal getNiLivello() {
        return niLivello;
    }

}
