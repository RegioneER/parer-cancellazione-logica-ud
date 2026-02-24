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
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package it.eng.parer.soft.delete.beans.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.eng.parer.soft.delete.beans.exceptions.AppGenericPersistenceException;
import it.eng.parer.soft.delete.beans.utils.CostantiDB;
import it.eng.parer.soft.delete.jpa.entity.AroErrRichSoftDelete;
import it.eng.parer.soft.delete.jpa.entity.AroItemRichSoftDelete;
import it.eng.parer.soft.delete.jpa.entity.AroRichAnnulVers;
import it.eng.parer.soft.delete.jpa.entity.AroRichSoftDelete;
import it.eng.parer.soft.delete.jpa.entity.AroStatoRichSoftDelete;
import it.eng.parer.soft.delete.jpa.entity.AroUnitaDoc;
import it.eng.parer.soft.delete.jpa.entity.AroXmlRichSoftDelete;
import it.eng.parer.soft.delete.jpa.entity.DmUdDel;
import it.eng.parer.soft.delete.jpa.entity.IamUser;
import it.eng.parer.soft.delete.jpa.entity.OrgStrut;
import it.eng.parer.soft.delete.jpa.entity.AroRichiestaRa;
import it.eng.parer.ws.xml.richSoftDelete.RichiestaCancellazioneLogica;
import it.eng.parer.ws.xml.richSoftDelete.RichiestaDiCancellazioneType;
import it.eng.parer.ws.xml.richSoftDelete.TipoRichiestaType;
import it.eng.parer.soft.delete.beans.IRegistrazioneRichiesteService;
import it.eng.parer.soft.delete.beans.IRegistrazioneRichiesteDao;
import it.eng.parer.soft.delete.beans.dto.RichiestaSacerDto;
import it.eng.parer.soft.delete.beans.dto.UnitaDocDto;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;

@ApplicationScoped
public class RegistrazioneRichiesteService implements IRegistrazioneRichiesteService {
    //
    private static final String LOG_ADD_RICH_SOFT_DELETE_ERR = "Eccezione imprevista durante il salvataggio della richiesta di cancellazione logica da parte del WS: ";
    //
    private static final String UNITA_DOCUMENTARIA_STRING = "L'unit\u00E0 documentaria ";
    private static final String RICHIESTA_ANN_VRS_STRING = "La richiesta di annullamento versamento ";
    private static final String RICHIESTA_REST_ARCH_STRING = "La richiesta di restituzione archivio ";
    private static final String RICHIESTA_SCARTO_ARCH_STRING = "La richiesta di scarto archivistico ";
    private static final String STATO_CONSERVAZIONE_NON_VALIDO_STRING = " ha stato di conservazione pari a ";
    private static final String NON_ANNULLATA_STRING = " non \u00E8 annullata";
    private static final String NON_RESTITUITA_STRING = " non \u00E8 restituita";
    private static final String NON_ESISTE_STRING = " non esiste";
    private static final String NON_PUO_ESSERE_ELABORATA_STRING = " e, quindi, non pu\u00F2 essere elaborata";

    //
    private static final Logger log = LoggerFactory.getLogger(RegistrazioneRichiesteService.class);
    //
    IRegistrazioneRichiesteDao registrazioneRichiesteDao;

    EntityManager entityManager;

    @Inject
    public RegistrazioneRichiesteService(EntityManager entityManager,
            IRegistrazioneRichiesteDao registrazioneRichiesteDao) {
        this.entityManager = entityManager;
        this.registrazioneRichiesteDao = registrazioneRichiesteDao;
    }

    /**
     * Esegue il salvataggio in transazione del nuovo record di richiesta di cancellazione logica
     *
     * @param idUserIam             identificativo dell'utente IAM
     * @param idStrut               identificativo della struttura corrente
     * @param cdRichSoftDelete      codice della richiesta di cancellazione logica
     * @param dsRichSoftDelete      descrizione della richiesta di cancellazione logica
     * @param ntRichSoftDelete      nota associata alla richiesta di cancellazione logica
     * @param tiRichSoftDelete      tipo della richiesta di cancellazione logica
     * @param dtCreazione           data di creazione della richiesta
     * @param tiAnnulRichSoftDelete tipo di annullamento della richiesta
     * @param cancellazioneLogica   oggetto contenente i dettagli dell'invio della richiesta di
     *                              cancellazione logica
     *
     * @return entità AroRichSoftDelete rappresentante la richiesta salvata
     *
     * @throws AppGenericPersistenceException errore generico
     */
    @Override
    public AroRichSoftDelete insertRichSoftDelete(long idUserIam, Long idStrut,
            String dsRichSoftDelete, String ntRichSoftDelete, String tiRichSoftDelete,
            LocalDateTime dtCreazione, String tiCancelRichSoftDelete)
            throws AppGenericPersistenceException {
        log.info("Eseguo il salvataggio della richiesta di cancellazione logica");
        AroRichSoftDelete rich = new AroRichSoftDelete();
        try {
            OrgStrut strut = entityManager.find(OrgStrut.class, idStrut);

            // Inizializzo la richiesta creata dal WS
            rich = initAroRichSoftDelete(dsRichSoftDelete, ntRichSoftDelete, tiRichSoftDelete,
                    dtCreazione, CostantiDB.TipoCreazioneRichSoftDelete.ASINCRONA.name(),
                    tiCancelRichSoftDelete, strut);

            // Persisto la richiesta
            entityManager.persist(rich);
            entityManager.flush();

            log.info("Salvataggio della richiesta di cancellazione logica completato dal WS ");
        } catch (Exception ex) {
            throw new AppGenericPersistenceException(ex,
                    LOG_ADD_RICH_SOFT_DELETE_ERR + ExceptionUtils.getRootCauseMessage(ex));
        }
        return rich;
    }

    private AroRichSoftDelete initAroRichSoftDelete(String dsRichSoftDelete,
            String ntRichSoftDelete, String tiRichSoftDelete, LocalDateTime now, String tiCreazione,
            String tiCancelRichSoftDelete, OrgStrut strut) {
        AroRichSoftDelete rich = new AroRichSoftDelete();
        rich.setDsRichSoftDelete(dsRichSoftDelete);
        rich.setNtRichSoftDelete(ntRichSoftDelete);
        rich.setTiRichSoftDelete(tiRichSoftDelete);
        rich.setDtCreazioneRichSoftDelete(now);
        rich.setTiCreazioneRichSoftDelete(tiCreazione);
        rich.setOrgStrut(strut);
        rich.setTiModCancellazione(tiCancelRichSoftDelete);
        if (rich.getAroItemRichSoftDelete() == null) {
            rich.setAroItemRichSoftDelete(new ArrayList<>());
        }
        if (rich.getAroStatoRichSoftDelete() == null) {
            rich.setAroStatoRichSoftDelete(new ArrayList<>());
        }
        return rich;
    }

    /**
     * Elabora gli item della richiesta utilizzando JPA Stream e batch processing. Determina
     * automaticamente il tipo di richiesta e delega al metodo appropriato.
     *
     * NOTA: Il sistema garantisce che tutte le entry in richiesteDiCancellazione siano della stessa
     * tipologia. Il tipo viene determinato dalla prima entry e applicato a tutte le successive
     * senza ulteriori controlli.
     */
    @Override
    public void createItems(AroRichSoftDelete rich,
            RichiestaCancellazioneLogica.RichiesteDiCancellazione richiesteDiCancellazione,
            long idUserIam) throws AppGenericPersistenceException {

        log.info("Elaborazione item con stream per la richiesta {}", rich.getIdRichSoftDelete());

        try {
            // Determina il tipo di richiesta dalla prima entry
            RichiestaDiCancellazioneType primaRichiesta = richiesteDiCancellazione
                    .getRichiestaDiCancellazione().get(0);
            TipoRichiestaType tipoRichiesta = primaRichiesta.getTipoRichiesta();

            if (TipoRichiestaType.UNITA_DOCUMENTARIA.value().equals(tipoRichiesta.value())) {
                // Richiesta di tipo Unità Documentaria - gestione dedicata
                elaboraItemUnitaDocumentaria(rich, richiesteDiCancellazione, idUserIam);
            } else {
                // Tutte le altre tipologie: ANNULLAMENTO_VERSAMENTO, RESTITUZIONE_ARCHIVIO,
                // SCARTO_ARCHIVISTICO - gestione centralizzata con stream
                elaboraItemConStream(rich, richiesteDiCancellazione, idUserIam, tipoRichiesta);
            }

            log.info("Elaborazione item completata per la richiesta {}",
                    rich.getIdRichSoftDelete());
        } catch (Exception e) {
            throw new AppGenericPersistenceException(e, "Errore durante l'elaborazione degli item: "
                    + ExceptionUtils.getRootCauseMessage(e));
        }
    }

