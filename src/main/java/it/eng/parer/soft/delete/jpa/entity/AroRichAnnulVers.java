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
 * The persistent class for the ARO_RICH_ANNUL_VERS database table.
 */
@Entity
@Table(name = "ARO_RICH_ANNUL_VERS")

public class AroRichAnnulVers extends SoftDelete implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "ID_RICH_ANNUL_VERS")
    private Long idRichAnnulVers;

    @Column(name = "CD_RICH_ANNUL_VERS")
    private String cdRichAnnulVers;

    @Column(name = "TI_ANNULLAMENTO")
    private String tiAnnullamento;

    /*
     * @OneToMany(mappedBy = "aroRichAnnulVers") private List<AroFileRichAnnulVers>
     * aroFileRichAnnulVers = new ArrayList<>();
     */

    @OneToMany(mappedBy = "aroRichAnnulVers")
    private List<AroItemRichAnnulVers> aroItemRichAnnulVers = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_STRUT")
    private OrgStrut orgStrut;

    @OneToMany(mappedBy = "aroRichAnnulVers")
    private List<AroStatoRichAnnulVers> aroStatoRichAnnulVers = new ArrayList<>();

    /*
     * @OneToMany(mappedBy = "aroRichAnnulVers") private List<AroXmlRichAnnulVers>
     * aroXmlRichAnnulVers = new ArrayList<>();
     */

    public AroRichAnnulVers() {/* Hibernate */
    }

    public Long getIdRichAnnulVers() {
        return this.idRichAnnulVers;
    }

    public void setIdRichAnnulVers(Long idRichAnnulVers) {
        this.idRichAnnulVers = idRichAnnulVers;
    }

    public String getCdRichAnnulVers() {
        return this.cdRichAnnulVers;
    }

    public void setCdRichAnnulVers(String cdRichAnnulVers) {
        this.cdRichAnnulVers = cdRichAnnulVers;
    }

    public String getTiAnnullamento() {
        return this.tiAnnullamento;
    }

    public void setTiAnnullamento(String tiAnnullamento) {
        this.tiAnnullamento = tiAnnullamento;
    }

    /*
     * public List<AroFileRichAnnulVers> getAroFileRichAnnulVers() { return
     * this.aroFileRichAnnulVers; }
     */

    public List<AroItemRichAnnulVers> getAroItemRichAnnulVers() {
        return this.aroItemRichAnnulVers;
    }

    public OrgStrut getOrgStrut() {
        return this.orgStrut;
    }

    public List<AroStatoRichAnnulVers> getAroStatoRichAnnulVers() {
        return this.aroStatoRichAnnulVers;
    }

    /*
     * public List<AroXmlRichAnnulVers> getAroXmlRichAnnulVers() { return this.aroXmlRichAnnulVers;
     * }
     */

}
