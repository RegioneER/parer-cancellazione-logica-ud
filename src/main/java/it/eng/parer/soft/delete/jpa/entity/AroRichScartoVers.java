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
import java.util.ArrayList;
import java.util.List;

import it.eng.parer.soft.delete.jpa.inheritance.SoftDelete;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/**
 * The persistent class for the ARO_RICH_SCARTO_VERS database table.
 */
@Entity
@Table(name = "ARO_RICH_SCARTO_VERS")

public class AroRichScartoVers extends SoftDelete implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "ID_RICH_SCARTO_VERS")
    private Long idRichScartoVers;

    @Column(name = "CD_RICH_SCARTO_VERS")
    private String cdRichScartoVers;

    /*
     * @OneToMany(mappedBy = "aroRichScartoVers") private List<AroFileRichScartoVers>
     * aroFileRichScartoVers = new ArrayList<>();
     */

    @OneToMany(mappedBy = "aroRichScartoVers")
    private List<AroItemRichScartoVers> aroItemRichScartoVers = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_STRUT")
    private OrgStrut orgStrut;

    @OneToMany(mappedBy = "aroRichScartoVers")
    private List<AroStatoRichScartoVers> aroStatoRichScartoVers = new ArrayList<>();

    /*
     * @OneToMany(mappedBy = "aroRichScartoVers") private List<AroXmlRichScartoVers>
     * aroXmlRichScartoVers = new ArrayList<>();
     */

    public AroRichScartoVers() {/* Hibernate */
    }

    public Long getIdRichScartoVers() {
        return this.idRichScartoVers;
    }

    public void setIdRichScartoVers(Long idRichScartoVers) {
        this.idRichScartoVers = idRichScartoVers;
    }

    public String getCdRichScartoVers() {
        return this.cdRichScartoVers;
    }

    public void setCdRichScartoVers(String cdRichScartoVers) {
        this.cdRichScartoVers = cdRichScartoVers;
    }

    /*
     * public List<AroFileRichScartoVers> getAroFileRichScartoVers() { return
     * this.aroFileRichScartoVers; }
     */

    public List<AroItemRichScartoVers> getAroItemRichScartoVers() {
        return this.aroItemRichScartoVers;
    }

    public OrgStrut getOrgStrut() {
        return this.orgStrut;
    }

    public List<AroStatoRichScartoVers> getAroStatoRichScartoVers() {
        return this.aroStatoRichScartoVers;
    }

    /*
     * public List<AroXmlRichScartoVers> getAroXmlRichScartoVers() { return
     * this.aroXmlRichScartoVers; }
     */

}
