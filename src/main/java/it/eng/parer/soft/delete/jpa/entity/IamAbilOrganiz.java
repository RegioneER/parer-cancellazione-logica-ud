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
import java.util.ArrayList;
import java.util.List;

import it.eng.parer.soft.delete.jpa.sequence.NonMonotonicSequence;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/**
 * The persistent class for the IAM_ABIL_ORGANIZ database table.
 *
 */
@Entity
@Table(name = "IAM_ABIL_ORGANIZ")
@NamedQuery(name = "IamAbilOrganiz.deleteByIdUser", query = "DELETE FROM IamAbilOrganiz i where i.iamUser = :iamUser")
public class IamAbilOrganiz implements Serializable {

    private static final long serialVersionUID = 1L;
    private Long idAbilOrganiz;
    private String flOrganizDefault;
    private BigDecimal idOrganizApplic;
    private IamUser iamUser;
    private List<IamAbilTipoDato> iamAbilTipoDatos = new ArrayList<>();
    private List<IamAutorServ> iamAutorServs = new ArrayList<>();

    public IamAbilOrganiz() {
        // hibernate constructor
    }

    @Id
    @NonMonotonicSequence(sequenceName = "SIAM_ABIL_ORGANIZ", incrementBy = 1)
    @Column(name = "ID_ABIL_ORGANIZ")
    public Long getIdAbilOrganiz() {
        return this.idAbilOrganiz;
    }

    public void setIdAbilOrganiz(Long idAbilOrganiz) {
        this.idAbilOrganiz = idAbilOrganiz;
    }

    @Column(name = "FL_ORGANIZ_DEFAULT", columnDefinition = "CHAR")
    public String getFlOrganizDefault() {
        return this.flOrganizDefault;
    }

    public void setFlOrganizDefault(String flOrganizDefault) {
        this.flOrganizDefault = flOrganizDefault;
    }

    @Column(name = "ID_ORGANIZ_APPLIC")
    public BigDecimal getIdOrganizApplic() {
        return this.idOrganizApplic;
    }

    public void setIdOrganizApplic(BigDecimal idOrganizApplic) {
        this.idOrganizApplic = idOrganizApplic;
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

    // bi-directional many-to-one association to IamAbilTipoDato
    @OneToMany(cascade = {
            CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH,
            CascadeType.REMOVE }, mappedBy = "iamAbilOrganiz")
    public List<IamAbilTipoDato> getIamAbilTipoDatos() {
        return this.iamAbilTipoDatos;
    }

    public void setIamAbilTipoDatos(List<IamAbilTipoDato> iamAbilTipoDatos) {
        this.iamAbilTipoDatos = iamAbilTipoDatos;
    }

    // bi-directional many-to-one association to IamAutorServ
    @OneToMany(cascade = {
            CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH,
            CascadeType.REMOVE }, mappedBy = "iamAbilOrganiz")
    public List<IamAutorServ> getIamAutorServs() {
        return this.iamAutorServs;
    }

    public void setIamAutorServs(List<IamAutorServ> iamAutorServs) {
        this.iamAutorServs = iamAutorServs;
    }

}