    /**
     * Elabora gli item di tipo Unità Documentaria
     */
    private void elaboraItemUnitaDocumentaria(AroRichSoftDelete rich,
            RichiestaCancellazioneLogica.RichiesteDiCancellazione richiesteDiCancellazione,
            long idUserIam) throws AppGenericPersistenceException {
        int progressivoItem = 1;
        log.info("Elaborazione item di tipo UNITA_DOCUMENTARIA per la richiesta {} - INIZIO",
                rich.getIdRichSoftDelete());
        Set<UnitaDocDto> udsInRich = new HashSet<>();

        // Ora elabora tutte le richieste di tipo UD
        for (RichiestaDiCancellazioneType richiestaDiCancellazione : richiesteDiCancellazione
                .getRichiestaDiCancellazione()) {
            entityManager.flush();
            entityManager.clear();

            BigDecimal idStrut = new BigDecimal(rich.getOrgStrut().getIdStrut());
            TipoRichiestaType tipoRichiesta = richiestaDiCancellazione.getTipoRichiesta();

            // Verifica che si tratti di una richiesta di tipo UD
            if (TipoRichiestaType.UNITA_DOCUMENTARIA.value().equals(tipoRichiesta.value())) {
                // Elaborazione delle unità documentarie dirette
                log.debug(" {} - elaborazione [{}-{}-{}-{}] - INIZIO", progressivoItem,
                        richiestaDiCancellazione.getTipoRichiesta(),
                        richiestaDiCancellazione.getTipoRegistro(),
                        richiestaDiCancellazione.getAnno(), richiestaDiCancellazione.getNumero());

                String registro = richiestaDiCancellazione.getTipoRegistro();
                long annoInt = richiestaDiCancellazione.getAnno();
                String numero = richiestaDiCancellazione.getNumero();
                BigDecimal anno = new BigDecimal(annoInt);

                if (udsInRich.add(new UnitaDocDto(idStrut, registro, anno, numero))) {
                    // Creo la AroItemRichSoftDelete figlia della
                    // AroRichSoftDelete
                    AroItemRichSoftDelete item = createAroItemRichSoftDeleteUd(rich, registro, anno,
                            numero, progressivoItem, CostantiDB.TiItemRichSoftDelete.UNI_DOC.name(),
                            CostantiDB.StatoItemRichSoftDelete.NON_ELABORABILE.name());

                    Long idUnitaDoc = registrazioneRichiesteDao.getIdUnitaDocVersataAnnul(idStrut,
                            registro, anno, numero);
                    log.debug("Controlli item da elaborare");
                    if (idUnitaDoc != null) {
                        // UD annullata esistente
                        AroUnitaDoc ud = entityManager.find(AroUnitaDoc.class, idUnitaDoc);
                        item.setAroUnitaDoc(ud);
                        // Già qui, controllo l'unità doc definita nella
                        // richiesta e che sto
                        // trattando
                        controlloItemDaElaborare(item, idUserIam);
                    } else {
                        // Se non esiste una ud annullata, allora controllo
                        // se esiste in generale
                        if (registrazioneRichiesteDao.existAroUnitaDoc(idStrut, registro, anno,
                                numero)) {
                            // UD esistente - creo record di errore
                            String dsErr = UNITA_DOCUMENTARIA_STRING + registro + "-" + annoInt
                                    + "-" + numero + NON_ANNULLATA_STRING;
                            createAroErrRichSoftDelete(item, BigDecimal.ONE,
                                    CostantiDB.TipoErrRichSoftDelete.ITEM_NON_ANNULLATO.name(),
                                    dsErr, CostantiDB.TipoGravitaErrore.ERRORE.name());
                        } else {
                            // UD non esistente - creo record di errore
                            String dsErr = UNITA_DOCUMENTARIA_STRING + registro + "-" + annoInt
                                    + "-" + numero + NON_ESISTE_STRING;
                            createAroErrRichSoftDelete(item, BigDecimal.ONE,
                                    CostantiDB.TipoErrRichSoftDelete.ITEM_NON_ESISTE.name(), dsErr,
                                    CostantiDB.TipoGravitaErrore.ERRORE.name());
                        }
                    }
                    progressivoItem++;
                } else {
                    // UD già presente nella richiesta
                    AroItemRichSoftDelete item = createAroItemRichSoftDeleteUd(rich, registro, anno,
                            numero, progressivoItem, CostantiDB.TiItemRichSoftDelete.UNI_DOC.name(),
                            CostantiDB.StatoItemRichSoftDelete.NON_ELABORABILE.name());
                    String dsErr = UNITA_DOCUMENTARIA_STRING + registro + "-" + annoInt + "-"
                            + numero
                            + " \u00E8 gi\u00E0 presente nella richiesta di cancellazione logica";
                    createAroErrRichSoftDelete(item, BigDecimal.ONE,
                            CostantiDB.TipoErrRichSoftDelete.ITEM_GIA_PRESENTE.name(), dsErr,
                            CostantiDB.TipoGravitaErrore.ERRORE.name());
                    progressivoItem++;
                }

                log.debug(" {} - elaborazione [{}-{}-{}-{}] - FINE", progressivoItem,
                        richiestaDiCancellazione.getTipoRichiesta(),
                        richiestaDiCancellazione.getTipoRegistro(),
                        richiestaDiCancellazione.getAnno(), richiestaDiCancellazione.getNumero());
            }
        }

        log.info("Elaborazione item di tipo UNITA_DOCUMENTARIA per la richiesta {} - FINE",
                rich.getIdRichSoftDelete());
    }

    /**
     * Elabora gli item con stream per le tipologie che richiedono elaborazione parallela. Supporta:
     * ANNULLAMENTO_VERSAMENTO, RESTITUZIONE_ARCHIVIO, SCARTO_ARCHIVISTICO. Utilizza JPA Stream e
     * batch processing con transazioni separate per ogni split.
     *
     * NOTA: Questo metodo assume che tutte le richieste nel batch siano della stessa tipologia.
     */
    private void elaboraItemConStream(AroRichSoftDelete rich,
            RichiestaCancellazioneLogica.RichiesteDiCancellazione richiesteDiCancellazione,
            long idUserIam, TipoRichiestaType tipoRichiesta) throws AppGenericPersistenceException {

        int progressivoItem = 1;
        String tipoRichiestaStr = tipoRichiesta.value();
        log.info("Elaborazione item di tipo {} per la richiesta {} - INIZIO", tipoRichiestaStr,
                rich.getIdRichSoftDelete());

        // Set per tracciare le richieste Sacer già elaborate (evita duplicati)
        Set<RichiestaSacerDto> idRichSacerInRich = new HashSet<>();

        // Elabora tutte le richieste del tipo specificato
        for (RichiestaDiCancellazioneType richiestaDiCancellazione : richiesteDiCancellazione
                .getRichiestaDiCancellazione()) {
            // Pulizia del contesto di persistenza per evitare accumulo di entità in memoria
            entityManager.flush();
            entityManager.clear();

            BigDecimal idStrut = new BigDecimal(rich.getOrgStrut().getIdStrut());

            // Log elaborazione singola richiesta
            log.debug(" {} - elaborazione [{}-{}] - INIZIO", progressivoItem,
                    richiestaDiCancellazione.getTipoRichiesta(),
                    richiestaDiCancellazione.getIDRichiestaSacer());

            BigDecimal idRichiestaSacer = new BigDecimal(
                    richiestaDiCancellazione.getIDRichiestaSacer().longValue());

            // Verifica se la richiesta Sacer non è già presente nella richiesta di cancellazione
            if (idRichSacerInRich.add(new RichiestaSacerDto(idStrut, idRichiestaSacer))) {

                // Determina il tipo di item in base alla tipologia di richiesta
                String tiItemRichSoftDelete = getTipoItemDaTipoRichiesta(tipoRichiesta);

                // Crea item padre per la richiesta Sacer
                // Lo stato iniziale è NON_ELABORABILE, verrà modificato se i controlli passano
                AroItemRichSoftDelete itemPadre = createAroItemRichSoftDeletePadre(
                        rich.getIdRichSoftDelete(), idRichiestaSacer, progressivoItem,
                        tiItemRichSoftDelete,
                        CostantiDB.StatoItemRichSoftDelete.NON_ELABORABILE.name(), idUserIam,
                        tipoRichiesta);

                // Se l'item padre ha superato i controlli ed è DA_ELABORARE,
                // procedi con l'elaborazione parallela delle UD associate
                if (itemPadre.getTiStatoItem()
                        .equals(CostantiDB.StatoItemRichSoftDelete.DA_ELABORARE.name())) {

                    // Parametri per l'elaborazione parallela
                    final long richId = rich.getIdRichSoftDelete();
                    final long itemPadreId = itemPadre.getIdItemRichSoftDelete();
                    final AtomicInteger progressivoItemChild = new AtomicInteger(1);

                    log.info("Inizio elaborazione parallela per {} con ID richiesta {}",
                            tipoRichiestaStr, idRichiestaSacer);

                    // Utilizzo stream JPA per recuperare le UD in modo efficiente
                    // senza materializzarle tutte in memoria
                    try (Stream<DmUdDel> udStream = registrazioneRichiesteDao
                            .streamUnitaDocDaRichiesta(idStrut.longValue(), idRichiestaSacer)) {

                        // Prepara il processor per la parallelizzazione con Spliterator
                        CustomSplitProcessor processor = new CustomSplitProcessor(richId,
                                itemPadreId, progressivoItemChild, idUserIam, tipoRichiesta);

                        // Recupera il conteggio esatto prima di avviare lo streaming
                        // Questo permette una divisione ottimale in batch
                        long exactCount = registrazioneRichiesteDao
                                .countUnitaDocDaRichiesta(idStrut.longValue(), idRichiestaSacer);
                        log.info("Dimensione esatta dello stream per {}: {} elementi",
                                tipoRichiestaStr, exactCount);

                        // Esegue l'elaborazione parallela con Spliterator e dimensione nota
                        processor.processSplits(udStream, exactCount);
                    }

                    log.info(
                            "Elaborazione parallela {} completata: {} unità documentarie elaborate",
                            tipoRichiestaStr, progressivoItemChild.get() - 1);
                }

                progressivoItem++;
            } else {
                // Richiesta Sacer già presente nella richiesta di cancellazione logica
                AroItemRichSoftDelete item = createAroItemRichSoftDeletePadre(
                        rich.getIdRichSoftDelete(), idRichiestaSacer, progressivoItem,
                        getTipoItemDaTipoRichiesta(tipoRichiesta),
                        CostantiDB.StatoItemRichSoftDelete.NON_ELABORABILE.name(), idUserIam,
                        tipoRichiesta);

                // Crea record di errore per duplicato
                String dsErr = getDescrizioneRichiesta(tipoRichiesta) + idRichiestaSacer
                        + " \u00E8 gi\u00E0 presente nella richiesta di cancellazione logica";
                createAroErrRichSoftDelete(item, BigDecimal.ONE,
                        CostantiDB.TipoErrRichSoftDelete.ITEM_GIA_PRESENTE.name(), dsErr,
                        CostantiDB.TipoGravitaErrore.ERRORE.name());
                progressivoItem++;
            }

            log.debug(" {} - elaborazione [{}-{}] - FINE", progressivoItem,
                    richiestaDiCancellazione.getTipoRichiesta(),
                    richiestaDiCancellazione.getIDRichiestaSacer());
        }

        log.info("Elaborazione item di tipo {} per la richiesta {} - FINE", tipoRichiestaStr,
                rich.getIdRichSoftDelete());
    }

