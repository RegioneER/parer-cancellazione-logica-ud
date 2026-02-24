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
import java.util.ArrayList;
import java.util.List;

import it.eng.parer.soft.delete.jpa.inheritance.SoftDelete;
import jakarta.persistence.*;

/**
 * The persistent class for the FAS_FASCICOLO database table.
 */
@Entity
@Table(name = "FAS_FASCICOLO")
public class FasFascicolo extends SoftDelete implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "ID_FASCICOLO")
    private Long idFascicolo;

    @OneToMany(mappedBy = "fasFascicolo")
    private List<ElvFascDaElabElenco> elvFascDaElabElencos = new ArrayList<>();

    @OneToMany(mappedBy = "fasFascicolo")
    private List<FasAmminPartec> fasAmminPartecs = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_UNITA_DOC_LAST")
    private AroUnitaDoc aroUnitaDocLast;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_UNITA_DOC_FIRST")
    private AroUnitaDoc aroUnitaDocFirst;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_FASCICOLO_PADRE")
    private FasFascicolo fasFascicoloPadre;

    @OneToMany(mappedBy = "fasFascicoloPadre")
    private List<FasFascicolo> fasFascicolos = new ArrayList<>();

    @OneToMany(mappedBy = "fasFascicolo")
    private List<FasStatoConservFascicolo> fasStatoConservFascicoloElencos = new ArrayList<>();

    @OneToMany(mappedBy = "fasFascicolo")
    private List<FasStatoFascicoloElenco> fasStatoFascicoloElencos = new ArrayList<>();

    @OneToMany(mappedBy = "fasFascicolo")
    private List<FasEventoFascicolo> fasEventoFascicolos = new ArrayList<>();

    @OneToOne(cascade = CascadeType.ALL)
    @PrimaryKeyJoinColumn
    private FasXmlVersFascObjectStorage fasXmlVersFascObjectStorage;

    @OneToOne(cascade = CascadeType.ALL)
    @PrimaryKeyJoinColumn
    private FasXmlFascObjectStorage fasXmlFascObjectStorage;

    @OneToMany(mappedBy = "fasFascicolo")
    private List<FasLinkFascicolo> fasLinkFascicolos1 = new ArrayList<>();

    @OneToMany(mappedBy = "fasFascicolo")
    private List<FasRespFascicolo> fasRespFascicolos = new ArrayList<>();

    @OneToMany(mappedBy = "fasFascicolo")
    private List<FasUniOrgRespFascicolo> fasUniOrgRespFascicolos = new ArrayList<>();

    @OneToMany(mappedBy = "fasFascicolo")
    private List<FasSogFascicolo> fasSogFascicolos = new ArrayList<>();

    @OneToMany(mappedBy = "fasFascicolo")
    private List<FasUnitaDocFascicolo> fasUnitaDocFascicolos = new ArrayList<>();

    @OneToMany(mappedBy = "fasFascicolo")
    private List<FasXmlFascicolo> fasXmlFascicolos = new ArrayList<>();

    @OneToMany(mappedBy = "fasFascicolo")
    private List<FasXmlVersFascicolo> fasXmlVersFascicolos = new ArrayList<>();

    @OneToMany(mappedBy = "fasFascicolo")
    private List<VrsSesFascicoloKo> vrsSesFascicoloKos = new ArrayList<>();

    @OneToMany(mappedBy = "fasFascicolo")
    private List<AroItemRichAnnulVers> aroItemRichAnnulVers = new ArrayList<>();

    @OneToMany(mappedBy = "fasFascicolo")
    private List<FasVerAipFascicolo> fasVerAipFascicolos = new ArrayList<>();

    @OneToMany(mappedBy = "fasFascicolo")
    private List<FasAipFascicoloDaElab> fasAipFascicoloDaElab = new ArrayList<>();

    @OneToMany(mappedBy = "fasFascicolo")
    private List<ElvElencoVersFascAnnul> elvElencoVersFascAnnuls = new ArrayList<>();

    @OneToMany(mappedBy = "fasFascicolo")
    private List<FasValoreAttribFascicolo> fasValoreAttribFascicolos = new ArrayList<>();

    @OneToMany(mappedBy = "fasFascicolo")
    private List<FasVoceClassif> fasVoceClassifs = new ArrayList<>();

    @OneToMany(mappedBy = "fasFascicolo")
    private List<FasWarnFascicolo> fasWarnFascicolos = new ArrayList<>();

    public FasFascicolo() {/* Hibernate */
    }

    public Long getIdFascicolo() {
        return this.idFascicolo;
    }

    public List<FasAmminPartec> getFasAmminPartecs() {
        return fasAmminPartecs;
    }

    public List<ElvFascDaElabElenco> getElvFascDaElabElencos() {
        return elvFascDaElabElencos;
    }

    public AroUnitaDoc getAroUnitaDocLast() {
        return this.aroUnitaDocLast;
    }

    public AroUnitaDoc getAroUnitaDocFirst() {
        return this.aroUnitaDocFirst;
    }

    public FasFascicolo getFasFascicoloPadre() {
        return this.fasFascicoloPadre;
    }

    public List<FasFascicolo> getFasFascicolos() {
        return this.fasFascicolos;
    }

    public List<FasStatoConservFascicolo> getFasStatoConservFascicoloElencos() {
        return this.fasStatoConservFascicoloElencos;
    }

    public List<FasStatoFascicoloElenco> getFasStatoFascicoloElencos() {
        return this.fasStatoFascicoloElencos;
    }

    public List<FasEventoFascicolo> getFasEventoFascicolos() {
        return this.fasEventoFascicolos;
    }

    public List<FasLinkFascicolo> getFasLinkFascicolos1() {
        return fasLinkFascicolos1;
    }

    public List<FasRespFascicolo> getFasRespFascicolos() {
        return fasRespFascicolos;
    }

    public List<FasUniOrgRespFascicolo> getFasUniOrgRespFascicolos() {
        return fasUniOrgRespFascicolos;
    }

    public List<FasSogFascicolo> getFasSogFascicolos() {
        return fasSogFascicolos;
    }

    public List<FasUnitaDocFascicolo> getFasUnitaDocFascicolos() {
        return fasUnitaDocFascicolos;
    }

    public List<FasXmlFascicolo> getFasXmlFascicolos() {
        return fasXmlFascicolos;
    }

    public List<FasXmlVersFascicolo> getFasXmlVersFascicolos() {
        return fasXmlVersFascicolos;
    }

    public List<VrsSesFascicoloKo> getVrsSesFascicoloKos() {
        return vrsSesFascicoloKos;
    }

    public List<AroItemRichAnnulVers> getAroItemRichAnnulVers() {
        return this.aroItemRichAnnulVers;
    }

    public List<FasVerAipFascicolo> getFasVerAipFascicolos() {
        return this.fasVerAipFascicolos;
    }

    public List<FasAipFascicoloDaElab> getFasAipFascicoloDaElabs() {
        return this.fasAipFascicoloDaElab;
    }

    public List<ElvElencoVersFascAnnul> getElvElencoVersFascAnnuls() {
        return this.elvElencoVersFascAnnuls;
    }

    public List<FasValoreAttribFascicolo> getFasValoreAttribFascicolos() {
        return this.fasValoreAttribFascicolos;
    }

    public List<FasVoceClassif> getFasVoceClassifs() {
        return this.fasVoceClassifs;
    }

    public List<FasWarnFascicolo> getFasWarnFascicolos() {
        return this.fasWarnFascicolos;
    }

    public FasXmlVersFascObjectStorage getFasXmlVersFascObjectStorage() {
        return fasXmlVersFascObjectStorage;
    }

    public FasXmlFascObjectStorage getFasXmlFascObjectStorage() {
        return fasXmlFascObjectStorage;
    }

}
