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

package it.eng.parer.soft.delete.beans.dao;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Stream;

import it.eng.parer.soft.delete.beans.utils.CostantiDB.Flag;
import it.eng.parer.soft.delete.beans.utils.converter.TypeConverter;

import org.hibernate.jpa.HibernateHints;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.eng.parer.soft.delete.beans.IRegistrazioneRichiesteDao;
import it.eng.parer.soft.delete.beans.exceptions.AppGenericPersistenceException;
import it.eng.parer.soft.delete.jpa.entity.AroErrRichSoftDelete;
import it.eng.parer.soft.delete.jpa.entity.AroRichSoftDelete;
import it.eng.parer.soft.delete.jpa.entity.AroXmlRichSoftDelete;
import it.eng.parer.soft.delete.jpa.entity.DmUdDel;
import it.eng.parer.soft.delete.jpa.entity.constraint.AroRichiestaRa.AroRichiestaTiStato;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

@ApplicationScoped
public class RegistrazioneRichiesteDao implements IRegistrazioneRichiesteDao {

    private final Logger log = LoggerFactory.getLogger(RegistrazioneRichiesteDao.class);

    private static final String QUERY_PARAM_ID_STRUT = "idStrut";
    private static final String QUERY_PARAM_ID_UNITA_DOC = "idUnitaDoc";
    private static final String QUERY_PARAM_ID_RICHIESTA_SACER = "idRichiestaSacer";
    private static final String QUERY_PARAM_ID_RICH_SOFT_DELETE = "idRichSoftDelete";
    private static final String QUERY_STRING_NOT_ID_RICH_SOFT_DELETE = "rich.idRichSoftDelete != :idRichSoftDelete";
    private static final String QUERY_STRING_AND = " AND ";

    EntityManager entityManager;

