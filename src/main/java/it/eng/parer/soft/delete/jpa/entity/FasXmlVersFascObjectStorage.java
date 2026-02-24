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

import jakarta.persistence.*;
import java.io.Serializable;

import it.eng.parer.soft.delete.beans.annotations.ForceCompleteMode;
import it.eng.parer.soft.delete.jpa.inheritance.SoftDelete;

@Entity
@Table(name = "FAS_XML_VERS_FASC_OBJECT_STORAGE")
@ForceCompleteMode
public class FasXmlVersFascObjectStorage extends SoftDelete implements Serializable {

    private static final long serialVersionUID = 1L;

    public FasXmlVersFascObjectStorage() {
        super();
    }

    @Id
    @Column(name = "ID_FASCICOLO")
    private Long idFascicolo;

    @MapsId
    @OneToOne(mappedBy = "fasXmlVersFascObjectStorage")
    @JoinColumn(name = "ID_FASCICOLO")
    private FasFascicolo fasFascicolo;

    public Long getIdFascicolo() {
        return idFascicolo;
    }

    public FasFascicolo getFasFascicolo() {
        return fasFascicolo;
    }

}
