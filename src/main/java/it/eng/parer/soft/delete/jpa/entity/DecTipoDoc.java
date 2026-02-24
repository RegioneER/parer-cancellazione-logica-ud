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

import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * The persistent class for the DEC_TIPO_DOC database table.
 */
@Entity
@Cacheable(true)
@Table(name = "DEC_TIPO_DOC")
public class DecTipoDoc implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idTipoDoc;

    private String dlNoteTipoDoc;

    private String dsPeriodicitaVers;

    private String dsTipoDoc;

    private LocalDateTime dtIstituz;

    private LocalDateTime dtSoppres;

    private String flTipoDocPrincipale;

    private String nmTipoDoc;

    // private List<AroDoc> aroDocs = new ArrayList<>();

    private OrgStrut orgStrut;

    public DecTipoDoc() {/* Hibernate */
    }

    @Id
    @Column(name = "ID_TIPO_DOC")
    public Long getIdTipoDoc() {
        return this.idTipoDoc;
    }

    public void setIdTipoDoc(Long idTipoDoc) {
        this.idTipoDoc = idTipoDoc;
    }

    @Column(name = "DL_NOTE_TIPO_DOC")
    public String getDlNoteTipoDoc() {
        return this.dlNoteTipoDoc;
    }

    public void setDlNoteTipoDoc(String dlNoteTipoDoc) {
        this.dlNoteTipoDoc = dlNoteTipoDoc;
    }

    @Column(name = "DS_PERIODICITA_VERS")
    public String getDsPeriodicitaVers() {
        return this.dsPeriodicitaVers;
    }

    public void setDsPeriodicitaVers(String dsPeriodicitaVers) {
        this.dsPeriodicitaVers = dsPeriodicitaVers;
    }

    @Column(name = "DS_TIPO_DOC")
    public String getDsTipoDoc() {
        return this.dsTipoDoc;
    }

    public void setDsTipoDoc(String dsTipoDoc) {
        this.dsTipoDoc = dsTipoDoc;
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

    @Column(name = "FL_TIPO_DOC_PRINCIPALE", columnDefinition = "char(1)")
    public String getFlTipoDocPrincipale() {
        return this.flTipoDocPrincipale;
    }

    public void setFlTipoDocPrincipale(String flTipoDocPrincipale) {
        this.flTipoDocPrincipale = flTipoDocPrincipale;
    }

    @Column(name = "NM_TIPO_DOC")
    public String getNmTipoDoc() {
        return this.nmTipoDoc;
    }

    public void setNmTipoDoc(String nmTipoDoc) {
        this.nmTipoDoc = nmTipoDoc;
    }

    // @OneToMany(mappedBy = "decTipoDoc")
    // public List<AroDoc> getAroDocs() {
    // return this.aroDocs;
    // }

    // public void setAroDocs(List<AroDoc> aroDocs) {
    // this.aroDocs = aroDocs;
    // }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_STRUT")
    public OrgStrut getOrgStrut() {
        return this.orgStrut;
    }

    public void setOrgStrut(OrgStrut orgStrut) {
        this.orgStrut = orgStrut;
    }

}
