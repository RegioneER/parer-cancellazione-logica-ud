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
 * The persistent class for the FAS_AIP_FASCICOLO_DA_ELAB database table.
 */
@Entity
@Table(name = "FAS_AIP_FASCICOLO_DA_ELAB")
public class FasAipFascicoloDaElab extends SoftDelete implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "ID_AIP_FASCICOLO_DA_ELAB")
    private Long idAipFascicoloDaElab;

    @OneToMany(mappedBy = "fasAipFascicoloDaElab")
    private List<FasUdAipFascicoloDaElab> fasUdAipFascicoloDaElabs = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_FASCICOLO")
    private FasFascicolo fasFascicolo;

    /*
     * @ManyToOne(fetch = FetchType.LAZY)
     *
     * @JoinColumn(name = "ID_ELENCO_VERS_FASC") private ElvElencoVersFasc elvElencoVersFasc;
     */

    public FasAipFascicoloDaElab() {/* Hibernate */
    }

    public Long getIdAipFascicoloDaElab() {
        return this.idAipFascicoloDaElab;
    }

    public List<FasUdAipFascicoloDaElab> getFasUdAipFascicoloDaElabs() {
        return this.fasUdAipFascicoloDaElabs;
    }

    public FasFascicolo getFasFascicolo() {
        return this.fasFascicolo;
    }

    /*
     * public ElvElencoVersFasc getElvElencoVersFasc() { return this.elvElencoVersFasc; }
     */

}
