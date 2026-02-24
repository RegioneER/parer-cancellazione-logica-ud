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
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

/**
 * The persistent class for the IAM_IND_IP_USER database table.
 *
 */
@Entity
@Table(name = "IAM_IND_IP_USER")
@NamedQuery(name = "IamIndIpUser.deleteByIdUser", query = "DELETE FROM IamIndIpUser i where i.iamUser = :iamUser")
public class IamIndIpUser implements Serializable {

    private static final long serialVersionUID = 1L;
    private Long idIndIpUser;
    private String cdIndIpUser;
    private IamUser iamUser;

    public IamIndIpUser() {
        // hibernate constructor
    }

    @Id
    @NonMonotonicSequence(sequenceName = "SIAM_IND_IP_USER", incrementBy = 1)
    @Column(name = "ID_IND_IP_USER")
    public Long getIdIndIpUser() {
        return this.idIndIpUser;
    }

    public void setIdIndIpUser(Long idIndIpUser) {
        this.idIndIpUser = idIndIpUser;
    }

    @Column(name = "CD_IND_IP_USER")
    public String getCdIndIpUser() {
        return this.cdIndIpUser;
    }

    public void setCdIndIpUser(String cdIndIpUser) {
        this.cdIndIpUser = cdIndIpUser;
    }

    // bi-directional many-to-one association to IamUser
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_USER_IAM")
    public IamUser getIamUser() {
        return this.iamUser;
    }

    public void setIamUser(IamUser iamUser) {
        this.iamUser = iamUser;
    }

}