    /**
     * Mappa il tipo di richiesta al corrispondente tipo di item da persistere.
     *
     * @param tipoRichiesta tipo della richiesta da mappare
     * @return la stringa corrispondente al tipo item per la persistenza
     * @throws IllegalArgumentException se il tipo richiesta non è supportato
     */
    private String getTipoItemDaTipoRichiesta(TipoRichiestaType tipoRichiesta) {
        return switch (tipoRichiesta) {
        case ANNULLAMENTO_VERSAMENTO -> CostantiDB.TiItemRichSoftDelete.ANNUL_VERS.name();
        case RESTITUZIONE_ARCHIVIO -> CostantiDB.TiItemRichSoftDelete.REST_ARCH.name();
        case SCARTO_ARCHIVISTICO -> CostantiDB.TiItemRichSoftDelete.SCARTO_ARCH.name();
        default -> throw new IllegalArgumentException(
                "Tipo richiesta non supportato per stream processing: " + tipoRichiesta);
        };
    }

    /**
     * Restituisce la descrizione testuale del tipo di richiesta per i messaggi di errore.
     *
     * @param tipoRichiesta tipo della richiesta
     * @return la stringa descrittiva da utilizzare nei messaggi
     */
    private String getDescrizioneRichiesta(TipoRichiestaType tipoRichiesta) {
        return switch (tipoRichiesta) {
        case ANNULLAMENTO_VERSAMENTO -> RICHIESTA_ANN_VRS_STRING;
        case RESTITUZIONE_ARCHIVIO -> RICHIESTA_REST_ARCH_STRING;
        case SCARTO_ARCHIVISTICO -> RICHIESTA_SCARTO_ARCH_STRING;
        default -> "La richiesta ";
        };
    }

    /**
     * Classe che gestisce il processamento parallelo usando lo Spliterator nativo, con una
     * transazione separata per ogni split. Supporta diverse tipologie di richieste mantenendo la
     * stessa logica di parallelizzazione.
     */
    private class CustomSplitProcessor {
        private final long richId;
        private final long itemPadreId;
        private final AtomicInteger progressivoItemChild;
        private final long idUserIam;
        private final TipoRichiestaType tipoRichiesta;
        private final AtomicInteger splitCount = new AtomicInteger(0);

        public CustomSplitProcessor(long richId, long itemPadreId,
                AtomicInteger progressivoItemChild, long idUserIam,
                TipoRichiestaType tipoRichiesta) {
            this.richId = richId;
            this.itemPadreId = itemPadreId;
            this.progressivoItemChild = progressivoItemChild;
            this.idUserIam = idUserIam;
            this.tipoRichiesta = tipoRichiesta;
        }

        public void processSplits(Stream<DmUdDel> stream, long exactSize)
                throws AppGenericPersistenceException {
            log.info("Avvio elaborazione {} con dimensione nota: {} elementi",
                    tipoRichiesta.value(), exactSize);

            // Ottiene lo spliterator di base dallo stream JPA
            Spliterator<DmUdDel> baseSpliterator = stream.spliterator();

            // Crea uno Spliterator con dimensione nota usando il metodo createSizedSpliterator
            // Questo è necessario perché lo spliterator JPA potrebbe non avere caratteristica SIZED
            Spliterator<DmUdDel> sizedSpliterator = createSizedSpliterator(baseSpliterator,
                    exactSize);

            // Divide basandosi sulla dimensione minima del batch (non sul numero di split)
            // Questo garantisce batch di dimensioni ottimali per le performance
            List<Spliterator<DmUdDel>> splits = splitByBatchSize(sizedSpliterator);

            log.info("Stream {} suddiviso in {} split", tipoRichiesta.value(), splits.size());

            // Per memorizzare la prima eccezione che si verifica durante l'elaborazione parallela
            AtomicReference<Exception> firstException = new AtomicReference<>();

            // Flag per segnalare interruzione agli altri thread in caso di errore
            AtomicBoolean interrupted = new AtomicBoolean(false);

            // Processa ogni split in parallelo usando parallel stream
            splits.parallelStream().forEach(split -> {
                // Non elaborare se è già stata segnalata un'interruzione
                if (interrupted.get()) {
                    return;
                }

                try {
                    // Crea uno stream dal singolo split
                    Stream<DmUdDel> splitStream = StreamSupport.stream(split, false);

                    // Raccoglie gli elementi di questo split in una lista
                    // (materializza solo questo split, non tutto lo stream)
                    List<DmUdDel> udBatch = splitStream.toList();

                    // Elabora l'intero batch in una singola transazione
                    int splitNum = splitCount.incrementAndGet();
                    log.info("Inizio elaborazione split {} di tipo {} con {} elementi", splitNum,
                            tipoRichiesta.value(), udBatch.size());

                    // Delega l'elaborazione del batch al metodo transazionale
                    processaBatchInNuovaTransazione(udBatch, richId, itemPadreId,
                            progressivoItemChild, idUserIam, splitNum, tipoRichiesta);
                } catch (Exception e) {
                    // Registra la prima eccezione che si verifica
                    firstException.compareAndSet(null, e);

                    // Segnala agli altri thread di interrompersi
                    interrupted.set(true);

                    log.error(
                            "Errore grave durante l'elaborazione {} - interrompo l'intera elaborazione: {}",
                            tipoRichiesta.value(), e.getMessage(), e);
                }
            });

            // Al termine dell'elaborazione parallela, verifica se si è verificata
            // un'eccezione
            if (firstException.get() != null) {
                throw new AppGenericPersistenceException(firstException.get(),
                        "Elaborazione " + tipoRichiesta.value() + " interrotta a causa di errore: "
                                + ExceptionUtils.getRootCauseMessage(firstException.get()));
            }
        }

        /**
         * Crea uno Spliterator con dimensione nota e caratteristica SIZED.
         */
        private <T> Spliterator<T> createSizedSpliterator(Spliterator<T> original, long exactSize) {
            return new Spliterator<T>() {
                private long remaining = exactSize;
                private final Spliterator<T> delegate = original;

                @Override
                public boolean tryAdvance(Consumer<? super T> action) {
                    if (remaining > 0) {
                        boolean advanced = delegate.tryAdvance(action);
                        if (advanced) {
                            remaining--;
                        }
                        return advanced;
                    }
                    return false;
                }

                @Override
                public Spliterator<T> trySplit() {
                    // Disabilita completamente la divisione a questo livello
                    // FixedBatchSpliterator gestirà tutta la logica di divisione
                    return null;
                }

                @Override
                public long estimateSize() {
                    return remaining;
                }

                @Override
                public int characteristics() {
                    return delegate.characteristics() | Spliterator.SIZED | Spliterator.SUBSIZED;
                }
            };
        }

