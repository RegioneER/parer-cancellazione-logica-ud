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

package it.eng.parer.soft.delete.jpa.viewEntity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import jakarta.persistence.Table;

/**
 * The persistent class for the ARO_V_LIS_ITEM_RICH_SOFT_DELETE database table.
 *
 */
@Entity
@Table(name = "ARO_V_LIS_ITEM_RICH_SOFT_DELETE")

public class AroVLisItemRichSoftDelete implements Serializable {
    private static final long serialVersionUID = 1L;
    private BigDecimal idRichiestaSacer;
    private BigDecimal aaKeyUnitaDoc;
    private String cdKeyUnitaDoc;
    private String cdRegistroKeyUnitaDoc;
    private String dsKeyItem;
    private String dsListaErr;
    private LocalDateTime dtAnnul;
    private BigDecimal idItemRichSoftDelete;
    private BigDecimal idRichSoftDelete;
    private BigDecimal idUnitaDoc;
    private BigDecimal pgItemRichSoftDelete;
    private String tiItemRichSoftDelete;
    private String tiStatoItem;

    public AroVLisItemRichSoftDelete() {/* Hibernate */
    }

    @Column(name = "ID_RICH_SOFT_DELETE")
    public BigDecimal getIdRichSoftDelete() {
        return this.idRichSoftDelete;
    }

    @Column(name = "ID_RICHIESTA_SACER")
    public BigDecimal getIdRichiestaSacer() {
        return this.idRichiestaSacer;
    }

    @Column(name = "AA_KEY_UNITA_DOC")
    public BigDecimal getAaKeyUnitaDoc() {
        return this.aaKeyUnitaDoc;
    }

    @Column(name = "CD_KEY_UNITA_DOC")
    public String getCdKeyUnitaDoc() {
        return this.cdKeyUnitaDoc;
    }

    @Column(name = "CD_REGISTRO_KEY_UNITA_DOC")
    public String getCdRegistroKeyUnitaDoc() {
        return this.cdRegistroKeyUnitaDoc;
    }

    @Column(name = "DS_KEY_ITEM")
    public String getDsKeyItem() {
        return this.dsKeyItem;
    }

    @Column(name = "DS_LISTA_ERR")
    public String getDsListaErr() {
        return this.dsListaErr;
    }

    @Column(name = "DT_ANNUL")
    public LocalDateTime getDtAnnul() {
        return this.dtAnnul;
    }

    @Id
    @Column(name = "ID_ITEM_RICH_SOFT_DELETE")
    public BigDecimal getIdItemRichSoftDelete() {
        return this.idItemRichSoftDelete;
    }

    @Column(name = "ID_UNITA_DOC")
    public BigDecimal getIdUnitaDoc() {
        return this.idUnitaDoc;
    }

    @Column(name = "PG_ITEM_RICH_SOFT_DELETE")
    public BigDecimal getPgItemRichSoftDelete() {
        return this.pgItemRichSoftDelete;
    }

    @Column(name = "TI_ITEM_RICH_SOFT_DELETE")
    public String getTiItemRichSoftDelete() {
        return this.tiItemRichSoftDelete;
    }

    @Column(name = "TI_STATO_ITEM")
    public String getTiStatoItem() {
        return this.tiStatoItem;
    }

}
