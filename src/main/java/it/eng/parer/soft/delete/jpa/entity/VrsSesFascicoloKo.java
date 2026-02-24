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
import jakarta.persistence.*;

/**
 * The persistent class for the VRS_SES_FASCICOLO_KO database table.
 */
@Entity
@Table(name = "VRS_SES_FASCICOLO_KO")
public class VrsSesFascicoloKo extends SoftDelete implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "ID_SES_FASCICOLO_KO")
    private Long idSesFascicoloKo;

    @OneToMany(mappedBy = "vrsSesFascicoloKo")
    private List<VrsErrSesFascicoloKo> vrsErrSesFascicoloKos = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_FASCICOLO")
    private FasFascicolo fasFascicolo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_FASCICOLO_KO")
    private VrsFascicoloKo vrsFascicoloKo;

    @OneToMany(mappedBy = "vrsSesFascicoloKo")
    private List<VrsXmlSesFascicoloKo> vrsXmlSesFascicoloKos = new ArrayList<>();

    @OneToMany(mappedBy = "vrsSesFascicoloKo")
    private List<VrsXmlSesFascKoObjectStorage> vrsXmlSesFascKoObjectStorages = new ArrayList<>();

    public VrsSesFascicoloKo() {/* Hibernate */
    }

    public Long getIdSesFascicoloKo() {
        return this.idSesFascicoloKo;
    }

    public List<VrsErrSesFascicoloKo> getVrsErrSesFascicoloKos() {
        return this.vrsErrSesFascicoloKos;
    }

    public FasFascicolo getFasFascicolo() {
        return this.fasFascicolo;
    }

    public VrsFascicoloKo getVrsFascicoloKo() {
        return this.vrsFascicoloKo;
    }

    public List<VrsXmlSesFascicoloKo> getVrsXmlSesFascicoloKos() {
        return this.vrsXmlSesFascicoloKos;
    }

    public List<VrsXmlSesFascKoObjectStorage> getVrsXmlSesFascKoObjectStorages() {
        return vrsXmlSesFascKoObjectStorages;
    }

}
