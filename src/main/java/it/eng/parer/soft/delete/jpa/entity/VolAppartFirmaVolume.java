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
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/**
 * The persistent class for the VOL_APPART_FIRMA_VOLUME database table.
 */
@Entity
@Table(name = "VOL_APPART_FIRMA_VOLUME")
public class VolAppartFirmaVolume extends SoftDelete implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "ID_APPART_FIRMA_VOLUME")
    private Long idAppartFirmaVolume;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {
            CascadeType.DETACH })
    @JoinColumn(name = "ID_FIRMA_COMP")
    private AroFirmaComp aroFirmaComp;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {
            CascadeType.DETACH })
    @JoinColumn(name = "ID_APPART_COMP_VOLUME")
    private VolAppartCompVolume volAppartCompVolume;

    @OneToMany(mappedBy = "volAppartFirmaVolume")
    private List<VolVerifFirmaVolume> volVerifFirmaVolumes = new ArrayList<>();

    public VolAppartFirmaVolume() {/* Hibernate */
    }

    public Long getIdAppartFirmaVolume() {
        return this.idAppartFirmaVolume;
    }

    public AroFirmaComp getAroFirmaComp() {
        return this.aroFirmaComp;
    }

    public VolAppartCompVolume getVolAppartCompVolume() {
        return this.volAppartCompVolume;
    }

    public List<VolVerifFirmaVolume> getVolVerifFirmaVolumes() {
        return this.volVerifFirmaVolumes;
    }

}
