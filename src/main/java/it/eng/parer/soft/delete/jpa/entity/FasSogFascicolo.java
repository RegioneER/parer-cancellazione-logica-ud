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
 * The persistent class for the FAS_SOG_FASCICOLO database table.
 */
@Entity
@Table(name = "FAS_SOG_FASCICOLO")
public class FasSogFascicolo extends SoftDelete implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "ID_SOG_FASCICOLO")
    private Long idSogFascicolo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_FASCICOLO")
    private FasFascicolo fasFascicolo;

    @OneToMany(mappedBy = "fasSogFascicolo")
    private List<FasCodIdeSog> fasCodIdeSogs = new ArrayList<>();

    @OneToMany(mappedBy = "fasSogFascicolo")
    private List<FasEventoSog> fasEventoSogs = new ArrayList<>();

    @OneToMany(mappedBy = "fasSogFascicolo")
    private List<FasIndRifSog> fasIndRifSogs = new ArrayList<>();

    public FasSogFascicolo() {/* Hibernate */
    }

    public Long getIdSogFascicolo() {
        return this.idSogFascicolo;
    }

    public FasFascicolo getFasFascicolo() {
        return this.fasFascicolo;
    }

    public List<FasCodIdeSog> getFasCodIdeSogs() {
        return this.fasCodIdeSogs;
    }

    public List<FasEventoSog> getFasEventoSogs() {
        return this.fasEventoSogs;
    }

    public List<FasIndRifSog> getFasIndRifSogs() {
        return this.fasIndRifSogs;
    }

}