    @Inject
    public RegistrazioneRichiesteDao(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public Long getIdUnitaDocVersataAnnul(BigDecimal idStrut, String cdRegistroKeyUnitaDoc,
            BigDecimal aaKeyUnitaDoc, String cdKeyUnitaDoc) throws AppGenericPersistenceException {
        String queryStr = "SELECT u.idUnitaDoc FROM AroUnitaDoc u "
                + "WHERE u.orgStrut.idStrut = :idStrut "
                + "AND u.cdRegistroKeyUnitaDoc = :cdRegistroKeyUnitaDoc "
                + "AND u.aaKeyUnitaDoc = :aaKeyUnitaDoc " + "AND u.cdKeyUnitaDoc = :cdKeyUnitaDoc "
                + "AND u.dtAnnul < :dtAnnul ";
        Query query = entityManager.createQuery(queryStr);
        query.setParameter(QUERY_PARAM_ID_STRUT, TypeConverter.longFromBigDecimal(idStrut));
        query.setParameter("cdRegistroKeyUnitaDoc", cdRegistroKeyUnitaDoc);
        query.setParameter("aaKeyUnitaDoc", aaKeyUnitaDoc);
        query.setParameter("cdKeyUnitaDoc", cdKeyUnitaDoc);
        Calendar cal = Calendar.getInstance();
        cal.set(2444, Calendar.DECEMBER, 31, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        query.setParameter("dtAnnul", cal.getTime().toInstant()
                .atZone(java.time.ZoneId.systemDefault()).toLocalDateTime());
        List<Long> listaUdVersate = query.getResultList();
        if (listaUdVersate != null && !listaUdVersate.isEmpty()) {
            return listaUdVersate.get(0);
        } else {
            return null;
        }
    }

    @Override
    public Long getIdRichAnnulVersEvasaDaCancel(BigDecimal idStrut, BigDecimal idRichiestaSacer)
            throws AppGenericPersistenceException {
        String queryStr = "SELECT u.idRichAnnulVers FROM AroRichAnnulVers u "
                + "JOIN u.aroStatoRichAnnulVers stati " + "WHERE u.orgStrut.idStrut = :idStrut "
                + "AND u.idRichAnnulVers = :idRichiestaSacer "
                + "AND u.tiAnnullamento = 'CANCELLAZIONE' "
                + "AND stati.pgStatoRichAnnulVers = (SELECT MAX(maxStati.pgStatoRichAnnulVers) FROM AroStatoRichAnnulVers maxStati WHERE maxStati.aroRichAnnulVers.idRichAnnulVers = u.idRichAnnulVers) AND stati.tiStatoRichAnnulVers = 'EVASA' ";
        Query query = entityManager.createQuery(queryStr);
        query.setParameter(QUERY_PARAM_ID_STRUT, TypeConverter.longFromBigDecimal(idStrut));
        query.setParameter(QUERY_PARAM_ID_RICHIESTA_SACER, idRichiestaSacer);
        List<Long> listaRichAnnVrs = query.getResultList();
        if (listaRichAnnVrs != null && !listaRichAnnVrs.isEmpty()) {
            return listaRichAnnVrs.get(0);
        } else {
            return null;
        }
    }

    @Override
    public BigDecimal getIdRichRestArchRestituita(BigDecimal idStrut, BigDecimal idRichiestaSacer)
            throws AppGenericPersistenceException {
        String queryStr = "SELECT u.idRichiestaRa FROM AroVRicRichRa u "
                + "WHERE u.idStrut = :idStrut " + "AND u.idRichiestaRa = :idRichiestaSacer "
                + "AND u.tiStato = :tiStato ";
        Query query = entityManager.createQuery(queryStr);
        query.setParameter(QUERY_PARAM_ID_STRUT, idStrut);
        query.setParameter(QUERY_PARAM_ID_RICHIESTA_SACER, idRichiestaSacer);
        query.setParameter("tiStato", AroRichiestaTiStato.RESTITUITO.name());
        List<BigDecimal> listaRichRa = query.getResultList();
        if (listaRichRa != null && !listaRichRa.isEmpty()) {
            return listaRichRa.get(0);
        } else {
            return null;
        }
    }

    /**
     * Ritorna la richiesta data l'unità documentaria come parametro se lo stato richiesta è
     * PRESA_IN CARICO o ACQUISITA
     *
     * @param idUnitaDoc       id unita doc
     * @param idRichSoftDelete id della richiesta da escludere nel controllo in quanto contiene già
     *                         l'ud
     *
     * @return true se è presente
     */
    @Override
    public AroRichSoftDelete getAroRichSoftDeleteContainingUd(Long idUnitaDoc,
            Long idRichSoftDelete) throws AppGenericPersistenceException {
        StringBuilder queryStr = new StringBuilder(
                "SELECT rich FROM AroItemRichSoftDelete item JOIN item.aroRichSoftDelete rich JOIN rich.aroStatoRichSoftDelete stati WHERE ");
        if (idRichSoftDelete != null) {
            queryStr.append(QUERY_STRING_NOT_ID_RICH_SOFT_DELETE).append(QUERY_STRING_AND);
        }
        queryStr.append(
                "item.aroUnitaDoc.idUnitaDoc = :idUnitaDoc AND stati.pgStatoRichSoftDelete = (SELECT MAX(maxStati.pgStatoRichSoftDelete) FROM AroStatoRichSoftDelete maxStati WHERE maxStati.aroRichSoftDelete.idRichSoftDelete = rich.idRichSoftDelete) AND stati.tiStatoRichSoftDelete IN ('PRESA_IN_CARICO', 'ACQUISITA') ");
        Query query = entityManager.createQuery(queryStr.toString());
        query.setParameter(QUERY_PARAM_ID_UNITA_DOC, idUnitaDoc);
        if (idRichSoftDelete != null) {
            query.setParameter(QUERY_PARAM_ID_RICH_SOFT_DELETE, idRichSoftDelete);
        }
        List<AroRichSoftDelete> list = query.getResultList();
        if (list != null && !list.isEmpty()) {
            return list.get(0);
        } else {
            return null;
        }
    }

    @Override
    public AroRichSoftDelete getAroRichSoftDeleteContainingRichAnnulVers(Long idRichAnnulVers,
            Long idRichSoftDelete) throws AppGenericPersistenceException {
        StringBuilder queryStr = new StringBuilder(
                "SELECT rich FROM AroItemRichSoftDelete item JOIN item.aroRichSoftDelete rich JOIN rich.aroStatoRichSoftDelete stati "
                        + "JOIN item.aroRichAnnulVers annul JOIN annul.aroStatoRichAnnulVers statiAnnul "
                        + "WHERE ");
        if (idRichSoftDelete != null) {
            queryStr.append(QUERY_STRING_NOT_ID_RICH_SOFT_DELETE).append(QUERY_STRING_AND);
        }
        queryStr.append("item.aroRichAnnulVers.idRichAnnulVers = :idRichAnnulVers AND "
                + "item.aroRichAnnulVers.tiAnnullamento = 'CANCELLAZIONE' AND "
                + "stati.pgStatoRichSoftDelete = (SELECT MAX(maxStati.pgStatoRichSoftDelete) FROM AroStatoRichSoftDelete maxStati WHERE maxStati.aroRichSoftDelete.idRichSoftDelete = rich.idRichSoftDelete) AND "
                + "stati.tiStatoRichSoftDelete IN ('PRESA_IN_CARICO', 'ACQUISITA') AND "
                + "statiAnnul.pgStatoRichAnnulVers = (SELECT MAX(maxStatiAn.pgStatoRichAnnulVers) FROM AroStatoRichAnnulVers maxStatiAn WHERE maxStatiAn.aroRichAnnulVers.idRichAnnulVers = annul.idRichAnnulVers) AND "
                + "statiAnnul.tiStatoRichAnnulVers = 'EVASA'");

        Query query = entityManager.createQuery(queryStr.toString());
        query.setParameter("idRichAnnulVers", idRichAnnulVers);
        if (idRichSoftDelete != null) {
            query.setParameter(QUERY_PARAM_ID_RICH_SOFT_DELETE, idRichSoftDelete);
        }
        List<AroRichSoftDelete> list = query.getResultList();
        if (list != null && !list.isEmpty()) {
            return list.get(0);
        } else {
            return null;
        }
    }

    @Override
    public AroRichSoftDelete getAroRichSoftDeleteContainingRichRestArch(Long idRichRestArch,
            Long idRichSoftDelete, Long idStrut) throws AppGenericPersistenceException {
        StringBuilder queryStr = new StringBuilder(
                "SELECT rich FROM AroItemRichSoftDelete item JOIN item.aroRichSoftDelete rich JOIN rich.aroStatoRichSoftDelete stati "
                        + "JOIN item.aroRichiestaRa ra " + "WHERE ");
        if (idRichSoftDelete != null) {
            queryStr.append(QUERY_STRING_NOT_ID_RICH_SOFT_DELETE).append(QUERY_STRING_AND);
        }
        queryStr.append("ra.idRichiestaRa = :idRichRestArch AND "
                + "rich.orgStrut.idStrut = :idStrut AND "
                + "stati.pgStatoRichSoftDelete = (SELECT MAX(maxStati.pgStatoRichSoftDelete) FROM AroStatoRichSoftDelete maxStati WHERE maxStati.aroRichSoftDelete.idRichSoftDelete = rich.idRichSoftDelete) AND "
                + "stati.tiStatoRichSoftDelete IN ('PRESA_IN_CARICO', 'ACQUISITA') AND "
                + "ra.tiStato = 'RESTITUITO'");

        Query query = entityManager.createQuery(queryStr.toString());
        query.setParameter("idRichRestArch", idRichRestArch);
        if (idRichSoftDelete != null) {
            query.setParameter(QUERY_PARAM_ID_RICH_SOFT_DELETE, idRichSoftDelete);
        }
        query.setParameter(QUERY_PARAM_ID_STRUT, idStrut);
        List<AroRichSoftDelete> list = query.getResultList();
        if (list != null && !list.isEmpty()) {
            return list.get(0);
        } else {
            return null;
        }
    }

    /**
     * Recupera le unità documentarie associate a una richiesta
     */
    @Override
    public List<DmUdDel> recuperaUnitaDocDaRichiesta(Long idStrut, BigDecimal idRichiestaSacer)
            throws AppGenericPersistenceException {
        String queryStr = "SELECT dm FROM DmUdDel dm " + "JOIN dm.dmUdDelRichieste dr "
                + "WHERE dr.idRichiesta = :idRichiestaSacer "
                + "AND dm.orgStrut.idStrut = :idStrut";

        Query query = entityManager.createQuery(queryStr);
        query.setParameter(QUERY_PARAM_ID_RICHIESTA_SACER, idRichiestaSacer);
        query.setParameter(QUERY_PARAM_ID_STRUT, idStrut);

        return query.getResultList();
    }

    @Override
    public boolean isUdNonAnnullata(long idUnitaDoc) {
        Query q = entityManager.createQuery("SELECT unitaDoc FROM AroUnitaDoc unitaDoc "
                + "WHERE unitaDoc.idUnitaDoc = :idUnitaDoc "
                + "AND unitaDoc.dtAnnul = :endOfTheParer ");
        Calendar c = Calendar.getInstance();
        c.set(2444, Calendar.DECEMBER, 31, 0, 0, 0);
        c.set(Calendar.MILLISECOND, 0);
        q.setParameter(QUERY_PARAM_ID_UNITA_DOC, idUnitaDoc);
        q.setParameter("endOfTheParer",
                c.getTime().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime());
        return !q.getResultList().isEmpty();
    }

    @Override
    public boolean isUdNonRestituita(long idUnitaDoc) {
        Query q = entityManager.createQuery("SELECT unitaDoc FROM AroUnitaDoc unitaDoc "
                + "JOIN unitaDoc.orgStrut struttura " + "WHERE unitaDoc.idUnitaDoc = :idUnitaDoc "
                + "AND struttura.flArchivioRestituito = :falseFlag ");
        q.setParameter(QUERY_PARAM_ID_UNITA_DOC, idUnitaDoc);
        q.setParameter("falseFlag", Flag.FALSE);
        return !q.getResultList().isEmpty();
    }

    /**
     * Restituisce l'elenco degli errori di un determinato item con una specifica gravità
     *
     * @param idItemRichSoftDelete id item richiesta di cancellazione
     * @param tiGravita            tipo gravita
     *
     * @return la lista di errori sull'item
     */
    @Override
    public List<AroErrRichSoftDelete> getAroErrRichSoftDeleteByGravity(long idItemRichSoftDelete,
            String tiGravita) throws AppGenericPersistenceException {
        Query query = entityManager.createQuery(
                "SELECT errRichSoftDelete FROM AroErrRichSoftDelete errRichSoftDelete JOIN errRichSoftDelete.aroItemRichSoftDelete itemRichSoftDelete "
                        + "WHERE itemRichSoftDelete.idItemRichSoftDelete = :idItemRichSoftDelete AND errRichSoftDelete.tiGravita = :tiGravita");
        query.setParameter("idItemRichSoftDelete", idItemRichSoftDelete);
        query.setParameter("tiGravita", tiGravita);
        return query.getResultList();
    }

    /**
     * Controlla che l’unità documentaria identificata dalla struttura versante, registro, anno e
     * numero esista
     * <p>
     * (NOTA: una unità doc può essere annullata più di una volta, per questo il conteggio può
     * essere superiore a 1)
     *
     * @param idStrut               id struttura
     * @param cdRegistroKeyUnitaDoc chiave registro unita doc
     * @param aaKeyUnitaDoc         anno unita doc
     * @param cdKeyUnitaDoc         numero unita doc
     *
     * @return true/false
     */
    @Override
    public boolean existAroUnitaDoc(BigDecimal idStrut, String cdRegistroKeyUnitaDoc,
            BigDecimal aaKeyUnitaDoc, String cdKeyUnitaDoc) throws AppGenericPersistenceException {
        String queryStr = "SELECT COUNT(u) FROM AroUnitaDoc u "
                + "WHERE u.orgStrut.idStrut = :idStrut "
                + "AND u.cdRegistroKeyUnitaDoc = :cdRegistroKeyUnitaDoc "
                + "AND u.aaKeyUnitaDoc = :aaKeyUnitaDoc " + "AND u.cdKeyUnitaDoc = :cdKeyUnitaDoc ";
        Query query = entityManager.createQuery(queryStr);
        query.setParameter(QUERY_PARAM_ID_STRUT, TypeConverter.longFromBigDecimal(idStrut));
        query.setParameter("cdRegistroKeyUnitaDoc", cdRegistroKeyUnitaDoc);
        query.setParameter("aaKeyUnitaDoc", aaKeyUnitaDoc);
        query.setParameter("cdKeyUnitaDoc", cdKeyUnitaDoc);
        Long numUd = (Long) query.getSingleResult();
        return numUd > 0;
    }

    /**
     * Controlla che esista la richiesta di annullamento versamento di tipo CANCELLAZIONE per l'UD
     * in input
     * <p>
     * (NOTA: una richiesta di annullamento versamento può essere evasa solo una volta, per questo
     * il conteggio non può essere superiore a 1)
     *
     * @param idStrut         id struttura
     * @param idRichAnnulVers idRichAnnulVers
     *
     * @return true/false
     */
    @Override
    public boolean existAroRichAnnulVersDaCancel(BigDecimal idStrut, BigDecimal idRichAnnulVers)
            throws AppGenericPersistenceException {
        String queryStr = "SELECT COUNT(u) FROM AroRichAnnulVers u "
                + "WHERE u.orgStrut.idStrut = :idStrut "
                + " AND u.tiAnnullamento = 'CANCELLAZIONE' "
                + "AND u.idRichAnnulVers = :idRichAnnulVers ";
        Query query = entityManager.createQuery(queryStr);
        query.setParameter(QUERY_PARAM_ID_STRUT, TypeConverter.longFromBigDecimal(idStrut));
        query.setParameter("idRichAnnulVers", idRichAnnulVers);
        Long numUd = (Long) query.getSingleResult();
        return numUd > 0;
    }

    /**
     * Controlla che esista la richiesta di restituzione archivio per l'UD in input
     * <p>
     * (NOTA: una richiesta di restituzione archivio può essere restituita solo una volta, per
     * questo il conteggio non può essere superiore a 1)
     *
     * @param idStrut        id struttura
     * @param idRichRestArch idRichRestArch
     *
     * @return true/false
     */
    @Override
    public boolean existAroRichRestArch(BigDecimal idStrut, BigDecimal idRichRestArch)
            throws AppGenericPersistenceException {
        String queryStr = "SELECT COUNT(u) FROM AroVRicRichRa u " + "WHERE u.idStrut = :idStrut "
                + "AND u.idRichiestaRa = :idRichRestArch ";
        Query query = entityManager.createQuery(queryStr);
        query.setParameter(QUERY_PARAM_ID_STRUT, idStrut);
        query.setParameter("idRichRestArch", idRichRestArch);
        Long numUd = (Long) query.getSingleResult();
        return numUd > 0;
    }

    /**
     * Restituisce il conteggio degli item di una richiesta di cancellazione logica
     *
     * @param idRichSoftDelete id della richiesta di cancellazione logica
     * @param tiStato          gli stati da considerare (se non passati vengono considerati tutti)
     *
     * @return il conteggio degli item
     */
    @Override
    public Long countAroItemRichSoftDelete(BigDecimal idRichSoftDelete, String tiItemRichSoftDelete,
            String... tiStato) {
        List<String> statiList = new ArrayList<>();
        if (tiStato.length > 0) {
            statiList = Arrays.asList(tiStato);
        }
        StringBuilder queryStr = new StringBuilder(
                "SELECT COUNT(i) FROM AroItemRichSoftDelete i WHERE i.aroRichSoftDelete.idRichSoftDelete = :idRichSoftDelete AND i.tiItemRichSoftDelete = :tiItemRichSoftDelete ");
        if (!statiList.isEmpty()) {
            if (statiList.size() == 1) {
                queryStr.append("AND i.tiStatoItem = :tiStatoItem ");
            } else {
                queryStr.append("AND i.tiStatoItem IN (:tiStatoItem) ");
            }
        }
        Query query = entityManager.createQuery(queryStr.toString());
        query.setParameter(QUERY_PARAM_ID_RICH_SOFT_DELETE,
                TypeConverter.longFromBigDecimal(idRichSoftDelete));
        query.setParameter("tiItemRichSoftDelete", tiItemRichSoftDelete);
        if (!statiList.isEmpty()) {
            if (statiList.size() == 1) {
                query.setParameter("tiStatoItem", statiList.get(0));
            } else {
                query.setParameter("tiStatoItem", statiList);
            }
        }
        return (Long) query.getSingleResult();
    }

    /**
     * Ricavo il progressivo più alto tra tutti gli stati di una determinata richiesta
     *
     * @param idRichSoftDelete l'id della richiesta di cui voglio conoscere il progressivo stato
     *                         maggiore
     *
     * @return il progressivo
     */
    @Override
    public BigDecimal getUltimoProgressivoStatoRichiesta(long idRichSoftDelete)
            throws AppGenericPersistenceException {
        Query q = entityManager.createQuery(
                "SELECT MAX(statoRichSoftDelete.pgStatoRichSoftDelete) FROM AroStatoRichSoftDelete statoRichSoftDelete "
                        + "WHERE statoRichSoftDelete.aroRichSoftDelete.idRichSoftDelete = :idRichSoftDelete ");
        q.setParameter(QUERY_PARAM_ID_RICH_SOFT_DELETE, idRichSoftDelete);
        return (BigDecimal) q.getSingleResult() != null ? (BigDecimal) q.getSingleResult()
                : BigDecimal.ZERO;
    }

    /**
     * Elimina tutti gli errori di un certo tipo sugli item della richiesta
     *
     * @param idRichSoftDelete    l'id della richiesta
     * @param tiErrRichSoftDelete il tipo di errore da eliminare
     */
    @Override
    public void deleteAroErrRichSoftDelete(long idRichSoftDelete, String... tiErrRichSoftDelete) {
        StringBuilder selectStr = new StringBuilder(
                "SELECT errRichSoftDelete FROM AroErrRichSoftDelete errRichSoftDelete "
                        + "WHERE errRichSoftDelete.aroItemRichSoftDelete.aroRichSoftDelete.idRichSoftDelete = :idRichSoftDelete ");
        if (tiErrRichSoftDelete != null) {
            if (tiErrRichSoftDelete.length > 1) {
                selectStr.append("AND errRichSoftDelete.tiErr IN (:tiErrRichSoftDelete) ");
            } else {
                selectStr.append("AND errRichSoftDelete.tiErr = :tiErrRichSoftDelete ");
            }
        }
        String deleteStr = "DELETE FROM AroErrRichSoftDelete e WHERE e IN (" + selectStr + ")";
        Query q = entityManager.createQuery(deleteStr);
        q.setParameter(QUERY_PARAM_ID_RICH_SOFT_DELETE, idRichSoftDelete);
        if (tiErrRichSoftDelete != null) {
            List<String> asList = Arrays.asList(tiErrRichSoftDelete);
            if (asList.size() > 1) {
                q.setParameter("tiErrRichSoftDelete", asList);
            } else {
                q.setParameter("tiErrRichSoftDelete", asList.get(0));
            }
        }
        q.executeUpdate();
    }

    @Override
    public AroXmlRichSoftDelete createAroXmlRichSoftDelete(AroRichSoftDelete richSoftDelete,
            String tiXmlRichSoftDelete, String blXmlRichSoftDelete, String cdVersioneXml)
            throws AppGenericPersistenceException {
        AroXmlRichSoftDelete xmlRichSoftDelete = new AroXmlRichSoftDelete();
        String message = String.format("Eseguo il salvataggio dell'xml %s cancellazione logica",
                tiXmlRichSoftDelete);
        log.info(message);
        xmlRichSoftDelete.setTiXmlRichSoftDelete(tiXmlRichSoftDelete);
        xmlRichSoftDelete.setBlXmlRichSoftDelete(blXmlRichSoftDelete);
        xmlRichSoftDelete.setCdVersioneXml(cdVersioneXml);
        richSoftDelete.addAroXmlRichSoftDelete(xmlRichSoftDelete);
        return xmlRichSoftDelete;
    }

    @Override
    public Stream<DmUdDel> streamUnitaDocDaRichiesta(Long idStrut, BigDecimal idRichiestaSacer)
            throws AppGenericPersistenceException {
        String queryStr = "SELECT dm FROM DmUdDel dm " + "JOIN dm.dmUdDelRichieste dr "
                + "WHERE dr.idRichiesta = :idRichiestaSacer "
                + "AND dm.orgStrut.idStrut = :idStrut";

        Query query = entityManager.createQuery(queryStr);
        query.setParameter(QUERY_PARAM_ID_RICHIESTA_SACER, idRichiestaSacer);
        query.setParameter(QUERY_PARAM_ID_STRUT, idStrut);
        query.setHint(HibernateHints.HINT_FETCH_SIZE, 100);

        return query.getResultStream();
    }

    /**
     * Conta le unità documentarie associate a una richiesta specifica
     */
    @Override
    public long countUnitaDocDaRichiesta(long idStrut, BigDecimal idRichiestaSacer)
            throws AppGenericPersistenceException {
        String jpql = "SELECT COUNT(dm) FROM DmUdDel dm " + "JOIN dm.dmUdDelRichieste dr "
                + "WHERE dr.idRichiesta = :idRichiestaSacer "
                + "AND dm.orgStrut.idStrut = :idStrut";

        return entityManager.createQuery(jpql, Long.class)
                .setParameter(QUERY_PARAM_ID_STRUT, idStrut)
                .setParameter(QUERY_PARAM_ID_RICHIESTA_SACER, idRichiestaSacer.longValue())
                .getSingleResult();
    }

}
