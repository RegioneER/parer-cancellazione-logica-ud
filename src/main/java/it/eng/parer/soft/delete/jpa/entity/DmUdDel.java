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
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

/**
 * The persistent class for the DM_UD_DEL database table.
 */
@Entity
@Table(name = "DM_UD_DEL")
public class DmUdDel implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idUnitaDoc;

    private AroUnitaDoc aroUnitaDoc;

    private DmUdDelRichieste dmUdDelRichieste;

    private String cdRegistroKeyUnitaDoc;

    private BigDecimal aaKeyUnitaDoc;

    private String cdKeyUnitaDoc;

    private OrgStrut orgStrut;

    private String nmStrut;

    private String tiStatoUdCancellate;

    private LocalDateTime dtVersamento;

    private LocalDateTime dtStatoUdCancellate;

    private List<DmUdDelRecRefTab> dmUdDelRecRefTabs;

    public DmUdDel() {/* Hibernate */
    }

    @Id
    @Column(name = "ID_UNITA_DOC")
    public Long getIdUnitaDoc() {
        return idUnitaDoc;
    }

    @MapsId
    @OneToOne(mappedBy = "dmUdDel")
    @JoinColumn(name = "ID_UNITA_DOC")
    public AroUnitaDoc getAroUnitaDoc() {
        return aroUnitaDoc;
    }

    public void setAroUnitaDoc(AroUnitaDoc aroUnitaDoc) {
        this.aroUnitaDoc = aroUnitaDoc;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_UD_DEL_RICHIESTA")
    public DmUdDelRichieste getDmUdDelRichieste() {
        return dmUdDelRichieste;
    }

    @Column(name = "DT_VERSAMENTO")
    public LocalDateTime getDtVersamento() {
        return this.dtVersamento;
    }

    @Column(name = "CD_REGISTRO_KEY_UNITA_DOC")
    public String getCdRegistroKeyUnitaDoc() {
        return this.cdRegistroKeyUnitaDoc;
    }

    public void setCdRegistroKeyUnitaDoc(String cdRegistroKeyUnitaDoc) {
        this.cdRegistroKeyUnitaDoc = cdRegistroKeyUnitaDoc;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_STRUT")
    public OrgStrut getOrgStrut() {
        return this.orgStrut;
    }

    @Column(name = "NM_STRUT")
    public String getNmStrut() {
        return this.nmStrut;
    }

    @Column(name = "TI_STATO_UD_CANCELLATE")
    public String getTiStatoUdCancellate() {
        return this.tiStatoUdCancellate;
    }

    @Column(name = "DT_STATO_UD_CANCELLATE")
    public LocalDateTime getDtStatoUdCancellate() {
        return this.dtStatoUdCancellate;
    }

    @OneToMany(mappedBy = "dmUdDel", fetch = FetchType.LAZY)
    public List<DmUdDelRecRefTab> getDmUdDelRecRefTabs() {
        return dmUdDelRecRefTabs;
    }

}
