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
 * The persistent class for the ARO_ITEM_RICH_SOFT_DELETE database table.
 */
@Entity
@Table(name = "ARO_ITEM_RICH_SOFT_DELETE")

public class AroItemRichSoftDelete implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idItemRichSoftDelete;

    private BigDecimal aaKeyUnitaDoc;

    private String cdKeyUnitaDoc;

    private String cdRegistroKeyUnitaDoc;

    private BigDecimal idRichiestaSacer;

    private BigDecimal idStrut;

    private BigDecimal pgItemRichSoftDelete;

    private String tiItemRichSoftDelete;

    private String tiStatoItem;

    private LocalDateTime dtClaim;

    private LocalDateTime dtFineElab;

    private String cdInstanceId;

    private String cdErrMsg;

    private List<AroErrRichSoftDelete> aroErrRichSoftDelete = new ArrayList<>();

    private AroRichSoftDelete aroRichSoftDelete;

    private AroUnitaDoc aroUnitaDoc;

    private AroRichAnnulVers aroRichAnnulVers;

    private AroRichiestaRa aroRichiestaRa;

    private AroItemRichSoftDelete aroItemRichPadre;

    public AroItemRichSoftDelete() {/* Hibernate */
    }

    @Id
    @NonMonotonicSequence(sequenceName = "SARO_ITEM_RICH_SOFT_DELETE", incrementBy = 1)
    @Column(name = "ID_ITEM_RICH_SOFT_DELETE")
    public Long getIdItemRichSoftDelete() {
        return this.idItemRichSoftDelete;
    }

    public void setIdItemRichSoftDelete(Long idItemRichSoftDelete) {
        this.idItemRichSoftDelete = idItemRichSoftDelete;
    }

    @Column(name = "AA_KEY_UNITA_DOC")
    public BigDecimal getAaKeyUnitaDoc() {
        return this.aaKeyUnitaDoc;
    }

    public void setAaKeyUnitaDoc(BigDecimal aaKeyUnitaDoc) {
        this.aaKeyUnitaDoc = aaKeyUnitaDoc;
    }

    @Column(name = "CD_KEY_UNITA_DOC")
    public String getCdKeyUnitaDoc() {
        return this.cdKeyUnitaDoc;
    }

    public void setCdKeyUnitaDoc(String cdKeyUnitaDoc) {
        this.cdKeyUnitaDoc = cdKeyUnitaDoc;
    }

    @Column(name = "CD_REGISTRO_KEY_UNITA_DOC")
    public String getCdRegistroKeyUnitaDoc() {
        return this.cdRegistroKeyUnitaDoc;
    }

    public void setCdRegistroKeyUnitaDoc(String cdRegistroKeyUnitaDoc) {
        this.cdRegistroKeyUnitaDoc = cdRegistroKeyUnitaDoc;
    }

    @Column(name = "ID_RICHIESTA_SACER")
    public BigDecimal getIdRichiestaSacer() {
        return this.idRichiestaSacer;
    }

    public void setIdRichiestaSacer(BigDecimal idRichiestaSacer) {
        this.idRichiestaSacer = idRichiestaSacer;
    }

    @Column(name = "ID_STRUT")
    public BigDecimal getIdStrut() {
        return this.idStrut;
    }

    public void setIdStrut(BigDecimal idStrut) {
        this.idStrut = idStrut;
    }

    @Column(name = "PG_ITEM_RICH_SOFT_DELETE")
    public BigDecimal getPgItemRichSoftDelete() {
        return this.pgItemRichSoftDelete;
    }

    public void setPgItemRichSoftDelete(BigDecimal pgItemRichSoftDelete) {
        this.pgItemRichSoftDelete = pgItemRichSoftDelete;
    }

    @Column(name = "TI_ITEM_RICH_SOFT_DELETE")
    public String getTiItemRichSoftDelete() {
        return this.tiItemRichSoftDelete;
    }

    public void setTiItemRichSoftDelete(String tiItemRichSoftDelete) {
        this.tiItemRichSoftDelete = tiItemRichSoftDelete;
    }

    @Column(name = "TI_STATO_ITEM")
    public String getTiStatoItem() {
        return this.tiStatoItem;
    }

    public void setTiStatoItem(String tiStatoItem) {
        this.tiStatoItem = tiStatoItem;
    }

    @Column(name = "DT_CLAIM")
    public LocalDateTime getDtClaim() {
        return dtClaim;
    }

    public void setDtClaim(LocalDateTime dtClaim) {
        this.dtClaim = dtClaim;
    }

    @Column(name = "DT_FINE_ELAB")
    public LocalDateTime getDtFineElab() {
        return dtFineElab;
    }

    public void setDtFineElab(LocalDateTime dtFineElab) {
        this.dtFineElab = dtFineElab;
    }

    @Column(name = "CD_INSTANCE_ID", length = 50)
    public String getCdInstanceId() {
        return cdInstanceId;
    }

    public void setCdInstanceId(String cdInstanceId) {
        this.cdInstanceId = cdInstanceId;
    }

    @Column(name = "CD_ERR_MSG", length = 4000)
    public String getCdErrMsg() {
        return cdErrMsg;
    }

    public void setCdErrMsg(String cdErrMsg) {
        this.cdErrMsg = cdErrMsg;
    }

    @OneToMany(mappedBy = "aroItemRichSoftDelete", cascade = CascadeType.PERSIST)
    public List<AroErrRichSoftDelete> getAroErrRichSoftDelete() {
        return this.aroErrRichSoftDelete;
    }

    public void setAroErrRichSoftDelete(List<AroErrRichSoftDelete> aroErrRichSoftDelete) {
        this.aroErrRichSoftDelete = aroErrRichSoftDelete;
    }

    public AroErrRichSoftDelete addAroErrRichSoftDelete(AroErrRichSoftDelete aroErrRichSoftDelete) {
        getAroErrRichSoftDelete().add(aroErrRichSoftDelete);
        aroErrRichSoftDelete.setAroItemRichSoftDelete(this);
        return aroErrRichSoftDelete;
    }

    public AroErrRichSoftDelete removeAroErrRichSoftDelete(
            AroErrRichSoftDelete aroErrRichSoftDelete) {
        getAroErrRichSoftDelete().remove(aroErrRichSoftDelete);
        aroErrRichSoftDelete.setAroItemRichSoftDelete(null);
        return aroErrRichSoftDelete;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_RICH_SOFT_DELETE")
    public AroRichSoftDelete getAroRichSoftDelete() {
        return this.aroRichSoftDelete;
    }

    public void setAroRichSoftDelete(AroRichSoftDelete aroRichSoftDelete) {
        this.aroRichSoftDelete = aroRichSoftDelete;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_UNITA_DOC")
    public AroUnitaDoc getAroUnitaDoc() {
        return this.aroUnitaDoc;
    }

    public void setAroUnitaDoc(AroUnitaDoc aroUnitaDoc) {
        this.aroUnitaDoc = aroUnitaDoc;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_RICH_ANNUL_VERS")
    public AroRichAnnulVers getAroRichAnnulVers() {
        return this.aroRichAnnulVers;
    }

    public void setAroRichAnnulVers(AroRichAnnulVers aroRichAnnulVers) {
        this.aroRichAnnulVers = aroRichAnnulVers;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_RICHIESTA_RA")
    public AroRichiestaRa getAroRichiestaRa() {
        return this.aroRichiestaRa;
    }

    public void setAroRichiestaRa(AroRichiestaRa aroRichiestaRa) {
        this.aroRichiestaRa = aroRichiestaRa;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_ITEM_RICH_PADRE")
    public AroItemRichSoftDelete getAroItemRichPadre() {
        return this.aroItemRichPadre;
    }

    public void setAroItemRichPadre(AroItemRichSoftDelete aroItemRichPadre) {
        this.aroItemRichPadre = aroItemRichPadre;
    }

}
