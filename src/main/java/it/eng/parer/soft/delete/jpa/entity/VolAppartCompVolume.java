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
 * The persistent class for the VOL_APPART_COMP_VOLUME database table.
 */
@Entity
@Table(name = "VOL_APPART_COMP_VOLUME")
public class VolAppartCompVolume extends SoftDelete implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "ID_APPART_COMP_VOLUME")
    private Long idAppartCompVolume;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_COMP_DOC")
    private AroCompDoc aroCompDoc;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_APPART_DOC_VOLUME")
    private VolAppartDocVolume volAppartDocVolume;

    @OneToMany(mappedBy = "volAppartCompVolume")
    private List<VolAppartFirmaVolume> volAppartFirmaVolumes = new ArrayList<>();

    public VolAppartCompVolume() {/* Hibernate */
    }

    public Long getIdAppartCompVolume() {
        return this.idAppartCompVolume;
    }

    public AroCompDoc getAroCompDoc() {
        return this.aroCompDoc;
    }

    public VolAppartDocVolume getVolAppartDocVolume() {
        return this.volAppartDocVolume;
    }

    public List<VolAppartFirmaVolume> getVolAppartFirmaVolumes() {
        return this.volAppartFirmaVolumes;
    }

}
