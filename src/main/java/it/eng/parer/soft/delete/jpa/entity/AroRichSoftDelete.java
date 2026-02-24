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
import java.math.BigDecimal;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/**
 * The persistent class for the ARO_RICH_SOFT_DELETE database table.
 */
@Entity
@Table(name = "ARO_RICH_SOFT_DELETE")

public class AroRichSoftDelete implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idRichSoftDelete;

    private String dsRichSoftDelete;

    private LocalDateTime dtCreazioneRichSoftDelete;

    private BigDecimal idStatoRichSoftDeleteCor;

    private String ntRichSoftDelete;

    private String tiCreazioneRichSoftDelete;

    private String tiRichSoftDelete;

    private List<AroItemRichSoftDelete> aroItemRichSoftDelete = new ArrayList<>();

    private OrgStrut orgStrut;

    private List<AroStatoRichSoftDelete> aroStatoRichSoftDelete = new ArrayList<>();

    private List<AroXmlRichSoftDelete> aroXmlRichSoftDelete = new ArrayList<>();

    private String tiModCancellazione;

    public AroRichSoftDelete() {/* Hibernate */
    }

    @Id
    @NonMonotonicSequence(sequenceName = "SARO_RICH_SOFT_DELETE", incrementBy = 1)
    @Column(name = "ID_RICH_SOFT_DELETE")
    public Long getIdRichSoftDelete() {
        return this.idRichSoftDelete;
    }

    public void setIdRichSoftDelete(Long idRichSoftDelete) {
        this.idRichSoftDelete = idRichSoftDelete;
    }

    @Column(name = "DS_RICH_SOFT_DELETE")
    public String getDsRichSoftDelete() {
        return this.dsRichSoftDelete;
    }

    public void setDsRichSoftDelete(String dsRichSoftDelete) {
        this.dsRichSoftDelete = dsRichSoftDelete;
    }

    @Column(name = "DT_CREAZIONE_RICH_SOFT_DELETE")
    public LocalDateTime getDtCreazioneRichSoftDelete() {
        return this.dtCreazioneRichSoftDelete;
    }

    public void setDtCreazioneRichSoftDelete(LocalDateTime dtCreazioneRichSoftDelete) {
        this.dtCreazioneRichSoftDelete = dtCreazioneRichSoftDelete;
    }

    @Column(name = "ID_STATO_RICH_SOFT_DELETE_COR")
    public BigDecimal getIdStatoRichSoftDeleteCor() {
        return this.idStatoRichSoftDeleteCor;
    }

    public void setIdStatoRichSoftDeleteCor(BigDecimal idStatoRichSoftDeleteCor) {
        this.idStatoRichSoftDeleteCor = idStatoRichSoftDeleteCor;
    }

    @Column(name = "NT_RICH_SOFT_DELETE")
    public String getNtRichSoftDelete() {
        return this.ntRichSoftDelete;
    }

    public void setNtRichSoftDelete(String ntRichSoftDelete) {
        this.ntRichSoftDelete = ntRichSoftDelete;
    }

    @Column(name = "TI_CREAZIONE_RICH_SOFT_DELETE")
    public String getTiCreazioneRichSoftDelete() {
        return this.tiCreazioneRichSoftDelete;
    }

    public void setTiCreazioneRichSoftDelete(String tiCreazioneRichSoftDelete) {
        this.tiCreazioneRichSoftDelete = tiCreazioneRichSoftDelete;
    }

    @Column(name = "TI_RICH_SOFT_DELETE")
    public String getTiRichSoftDelete() {
        return this.tiRichSoftDelete;
    }

    public void setTiRichSoftDelete(String tiRichSoftDelete) {
        this.tiRichSoftDelete = tiRichSoftDelete;
    }

    @OneToMany(mappedBy = "aroRichSoftDelete", cascade = CascadeType.PERSIST)
    public List<AroItemRichSoftDelete> getAroItemRichSoftDelete() {
        return this.aroItemRichSoftDelete;
    }

    public void setAroItemRichSoftDelete(List<AroItemRichSoftDelete> aroItemRichSoftDelete) {
        this.aroItemRichSoftDelete = aroItemRichSoftDelete;
    }

    public AroItemRichSoftDelete addAroItemRichSoftDelete(
            AroItemRichSoftDelete aroItemRichSoftDelete) {
        getAroItemRichSoftDelete().add(aroItemRichSoftDelete);
        aroItemRichSoftDelete.setAroRichSoftDelete(this);
        return aroItemRichSoftDelete;
    }

    public AroItemRichSoftDelete removeAroItemRichSoftDelete(
            AroItemRichSoftDelete aroItemRichSoftDelete) {
        getAroItemRichSoftDelete().remove(aroItemRichSoftDelete);
        aroItemRichSoftDelete.setAroRichSoftDelete(null);
        return aroItemRichSoftDelete;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_STRUT")
    public OrgStrut getOrgStrut() {
        return this.orgStrut;
    }

    public void setOrgStrut(OrgStrut orgStrut) {
        this.orgStrut = orgStrut;
    }

    @OneToMany(mappedBy = "aroRichSoftDelete", cascade = CascadeType.PERSIST)
    public List<AroStatoRichSoftDelete> getAroStatoRichSoftDelete() {
        return this.aroStatoRichSoftDelete;
    }

    public void setAroStatoRichSoftDelete(List<AroStatoRichSoftDelete> aroStatoRichSoftDelete) {
        this.aroStatoRichSoftDelete = aroStatoRichSoftDelete;
    }

    public AroStatoRichSoftDelete addAroStatoRichSoftDelete(
            AroStatoRichSoftDelete aroStatoRichSoftDelete) {
        getAroStatoRichSoftDelete().add(aroStatoRichSoftDelete);
        aroStatoRichSoftDelete.setAroRichSoftDelete(this);
        return aroStatoRichSoftDelete;
    }

    public AroStatoRichSoftDelete removeAroStatoRichSoftDelete(
            AroStatoRichSoftDelete aroStatoRichSoftDelete) {
        getAroStatoRichSoftDelete().remove(aroStatoRichSoftDelete);
        aroStatoRichSoftDelete.setAroRichSoftDelete(null);
        return aroStatoRichSoftDelete;
    }

    @OneToMany(mappedBy = "aroRichSoftDelete", cascade = CascadeType.PERSIST)
    public List<AroXmlRichSoftDelete> getAroXmlRichSoftDelete() {
        return this.aroXmlRichSoftDelete;
    }

    public void setAroXmlRichSoftDelete(List<AroXmlRichSoftDelete> aroXmlRichSoftDelete) {
        this.aroXmlRichSoftDelete = aroXmlRichSoftDelete;
    }

    public AroXmlRichSoftDelete addAroXmlRichSoftDelete(AroXmlRichSoftDelete aroXmlRichSoftDelete) {
        getAroXmlRichSoftDelete().add(aroXmlRichSoftDelete);
        aroXmlRichSoftDelete.setAroRichSoftDelete(this);
        return aroXmlRichSoftDelete;
    }

    public AroXmlRichSoftDelete removeAroXmlRichSoftDelete(
            AroXmlRichSoftDelete aroXmlRichSoftDelete) {
        getAroXmlRichSoftDelete().remove(aroXmlRichSoftDelete);
        aroXmlRichSoftDelete.setAroRichSoftDelete(null);
        return aroXmlRichSoftDelete;
    }

    @Column(name = "TI_MOD_CANCELLAZIONE")
    public String getTiModCancellazione() {
        return this.tiModCancellazione;
    }

    public void setTiModCancellazione(String tiModCancellazione) {
        this.tiModCancellazione = tiModCancellazione;
    }

}
