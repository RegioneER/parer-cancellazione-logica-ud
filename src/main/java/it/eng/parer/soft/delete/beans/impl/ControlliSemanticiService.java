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

/*
 * To change this template, choose Tools | Templates and open the template in the editor.
 */
package it.eng.parer.soft.delete.beans.impl;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.exception.ExceptionUtils;

import it.eng.parer.soft.delete.beans.IConfigurationDao;
import it.eng.parer.soft.delete.beans.IControlliSemanticiService;
import it.eng.parer.soft.delete.beans.dto.base.CSChiave;
import it.eng.parer.soft.delete.beans.dto.base.CSVersatore;
import it.eng.parer.soft.delete.beans.dto.base.RispostaControlli;
import it.eng.parer.soft.delete.beans.utils.Costanti.TipiGestioneUDAnnullate;
import it.eng.parer.soft.delete.beans.utils.CostantiDB.StatoConservazioneUnitaDoc;
import it.eng.parer.soft.delete.beans.utils.messages.MessaggiWSBundle;
import it.eng.parer.soft.delete.beans.utils.messages.MessaggiWSFormat;
import it.eng.parer.soft.delete.jpa.entity.AroUnitaDoc;
import it.eng.parer.soft.delete.jpa.entity.OrgAmbiente;
import it.eng.parer.soft.delete.jpa.entity.OrgEnte;
import it.eng.parer.soft.delete.jpa.entity.OrgStrut;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;

@SuppressWarnings("unchecked")
@ApplicationScoped
public class ControlliSemanticiService implements IControlliSemanticiService {

    EntityManager entityManager;

    IConfigurationDao configurationDao;

    @Inject
    public ControlliSemanticiService(EntityManager entityManager,
            IConfigurationDao configurationDao) {
        this.entityManager = entityManager;
        this.configurationDao = configurationDao;
    }

    @Override
    @Transactional
    public RispostaControlli caricaDefaultDaDB(String tipoPar) {
        return caricaDefaultDaDB(new String[] {
                tipoPar });
    }