        /**
         * Divide lo spliterator in parti basandosi sulla dimensione minima desiderata del batch.
         */
        private List<Spliterator<DmUdDel>> splitByBatchSize(Spliterator<DmUdDel> spliterator) {
            // Otteniamo la dimensione esatta dallo spliterator con SIZED
            long exactSize = spliterator.getExactSizeIfKnown();
            if (exactSize == Long.MAX_VALUE) {
                throw new IllegalArgumentException(
                        "Lo spliterator deve avere una dimensione esatta nota");
            }

            // Determina la dimensione minima per batch in base alla dimensione totale
            final int MIN_ELEMENTS_PER_BATCH;
            if (exactSize < 5000) {
                MIN_ELEMENTS_PER_BATCH = 25; // Per dataset piccoli
            } else if (exactSize < 10000) {
                MIN_ELEMENTS_PER_BATCH = 50; // Per dataset medi
            } else if (exactSize < 100000) {
                MIN_ELEMENTS_PER_BATCH = 100; // Per dataset grandi
            } else if (exactSize < 500000) {
                MIN_ELEMENTS_PER_BATCH = 250; // Per dataset molto grandi
            } else if (exactSize < 1000000) {
                MIN_ELEMENTS_PER_BATCH = 500; // Per dataset enormi
            } else {
                MIN_ELEMENTS_PER_BATCH = 1000; // Per dataset giganteschi (1M+)
            }

            log.info("Divisione spliterator: dimensione={}, min_elements_per_batch={}", exactSize,
                    MIN_ELEMENTS_PER_BATCH);

            // Crea un FixedBatchSpliterator che suddivide in batch della dimensione specificata
            FixedBatchSpliterator<DmUdDel> batchSpliterator = new FixedBatchSpliterator<>(
                    spliterator, MIN_ELEMENTS_PER_BATCH);

            List<Spliterator<DmUdDel>> splits = new ArrayList<>();
            // Continua a dividere il batchSpliterator finché possibile
            Spliterator<DmUdDel> split;
            while ((split = batchSpliterator.trySplit()) != null) {
                splits.add(split);
            }

            // Aggiungi l'ultimo spliterator rimanente (se contiene elementi)
            if (batchSpliterator.estimateSize() > 0) {
                splits.add(batchSpliterator);
            }

            log.info(
                    "Divisione completata: creati {} batch, dimensione media: {} elementi per batch",
                    splits.size(), exactSize / Math.max(1, splits.size()));

            return splits;
        }
    }

    /**
     * Implementazione personalizzata di Spliterator che divide il flusso in batch di dimensione
     * fissa.
     */
    private static class FixedBatchSpliterator<T> implements Spliterator<T> {
        private final Spliterator<T> source;
        private final int batchSize;
        private long estimatedSize;

        public FixedBatchSpliterator(Spliterator<T> source, int batchSize) {
            this.source = source;
            this.batchSize = batchSize;
            // Usa la dimensione stimata dello spliterator sorgente
            this.estimatedSize = source.estimateSize();
        }

        @Override
        public boolean tryAdvance(Consumer<? super T> action) {
            return source.tryAdvance(action);
        }

        @Override
        public Spliterator<T> trySplit() {
            // Holder per memorizzare temporaneamente un elemento
            class Holder<E> implements Consumer<E> {
                E value;

                @Override
                public void accept(E e) {
                    value = e;
                }
            }

            // Controlla se ci sono elementi da processare
            Holder<T> holder = new Holder<>();
            if (!tryAdvance(holder)) {
                return null; // Nessun elemento rimasto
            }

            // Crea un array per contenere il batch
            Object[] batch = new Object[batchSize];
            int pos = 0;
            batch[pos++] = holder.value; // Inserisci il primo elemento già letto

            // Riempi il batch fino alla dimensione specificata o alla fine dello stream
            while (pos < batchSize && source.tryAdvance(holder)) {
                batch[pos++] = holder.value;
            }

            // Aggiorna la dimensione stimata
            if (estimatedSize != Long.MAX_VALUE) {
                estimatedSize -= pos;
            }

            // Crea un nuovo Spliterator con gli elementi raccolti
            return Spliterators.spliterator(batch, 0, pos, characteristics());
        }

        @Override
        public long estimateSize() {
            return estimatedSize;
        }

        @Override
        public int characteristics() {
            // Mantiene le caratteristiche originali ma aggiunge SIZED e SUBSIZED se
            // possibile
            int characteristics = source.characteristics();
            if ((characteristics & SIZED) != 0) {
                characteristics |= SUBSIZED;
            }
            return characteristics;
        }

        @Override
        public Comparator<? super T> getComparator() {
            return source.getComparator();
        }
    }

    /**
     * Processa un batch di unità documentarie in una transazione separata. Ogni split dello stream
     * verrà elaborato in una propria transazione.
     */
    @Transactional(value = TxType.REQUIRES_NEW)
    public void processaBatchInNuovaTransazione(List<DmUdDel> batch, long richId, long itemPadreId,
            AtomicInteger progressivoItemChild, long idUserIam, int splitNumber,
            TipoRichiestaType tipoRichiesta) throws AppGenericPersistenceException {

        log.info("Split {}: Thread {}: Inizio elaborazione batch {} di {} unità documentarie",
                splitNumber, Thread.currentThread().threadId(), tipoRichiesta.value(),
                batch.size());

        try {
            // Carica entità fresche per questa transazione
            AroRichSoftDelete rich = entityManager.find(AroRichSoftDelete.class, richId);
            AroItemRichSoftDelete itemPadre = entityManager.find(AroItemRichSoftDelete.class,
                    itemPadreId);

            // Verifica se itemPadre è nullo e interrompi l'elaborazione in tal caso
            if (itemPadre == null) {
                log.error(
                        "Split {}: Thread {}: Impossibile elaborare il batch {} - itemPadre con ID {} non trovato",
                        splitNumber, Thread.currentThread().threadId(), tipoRichiesta.value(),
                        itemPadreId);
                throw new IllegalArgumentException(
                        "Impossibile elaborare il batch " + tipoRichiesta.value()
                                + ": elemento padre non trovato (ID: " + itemPadreId + ")");
            }

            // Conta gli elementi elaborati in questo batch
            int processed = 0;

            // Elabora tutte le UD in questo batch in sequenza
            for (DmUdDel udDel : batch) {
                // Ottiene un progressivo atomico
                int progressivo = progressivoItemChild.getAndIncrement();

                // Crea un item per questa UD associata alla richiesta
                AroItemRichSoftDelete item = createAroItemRichSoftDeleteChild(rich,
                        udDel.getCdRegistroKeyUnitaDoc(), udDel.getAaKeyUnitaDoc(),
                        udDel.getCdKeyUnitaDoc(), itemPadre, progressivo,
                        CostantiDB.TiItemRichSoftDelete.UNI_DOC.name(),
                        CostantiDB.StatoItemRichSoftDelete.NON_ELABORABILE.name());

                // Recupera l'unità documentaria
                AroUnitaDoc unitaDoc = udDel.getAroUnitaDoc();

                if (unitaDoc != null) {
                    // Verifica stato conservazione in base alla tipologia
                    boolean statoConservazioneValido = verificaStatoConservazionePerTipologia(
                            unitaDoc.getTiStatoConservazione(), tipoRichiesta);

                    if (statoConservazioneValido) {
                        // UD con stato conservazione valido per la tipologia
                        item.setAroUnitaDoc(unitaDoc);

                        try {
                            // Controlla l'item passando la tipologia
                            controlloItemDaElaborare(item, idUserIam, tipoRichiesta);
                        } catch (Exception e) {
                            log.error("Errore nel controllo dell'item {}: {}",
                                    tipoRichiesta.value(), e.getMessage(), e);
                        }
                    } else {
                        // UD esistente ma con stato conservazione non valido per la tipologia
                        String dsErr = UNITA_DOCUMENTARIA_STRING
                                + unitaDoc.getCdRegistroKeyUnitaDoc() + "-"
                                + unitaDoc.getAaKeyUnitaDoc() + "-" + unitaDoc.getCdKeyUnitaDoc()
                                + STATO_CONSERVAZIONE_NON_VALIDO_STRING
                                + unitaDoc.getTiStatoConservazione() + " non valido per "
                                + tipoRichiesta.value();
                        createAroErrRichSoftDelete(item, BigDecimal.ONE,
                                CostantiDB.TipoErrRichSoftDelete.STATO_CONSERV_NON_AMMESSO.name(),
                                dsErr, CostantiDB.TipoGravitaErrore.ERRORE.name());
                    }
                } else {
                    // UD non esistente
                    String dsErr = UNITA_DOCUMENTARIA_STRING + udDel.getCdRegistroKeyUnitaDoc()
                            + "-" + udDel.getAaKeyUnitaDoc() + "-" + udDel.getCdKeyUnitaDoc()
                            + NON_ESISTE_STRING;
                    createAroErrRichSoftDelete(item, BigDecimal.ONE,
                            CostantiDB.TipoErrRichSoftDelete.ITEM_NON_ESISTE.name(), dsErr,
                            CostantiDB.TipoGravitaErrore.ERRORE.name());
                }

                // Incrementa il conteggio locale
                processed++;

                // Flush periodico per non accumulare troppi oggetti in memoria
                if (processed % 50 == 0) {
                    entityManager.flush();
                    log.debug("Split {} ({}): Elaborati {} elementi su {}", splitNumber,
                            tipoRichiesta.value(), processed, batch.size());
                }
            }

            // Flush finale per questo batch
            entityManager.flush();
            log.info(
                    "Split {}: Thread {}: Completata elaborazione batch {} di {} unità documentarie",
                    splitNumber, Thread.currentThread().threadId(), tipoRichiesta.value(),
                    processed);

        } catch (Exception e) {
            throw new AppGenericPersistenceException(e,
                    "Split " + splitNumber + " (" + tipoRichiesta.value()
                            + "): Errore durante l'elaborazione del batch: "
                            + ExceptionUtils.getRootCauseMessage(e));
        }
    }

