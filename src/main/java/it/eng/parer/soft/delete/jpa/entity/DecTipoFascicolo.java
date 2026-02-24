/*
 * Engineering Ingegneria Informatica S.p.A.
 *
 * Copyright (C) 2023 Regione Emilia-Romagna
 * <p/>
 * This program is free software: you can redistribute it and/or modify it under the terms of
 * the GNU Affero General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 * <p/>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <https://www.gnu.org/licenses/>.
 */

package it.eng.parer.soft.delete.jpa.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import it.eng.parer.soft.delete.jpa.sequence.NonMonotonicSequence;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/**
 * The persistent class for the DEC_TIPO_FASCICOLO database table.
 *
 */
@Entity
@Table(name = "DEC_TIPO_FASCICOLO")
@NamedQuery(name = "DecTipoFascicolo.findAll", query = "SELECT d FROM DecTipoFascicolo d")
@NamedQuery(name = "DecTipoFascicolo.findById", query = "SELECT d FROM DecTipoFascicolo d WHERE d.idTipoFascicolo = :idTipoFascicolo")
public class DecTipoFascicolo implements Serializable {

    private static final long serialVersionUID = 1L;
    private Long idTipoFascicolo;
    private String dsTipoFascicolo;
    private LocalDateTime dtIstituz;
    private LocalDateTime dtSoppres;
    private String nmTipoFascicolo;
    private List<DecAaTipoFascicolo> decAaTipoFascicolos = new ArrayList<>();
    private OrgStrut orgStrut;

    public DecTipoFascicolo() {
        // hibernate constructor
    }

    @Id
    @NonMonotonicSequence(sequenceName = "SDEC_TIPO_FASCICOLO", incrementBy = 1)
    @Column(name = "ID_TIPO_FASCICOLO")
    public Long getIdTipoFascicolo() {
        return this.idTipoFascicolo;
    }

    public void setIdTipoFascicolo(Long idTipoFascicolo) {
        this.idTipoFascicolo = idTipoFascicolo;
    }

    @Column(name = "DS_TIPO_FASCICOLO")
    public String getDsTipoFascicolo() {
        return this.dsTipoFascicolo;
    }

    public void setDsTipoFascicolo(String dsTipoFascicolo) {
        this.dsTipoFascicolo = dsTipoFascicolo;
    }

    @Column(name = "DT_ISTITUZ")
    public LocalDateTime getDtIstituz() {
        return this.dtIstituz;
    }

    public void setDtIstituz(LocalDateTime dtIstituz) {
        this.dtIstituz = dtIstituz;
    }

    @Column(name = "DT_SOPPRES")
    public LocalDateTime getDtSoppres() {
        return this.dtSoppres;
    }

    public void setDtSoppres(LocalDateTime dtSoppres) {
        this.dtSoppres = dtSoppres;
    }

    @Column(name = "NM_TIPO_FASCICOLO")
    public String getNmTipoFascicolo() {
        return this.nmTipoFascicolo;
    }

    public void setNmTipoFascicolo(String nmTipoFascicolo) {
        this.nmTipoFascicolo = nmTipoFascicolo;
    }

    // bi-directional many-to-one association to OrgStrut
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_STRUT")

    public OrgStrut getOrgStrut() {
        return this.orgStrut;
    }

    public void setOrgStrut(OrgStrut orgStrut) {
        this.orgStrut = orgStrut;
    }

    // bi-directional many-to-one association to DecAaTipoFascicolo
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "decTipoFascicolo", fetch = FetchType.LAZY)
    public List<DecAaTipoFascicolo> getDecAaTipoFascicolos() {
        return this.decAaTipoFascicolos;
    }

    public void setDecAaTipoFascicolos(List<DecAaTipoFascicolo> decAaTipoFascicolos) {
        this.decAaTipoFascicolos = decAaTipoFascicolos;
    }
}
