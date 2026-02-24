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

import it.eng.parer.soft.delete.jpa.sequence.NonMonotonicSequence;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * The persistent class for the ARO_XML_RICH_SOFT_DELETE database table.
 */
@Entity
@Table(name = "ARO_XML_RICH_SOFT_DELETE")

public class AroXmlRichSoftDelete implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idXmlRichSoftDelete;

    private String blXmlRichSoftDelete;

    private String tiXmlRichSoftDelete;

    private String cdVersioneXml;

    private AroRichSoftDelete aroRichSoftDelete;

    public AroXmlRichSoftDelete() {/* Hibernate */
    }

    @Id
    @NonMonotonicSequence(sequenceName = "SARO_XML_RICH_SOFT_DELETE", incrementBy = 1)
    @Column(name = "ID_XML_RICH_SOFT_DELETE")
    public Long getIdXmlRichSoftDelete() {
        return this.idXmlRichSoftDelete;
    }

    public void setIdXmlRichSoftDelete(Long idXmlRichSoftDelete) {
        this.idXmlRichSoftDelete = idXmlRichSoftDelete;
    }

    @Lob
    @Column(name = "BL_XML_RICH_SOFT_DELETE")
    public String getBlXmlRichSoftDelete() {
        return this.blXmlRichSoftDelete;
    }

    public void setBlXmlRichSoftDelete(String blXmlRichSoftDelete) {
        this.blXmlRichSoftDelete = blXmlRichSoftDelete;
    }

    @Column(name = "TI_XML_RICH_SOFT_DELETE")
    public String getTiXmlRichSoftDelete() {
        return this.tiXmlRichSoftDelete;
    }

    public void setTiXmlRichSoftDelete(String tiXmlRichSoftDelete) {
        this.tiXmlRichSoftDelete = tiXmlRichSoftDelete;
    }

    @Column(name = "CD_VERSIONE_XML")
    public String getCdVersioneXml() {
        return this.cdVersioneXml;
    }

    public void setCdVersioneXml(String cdVersioneXml) {
        this.cdVersioneXml = cdVersioneXml;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_RICH_SOFT_DELETE")
    public AroRichSoftDelete getAroRichSoftDelete() {
        return this.aroRichSoftDelete;
    }

    public void setAroRichSoftDelete(AroRichSoftDelete aroRichSoftDelete) {
        this.aroRichSoftDelete = aroRichSoftDelete;
    }
}
