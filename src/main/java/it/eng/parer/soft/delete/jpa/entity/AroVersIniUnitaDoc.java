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
 * The persistent class for the ARO_VERS_INI_UNITA_DOC database table.
 */
@Entity
@Table(name = "ARO_VERS_INI_UNITA_DOC")

public class AroVersIniUnitaDoc extends SoftDelete implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "ID_VERS_INI_UNITA_DOC")
    private Long idVersIniUnitaDoc;

    // @OneToOne(fetch = FetchType.LAZY)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_UNITA_DOC")
    private AroUnitaDoc aroUnitaDoc;

    @OneToMany(mappedBy = "aroVersIniUnitaDoc")
    private List<AroVersIniArchivSec> aroVersIniArchivSecs = new ArrayList<>();

    @OneToMany(mappedBy = "aroVersIniUnitaDoc")
    private List<AroVersIniLinkUnitaDoc> aroVersIniLinkUnitaDocs = new ArrayList<>();

    @OneToMany(mappedBy = "aroVersIniUnitaDoc")
    private List<AroVersIniDoc> aroVersIniDocs = new ArrayList<>();

    /*
     * @OneToMany(mappedBy = "aroVersIniUnitaDoc") private List<AroVersIniDatiSpec>
     * aroVersIniDatiSpecs = new ArrayList<>();
     */

    public AroVersIniUnitaDoc() {/* Hibernate */
    }

    public Long getIdVersIniUnitaDoc() {
        return this.idVersIniUnitaDoc;
    }

    public AroUnitaDoc getAroUnitaDoc() {
        return this.aroUnitaDoc;
    }

    public List<AroVersIniArchivSec> getAroVersIniArchivSecs() {
        return this.aroVersIniArchivSecs;
    }

    public List<AroVersIniLinkUnitaDoc> getAroVersIniLinkUnitaDocs() {
        return this.aroVersIniLinkUnitaDocs;
    }

    public List<AroVersIniDoc> getAroVersIniDocs() {
        return this.aroVersIniDocs;
    }

    /*
     * public List<AroVersIniDatiSpec> getAroVersIniDatiSpecs() { return this.aroVersIniDatiSpecs; }
     */
}