    /**
     * Verifica se lo stato di conservazione è valido per la tipologia di richiesta specificata.
     * Ogni tipologia richiede stati di conservazione specifici.
     */
    private boolean verificaStatoConservazionePerTipologia(String statoConservazione,
            TipoRichiestaType tipoRichiesta) {

        return switch (tipoRichiesta) {
        case ANNULLAMENTO_VERSAMENTO ->
            // Per annullamento deve essere ANNULLATA
            CostantiDB.StatoConservazioneUnitaDoc.ANNULLATA.name().equals(statoConservazione);

        case RESTITUZIONE_ARCHIVIO ->
            // Per restituzione archivio deve essere AIP_FIRMATO o IN_ARCHIVIO o ANNULLATA
            CostantiDB.StatoConservazioneUnitaDoc.AIP_FIRMATO.name().equals(statoConservazione)
                    || CostantiDB.StatoConservazioneUnitaDoc.IN_ARCHIVIO.name()
                            .equals(statoConservazione)
                    || CostantiDB.StatoConservazioneUnitaDoc.ANNULLATA.name()
                            .equals(statoConservazione);

        case SCARTO_ARCHIVISTICO -> {
            // TODO: Definire gli stati validi per SCARTO_ARCHIVISTICO
            // Placeholder: accetta qualsiasi stato per ora
            log.debug("Verifica stato conservazione per SCARTO_ARCHIVISTICO da implementare");
            yield true;
        }

        default -> {
            log.warn("Tipologia sconosciuta: {}", tipoRichiesta);
            yield false;
        }
        };
    }

    @Transactional
    public AroItemRichSoftDelete createAroItemRichSoftDeleteUd(AroRichSoftDelete rich,
            String registro, BigDecimal anno, String numero, int progressivo,
            String tiItemRichSoftDelete, String tiStatoItem) {
        return createAroItemRichSoftDelete(rich, registro, anno, numero, null, null, progressivo,
                tiItemRichSoftDelete, tiStatoItem);
    }

    /**
     * Crea l'item padre per una richiesta Sacer, applicando i controlli specifici in base alla
     * tipologia di richiesta.
     */
    @Transactional(value = TxType.REQUIRES_NEW)
    public AroItemRichSoftDelete createAroItemRichSoftDeletePadre(Long idRichSoftDelete,
            BigDecimal idRichiestaSacer, int progressivo, String tiItemRichSoftDelete,
            String tiStatoItem, Long idUserIam, TipoRichiestaType tipoRichiesta)
            throws AppGenericPersistenceException {

        // Recupera una versione fresh dell'entità
        AroRichSoftDelete rich = entityManager.find(AroRichSoftDelete.class, idRichSoftDelete);

        BigDecimal idStrut = new BigDecimal(rich.getOrgStrut().getIdStrut());

        // Crea item padre del tipo specificato (ANNUL_VERS, REST_ARCH, SCARTO_ARCH)
        AroItemRichSoftDelete itemPadre = createAroItemRichSoftDelete(rich, null, null, null,
                idRichiestaSacer, null, progressivo, tiItemRichSoftDelete, tiStatoItem);

        // Applica la logica specifica in base alla tipologia di richiesta
        switch (tipoRichiesta) {
        case ANNULLAMENTO_VERSAMENTO -> gestisciItemPadreAnnullamentoVersamento(itemPadre, idStrut,
                idRichiestaSacer, idUserIam);
        case RESTITUZIONE_ARCHIVIO ->
            gestisciItemPadreRestituzioneArchivio(itemPadre, idStrut, idRichiestaSacer, idUserIam);
        case SCARTO_ARCHIVISTICO ->
            gestisciItemPadreScartoArchivistico(itemPadre, idStrut, idRichiestaSacer, idUserIam);
        default -> throw new IllegalArgumentException(
                "Tipologia non supportata per item padre: " + tipoRichiesta);
        }

        return itemPadre;
    }

    /**
     * Gestisce la logica specifica per item padre di tipo ANNULLAMENTO_VERSAMENTO. Verifica
     * l'esistenza della richiesta di annullamento versamento e ne controlla lo stato.
     */
    private void gestisciItemPadreAnnullamentoVersamento(AroItemRichSoftDelete itemPadre,
            BigDecimal idStrut, BigDecimal idRichiestaSacer, Long idUserIam)
            throws AppGenericPersistenceException {

        // Recupera l'ID della richiesta di annullamento versamento dell'UD
        Long idRichAnnulVers = registrazioneRichiesteDao.getIdRichAnnulVersEvasaDaCancel(idStrut,
                idRichiestaSacer);

        if (idRichAnnulVers != null) {
            // Richiesta di annullamento versamento con stato EVASA di tipo CANCELLAZIONE esistente
            AroRichAnnulVers richAnnVrs = entityManager.find(AroRichAnnulVers.class,
                    idRichAnnulVers);
            itemPadre.setAroRichAnnulVers(richAnnVrs);

            // Già qui, controllo l'annullamento definito nella richiesta e che sto trattando
            controlloItemDaElaborare(itemPadre, idUserIam,
                    TipoRichiestaType.ANNULLAMENTO_VERSAMENTO);
        } else {
            // Se non esiste una richiesta di annullamento versamento di tipo
            // CANCELLAZIONE con
            // stato EVASA, allora controllo se esiste in generale
            if (registrazioneRichiesteDao.existAroRichAnnulVersDaCancel(idStrut,
                    idRichiestaSacer)) {
                // Richiesta di annullamento versamento esistente - creo record di errore
                String dsErr = RICHIESTA_ANN_VRS_STRING + idRichiestaSacer
                        + " di tipo CANCELLAZIONE \u00E8 in corso di annullamento";
                createAroErrRichSoftDelete(itemPadre, BigDecimal.ONE,
                        CostantiDB.TipoErrRichSoftDelete.ITEM_IN_CORSO_DI_ANNUL.name(), dsErr,
                        CostantiDB.TipoGravitaErrore.ERRORE.name());
            } else {
                // Richiesta di annullamento versamento di tipo CANCELLAZIONE non
                // esistente -
                // creo record di errore
                String dsErr = RICHIESTA_ANN_VRS_STRING + idRichiestaSacer
                        + " di tipo CANCELLAZIONE non esiste";
                createAroErrRichSoftDelete(itemPadre, BigDecimal.ONE,
                        CostantiDB.TipoErrRichSoftDelete.ITEM_NON_ESISTE.name(), dsErr,
                        CostantiDB.TipoGravitaErrore.ERRORE.name());
            }
        }
    }

    /**
     * Gestisce la logica specifica per item padre di tipo RESTITUZIONE_ARCHIVIO. Verifica
     * l'esistenza della richiesta di restituzione archivio e ne controlla lo stato.
     */
    private void gestisciItemPadreRestituzioneArchivio(AroItemRichSoftDelete itemPadre,
            BigDecimal idStrut, BigDecimal idRichiestaSacer, Long idUserIam)
            throws AppGenericPersistenceException {

        // Recupera l'ID della richiesta di restituzione archivio dell'UD
        BigDecimal idRichRestArch = registrazioneRichiesteDao.getIdRichRestArchRestituita(idStrut,
                idRichiestaSacer);

        if (idRichRestArch != null) {
            // Richiesta di restituzione archivio con stato RESTITUITO esistente
            AroRichiestaRa richRa = entityManager.find(AroRichiestaRa.class,
                    idRichRestArch.longValue());
            itemPadre.setAroRichiestaRa(richRa);

            // Già qui, controllo la restituzione definita nella richiesta e che sto trattando
            controlloItemDaElaborare(itemPadre, idUserIam, TipoRichiestaType.RESTITUZIONE_ARCHIVIO);
        } else {
            // Se non esiste una richiesta di restituzione archivio con
            // stato RESTITUITO, allora controllo se esiste in generale
            if (registrazioneRichiesteDao.existAroRichRestArch(idStrut, idRichiestaSacer)) {
                // Richiesta di restituzione archivio esistente - creo record di errore
                String dsErr = RICHIESTA_REST_ARCH_STRING + idRichiestaSacer
                        + " \u00E8 in corso di restituzione";
                createAroErrRichSoftDelete(itemPadre, BigDecimal.ONE,
                        CostantiDB.TipoErrRichSoftDelete.ITEM_IN_CORSO_DI_REST_ARCH.name(), dsErr,
                        CostantiDB.TipoGravitaErrore.ERRORE.name());
            } else {
                // Richiesta di restituzione archivio non esistente -
                // creo record di errore
                String dsErr = RICHIESTA_REST_ARCH_STRING + idRichiestaSacer + NON_ESISTE_STRING;
                createAroErrRichSoftDelete(itemPadre, BigDecimal.ONE,
                        CostantiDB.TipoErrRichSoftDelete.ITEM_NON_ESISTE.name(), dsErr,
                        CostantiDB.TipoGravitaErrore.ERRORE.name());
            }
        }
    }

