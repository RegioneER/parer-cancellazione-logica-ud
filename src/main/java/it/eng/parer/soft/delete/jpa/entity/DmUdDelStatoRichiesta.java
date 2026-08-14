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
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * The persistent class for the DM_UD_DEL_STATO_RICHIESTA database table.
 */
@Entity
@Table(name = "DM_UD_DEL_STATO_RICHIESTA")
public class DmUdDelStatoRichiesta implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idStatoUdDelRichiesta;

    private DmUdDelRichieste dmUdDelRichieste;

    private LocalDateTime dtRegStato;

    private Long pgStatoRich;

    private DmUdDelDecodStatoInterno dmUdDelDecodStatoInterno;

    public DmUdDelStatoRichiesta() {/* Hibernate */
    }

    @Id
    @Column(name = "ID_STATO_UD_DEL_RICHIESTA")
    public Long getIdStatoUdDelRichiesta() {
        return idStatoUdDelRichiesta;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_UD_DEL_RICHIESTA", nullable = false)
    public DmUdDelRichieste getDmUdDelRichieste() {
        return dmUdDelRichieste;
    }

    @Column(name = "DT_REG_STATO", nullable = false)
    public LocalDateTime getDtRegStato() {
        return dtRegStato;
    }

    @Column(name = "PG_STATO_RICH", nullable = false)
    public Long getPgStatoRich() {
        return pgStatoRich;
    }

    // bi-directional many-to-one association to DmUdDelDecodStatoInterno
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_DECOD_STATO_INTERNO")
    public DmUdDelDecodStatoInterno getDmUdDelDecodStatoInterno() {
        return this.dmUdDelDecodStatoInterno;
    }

}