    private RispostaControlli caricaDefaultDaDB(String[] tipoPars) {
        RispostaControlli rcCaricaDefaultDaDB = new RispostaControlli();
        rcCaricaDefaultDaDB.setrLong(-1);
        rcCaricaDefaultDaDB.setrBoolean(false);

        try {
            // carico i parametri applicativi
            Map<String, String> tmpDefaults = configurationDao
                    .getValoreParamApplicByTiParamApplicAsMap(Arrays.asList(tipoPars));

            if (!tmpDefaults.isEmpty()) {
                rcCaricaDefaultDaDB.setrObject(tmpDefaults);
                rcCaricaDefaultDaDB.setrBoolean(true);
            } else {
                String joinpars = String.join(",", tipoPars);
                //
                rcCaricaDefaultDaDB.setCodErr(MessaggiWSBundle.ERR_666);
                rcCaricaDefaultDaDB.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
                        "ControlliSemanticiService.caricaDefaultDaDB: Parametri applicativi non correttamente configurati per {}",
                        joinpars));
            }
        } catch (Exception e) {
            rcCaricaDefaultDaDB.setCodErr(MessaggiWSBundle.ERR_666);
            rcCaricaDefaultDaDB.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
                    "ControlliSemanticiService.caricaDefaultDaDB: "
                            + ExceptionUtils.getRootCauseMessage(e)));
        }

        return rcCaricaDefaultDaDB;
    }

    @Override
    @Transactional
    public RispostaControlli checkIdStrut(CSVersatore vers) {
        RispostaControlli rcCheckIdStrut = new RispostaControlli();
        rcCheckIdStrut.setrLong(-1);

        // return -1 che è il codice di errore
        long idAmb = -1;
        long idEnte = -1;

        // prendo i paramentri dell'xml
        String amb = vers.getAmbiente();
        String ente = vers.getEnte();
        String strut = vers.getStruttura();

        // lista entity JPA ritornate dalle Query
        List<OrgStrut> orgStrutS = null;
        List<OrgEnte> orgEnteS = null;
        List<OrgAmbiente> orgAmbienteS = null;

        // lancio query di controllo
        try {
            // controllo ambiente
            final String queryStrAmb = "select amb " + "from OrgAmbiente amb "
                    + "where amb.nmAmbiente =  :nmAmbienteIn";
            Query query = entityManager.createQuery(queryStrAmb, OrgAmbiente.class);
            query.setParameter("nmAmbienteIn", amb);
            orgAmbienteS = query.getResultList();

            // assente
            if (orgAmbienteS.isEmpty()) {
                rcCheckIdStrut.setCodErr(MessaggiWSBundle.UD_001_001);
                rcCheckIdStrut
                        .setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.UD_001_001, amb));
                return rcCheckIdStrut;
            }
            // too many rows
            if (orgAmbienteS.size() > 1) {
                rcCheckIdStrut.setCodErr(MessaggiWSBundle.ERR_666);
                rcCheckIdStrut.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
                        "ControlliSemanticiService.checkIdStrut: Ambiente duplicato"));
                return rcCheckIdStrut;
            }
            for (OrgAmbiente a : orgAmbienteS) {
                idAmb = a.getIdAmbiente();
            }

            // controllo ente
            final String queryStrEnte = "select ente " + "from OrgEnte ente "
                    + "join ente.orgAmbiente org " + "where org.idAmbiente = :idAmbienteIn "
                    + " and ente.nmEnte = :nmEnteIn ";
            query = entityManager.createQuery(queryStrEnte, OrgEnte.class);
            query.setParameter("idAmbienteIn", idAmb);
            query.setParameter("nmEnteIn", ente);
            orgEnteS = query.getResultList();
            // assente
            if (orgEnteS.isEmpty()) {
                rcCheckIdStrut.setCodErr(MessaggiWSBundle.UD_001_002);
                rcCheckIdStrut
                        .setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.UD_001_002, ente));
                return rcCheckIdStrut;
            }
            // too many rows
            if (orgEnteS.size() > 1) {
                rcCheckIdStrut.setCodErr(MessaggiWSBundle.ERR_666);
                rcCheckIdStrut.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
                        "ControlliSemanticiService.checkIdStrut: Ente duplicato"));
                return rcCheckIdStrut;
            }
            for (OrgEnte e : orgEnteS) {
                idEnte = e.getIdEnte();
            }

            // controllo struttura
            final String queryStrStrut = "select strut " + "from OrgStrut strut "
                    + "join strut.orgEnte ente " + "where ente.idEnte = :idEnteIn "
                    + " and strut.nmStrut = :nmStrutIn ";
            query = entityManager.createQuery(queryStrStrut, OrgStrut.class);
            query.setParameter("idEnteIn", idEnte);
            query.setParameter("nmStrutIn", strut);
            orgStrutS = query.getResultList();
            // assente
            if (orgStrutS.isEmpty()) {
                rcCheckIdStrut.setCodErr(MessaggiWSBundle.UD_001_003);
                rcCheckIdStrut
                        .setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.UD_001_003, strut));
                return rcCheckIdStrut;
            }
            // too many rows
            if (orgStrutS.size() > 1) {
                rcCheckIdStrut.setCodErr(MessaggiWSBundle.ERR_666);
                rcCheckIdStrut.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
                        "ControlliSemanticiService.checkIdStrut: Struttura duplicata"));
                return rcCheckIdStrut;
            }
            //
            rcCheckIdStrut.setrLong(orgStrutS.get(0).getIdStrut());
        } catch (Exception e) {
            rcCheckIdStrut.setCodErr(MessaggiWSBundle.ERR_666);
            rcCheckIdStrut.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
                    "ControlliSemanticiService.checkIdStrut: "
                            + ExceptionUtils.getRootCauseMessage(e)));
        }

        return rcCheckIdStrut;
    }

    @Override
    @Transactional
    public RispostaControlli checkChiave(CSChiave key, long idStruttura,
            TipiGestioneUDAnnullate tguda) {
        RispostaControlli rcCheckChiave = new RispostaControlli();
        rcCheckChiave.setrLong(-1);
        rcCheckChiave.setrBoolean(false);

        // prendo i paramentri dell'xml
        String numero = key.getNumero();
        Long anno = key.getAnno();
        String tipoReg = key.getTipoRegistro();

        // lista entity JPA ritornate dalle Query
        List<AroUnitaDoc> unitaDocS = null;

        // lancio query di controllo
        try {
            // ricavo le ud presenti in base ai parametri impostati
            final String queryStr = "select ud " + "from AroUnitaDoc ud " + "join ud.orgStrut org "
                    + "where org.idStrut = :idStrutIn "
                    + " and ud.cdKeyUnitaDoc = :cdKeyUnitaDocIn "
                    + " and ud.aaKeyUnitaDoc = :aaKeyUnitaDocIn "
                    + " and ud.cdRegistroKeyUnitaDoc = :cdRegistroKeyUnitaDocIn "
                    + " order by ud.dtCreazione desc";
            Query query = entityManager.createQuery(queryStr, AroUnitaDoc.class);
            query.setParameter("idStrutIn", idStruttura);
            query.setParameter("cdKeyUnitaDocIn", numero);
            query.setParameter("aaKeyUnitaDocIn", new BigDecimal(anno));
            query.setParameter("cdRegistroKeyUnitaDocIn", tipoReg);
            unitaDocS = query.getResultList();

            // chiave già presente (uno o più righe trovate, mi interessa solo l'ultima
            // -
            // più recente)
            if (!unitaDocS.isEmpty()) {
                StatoConservazioneUnitaDoc scud = StatoConservazioneUnitaDoc
                        .valueOf(unitaDocS.get(0).getTiStatoConservazione());
                if (scud == StatoConservazioneUnitaDoc.ANNULLATA
                        && tguda == TipiGestioneUDAnnullate.CONSIDERA_ASSENTE) {
                    // commuto l'errore in UD annullata e rendo true come
                    // risposta: in pratica come
                    // se non
                    // avesse trovato l'UD ma con un errore diverso: è lo stesso
                    // comportamento della
                    // vecchia versione del metodo
                    rcCheckChiave.setCodErr(MessaggiWSBundle.UD_012_002);
                    rcCheckChiave.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.UD_012_002,
                            MessaggiWSFormat.formattaUrnPartUnitaDoc(key)));
                    rcCheckChiave.setrBoolean(true);
                } else {
                    // gestione normale: ho trovato l'UD e non è annullata.
                    // Oppure è annullata e voglio caricarla lo stesso (il solo
                    // caso è nel ws
                    // recupero stato UD)
                    // intanto rendo l'errore di chiave già presente
                    rcCheckChiave.setCodErr(MessaggiWSBundle.UD_002_001);
                    rcCheckChiave.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.UD_002_001,
                            MessaggiWSFormat.formattaUrnPartUnitaDoc(key)));

                    rcCheckChiave.setrLong(unitaDocS.get(0).getIdUnitaDoc());
                    // se la chiave è già presente, oltre all'id dell'UD
                    // trovata,
                    // recupero il tipo di salvataggio. Mi serve sia
                    // nell'aggiunta documenti
                    // che nel recupero UD
                    rcCheckChiave.setrString(unitaDocS.get(0).getDecTipoUnitaDoc().getTiSaveFile());
                    //
                    rcCheckChiave.setrStringExtended(
                            unitaDocS.get(0).getDecTipoUnitaDoc().getNmTipoUnitaDoc());
                    // lo stato conservazione viene usato per l'aggiunta doc:
                    // non posso aggiungere doc se l'ud è nello stato sbagliato
                    rcCheckChiave.setrObject(scud);

                    // recupero id registro
                    rcCheckChiave.setrLongExtended(unitaDocS.get(0).getIdDecRegistroUnitaDoc());
                    // **************
                    // EXTENDED VALUES
                    // **************
                    // recupero id tipo ud
                    rcCheckChiave.getrMap().put(RispostaControlli.ValuesOnrMap.ID_TIPO_UD.name(),
                            unitaDocS.get(0).getDecTipoUnitaDoc().getIdTipoUnitaDoc());
                    // recupero chiave normalizzata (se esiste)
                    rcCheckChiave.getrMap().put(
                            RispostaControlli.ValuesOnrMap.CD_KEY_NORMALIZED.name(),
                            unitaDocS.get(0).getCdKeyUnitaDocNormaliz());
                }
                return rcCheckChiave;
            }

            // Chiave non trovata
            rcCheckChiave.setCodErr(MessaggiWSBundle.UD_005_001);
            rcCheckChiave.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.UD_005_001,
                    MessaggiWSFormat.formattaUrnPartUnitaDoc(key)));
            rcCheckChiave.setrBoolean(true);

        } catch (Exception e) {
            rcCheckChiave.setCodErr(MessaggiWSBundle.ERR_666);
            rcCheckChiave.setDsErr(MessaggiWSBundle.getString(MessaggiWSBundle.ERR_666,
                    "ControlliSemanticiService.checkChiave: "
                            + ExceptionUtils.getRootCauseMessage(e)));
        }

        return rcCheckChiave;
    }

}