    /**
     * Gestisce la logica specifica per item padre di tipo SCARTO_ARCHIVISTICO. TODO: Implementare
     * la logica specifica quando disponibile.
     */
    private void gestisciItemPadreScartoArchivistico(AroItemRichSoftDelete itemPadre,
            BigDecimal idStrut, BigDecimal idRichiestaSacer, Long idUserIam)
            throws AppGenericPersistenceException {

        // TODO: Implementare la logica specifica per SCARTO_ARCHIVISTICO
        // Al momento utilizziamo una logica placeholder simile ad ANNULLAMENTO_VERSAMENTO
        log.warn(
                "Logica per SCARTO_ARCHIVISTICO non ancora implementata completamente - richiesta: {}",
                idRichiestaSacer);

        // Placeholder - da sostituire con la logica effettiva quando sarà definita
        String dsErr = RICHIESTA_SCARTO_ARCH_STRING + idRichiestaSacer
                + " - funzionalità in fase di implementazione";
        createAroErrRichSoftDelete(itemPadre, BigDecimal.ONE,
                CostantiDB.TipoErrRichSoftDelete.ITEM_NON_ESISTE.name(), dsErr,
                CostantiDB.TipoGravitaErrore.WARNING.name());
    }

    @Transactional
    public AroItemRichSoftDelete createAroItemRichSoftDeleteChild(AroRichSoftDelete rich,
            String registro, BigDecimal anno, String numero, AroItemRichSoftDelete itemPadre,
            int progressivo, String tiItemRichSoftDelete, String tiStatoItem) {

        return createAroItemRichSoftDelete(rich, registro, anno, numero, null, itemPadre,
                progressivo, tiItemRichSoftDelete, tiStatoItem);
    }

    @Transactional
    public AroItemRichSoftDelete createAroItemRichSoftDelete(AroRichSoftDelete rich,
            String registro, BigDecimal anno, String numero, BigDecimal idRichiestaSacer,
            AroItemRichSoftDelete itemPadre, int progressivo, String tiItemRichSoftDelete,
            String tiStatoItem) {

        // Dovrei aver ottenuto tutti i campi necessari per creare il nuovo record
        AroItemRichSoftDelete item = new AroItemRichSoftDelete();
        item.setCdRegistroKeyUnitaDoc(registro);
        item.setAaKeyUnitaDoc(anno);
        item.setCdKeyUnitaDoc(numero);
        item.setIdRichiestaSacer(idRichiestaSacer);
        item.setIdStrut(new BigDecimal(rich.getOrgStrut().getIdStrut()));
        item.setPgItemRichSoftDelete(new BigDecimal(progressivo));
        item.setTiItemRichSoftDelete(tiItemRichSoftDelete);
        item.setTiStatoItem(tiStatoItem);
        if (item.getAroErrRichSoftDelete() == null) {
            item.setAroErrRichSoftDelete(new ArrayList<>());
        }
        item.setAroItemRichPadre(itemPadre);
        rich.addAroItemRichSoftDelete(item);
        entityManager.persist(item);
        return item;
    }

    /**
     * Controlli sull'item candidato alla cancellazione logica tenendo conto delle abilitazioni
     * dell'utente passato in ingresso. Versione che assume UNITA_DOCUMENTARIA come tipo di
     * richiesta.
     */
    private void controlloItemDaElaborare(AroItemRichSoftDelete item, Long idUserIam)
            throws AppGenericPersistenceException {
        // Delega alla versione con tipologia esplicita, assumendo UNITA_DOCUMENTARIA
        controlloItemDaElaborare(item, idUserIam, TipoRichiestaType.UNITA_DOCUMENTARIA);
    }

    /**
     * Controlli sull'item candidato alla cancellazione logica tenendo conto delle abilitazioni
     * dell'utente passato in ingresso e della tipologia di richiesta. I controlli comuni vengono
     * applicati a tutti i tipi, mentre quelli specifici vengono applicati solo alle tipologie
     * pertinenti.
     */
    private void controlloItemDaElaborare(AroItemRichSoftDelete item, Long idUserIam,
            TipoRichiestaType tipoRichiesta) throws AppGenericPersistenceException {

        int progressivoErr = 1;
        AroUnitaDoc ud = item.getAroUnitaDoc();
        AroRichAnnulVers richAnnVrs = item.getAroRichAnnulVers();
        AroRichiestaRa richRestArch = item.getAroRichiestaRa();

        // Se item di tipo UNI_DOC e se è definito l'identificatore dell'unità doc
        if (ud != null && item.getTiItemRichSoftDelete()
                .equals(CostantiDB.TiItemRichSoftDelete.UNI_DOC.name())) {

            // ============================================
            // CONTROLLI COMUNI PER UNITÀ DOCUMENTARIE
            // ============================================

            // Controllo se esiste già un'altra richiesta di cancellazione logica, diversa da quella
            // corrente, con stato PRESA_IN_CARICO o ACQUISITA che contiene quella unita doc da
            // elaborare
            progressivoErr = controlloRichiestaDuplicataPerUd(item, ud, progressivoErr);

            // ============================================
            // CONTROLLI SPECIFICI PER TIPOLOGIA
            // ============================================

            switch (tipoRichiesta) {
            case ANNULLAMENTO_VERSAMENTO -> {
                // Controlli specifici per annullamento versamento
                progressivoErr = controlliSpecificiAnnullamentoVersamento(item, ud, progressivoErr);
            }
            case RESTITUZIONE_ARCHIVIO -> {
                // Controlli specifici per restituzione archivio
                progressivoErr = controlliSpecificiRestituzioneArchivio(item, ud, progressivoErr);
            }
            case SCARTO_ARCHIVISTICO -> {
                // Controlli specifici per scarto archivistico
                progressivoErr = controlliSpecificiScartoArchivistico(item, ud, progressivoErr);
            }
            case UNITA_DOCUMENTARIA -> {
                // Per UNITA_DOCUMENTARIA vengono applicati controlli specifici
                progressivoErr = controlliSpecificiUnitaDocumentaria(item, ud, progressivoErr);
            }
            }

        } else if (richAnnVrs != null && item.getTiItemRichSoftDelete()
                .equals(CostantiDB.TiItemRichSoftDelete.ANNUL_VERS.name())) {

            // ============================================
            // CONTROLLI PER RICHIESTE DI ANNULLAMENTO
            // ============================================

            // Controllo se esiste già un'altra richiesta di cancellazione logica, diversa da quella
            // corrente, con stato PRESA_IN_CARICO o ACQUISITA che contiene quella richiesta di
            // annullamento versamento con stato EVASA di tipo CANCELLAZIONE da elaborare
            progressivoErr = controlloRichiestaDuplicataPerAnnullamento(item, richAnnVrs,
                    progressivoErr, tipoRichiesta);

        } else if (richRestArch != null && item.getTiItemRichSoftDelete()
                .equals(CostantiDB.TiItemRichSoftDelete.REST_ARCH.name())) {
            // ====================================================
            // CONTROLLI PER RICHIESTE DI RESTITUZIONE ARCHIVIO
            // ====================================================

            // Controllo se esiste già un'altra richiesta di cancellazione logica, diversa da quella
            // corrente, con stato PRESA_IN_CARICO o ACQUISITA che contiene quella richiesta di
            // restituzione archivio con stato RESTITUITO da elaborare
            progressivoErr = controlloRichiestaDuplicataPerRestituzioneArchivio(item, richRestArch,
                    progressivoErr, tipoRichiesta);
        }

        // ============================================
        // VALUTAZIONE FINALE DELLO STATO
        // ============================================

        // Se per l'item corrente non sono presenti errori con gravità ERRORE,
        // imposta lo stato a DA_ELABORARE
        if (registrazioneRichiesteDao
                .getAroErrRichSoftDeleteByGravity(item.getIdItemRichSoftDelete(),
                        CostantiDB.TipoGravitaErrore.ERRORE.name())
                .isEmpty()) {
            item.setTiStatoItem(CostantiDB.StatoItemRichSoftDelete.DA_ELABORARE.name());
        }
    }

    /**
     * Controllo comune: verifica se l'unità documentaria è già presente in un'altra richiesta di
     * cancellazione logica in corso di elaborazione.
     */
    private int controlloRichiestaDuplicataPerUd(AroItemRichSoftDelete item, AroUnitaDoc ud,
            int progressivoErr) throws AppGenericPersistenceException {

        AroRichSoftDelete existingRich = registrazioneRichiesteDao.getAroRichSoftDeleteContainingUd(
                ud.getIdUnitaDoc(), item.getAroRichSoftDelete().getIdRichSoftDelete());

        if (existingRich != null) {
            // UD già presente in un'altra richiesta di cancellazione logica diversa da
            // quella corrente
            String dsErr = UNITA_DOCUMENTARIA_STRING + ud.getCdRegistroKeyUnitaDoc() + "-"
                    + ud.getAaKeyUnitaDoc().toPlainString() + "-" + ud.getCdKeyUnitaDoc()
                    + " \u00E8 in corso di elaborazione nella richiesta con id "
                    + existingRich.getIdRichSoftDelete();
            createAroErrRichSoftDelete(item, new BigDecimal(progressivoErr++),
                    CostantiDB.TipoErrRichSoftDelete.ITEM_IN_CORSO_DI_ELAB.name(), dsErr,
                    CostantiDB.TipoGravitaErrore.ERRORE.name());
        }

        return progressivoErr;
    }

