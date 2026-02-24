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
 * The persistent class for the ARO_MARCA_COMP database table.
 */
@Entity
@Table(name = "ARO_MARCA_COMP")
public class AroMarcaComp extends SoftDelete implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "ID_MARCA_COMP")
    private Long idMarcaComp;

    @OneToMany(mappedBy = "aroMarcaComp")
    private List<AroContrMarcaComp> aroContrMarcaComps = new ArrayList<>();

    @OneToMany(mappedBy = "aroMarcaComp")
    private List<AroFirmaComp> aroFirmaComps = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_BUSTA_CRITTOG", nullable = false)
    private AroBustaCrittog aroBustaCrittog;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_COMP_DOC")
    private AroCompDoc aroCompDoc;

    public AroMarcaComp() {/* Hibernate */
    }

    public Long getIdMarcaComp() {
        return this.idMarcaComp;
    }

    public List<AroContrMarcaComp> getAroContrMarcaComps() {
        return this.aroContrMarcaComps;
    }

    public List<AroFirmaComp> getAroFirmaComps() {
        return this.aroFirmaComps;
    }

    public AroBustaCrittog getAroBustaCrittog() {
        return this.aroBustaCrittog;
    }

    public AroCompDoc getAroCompDoc() {
        return this.aroCompDoc;
    }

}
