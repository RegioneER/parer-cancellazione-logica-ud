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

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/**
 * The persistent class for the DM_UD_DEL_DECOD_STATO_INTERNO database table.
 */
@Entity
@Table(name = "DM_UD_DEL_DECOD_STATO_INTERNO")
public class DmUdDelDecodStatoInterno implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long idDecodStatoInterno;

    private String tiStatoInternoRich;

    private String dsStatoInternoRich;

    private List<DmUdDelStatoRichiesta> dmUdDelStatoRichiestas = new ArrayList<>();

    public DmUdDelDecodStatoInterno() {/* Hibernate */
    }

    @Id
    @Column(name = "ID_DECOD_STATO_INTERNO")
    public Long getIdDecodStatoInterno() {
        return idDecodStatoInterno;
    }

    @Column(name = "TI_STATO_INTERNO_RICH")
    public String getTiStatoInternoRich() {
        return tiStatoInternoRich;
    }

    @Column(name = "DS_STATO_INTERNO_RICH")
    public String getDsStatoInternoRich() {
        return dsStatoInternoRich;
    }

    // bi-directional many-to-one association to DmUdDelStatoRichiesta
    @OneToMany(mappedBy = "dmUdDelDecodStatoInterno")
    public List<DmUdDelStatoRichiesta> getDmUdDelStatoRichiestas() {
        return this.dmUdDelStatoRichiestas;
    }

    public void setDmUdDelStatoRichiestas(List<DmUdDelStatoRichiesta> dmUdDelStatoRichiestas) {
        this.dmUdDelStatoRichiestas = dmUdDelStatoRichiestas;
    }

}