    /**
     * Verifica se la richiesta di annullamento è già presente in un'altra richiesta di
     * cancellazione logica in corso di elaborazione.
     */
    private int controlloRichiestaDuplicataPerAnnullamento(AroItemRichSoftDelete item,
            AroRichAnnulVers richAnnVrs, int progressivoErr, TipoRichiestaType tipoRichiesta)
            throws AppGenericPersistenceException {

        AroRichSoftDelete existingRich = registrazioneRichiesteDao
                .getAroRichSoftDeleteContainingRichAnnulVers(richAnnVrs.getIdRichAnnulVers(),
                        item.getAroRichSoftDelete().getIdRichSoftDelete());

        if (existingRich != null) {
            // Richiesta di annullamento versamento già presente in un'altra richiesta di
            // cancellazione logica diversa da quella corrente
            String dsErr = getDescrizioneRichiesta(tipoRichiesta) + richAnnVrs.getCdRichAnnulVers()
                    + " di tipo CANCELLAZIONE \u00E8 in corso di elaborazione nella richiesta con id "
                    + existingRich.getIdRichSoftDelete();
            createAroErrRichSoftDelete(item, new BigDecimal(progressivoErr++),
                    CostantiDB.TipoErrRichSoftDelete.ITEM_IN_CORSO_DI_ELAB.name(), dsErr,
                    CostantiDB.TipoGravitaErrore.ERRORE.name());
        }

        return progressivoErr;
    }

    /**
     * Verifica se la richiesta di restituzione archivio è già presente in un'altra richiesta di
     * cancellazione logica in corso di elaborazione.
     */
    private int controlloRichiestaDuplicataPerRestituzioneArchivio(AroItemRichSoftDelete item,
            AroRichiestaRa richRestArch, int progressivoErr, TipoRichiestaType tipoRichiesta)
            throws AppGenericPersistenceException {

        AroRichSoftDelete existingRich = registrazioneRichiesteDao
                .getAroRichSoftDeleteContainingRichRestArch(richRestArch.getIdRichiestaRa(),
                        item.getAroRichSoftDelete().getIdRichSoftDelete(),
                        item.getAroRichSoftDelete().getOrgStrut().getIdStrut());

        if (existingRich != null) {
            // Richiesta di restituzione archivio già presente in un'altra richiesta di
            // cancellazione logica diversa da quella corrente
            String dsErr = getDescrizioneRichiesta(tipoRichiesta) + richRestArch.getIdRichiestaRa()
                    + " \u00E8 in corso di elaborazione nella richiesta con id "
                    + existingRich.getIdRichSoftDelete();
            createAroErrRichSoftDelete(item, new BigDecimal(progressivoErr++),
                    CostantiDB.TipoErrRichSoftDelete.ITEM_IN_CORSO_DI_ELAB.name(), dsErr,
                    CostantiDB.TipoGravitaErrore.ERRORE.name());
        }

        return progressivoErr;
    }

    /**
     * Controlli specifici per tipologia ANNULLAMENTO_VERSAMENTO. Verifica che l'UD sia in stato
     * ANNULLATA e che sia effettivamente annullata.
     */
    private int controlliSpecificiAnnullamentoVersamento(AroItemRichSoftDelete item, AroUnitaDoc ud,
            int progressivoErr) throws AppGenericPersistenceException {

        // Per ANNULLAMENTO_VERSAMENTO lo stato di conservazione deve essere ANNULLATA
        if (!ud.getTiStatoConservazione()
                .equals(CostantiDB.StatoConservazioneUnitaDoc.ANNULLATA.name())) {
            // Stato conservazione errato per annullamento versamento
            String dsErr = UNITA_DOCUMENTARIA_STRING + ud.getCdRegistroKeyUnitaDoc() + "-"
                    + ud.getAaKeyUnitaDoc().toPlainString() + "-" + ud.getCdKeyUnitaDoc()
                    + STATO_CONSERVAZIONE_NON_VALIDO_STRING + ud.getTiStatoConservazione()
                    + NON_PUO_ESSERE_ELABORATA_STRING;
            createAroErrRichSoftDelete(item, new BigDecimal(progressivoErr++),
                    CostantiDB.TipoErrRichSoftDelete.STATO_CONSERV_NON_AMMESSO.name(), dsErr,
                    CostantiDB.TipoGravitaErrore.ERRORE.name());
        }

        // Controlla se l'unità documentaria definita per item corrente non è annullata,
        // in caso positivo registro l'errore
        if (registrazioneRichiesteDao.isUdNonAnnullata(ud.getIdUnitaDoc())) {
            String dsErr = UNITA_DOCUMENTARIA_STRING + ud.getCdRegistroKeyUnitaDoc() + "-"
                    + ud.getAaKeyUnitaDoc().toPlainString() + "-" + ud.getCdKeyUnitaDoc()
                    + NON_ANNULLATA_STRING;
            createAroErrRichSoftDelete(item, new BigDecimal(progressivoErr++),
                    CostantiDB.TipoErrRichSoftDelete.ITEM_NON_ANNULLATO.name(), dsErr,
                    CostantiDB.TipoGravitaErrore.ERRORE.name());
        }

        return progressivoErr;
    }

    /**
     * Controlli specifici per tipologia RESTITUZIONE_ARCHIVIO. Verifica che l'UD sia in stato
     * AIP_FIRMATO o IN_ARCHIVIO e che sia effettivamente restituita.
     */
    private int controlliSpecificiRestituzioneArchivio(AroItemRichSoftDelete item, AroUnitaDoc ud,
            int progressivoErr) throws AppGenericPersistenceException {

        // Per RESTITUZIONE_ARCHIVIO lo stato di conservazione deve essere AIP_FIRMATO o IN_ARCHIVIO
        // o ANNULLATA
        if (!ud.getTiStatoConservazione()
                .equals(CostantiDB.StatoConservazioneUnitaDoc.AIP_FIRMATO.name())
                && !ud.getTiStatoConservazione()
                        .equals(CostantiDB.StatoConservazioneUnitaDoc.IN_ARCHIVIO.name())
                && !ud.getTiStatoConservazione()
                        .equals(CostantiDB.StatoConservazioneUnitaDoc.ANNULLATA.name())) {
            // Stato conservazione errato per restituzione archivio
            String dsErr = UNITA_DOCUMENTARIA_STRING + ud.getCdRegistroKeyUnitaDoc() + "-"
                    + ud.getAaKeyUnitaDoc().toPlainString() + "-" + ud.getCdKeyUnitaDoc()
                    + STATO_CONSERVAZIONE_NON_VALIDO_STRING + ud.getTiStatoConservazione()
                    + NON_PUO_ESSERE_ELABORATA_STRING;
            createAroErrRichSoftDelete(item, new BigDecimal(progressivoErr++),
                    CostantiDB.TipoErrRichSoftDelete.STATO_CONSERV_NON_AMMESSO.name(), dsErr,
                    CostantiDB.TipoGravitaErrore.ERRORE.name());
        }

        // Controlla se l'unità documentaria definita per item corrente non è restituita,
        // in caso positivo registro l'errore
        if (registrazioneRichiesteDao.isUdNonRestituita(ud.getIdUnitaDoc())) {
            String dsErr = UNITA_DOCUMENTARIA_STRING + ud.getCdRegistroKeyUnitaDoc() + "-"
                    + ud.getAaKeyUnitaDoc().toPlainString() + "-" + ud.getCdKeyUnitaDoc()
                    + NON_RESTITUITA_STRING;
            createAroErrRichSoftDelete(item, new BigDecimal(progressivoErr++),
                    CostantiDB.TipoErrRichSoftDelete.ITEM_NON_RESTITUITO.name(), dsErr,
                    CostantiDB.TipoGravitaErrore.ERRORE.name());
        }

        return progressivoErr;
    }

    /**
     * Controlli specifici per tipologia SCARTO_ARCHIVISTICO. TODO: Implementare controlli specifici
     * quando la logica sarà definita.
     */
    private int controlliSpecificiScartoArchivistico(AroItemRichSoftDelete item, AroUnitaDoc ud,
            int progressivoErr) throws AppGenericPersistenceException {

        // TODO: Implementare controlli specifici per SCARTO_ARCHIVISTICO
        // Esempio: verifica di stati di conservazione specifici per lo scarto (es. VERSATA, ecc.)
        log.debug("Controlli specifici per SCARTO_ARCHIVISTICO da implementare per UD: {}-{}-{}",
                ud.getCdRegistroKeyUnitaDoc(), ud.getAaKeyUnitaDoc(), ud.getCdKeyUnitaDoc());

        return progressivoErr;
    }

