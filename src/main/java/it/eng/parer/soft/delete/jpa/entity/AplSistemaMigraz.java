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
import java.util.ArrayList;
import java.util.List;

import it.eng.parer.soft.delete.jpa.sequence.NonMonotonicSequence;
import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/**
 * The persistent class for the APL_SISTEMA_MIGRAZ database table.
 *
 */
@Entity
@Cacheable(true)
@Table(name = "APL_SISTEMA_MIGRAZ")
public class AplSistemaMigraz implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long idSistemaMigraz;
    private String dsSistemaMigraz;
    private String nmSistemaMigraz;
    private List<OrgUsoSistemaMigraz> orgUsoSistemaMigrazs = new ArrayList<>();

    public AplSistemaMigraz() {
        // hibernate constructor
    }

    @Id
    @NonMonotonicSequence(sequenceName = "SAPL_SISTEMA_MIGRAZ", incrementBy = 1)
    @Column(name = "ID_SISTEMA_MIGRAZ")
    public Long getIdSistemaMigraz() {
        return this.idSistemaMigraz;
    }

    public void setIdSistemaMigraz(Long idSistemaMigraz) {
        this.idSistemaMigraz = idSistemaMigraz;
    }

    @Column(name = "DS_SISTEMA_MIGRAZ")
    public String getDsSistemaMigraz() {
        return this.dsSistemaMigraz;
    }

    public void setDsSistemaMigraz(String dsSistemaMigraz) {
        this.dsSistemaMigraz = dsSistemaMigraz;
    }

    @Column(name = "NM_SISTEMA_MIGRAZ")
    public String getNmSistemaMigraz() {
        return this.nmSistemaMigraz;
    }

    public void setNmSistemaMigraz(String nmSistemaMigraz) {
        this.nmSistemaMigraz = nmSistemaMigraz;
    }

    // bi-directional many-to-one association to OrgUsoSistemaMigraz
    @OneToMany(mappedBy = "aplSistemaMigraz")
    public List<OrgUsoSistemaMigraz> getOrgUsoSistemaMigrazs() {
        return this.orgUsoSistemaMigrazs;
    }

    public void setOrgUsoSistemaMigrazs(List<OrgUsoSistemaMigraz> orgUsoSistemaMigrazs) {
        this.orgUsoSistemaMigrazs = orgUsoSistemaMigrazs;
    }

}
