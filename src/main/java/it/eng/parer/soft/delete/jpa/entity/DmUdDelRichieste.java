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
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/**
 * The persistent class for the DM_UD_DEL database table.
 */
@Entity
@Table(name = "DM_UD_DEL_RICHIESTE")
public class DmUdDelRichieste implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idUdDelRichiesta;

    private Long idRichiesta;

    private String cdRichiesta;

    private String tiMotCancellazione;

    private String tiStatoRichiesta;

    private LocalDateTime dtCreazione;

    private String tiStatoInternoRich;

    private LocalDateTime dtUltimoAggiornamento;

    private String tiModDel;

    private String dsMessaggioErrore;

    private List<DmUdDel> dmUdDel;

    public DmUdDelRichieste() {/* Hibernate */
    }

    @Id
    @Column(name = "ID_UD_DEL_RICHIESTA")
    public Long getIdUdDelRichiesta() {
        return idUdDelRichiesta;
    }

    @Column(name = "ID_RICHIESTA")
    public Long getIdRichiesta() {
        return idRichiesta;
    }

    @Column(name = "CD_RICHIESTA")
    public String getCdRichiesta() {
        return cdRichiesta;
    }

    @Column(name = "TI_MOT_CANCELLAZIONE")
    public String getTiMotCancellazione() {
        return this.tiMotCancellazione;
    }

    @Column(name = "TI_STATO_RICHIESTA")
    public String getTiStatoRichiesta() {
        return this.tiStatoRichiesta;
    }

    @Column(name = "TI_MOD_DEL")
    public String getTiModDel() {
        return this.tiModDel;
    }

    @Column(name = "DT_CREAZIONE")
    public LocalDateTime getDtCreazione() {
        return this.dtCreazione;
    }

    @Column(name = "TI_STATO_INTERNO_RICH")
    public String getTiStatoInternoRich() {
        return tiStatoInternoRich;
    }

    @Column(name = "DT_ULTIMO_AGGIORNAMENTO")
    public LocalDateTime getDtUltimoAggiornamento() {
        return this.dtUltimoAggiornamento;
    }

    @Column(name = "DS_MESSAGGIO_ERRORE")
    public String getDsMessaggioErrore() {
        return this.dsMessaggioErrore;
    }

    @OneToMany(mappedBy = "dmUdDelRichieste", fetch = FetchType.LAZY)
    public List<DmUdDel> getDmUdDel() {
        return dmUdDel;
    }

}