    /**
     * Controlli specifici per tipologia UNITA_DOCUMENTARIA (richiesta diretta). Applica tutti i
     * controlli necessari per UD richieste direttamente.
     */
    private int controlliSpecificiUnitaDocumentaria(AroItemRichSoftDelete item, AroUnitaDoc ud,
            int progressivoErr) throws AppGenericPersistenceException {

        // Per richieste dirette di UD, lo stato deve essere ANNULLATA
        if (!ud.getTiStatoConservazione()
                .equals(CostantiDB.StatoConservazioneUnitaDoc.ANNULLATA.name())) {
            // Stato conservazione errato
            String dsErr = UNITA_DOCUMENTARIA_STRING + ud.getCdRegistroKeyUnitaDoc() + "-"
                    + ud.getAaKeyUnitaDoc().toPlainString() + "-" + ud.getCdKeyUnitaDoc()
                    + STATO_CONSERVAZIONE_NON_VALIDO_STRING + ud.getTiStatoConservazione()
                    + NON_PUO_ESSERE_ELABORATA_STRING;
            createAroErrRichSoftDelete(item, new BigDecimal(progressivoErr++),
                    CostantiDB.TipoErrRichSoftDelete.STATO_CONSERV_NON_AMMESSO.name(), dsErr,
                    CostantiDB.TipoGravitaErrore.ERRORE.name());
        }

        // Controlla se l'unità documentaria definita per item corrente non è annullata,
        // in caso positivo registro l'errore
        if (registrazioneRichiesteDao.isUdNonAnnullata(ud.getIdUnitaDoc())) {
            String dsErr = UNITA_DOCUMENTARIA_STRING + ud.getCdRegistroKeyUnitaDoc() + "-"
                    + ud.getAaKeyUnitaDoc().toPlainString() + "-" + ud.getCdKeyUnitaDoc()
                    + NON_ANNULLATA_STRING;
            createAroErrRichSoftDelete(item, new BigDecimal(progressivoErr++),
                    CostantiDB.TipoErrRichSoftDelete.ITEM_NON_ANNULLATO.name(), dsErr,
                    CostantiDB.TipoGravitaErrore.ERRORE.name());
        }

        return progressivoErr;
    }

    private void createAroErrRichSoftDelete(AroItemRichSoftDelete item, BigDecimal pgErr,
            String tiErr, String dsErr, String tiGravitaErr) {
        AroErrRichSoftDelete err = new AroErrRichSoftDelete();
        err.setPgErr(pgErr);
        err.setTiErr(tiErr);
        err.setDsErr(dsErr);
        err.setTiGravita(tiGravitaErr);
        item.addAroErrRichSoftDelete(err);
        entityManager.persist(err);
        entityManager.flush();
    }

    /**
     * Ritorna il numero di items all'interno della richiesta con id <code>idRichSoftDelete</code>
     * con gli stati elencati
     *
     * @param idRichSoftDelete     id della richiesta di cancellazione logica
     * @param tiItemRichSoftDelete tipo di item della richiesta di cancellazione logica
     * @param statiItems           stati da controllare
     *
     * @return il numero di items
     */
    @Override
    public Long countItemsInRichSoftDelete(BigDecimal idRichSoftDelete, String tiItemRichSoftDelete,
            String... statiItems) throws AppGenericPersistenceException {
        return registrazioneRichiesteDao.countAroItemRichSoftDelete(idRichSoftDelete,
                tiItemRichSoftDelete, statiItems);
    }

    /**
     * Ritorna il numero di items all'interno della richiesta con id <code>idRichSoftDelete</code>
     *
     * @param idRichSoftDelete     id della richiesta cancellazione logica
     * @param tiItemRichSoftDelete tipo di item della richiesta cancellazione logica
     *
     * @return il numero di items
     */
    @Override
    public Long countItemsInRichSoftDelete(BigDecimal idRichSoftDelete, String tiItemRichSoftDelete)
            throws AppGenericPersistenceException {
        return registrazioneRichiesteDao.countAroItemRichSoftDelete(idRichSoftDelete,
                tiItemRichSoftDelete);
    }

    @Override
    public AroStatoRichSoftDelete insertAroStatoRichSoftDelete(AroRichSoftDelete richSoftDelete,
            String tiStatoRichSoftDelete, LocalDateTime dtRegStatoRichSoftDelete, long idUser)
            throws AppGenericPersistenceException {

        AroStatoRichSoftDelete statoRichSoftDelete = new AroStatoRichSoftDelete();
        log.info("Eseguo il salvataggio dello stato richiesta di cancellazione logica pari a {}",
                tiStatoRichSoftDelete);
        statoRichSoftDelete.setPgStatoRichSoftDelete(registrazioneRichiesteDao
                .getUltimoProgressivoStatoRichiesta(richSoftDelete.getIdRichSoftDelete())
                .add(BigDecimal.ONE));
        statoRichSoftDelete.setTiStatoRichSoftDelete(tiStatoRichSoftDelete);
        statoRichSoftDelete.setDtRegStatoRichSoftDelete(dtRegStatoRichSoftDelete);
        statoRichSoftDelete.setIamUser(entityManager.find(IamUser.class, idUser));
        richSoftDelete.addAroStatoRichSoftDelete(statoRichSoftDelete);
        entityManager.persist(statoRichSoftDelete);
        entityManager.flush();
        richSoftDelete.setIdStatoRichSoftDeleteCor(
                BigDecimal.valueOf(statoRichSoftDelete.getIdStatoRichSoftDelete()));
        entityManager.merge(richSoftDelete);

        return statoRichSoftDelete;
    }

    @Override
    @Transactional(value = TxType.REQUIRES_NEW, rollbackOn = {
            AppGenericPersistenceException.class })
    public AroXmlRichSoftDelete createAroXmlRichSoftDelete(AroRichSoftDelete richSoftDelete,
            String tiXmlRichSoftDelete, String blXmlRichSoftDelete, String cdVersioneXml)
            throws AppGenericPersistenceException {
        return registrazioneRichiesteDao.createAroXmlRichSoftDelete(richSoftDelete,
                tiXmlRichSoftDelete, blXmlRichSoftDelete, cdVersioneXml);
    }

    @Override
    @Transactional(value = TxType.REQUIRES_NEW, rollbackOn = {
            AppGenericPersistenceException.class })
    public AroStatoRichSoftDelete updateStatoRichiestaToErrore(Long idRichSoftDelete,
            Long idUserIam) throws AppGenericPersistenceException {

        log.debug("Aggiorno lo stato della richiesta {} a ERRORE", idRichSoftDelete);

        // Recupero l'entità con lock per evitare concorrenza
        AroRichSoftDelete richSoftDelete = entityManager.find(AroRichSoftDelete.class,
                idRichSoftDelete, LockModeType.PESSIMISTIC_WRITE);

        // Recupero gli stati associati alla richiesta
        TypedQuery<AroStatoRichSoftDelete> query = entityManager.createQuery(
                "SELECT a FROM AroStatoRichSoftDelete a JOIN FETCH a.aroRichSoftDelete rich WHERE rich = :aroRichSoftDelete",
                AroStatoRichSoftDelete.class);
        query.setParameter("aroRichSoftDelete", richSoftDelete);
        richSoftDelete.setAroStatoRichSoftDelete(query.getResultList());

        // Creo il nuovo stato ERRORE
        AroStatoRichSoftDelete statoRichSoftDeleteNew = insertAroStatoRichSoftDelete(richSoftDelete,
                CostantiDB.StatoRichSoftDelete.ERRORE.name(), LocalDateTime.now(), idUserIam);

        // Aggiorno lo stato corrente della richiesta
        richSoftDelete.setIdStatoRichSoftDeleteCor(
                new BigDecimal(statoRichSoftDeleteNew.getIdStatoRichSoftDelete()));

        entityManager.flush();

        log.info("Stato della richiesta {} aggiornato a ERRORE", idRichSoftDelete);
        return statoRichSoftDeleteNew;
    }

    /**
     * Recupera lo stato corrente di una richiesta di cancellazione logica tramite join diretto sul
     * campo idStatoRichSoftDeleteCor.
     *
     * @param idRichSoftDelete ID della richiesta di cancellazione logica
     *
     * @return L'oggetto AroStatoRichSoftDelete rappresentante lo stato corrente della richiesta
     *
     * @throws AppGenericPersistenceException in caso di errori di persistenza
     */
    @Override
    @Transactional(value = TxType.REQUIRED)
    public AroStatoRichSoftDelete getStatoCorrenteRichiesta(Long idRichSoftDelete)
            throws AppGenericPersistenceException {
        try {
            log.debug("Recupero lo stato corrente della richiesta {}", idRichSoftDelete);

            // Query con join sulla chiave di stato corrente
            TypedQuery<AroStatoRichSoftDelete> query = entityManager.createQuery(
                    "SELECT s FROM AroStatoRichSoftDelete s "
                            + "JOIN AroRichSoftDelete r ON r.idStatoRichSoftDeleteCor = s.idStatoRichSoftDelete "
                            + "WHERE r.idRichSoftDelete = :idRichSoftDelete",
                    AroStatoRichSoftDelete.class);

            query.setParameter("idRichSoftDelete", idRichSoftDelete);

            AroStatoRichSoftDelete statoCorrente = query.getSingleResult();

            log.debug("Stato corrente della richiesta {}: {}", idRichSoftDelete,
                    statoCorrente.getTiStatoRichSoftDelete());

            return statoCorrente;
        } catch (Exception ex) {
            throw new AppGenericPersistenceException(ex,
                    "Errore durante il recupero dello stato corrente della richiesta "
                            + idRichSoftDelete + ": " + ExceptionUtils.getRootCauseMessage(ex));
        }
    }

}
