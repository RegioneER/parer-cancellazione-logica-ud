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
 * The persistent class for the ARO_FIRMA_COMP database table.
 */
@Entity
@Table(name = "ARO_FIRMA_COMP")
public class AroFirmaComp extends SoftDelete implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "ID_FIRMA_COMP")
    private Long idFirmaComp;

    @OneToMany(mappedBy = "aroFirmaPadre")
    private List<AroControfirmaFirma> aroControfirmaFirmaFiglios = new ArrayList<>();

    @OneToMany(mappedBy = "aroFirmaFiglio")
    private List<AroControfirmaFirma> aroControfirmaFirmaPadres = new ArrayList<>();

    @OneToMany(mappedBy = "aroFirmaComp")
    private List<AroContrFirmaComp> aroContrFirmaComps = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_BUSTA_CRITTOG")
    private AroBustaCrittog aroBustaCrittog;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_COMP_DOC", nullable = false)
    private AroCompDoc aroCompDoc;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_MARCA_COMP")
    private AroMarcaComp aroMarcaComp;

    @OneToMany(mappedBy = "aroFirmaComp")
    private List<VolAppartFirmaVolume> volAppartFirmaVolumes = new ArrayList<>();

    @OneToMany(mappedBy = "aroFirmaComp")
    private List<AroVerifFirmaDtVer> aroVerifFirmaDtVers = new ArrayList<>();

    public AroFirmaComp() {/* Hibernate */
    }

    public Long getIdFirmaComp() {
        return this.idFirmaComp;
    }

    public List<AroControfirmaFirma> getAroControfirmaFirmaFiglios() {
        return this.aroControfirmaFirmaFiglios;
    }

    public List<AroControfirmaFirma> getAroControfirmaFirmaPadres() {
        return this.aroControfirmaFirmaPadres;
    }

    public List<AroContrFirmaComp> getAroContrFirmaComps() {
        return this.aroContrFirmaComps;
    }

    public AroBustaCrittog getAroBustaCrittog() {
        return this.aroBustaCrittog;
    }

    public AroCompDoc getAroCompDoc() {
        return this.aroCompDoc;
    }

    public AroMarcaComp getAroMarcaComp() {
        return this.aroMarcaComp;
    }

    public List<VolAppartFirmaVolume> getVolAppartFirmaVolumes() {
        return this.volAppartFirmaVolumes;
    }

    public List<AroVerifFirmaDtVer> getAroVerifFirmaDtVers() {
        return this.aroVerifFirmaDtVers;
    }

}
