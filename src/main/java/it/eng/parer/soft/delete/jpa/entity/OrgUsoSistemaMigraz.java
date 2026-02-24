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

import it.eng.parer.soft.delete.jpa.sequence.NonMonotonicSequence;
import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * The persistent class for the ORG_USO_SISTEMA_MIGRAZ database table.
 *
 */
@Entity
@Cacheable(true)
@Table(name = "ORG_USO_SISTEMA_MIGRAZ")
public class OrgUsoSistemaMigraz implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long idUsoSistemaMigraz;
    private AplSistemaMigraz aplSistemaMigraz;
    private OrgStrut orgStrut;

    public OrgUsoSistemaMigraz() {
        // hibernate constructor
    }

    @Id
    @NonMonotonicSequence(sequenceName = "SORG_USO_SISTEMA_MIGRAZ", incrementBy = 1)
    @Column(name = "ID_USO_SISTEMA_MIGRAZ")
    public Long getIdUsoSistemaMigraz() {
        return this.idUsoSistemaMigraz;
    }

    public void setIdUsoSistemaMigraz(Long idUsoSistemaMigraz) {
        this.idUsoSistemaMigraz = idUsoSistemaMigraz;
    }

    // bi-directional many-to-one association to AplSistemaMigraz
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_SISTEMA_MIGRAZ")
    public AplSistemaMigraz getAplSistemaMigraz() {
        return this.aplSistemaMigraz;
    }

    public void setAplSistemaMigraz(AplSistemaMigraz aplSistemaMigraz) {
        this.aplSistemaMigraz = aplSistemaMigraz;
    }

    // bi-directional many-to-one association to OrgStrut
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_STRUT")
    public OrgStrut getOrgStrut() {
        return this.orgStrut;
    }

    public void setOrgStrut(OrgStrut orgStrut) {
        this.orgStrut = orgStrut;
    }

}
