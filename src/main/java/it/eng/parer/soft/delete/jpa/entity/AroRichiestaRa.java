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

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import jakarta.persistence.Table;

import it.eng.parer.soft.delete.jpa.entity.constraint.AroRichiestaRa.AroRichiestaTiStato;
import it.eng.parer.soft.delete.jpa.sequence.NonMonotonicSequence;

/**
 * The persistent class for the ARO_RICHIESTA_RA database table.
 */
@Entity
@Table(name = "ARO_RICHIESTA_RA")

public class AroRichiestaRa implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idRichiestaRa;

    private OrgStrut orgStrut;

    private IamUser iamUser;

    private AroRichiestaTiStato tiStato;

    // private List<AroAipRestituzioneArchivio> aroAipRestituzioneArchivios = new ArrayList<>();

    public AroRichiestaRa() {/* Hibernate */
    }

    @Id
    @NonMonotonicSequence(sequenceName = "SARO_RICHIESTA_RA", incrementBy = 1)
    @Column(name = "ID_RICHIESTA_RA")
    public Long getIdRichiestaRa() {
        return this.idRichiestaRa;
    }

    public void setIdRichiestaRa(Long idRichiestaRa) {
        this.idRichiestaRa = idRichiestaRa;
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "TI_STATO")
    public AroRichiestaTiStato getTiStato() {
        return this.tiStato;
    }

    public void setTiStato(AroRichiestaTiStato tiStato) {
        this.tiStato = tiStato;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_STRUT")
    public OrgStrut getOrgStrut() {
        return this.orgStrut;
    }

    public void setOrgStrut(OrgStrut orgStrut) {
        this.orgStrut = orgStrut;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_USER_IAM")
    public IamUser getIamUser() {
        return this.iamUser;
    }

    public void setIamUser(IamUser iamUser) {
        this.iamUser = iamUser;
    }
}
