<img src="src/docs/quarkus.png" width="300">
<br/><br/>

# Cancellazione Logica UD

Fonte template redazione documento:  https://www.makeareadme.com/.


# Descrizione

Microservizio per l'implementazione della "cancellazione logica" (soft delete) delle Unità Documentarie (UD) e dell'intera gerarchia di entità collegate, con obiettivi di:
- Registrazione asincrona delle richieste di cancellazione logica (multi–tipologia)
- Minimizzare la latenza tra richiesta e primo item elaborato (pipeline sovrapposta).
- Garantire coerenza relazionale multi‑livello mediante algoritmo BFS ottimizzato e batch query.
- Supportare modalità di elaborazione CAMPIONE e COMPLETA (bottom‑up sulle relazioni).
- Scalare orizzontalmente (multi‑pod) con claim cooperativo (worker cooperanti) e isolamento transazionale per item/batch.
- Garantire coerenza del timestamp di soft delete tra pod tramite cache distribuita, prevenendo race‑condition e perdita di eventi CDC.
- Stream JPA + Spliterator per parallelismo intra‑pod e batching adattivo.
- Ottimizzazione query relazionali tramite annotazioni custom @RelationQuery
- Forzatura selettiva della modalità COMPLETA per singole entità tramite annotazione custom, indipendentemente dalla modalità globale configurata.
- Tracciare in modo trasparente stati, errori, duplicati e volumi elaborati.
- Esporre metadati strutturati (`TS_SOFT_DELETE`, `DM_SOFT_DELETE`) su centinaia di tabelle dominio per integrazione downstream (Kafka Connect / ksqlDB / DataMart).

---

## Panoramica
Il microservizio riceve richieste XML di cancellazione logica di UD o altri tipi correlati. Registra subito la richiesta (stato PRESA_IN_CARICO) e fornisce immediatamente l’XML di risposta preliminare. In parallelo avvia:
- Preprocessing: parsing e generazione items (unità elaborabili).
- Processing: worker che iniziano ad elaborare gli items già generati (overlapping).

La cancellazione logica implica l’aggiornamento timestamp e la creazione di un payload JSON descrittivo su tutte le entità raggiungibili a partire dalla UD root tramite relazioni JPA:
- Aggiornando `TS_SOFT_DELETE`
- Popolando `DM_SOFT_DELETE` (JSON tracciabile)
- Preservando la consistenza referenziale
- Garantendo audit completo (log, errori, versionamento XML richiesta/esito)

Supporta richieste eterogenee (UD, Annullamento Versamento, Restituzione Archivio e Scarto Archivistico) con elaborazione asincrona, resiliente e scalabile.

---

## Glossario
| Termine | Descrizione |
|---------|-------------|
| Richiesta | Entità radice del processo di soft delete (ARO_RICH_SOFT_DELETE). |
| Item | Un elemento elaborabile (UD, ANNUL_VERS, …) legato alla richiesta. |
| Preprocessing | Costruzione items tramite streaming JPA + Spliterator parallelizzato. |
| Processing | Cancellazione bottom‑up e aggiornamento stato item. |
| BFS | Strategia di esplorazione a livello (layered) della gerarchia parent→child. |
| CAMPIONE | Aggiorna 1 figlio rappresentativo per relazione. |
| COMPLETA | Aggiorna tutti i figli (fan‑out completo) e registra duplicati. |
| Claim | Acquisizione concorrente di item via SKIP LOCKED. |
| DM_SOFT_DELETE | JSON con coordinate logiche dell’operazione (per tracciamento/ETL). |
| Duplicato | Figlio addizionale nella stessa relazione (tracciato in tabella dedicata). |

---

## Flusso end‑to‑end
1. Ricezione XML richiesta (WS).
2. Validazioni (versione, credenziali, formato).
3. Persistenza richiesta + XML richiesta → Stato PRESA_IN_CARICO → Risposta XML immediata al chiamante.
4. Avvio asincrono preprocessing: generazione progressiva items (streaming + parallel batch).
5. Items diventano subito `DA_ELABORARE` e possono essere claimati dai worker (overlap).
6. Fine preprocessing → Stato ACQUISITA (nessun nuovo item sarà generato).
7. Worker (multi‑pod) completano elaborazione item (soft delete gerarchica).
8. Quando nessun item rimane `DA_ELABORARE` → Stato EVASA (o ERRORE).

---

## Stati della richiesta e pipeline sovrapposta
| Stato | Significato | Evento di ingresso |
|-------|-------------|--------------------|
| PRESA_IN_CARICO | Richiesta registrata, risposta inviata | Creazione richiesta |
| ACQUISITA | Preprocessing items terminato | Fine generazione items |
| EVASA | Tutti gli item elaborati (ELABORATO / NON_ELABORABILE / ERRORE_ELABORAZIONE) | Finalizer periodico |
| ACQUISIZIONE_FALLITA | Nessun item elaborabile (tutti NON_ELABORABILE) | Post-preprocessing |
| ERRORE | Errore impeditivo | Eccezioni critiche |

Pipeline overlappata: i worker possono iniziare ad elaborare items mentre lo stato è ancora PRESA_IN_CARICO.

---

## Architettura logica interna
Componenti:
- API WS (ingresso richieste)
- Servizio registrazione richieste
- Preprocessing parallelizzato (Spliterator)
- Scheduler (`quarkus-scheduler`) per:
  - Poll & claim batch items
  - Finalizzazione richieste
- Worker asincroni (ManagedExecutor / vert.x)
- Cache distribuita (Infinispan in modalità DIST_SYNC)
- Motore BFS + soft delete bottom‑up
- Persistenza log, errori, duplicati, stati, XML

---

## Gestione concorrenza & scalabilità multi‑pod
Pattern: “Cooperative Consumers” su DB:
- Claim item: `PESSIMISTIC_WRITE` + lock timeout `SKIP LOCKED`
- Campi `CD_INSTANCE_ID`, `DT_CLAIM` anti doppio processing
- Timeout reclaim (configurabile) per resilienza in caso di crash pod
- Batch size configurabile (`worker.batch.size`) per regolare throughput

---

## Cache distribuita tra pod — Coerenza del timestamp CDC

**Cache distribuita** (Infinispan in modalità cluster `DIST_SYNC` con replica su n nodi) che funge da **registro condiviso dell'ultimo timestamp noto tra tutti i pod**. Prima di eseguire gli UPDATE, ogni pod:

1. **Legge** dalla cache il timestamp più recente conosciuto dal cluster.
2. Calcola il proprio timestamp come valore **strettamente maggiore** di quello in cache.
3. **Aggiorna** la cache con il nuovo valore, garantendo monotonicità (l'aggiornamento avviene solo se il nuovo valore è effettivamente maggiore di quello già presente).
4. Esegue gli UPDATE su DB con il timestamp così determinato.

In questo modo tutti i pod producono timestamp **monotoni e coordinati**, allineati all'ultimo valore noto, e il connector può sempre catturarli correttamente nel ciclo di polling successivo.

Per ridurre la frequenza di accesso alla cache distribuita, ogni pod mantiene una **replica locale con TTL configurabile** (nell'ordine delle centinaia di millisecondi): entro questo intervallo il pod riusa il valore già letto senza interrogare Infinispan. Questo introduce una **micro‑finestra controllata** in cui due pod potrebbero teoricamente lavorare con lo stesso valore di base, ma grazie alla regola di monotonicità sull'aggiornamento il rischio è **drasticamente ridotto** rispetto allo scenario senza cache, **temporalmente circoscritto** al solo TTL configurato e statisticamente raro.

---

## Preprocessing items (parallelismo interno)
Pipeline:
1. Lettura streaming JPA (senza materializzare l’intero dataset).
2. Calcolo cardinalità → Spliterator “dimensione nota”.
3. `FixedBatchSpliterator` definisce batch adaptivo:
   - < 5k → 25
   - 5k–10k → 50
   - 10k–100k → 100
   - 100k–500k → 250
   - 500k–1M → 500
   - > 1M → 1000
4. `parallelStream()` dei batch → transazione separata (REQUIRES_NEW).
5. Items creati e messi subito `DA_ELABORARE`.
6. Early claim: riduce time-to-first-elaboration.

Gestione errori: interruzione coordinata su prima eccezione → rollback batch → prosieguo altri batch se non critico.

Vantaggi:
- Controllo memoria (materializza solo batch)
- Scalabilità CPU intra‑pod
- Isolamento transazionale per batch (rollback circoscritto)

---

## Scheduler, worker loop e vert.x worker pool
| Funzione | Meccanismo |
|----------|------------|
| Polling claim | `@Scheduled(every="5s")` |
| Finalizzazione richieste | `@Scheduled(every="3s")` |
| Esecuzioni asincrone | ManagedExecutor / Vert.x worker pool |
| Claim item | SELECT FOR UPDATE SKIP LOCKED + campi `CD_INSTANCE_ID`, `DT_CLAIM` |
| Timeout claim | Reclaim se `DT_CLAIM` scaduto (configurabile) |

Multi-pod scaling: ciascun pod esegue lo scheduler e compete in modo cooperativo.

---

## Algoritmo BFS multi‑livello
Obiettivo: costruire gerarchia parent→child con efficienza.
Passi:
1. Livello 0 (root UD) aggiunto come seed.
2. Per livello L: raggruppa nodi per classe parent.
3. Recupera figli mediante query BATCH (`IN :parentIds`) per ogni (childClass,parentClass).
4. Marca duplicati se un child appare in più relazioni.
5. Registra per ogni livello un `StreamSource` lazy.
6. Terminata la discovery: elabora livelli in ordine decrescente (bottom‑up), sfruttando grouping per relazione.

Vantaggi:
- Elimina N+1 query.
- Minimizza carico di memoria (stream consumati per livello).
- Riduce round-trip per fan‑out complessi.

---

## Strategia di query (batch universale + opzionale @RelationQuery)
Principi:
- Default: generazione dinamica query batch (`SELECT c.pk, c.parent.pk ... WHERE parent.id IN :parentIds`).
- Se presente @RelationQuery con `IN :parentIds` → uso diretto.
- Se presente @RelationQuery senza clausola batch → viene comunque privilegiato approccio batch se possibile (o fallback iterativo se la struttura non lo consente).
- Caching in `RelationQueryExecutorService` per `(childClass,parentClass,level)`.

---

## Ottimizzazione query relazionali
Annotazione `@RelationQuery` consente:
- Query custom per relazione (child→parent) filtrando su root (`:rootId`) o parent batch (`IN :parentIds`)
- Specifica livelli applicazione (`levels = {2,3,...}`)
- Parametri configurabili (`parentIdParam`, `parentIdsParam`, `rootIdParam` default)
- Fallback automatico a query generate se non definito.

Esempio (da `AroCompVerIndiceAipUd`):
```java
@RelationQuery(
  parentClass = AroCompDoc.class,
  query = "SELECT c.idCompVerIndiceAipUd, c.aroCompDoc.idCompDoc FROM AroCompVerIndiceAipUd c ... WHERE p.idCompDoc IN :parentIds ...",
  levels = {2},
  parentIdsParam = "parentIds"
)
```
`RelationQueryExecutorService`:
- Cache per relazione+livello
- Rileva capacità batch (clausola `IN`) → usa esecuzione unica per n parent
- Stream `Object[]` → mappato in `EntityNode`
- Chiusura sicura stream con `onClose`

---

## Modalità CAMPIONE vs COMPLETA
| Aspetto | CAMPIONE | COMPLETA |
|---------|----------|----------|
| Figli elaborati per relazione | 1 rappresentativo | Tutti |
| Duplicati | Ignorati | Inseriti in tabella duplicati |
| Volume update | Ridotto | Massimo |
| Uso tipico | Analisi, riduzione impatto | Produzione / full archival logic |
| Costi DB | Minimi | Elevati ma batch-ottimizzati |

---

## Modalità COMPLETA per singole entità

**Annotazione custom** applicabile a livello di classe entity che consente di **forzare la modalità COMPLETA** sulla singola entità, anche quando il processo generale opera in modalità CAMPIONE. Durante la traversata BFS, prima di elaborare ogni relazione, il motore verifica la presenza dell'annotazione sull'entity coinvolta: se presente, quella specifica entità viene elaborata in modalità COMPLETA (fan‑out totale + registrazione duplicati), mentre il resto della gerarchia continua con la modalità globale configurata.

Questo permette di:
- Applicare la logica completa **solo dove necessario**, senza impatto sulle prestazioni globali.
- Mantenere i vantaggi della modalità CAMPIONE sul resto del grafo.
- Centralizzare la configurazione a livello di entity, evitando logiche condizionali disperse nel codice.

## Soft delete bottom‑up
Per ogni livello (discendente):
1. Raggruppa figli per relazione.
2. Modalità CAMPIONE:
   - Singolo UPDATE per entità scelta.
3. Modalità COMPLETA:
   - Bulk UPDATE figli (`UPDATE ... SET ts_soft_delete, dm_soft_delete ...`).
   - Inserimento duplicati con `INSERT ... SELECT` se più di un figlio rilevante.
4. Aggiornamento item root a ELABORATO quando la catena raggiunge il livello 0.
5. Logging volumetrico per tabella e livello.

Offset temporale: per ordinamento logico, i figli ricevono timestamp differenziato (nano offset) in base all’ordine.


## Stato richieste & item

### Richieste (tabella `ARO_STATO_RICH_SOFT_DELETE`)
| Stato | Significato |
|-------|-------------|
| PRESA_IN_CARICO | Richiesta registrata, in attesa generazione item |
| ACQUISITA | Item creati e pronti per elaborazione |
| EVASA | Tutti item elaborati |
| ACQUISIZIONE_FALLITA | Nessun item valido (tutti NON_ELABORABILE) |
| ERRORE | Errore grave (aggiornamento stato forzato) |

### Item (colonna `TI_STATO_ITEM`)
| Stato | Significato |
|-------|-------------|
| DA_ELABORARE | In coda per claim |
| NON_ELABORABILE | Violazioni controlli pre‑processing |
| ELABORATO | Soft delete completata |
| ERRORE_ELABORAZIONE | Eccezione durante processing |

Errori item → registrati in `ARO_ERR_RICH_SOFT_DELETE`.

---

## Modello dati (tabelle principali)
| Tabella | Ruolo | Note |
|---------|-------|------|
| ARO_RICH_SOFT_DELETE | Richieste | Modalità, tipo, struttura, stato corrente |
| ARO_ITEM_RICH_SOFT_DELETE | Item | Supporta gerarchie (FK padre) + claim fields |
| ARO_STATO_RICH_SOFT_DELETE | Storico stati | Progressivo + user |
| ARO_XML_RICH_SOFT_DELETE | XML richiesta / risposta | Versionamento WS |
| ARO_ERR_RICH_SOFT_DELETE | Errori validazione item | Gravità ERRORE/WARNING |
| ARO_LOG_RICH_SOFT_DELETE | Log volumetrico per livello/tabella | Conteggio righe aggiornate |
| ARO_DUP_RICH_SOFT_DELETE | Figli duplicati | Conserva `DM_SOFT_DELETE` |
| *Tabelle dominio* | Aggiunta colonne `TS_SOFT_DELETE`, `DM_SOFT_DELETE` | Indici su `TS_SOFT_DELETE` |

---

## Errori e gestione eccezioni
| Scenario | Azione |
|----------|-------|
| Errore batch preprocessing | Batch rollback, altri batch continuano. |
| Eccezione durante soft delete item | Item → ERRORE_ELABORAZIONE (CD_ERR_MSG valorizzato). |
| Fallimento globalizzato | Stato richiesta → ERRORE. |
| Nessun item valido | Stato → ACQUISIZIONE_FALLITA. |
| Query relazione problematica | Fallback a query batch generata; se impossibile, relazione saltata (WARN). |

---

## Configurazione (MicroProfile)
| Property | Default | Descrizione |
|----------|---------|-------------|
| worker.batch.size | 5 | Item max per claim loop. |
| worker.poll.enabled | true | Abilita scheduler di polling. |
| worker.claim.timeout-minutes | 30 | Timeout per reclaim item. |
| quarkus.uuid | auto | Identificativo univoco istanza (claim). |
| (futuro) bfs.fetch.size | 100 | Hint fetch query relazioni. |
| (futuro) softdelete.mode.default | COMPLETA | Modalità di default. |

---

## Performance e tuning
| Area | Ottimizzazione | Impatto |
|------|---------------|---------|
| Preprocessing | Spliterator adattivo | Bilanciamento throughput/memoria |
| BFS | Query batch universali | Riduce round-trip massivi |
| Soft delete COMPLETA | Bulk update / insert duplicati | Miglior rapporto CPU/I/O |
| Timestamp offset | plusNanos(offset*1000) | Ordering logico riproducibile |
| Claim concurrency | Ridurre / aumentare `worker.batch.size` | Adattamento carico DB |
| Modalità CAMPIONE | Minimizza scritture | Per validazioni rapide |

---

## Estendibilità (nuove relazioni / entità)
Procedura:
1. Aggiungere `TS_SOFT_DELETE`, `DM_SOFT_DELETE` + indice su tabella.
2. Se necessario performance extra, annotare entity figlia con `@RelationQuery` (IN :parentIds).
3. Assicurarsi che la relazione JPA (OneToMany / ManyToOne / OneToOne) sia mappata
4. Verificare livello BFS (profondità).
5. Eseguire test CAMPIONE e COMPLETA (con duplicati).
6. Validare JSON `DM_SOFT_DELETE`.
7. Aggiornare questo README e script di migrazione.

---

## Appendix A – Esempio timeline overlappata
```
T0      : Richiesta salvata → XML risposta (PRESA_IN_CARICO)
T0+Δ1   : Primi batch items creati
T0+Δ1   : Worker claim primi items
T0+Δ2   : Preprocessing continua (nuovi items)
T0+Δ3   : Preprocessing termina → ACQUISITA
T0+Δ4   : Ultimo item elaborato → EVASA
```

## Appendix B – Pseudo Sequence (Testo)
1. WS IN → `CancellazioneLogicaService.init()/verifica*()`
2. `registraRichieste()` → persist Richiesta + XML → stato PRESA_IN_CARICO
3. Async: `createItemsInNewTransaction()` → stream + batch → build item → stato ACQUISITA
4. Worker loop:
   - `claimBatch()` → item[DA_ELABORARE]
   - per item → `softDeleteBottomUp()`
     - build gerarchia → livelli desc
     - per livello → update / insert duplicati
     - item → ELABORATO
5. Finalizer: `findRequestsToFinalize()` → se nessun item pending → stato EVASA
6. XML esito registrato

## Appendix C – Esempio DM_SOFT_DELETE root
```json
{"ID_UD":101,"ID_PK":101,"ID_FK":0,"NI_LVL":0,"NM_TAB":"ARO_UNITA_DOC","NM_PK":"ID_UNITA_DOC"}
```

## Appendix D – Esempio DM_SOFT_DELETE figlio
```json
{"ID_UD":101,"ID_PK":5555,"ID_FK":101,"NI_LVL":2,"NM_TAB":"ARO_COMP_DOC","NM_PK":"ID_COMP_DOC","NM_FK":"ID_UNITA_DOC"}
```

## Appendix E – Esempio query batch generata (fallback)
```sql
SELECT c.idCompDoc, c.aroUnitaDoc.idUnitaDoc
FROM AroCompDoc c
WHERE c.aroUnitaDoc.idUnitaDoc IN :parentIds
```

## Appendix F – Esempio controlli item (UD)
- Esistenza UD
- Stato conservazione = ANNULLATA
- Duplicato nella stessa richiesta
- Già presente in altra richiesta in corso (logica predisposta / commentata)
→ Errori codificati in `ARO_ERR_RICH_SOFT_DELETE` con gravità ERRORE/WARNING.

## Appendix G – Esempio configurazione (application.properties)
```properties
worker.batch.size=10
worker.poll.enabled=true
worker.claim.timeout-minutes=20
quarkus.log.category."it.eng.parer.soft.delete".level=INFO
```

## Appendix H – Esempio @RelationQuery multi-livello
```java
@RelationQuery(
  parentClass = AroVerIndiceAipUd.class,
  query = "SELECT c.idCompVerIndiceAipUd, c.aroVerIndiceAipUd.idVerIndiceAip " +
          "FROM AroCompVerIndiceAipUd c JOIN c.aroVerIndiceAipUd v JOIN v.aroIndiceAipUd d " +
          "WHERE v.idVerIndiceAip IN :parentIds AND d.aroUnitaDoc.idUnitaDoc = :rootId",
  levels = {3},
  parentIdParam = "parentIds",
  parentIdsParam = "parentIds"
)
```

## Appendix I – Strategia fallback query relazioni
1. Prova query ottimizzata batch (se clausola `IN`)
2. Se non presente → query ottimizzata singola per parent
3. Se non annotata → query generata standard `SELECT child.id, child.parent.id FROM Child ...`
4. Sempre restituito stream `EntityNode`, con flag `isDuplicate`.

## Appendix L – Esempio log di livello BFS
```
INFO  L=3 mode=COMPLETA parents=412 width=128 updated=356ms duplicates=23
```

## Appendix M – Esempio claim loop
```
[claim] items=5 instance=pod-01 timeoutThreshold=2025-09-11T10:22Z
```

## Appendix N – Campi claim item
| Campo | Descrizione |
|-------|-------------|
| DT_CLAIM | Timestamp acquisizione item |
| CD_INSTANCE_ID | Identificativo pod |
| DT_FINE_ELAB | Chiusura elaborazione |
| CD_ERR_MSG | Messaggio errore se fallito |

## Appendix O – Metriche interpretazione rapida
| Metrica | Azione se anomala |
|---------|-------------------|
| request.duration.ms elevato | Verificare overlap effettivo e batch size |
| bfs.level.width molto alta | Profilare relazioni / fan‑out non previsto |
| duplicates.count alta | Valutare efficacia modalità CAMPIONE per casi non critici |
| items.failed.count > 0 | Ispezionare CD_ERR_MSG e errori DB |
| claim.timeout riacquisizioni frequenti | Aumentare timeout o ridurre batch size |

---

## Note finali
Questo microservizio realizza una pipeline di cancellazione logica ad alte prestazioni grazie alla combinazione di:
- Overlapping tra generazione e consumo degli item
- BFS multi‑livello con query batch universali
- Parallelismo controllato su Spliterator adattivi
- Strategie di lock non bloccanti (SKIP LOCKED)
- Tracciabilità completa (log, duplicati, metadati JSON, stati)

Ogni estensione deve preservare atomicità per item, coerenza transazionale per livello e auditabilità end‑to‑end.

---

# Installazione

Di seguito sono riportate le varie modalità con cui è possibile rendere operativo questo microservizio. 

## Rilascio su RedHat Openshift

Per la creazione dell'applicazione con risorse necessarie correlate sotto Openshift (https://www.redhat.com/it/technologies/cloud-computing/openshift) viene fornito un apposito template (la soluzione, modificabile, è basata su Oracle DB) [template](src/main/openshift/organizzazioni-abilitate-template.yml) che permette la creazione dell'applicazione su soluzione Openshift (sia licensed che open).

# Utilizzo

Basato su [Quarkus](https://quarkus.io/) e Java 21, è consigliato fare riferimento alle [linee guida](https://quarkus.io/guides/) ufficiali per avere ulteriori dettagli nonché le best practice da utilizzare in caso di modifiche ed interventi al progetto stesso. 
Le configurazioni sono legate ai file yaml che sono gestiti come previsto dai meccanismi di overrinding messi a disposizione, vedi apposita [guida](https://quarkus.io/guides/config).

# Immagine Docker

Per effettuare una build del progetto via Docker è stato predisposto lo standard [Dockerfile](src/main/docker/Dockerfile.jvm) e una directory [docker_build](docker_build) con all'interno i file da integrare all'immagine base.
La directory [docker_build](docker_build) è strutturata come segue: 

```bash
|____README.md
|____certs
| |____README.md

```
al fine di integrare certificati non presenti di default nell'immagine principale è stata introdotta la sotto-directory [docker_build/certs](docker_build/certs) in cui dovranno essere inseriti gli appositi certificati che verranno "trustati" in fase di build dell'immagine.
La compilazione dell'immagine può essere eseguita con il comando: 

```bash
docker build -t <registry> -f ./Dockerfile --build-arg EXTRA_CA_CERTS_DIR=docker_build/certs .
```

# Requisiti e librerie utilizzate

Installazione wrapper maven, attraverso il seguente comando:

```shell script
mvn wrapper:wrapper
```

Richiesti: 

- Java versione 21+ https://jdk.java.net/archive/
- Apache Maven 3.9.0+ https://maven.apache.org/


# Librerie utilizzate

|  GroupId | ArtifactId  | Version |
|:---:|:---:|:---:|
|aopalliance|aopalliance|1.0|
|com.aayushatharva.brotli4j|brotli4j|1.16.0|
|com.aayushatharva.brotli4j|native-linux-x86_64|1.16.0|
|com.aayushatharva.brotli4j|service|1.16.0|
|com.carrotsearch|hppc|0.10.0|
|com.cronutils|cron-utils|9.2.1|
|com.fasterxml.jackson.core|jackson-annotations|2.20|
|com.fasterxml.jackson.core|jackson-core|2.20.1|
|com.fasterxml.jackson.core|jackson-databind|2.20.1|
|com.fasterxml.jackson.dataformat|jackson-dataformat-yaml|2.20.1|
|com.fasterxml.jackson.datatype|jackson-datatype-jdk8|2.20.1|
|com.fasterxml.jackson.datatype|jackson-datatype-jsr310|2.20.1|
|com.fasterxml.jackson.module|jackson-module-parameter-names|2.20.1|
|com.fasterxml.woodstox|woodstox-core|6.6.0|
|com.fasterxml|classmate|1.7.1|
|com.github.ben-manes.caffeine|caffeine|3.2.3|
|com.google.errorprone|error_prone_annotations|2.42.0|
|com.google.guava|failureaccess|1.0.1|
|com.google.guava|guava|33.5.0-jre|
|com.google.inject|guice|classes|
|com.h2database|h2|2.4.240|
|com.oracle.database.jdbc|ojdbc17|23.26.0.0.0|
|com.oracle.database.nls|orai18n|23.26.0.0.0|
|com.sun.istack|istack-commons-runtime|4.1.2|
|commons-cli|commons-cli|1.9.0|
|commons-codec|commons-codec|1.19.0|
|commons-io|commons-io|2.18.0|
|commons-net|commons-net|3.11.1|
|io.agroal|agroal-api|2.8|
|io.agroal|agroal-narayana|2.8|
|io.agroal|agroal-pool|2.8|
|io.github.dmlloyd|jdk-classfile-backport|25.1|
|io.micrometer|micrometer-commons|1.14.7|
|io.micrometer|micrometer-core|1.14.7|
|io.micrometer|micrometer-observation|1.14.7|
|io.micrometer|micrometer-registry-prometheus-simpleclient|1.14.7|
|io.netty|netty-buffer|4.1.128.Final|
|io.netty|netty-codec-dns|4.1.128.Final|
|io.netty|netty-codec-haproxy|4.1.128.Final|
|io.netty|netty-codec-http2|4.1.128.Final|
|io.netty|netty-codec-http|4.1.128.Final|
|io.netty|netty-codec-socks|4.1.128.Final|
|io.netty|netty-codec|4.1.128.Final|
|io.netty|netty-common|4.1.128.Final|
|io.netty|netty-handler-proxy|4.1.128.Final|
|io.netty|netty-handler|4.1.128.Final|
|io.netty|netty-resolver-dns|4.1.128.Final|
|io.netty|netty-resolver|4.1.128.Final|
|io.netty|netty-transport-native-unix-common|4.1.128.Final|
|io.netty|netty-transport|4.1.128.Final|
|io.opentelemetry.instrumentation|opentelemetry-instrumentation-api|2.12.0|
|io.opentelemetry.semconv|opentelemetry-semconv|1.29.0-alpha|
|io.opentelemetry|opentelemetry-api-incubator|1.46.0-alpha|
|io.opentelemetry|opentelemetry-api|1.46.0|
|io.opentelemetry|opentelemetry-context|1.46.0|
|io.opentelemetry|opentelemetry-sdk-common|1.46.0|
|io.opentelemetry|opentelemetry-sdk-metrics|1.46.0|
|io.prometheus|simpleclient|0.16.0|
|io.prometheus|simpleclient_common|0.16.0|
|io.prometheus|simpleclient_tracer_common|0.16.0|
|io.prometheus|simpleclient_tracer_otel|0.16.0|
|io.prometheus|simpleclient_tracer_otel_agent|0.16.0|
|io.quarkiverse.infinispan|quarkus-infinispan-embedded|1.3.0|
|io.quarkus.arc|arc-processor|3.29.4|
|io.quarkus.arc|arc|3.29.4|
|io.quarkus.gizmo|gizmo2|2.0.0.Beta8|
|io.quarkus.gizmo|gizmo|1.9.0|
|io.quarkus.qute|qute-core|3.29.4|
|io.quarkus.resteasy.reactive|resteasy-reactive-common-types|3.29.4|
|io.quarkus.resteasy.reactive|resteasy-reactive-common|3.29.4|
|io.quarkus.resteasy.reactive|resteasy-reactive-jackson|3.29.4|
|io.quarkus.resteasy.reactive|resteasy-reactive-vertx|3.29.4|
|io.quarkus.resteasy.reactive|resteasy-reactive|3.29.4|
|io.quarkus.security|quarkus-security|2.2.1|
|io.quarkus.vertx.utils|quarkus-vertx-utils|3.29.4|
|io.quarkus|quarkus-agroal|3.29.4|
|io.quarkus|quarkus-arc-deployment|3.29.4|
|io.quarkus|quarkus-arc-dev|3.29.4|
|io.quarkus|quarkus-arc|3.29.4|
|io.quarkus|quarkus-bootstrap-app-model|3.29.4|
|io.quarkus|quarkus-bootstrap-core|3.29.4|
|io.quarkus|quarkus-bootstrap-gradle-resolver|3.29.4|
|io.quarkus|quarkus-bootstrap-maven-resolver|3.29.4|
|io.quarkus|quarkus-bootstrap-maven4-resolver|3.29.4|
|io.quarkus|quarkus-bootstrap-runner|3.29.4|
|io.quarkus|quarkus-builder|3.29.4|
|io.quarkus|quarkus-cache-runtime-spi|3.29.4|
|io.quarkus|quarkus-cache|3.29.4|
|io.quarkus|quarkus-caffeine|3.29.4|
|io.quarkus|quarkus-class-change-agent|3.29.4|
|io.quarkus|quarkus-classloader-commons|3.29.4|
|io.quarkus|quarkus-config-yaml|3.29.4|
|io.quarkus|quarkus-core-deployment|3.29.4|
|io.quarkus|quarkus-core|3.29.4|
|io.quarkus|quarkus-credentials|3.29.4|
|io.quarkus|quarkus-datasource-common|3.29.4|
|io.quarkus|quarkus-datasource|3.29.4|
|io.quarkus|quarkus-development-mode-spi|3.29.4|
|io.quarkus|quarkus-devui-deployment-spi|3.29.4|
|io.quarkus|quarkus-elytron-security-common|3.29.4|
|io.quarkus|quarkus-elytron-security-properties-file|3.29.4|
|io.quarkus|quarkus-elytron-security|3.29.4|
|io.quarkus|quarkus-fs-util|1.2.0|
|io.quarkus|quarkus-hibernate-orm|3.29.4|
|io.quarkus|quarkus-hibernate-validator-spi|3.29.4|
|io.quarkus|quarkus-hibernate-validator|3.29.4|
|io.quarkus|quarkus-ide-launcher|3.29.4|
|io.quarkus|quarkus-jackson|3.29.4|
|io.quarkus|quarkus-jacoco|3.29.4|
|io.quarkus|quarkus-jaxb|3.29.4|
|io.quarkus|quarkus-jaxp|3.29.4|
|io.quarkus|quarkus-jdbc-oracle|3.29.4|
|io.quarkus|quarkus-jsonp|3.29.4|
|io.quarkus|quarkus-junit5-config|3.29.4|
|io.quarkus|quarkus-junit5-mockito-config|3.29.4|
|io.quarkus|quarkus-junit5-mockito|3.29.4|
|io.quarkus|quarkus-junit5|3.29.4|
|io.quarkus|quarkus-logging-json|3.29.4|
|io.quarkus|quarkus-micrometer-registry-prometheus|3.29.4|
|io.quarkus|quarkus-micrometer|3.29.4|
|io.quarkus|quarkus-mutiny|3.29.4|
|io.quarkus|quarkus-narayana-jta|3.29.4|
|io.quarkus|quarkus-netty|3.29.4|
|io.quarkus|quarkus-oidc-common|3.29.4|
|io.quarkus|quarkus-oidc|3.29.4|
|io.quarkus|quarkus-qute|3.29.4|
|io.quarkus|quarkus-rest-common|3.29.4|
|io.quarkus|quarkus-rest-jackson-common|3.29.4|
|io.quarkus|quarkus-rest-jackson|3.29.4|
|io.quarkus|quarkus-rest-jaxb|3.29.4|
|io.quarkus|quarkus-rest-qute|3.29.4|
|io.quarkus|quarkus-rest|3.29.4|
|io.quarkus|quarkus-scheduler-api|3.29.4|
|io.quarkus|quarkus-scheduler-common|3.29.4|
|io.quarkus|quarkus-scheduler-kotlin|3.29.4|
|io.quarkus|quarkus-scheduler-spi|3.29.4|
|io.quarkus|quarkus-scheduler|3.29.4|
|io.quarkus|quarkus-security-jpa-common|3.29.4|
|io.quarkus|quarkus-security-jpa|3.29.4|
|io.quarkus|quarkus-security-runtime-spi|3.29.4|
|io.quarkus|quarkus-security|3.29.4|
|io.quarkus|quarkus-smallrye-context-propagation-spi|3.29.4|
|io.quarkus|quarkus-smallrye-context-propagation|3.29.4|
|io.quarkus|quarkus-smallrye-health|3.29.4|
|io.quarkus|quarkus-smallrye-jwt-build|3.29.4|
|io.quarkus|quarkus-smallrye-openapi|3.29.4|
|io.quarkus|quarkus-swagger-ui|3.29.4|
|io.quarkus|quarkus-test-common|3.29.4|
|io.quarkus|quarkus-test-h2|3.29.4|
|io.quarkus|quarkus-test-security|3.29.4|
|io.quarkus|quarkus-tls-registry-spi|3.29.4|
|io.quarkus|quarkus-tls-registry|3.29.4|
|io.quarkus|quarkus-transaction-annotations|3.29.4|
|io.quarkus|quarkus-vertx-http|3.29.4|
|io.quarkus|quarkus-vertx-latebound-mdc-provider|3.29.4|
|io.quarkus|quarkus-vertx|3.29.4|
|io.quarkus|quarkus-virtual-threads|3.29.4|
|io.quarkus|quarkus-websockets-next-spi|3.29.4|
|io.reactivex.rxjava3|rxjava|3.1.10|
|io.rest-assured|json-path|5.5.6|
|io.rest-assured|rest-assured-common|5.5.6|
|io.rest-assured|rest-assured|5.5.6|
|io.rest-assured|xml-path|5.5.6|
|io.smallrye.beanbag|smallrye-beanbag-maven|1.5.3|
|io.smallrye.beanbag|smallrye-beanbag-sisu|1.5.3|
|io.smallrye.beanbag|smallrye-beanbag|1.5.3|
|io.smallrye.certs|smallrye-private-key-pem-parser|0.9.2|
|io.smallrye.common|smallrye-common-annotation|2.14.0|
|io.smallrye.common|smallrye-common-classloader|2.14.0|
|io.smallrye.common|smallrye-common-constraint|2.14.0|
|io.smallrye.common|smallrye-common-cpu|2.14.0|
|io.smallrye.common|smallrye-common-expression|2.14.0|
|io.smallrye.common|smallrye-common-function|2.14.0|
|io.smallrye.common|smallrye-common-io|2.14.0|
|io.smallrye.common|smallrye-common-net|2.14.0|
|io.smallrye.common|smallrye-common-os|2.14.0|
|io.smallrye.common|smallrye-common-process|2.14.0|
|io.smallrye.common|smallrye-common-ref|2.14.0|
|io.smallrye.common|smallrye-common-resource|2.14.0|
|io.smallrye.common|smallrye-common-vertx-context|2.14.0|
|io.smallrye.config|smallrye-config-common|3.14.1|
|io.smallrye.config|smallrye-config-core|3.14.1|
|io.smallrye.config|smallrye-config-source-yaml|3.14.1|
|io.smallrye.config|smallrye-config-validator|3.14.1|
|io.smallrye.config|smallrye-config|3.14.1|
|io.smallrye.reactive|mutiny-smallrye-context-propagation|3.0.1|
|io.smallrye.reactive|mutiny-zero-flow-adapters|1.1.1|
|io.smallrye.reactive|mutiny|3.0.1|
|io.smallrye.reactive|smallrye-mutiny-vertx-auth-common|3.20.1|
|io.smallrye.reactive|smallrye-mutiny-vertx-bridge-common|3.20.1|
|io.smallrye.reactive|smallrye-mutiny-vertx-core|3.20.1|
|io.smallrye.reactive|smallrye-mutiny-vertx-runtime|3.20.1|
|io.smallrye.reactive|smallrye-mutiny-vertx-uri-template|3.20.1|
|io.smallrye.reactive|smallrye-mutiny-vertx-web-client|3.20.1|
|io.smallrye.reactive|smallrye-mutiny-vertx-web-common|3.20.1|
|io.smallrye.reactive|smallrye-mutiny-vertx-web|3.20.1|
|io.smallrye.reactive|smallrye-reactive-converter-api|3.0.3|
|io.smallrye.reactive|smallrye-reactive-converter-mutiny|3.0.3|
|io.smallrye.reactive|vertx-mutiny-generator|3.20.1|
|io.smallrye|jandex-gizmo2|3.5.2|
|io.smallrye|jandex|3.5.2|
|io.smallrye|smallrye-context-propagation-api|2.2.1|
|io.smallrye|smallrye-context-propagation-jta|2.2.1|
|io.smallrye|smallrye-context-propagation-storage|2.2.1|
|io.smallrye|smallrye-context-propagation|2.2.1|
|io.smallrye|smallrye-fault-tolerance-vertx|6.9.3|
|io.smallrye|smallrye-health-api|4.2.0|
|io.smallrye|smallrye-health-provided-checks|4.2.0|
|io.smallrye|smallrye-health|4.2.0|
|io.smallrye|smallrye-jwt-build|4.6.2|
|io.smallrye|smallrye-jwt-common|4.6.2|
|io.smallrye|smallrye-jwt|4.6.2|
|io.smallrye|smallrye-open-api-core|4.2.1|
|io.smallrye|smallrye-open-api-model|4.2.1|
|io.vertx|vertx-auth-common|4.5.22|
|io.vertx|vertx-bridge-common|4.5.22|
|io.vertx|vertx-codegen|4.5.22|
|io.vertx|vertx-core|4.5.22|
|io.vertx|vertx-uri-template|4.5.22|
|io.vertx|vertx-web-client|4.5.22|
|io.vertx|vertx-web-common|4.5.22|
|io.vertx|vertx-web|4.5.22|
|it.eng.parer|idp-jaas-rdbms|0.0.9|
|it.eng.parer|quarkus-custom-log-handlers|1.3.0|
|jakarta.activation|jakarta.activation-api|2.1.4|
|jakarta.annotation|jakarta.annotation-api|3.0.0|
|jakarta.authentication|jakarta.authentication-api|3.1.0|
|jakarta.authorization|jakarta.authorization-api|3.0.0|
|jakarta.el|jakarta.el-api|6.0.1|
|jakarta.enterprise|jakarta.enterprise.cdi-api|4.1.0|
|jakarta.enterprise|jakarta.enterprise.lang-model|4.1.0|
|jakarta.inject|jakarta.inject-api|2.0.1|
|jakarta.interceptor|jakarta.interceptor-api|2.2.0|
|jakarta.json|jakarta.json-api|2.1.3|
|jakarta.persistence|jakarta.persistence-api|3.2.0|
|jakarta.resource|jakarta.resource-api|2.1.0|
|jakarta.servlet|jakarta.servlet-api|6.0.0|
|jakarta.transaction|jakarta.transaction-api|2.0.1|
|jakarta.validation|jakarta.validation-api|3.1.1|
|jakarta.ws.rs|jakarta.ws.rs-api|3.1.0|
|jakarta.xml.bind|jakarta.xml.bind-api|4.0.4|
|javax.annotation|javax.annotation-api|1.3.2|
|javax.inject|javax.inject|1|
|net.bytebuddy|byte-buddy-agent|1.17.7|
|net.bytebuddy|byte-buddy|1.17.6|
|org.aesh|aesh|2.8.2|
|org.aesh|readline|2.6|
|org.antlr|antlr4-runtime|4.13.2|
|org.apache.commons|commons-compress|1.28.0|
|org.apache.commons|commons-lang3|3.18.0|
|org.apache.commons|commons-text|1.13.0|
|org.apache.groovy|groovy-json|4.0.22|
|org.apache.groovy|groovy-xml|4.0.22|
|org.apache.groovy|groovy|4.0.22|
|org.apache.httpcomponents|httpclient|4.5.14|
|org.apache.httpcomponents|httpcore|4.4.16|
|org.apache.httpcomponents|httpmime|4.5.14|
|org.apache.lucene|lucene-analysis-common|9.12.2|
|org.apache.lucene|lucene-core|9.12.2|
|org.apache.lucene|lucene-facet|9.12.2|
|org.apache.lucene|lucene-highlighter|9.12.2|
|org.apache.lucene|lucene-join|9.12.2|
|org.apache.lucene|lucene-memory|9.12.2|
|org.apache.lucene|lucene-queries|9.12.2|
|org.apache.lucene|lucene-queryparser|9.12.2|
|org.apache.lucene|lucene-sandbox|9.12.2|
|org.apache.maven.resolver|maven-resolver-api|1.9.24|
|org.apache.maven.resolver|maven-resolver-connector-basic|1.9.24|
|org.apache.maven.resolver|maven-resolver-impl|1.9.24|
|org.apache.maven.resolver|maven-resolver-named-locks|1.9.24|
|org.apache.maven.resolver|maven-resolver-spi|1.9.24|
|org.apache.maven.resolver|maven-resolver-transport-http|1.9.23|
|org.apache.maven.resolver|maven-resolver-transport-wagon|1.9.24|
|org.apache.maven.resolver|maven-resolver-util|1.9.24|
|org.apache.maven.shared|maven-shared-utils|3.4.2|
|org.apache.maven.wagon|wagon-file|3.5.3|
|org.apache.maven.wagon|wagon-http-shared|3.5.3|
|org.apache.maven.wagon|wagon-http|3.5.3|
|org.apache.maven.wagon|wagon-provider-api|3.5.3|
|org.apache.maven|maven-api-meta|4.0.0-alpha-7|
|org.apache.maven|maven-api-xml|4.0.0-alpha-7|
|org.apache.maven|maven-artifact|3.9.11|
|org.apache.maven|maven-builder-support|3.9.11|
|org.apache.maven|maven-core|3.9.11|
|org.apache.maven|maven-embedder|3.9.11|
|org.apache.maven|maven-model-builder|3.9.11|
|org.apache.maven|maven-model|3.9.11|
|org.apache.maven|maven-plugin-api|3.9.11|
|org.apache.maven|maven-repository-metadata|3.9.11|
|org.apache.maven|maven-resolver-provider|3.9.11|
|org.apache.maven|maven-settings-builder|3.9.11|
|org.apache.maven|maven-settings|3.9.11|
|org.apache.maven|maven-xml-impl|4.0.0-alpha-7|
|org.apache.santuario|xmlsec|4.0.2|
|org.apiguardian|apiguardian-api|1.1.2|
|org.bitbucket.b_c|jose4j|0.9.6|
|org.ccil.cowan.tagsoup|tagsoup|1.2.1|
|org.codehaus.plexus|plexus-cipher|2.0|
|org.codehaus.plexus|plexus-classworlds|2.6.0|
|org.codehaus.plexus|plexus-component-annotations|2.1.0|
|org.codehaus.plexus|plexus-interpolation|1.26|
|org.codehaus.plexus|plexus-sec-dispatcher|2.0|
|org.codehaus.plexus|plexus-utils|3.5.1|
|org.codehaus.plexus|plexus-xml|4.0.2|
|org.codehaus.woodstox|stax2-api|4.2.2|
|org.crac|crac|1.5.0|
|org.dom4j|dom4j|2.1.3|
|org.eclipse.angus|angus-activation|2.0.3|
|org.eclipse.microprofile.config|microprofile-config-api|3.1|
|org.eclipse.microprofile.context-propagation|microprofile-context-propagation-api|1.3|
|org.eclipse.microprofile.health|microprofile-health-api|4.0.1|
|org.eclipse.microprofile.jwt|microprofile-jwt-auth-api|2.1|
|org.eclipse.microprofile.metrics|microprofile-metrics-api|4.0.1|
|org.eclipse.microprofile.openapi|microprofile-openapi-api|4.1.1|
|org.eclipse.microprofile.reactive-streams-operators|microprofile-reactive-streams-operators-api|3.0.1|
|org.eclipse.parsson|parsson|1.1.7|
|org.eclipse.sisu|org.eclipse.sisu.inject|0.9.0.M4|
|org.eclipse.sisu|org.eclipse.sisu.plexus|0.9.0.M4|
|org.fusesource.jansi|jansi|2.4.0|
|org.fusesource.leveldbjni|leveldbjni-all|1.8|
|org.glassfish.expressly|expressly|6.0.0|
|org.glassfish.jaxb|jaxb-core|4.0.6|
|org.glassfish.jaxb|jaxb-runtime|4.0.6|
|org.glassfish.jaxb|txw2|4.0.6|
|org.graalvm.sdk|nativeimage|23.1.2|
|org.graalvm.sdk|word|23.1.2|
|org.hamcrest|hamcrest|2.2|
|org.hdrhistogram|HdrHistogram|2.2.2|
|org.hibernate.models|hibernate-models|1.0.1|
|org.hibernate.orm|hibernate-core|7.1.6.Final|
|org.hibernate.orm|hibernate-graalvm|7.1.6.Final|
|org.hibernate.search|hibernate-search-backend-lucene|8.1.2.Final|
|org.hibernate.search|hibernate-search-engine|8.1.2.Final|
|org.hibernate.search|hibernate-search-mapper-pojo-base|8.1.2.Final|
|org.hibernate.search|hibernate-search-util-common|8.1.2.Final|
|org.hibernate.search|hibernate-search-v5migrationhelper-engine|8.1.2.Final|
|org.hibernate.validator|hibernate-validator|9.0.1.Final|
|org.hibernate|quarkus-local-cache|0.3.1|
|org.infinispan.protostream|protostream-processor|5.0.13.Final|
|org.infinispan.protostream|protostream-types|5.0.13.Final|
|org.infinispan.protostream|protostream|5.0.13.Final|
|org.infinispan|infinispan-api|15.0.19.Final|
|org.infinispan|infinispan-commons-graalvm|15.0.19.Final|
|org.infinispan|infinispan-commons|15.0.19.Final|
|org.infinispan|infinispan-core-graalvm|15.0.19.Final|
|org.infinispan|infinispan-core|15.0.19.Final|
|org.infinispan|infinispan-objectfilter|15.0.19.Final|
|org.infinispan|infinispan-query-core|15.0.19.Final|
|org.infinispan|infinispan-query-dsl|15.0.19.Final|
|org.infinispan|infinispan-query|15.0.19.Final|
|org.jacoco|org.jacoco.agent|0.8.14|
|org.jacoco|org.jacoco.agent|runtime|
|org.jacoco|org.jacoco.core|0.8.14|
|org.jacoco|org.jacoco.report|0.8.14|
|org.jboss.invocation|jboss-invocation|2.0.0.Final|
|org.jboss.logging|commons-logging-jboss-logging|1.0.0.Final|
|org.jboss.logging|jboss-logging|3.6.1.Final|
|org.jboss.logmanager|jboss-logmanager|3.1.2.Final|
|org.jboss.narayana.jta|narayana-jta|7.3.3.Final|
|org.jboss.narayana.jts|narayana-jts-integration|7.3.3.Final|
|org.jboss.slf4j|slf4j-jboss-logmanager|2.0.2.Final|
|org.jboss.threads|jboss-threads|3.9.1|
|org.jboss|jboss-transaction-spi|8.0.0.Final|
|org.jctools|jctools-core|4.0.5|
|org.jgroups|jgroups-raft|1.0.14.Final|
|org.jgroups|jgroups|5.3.16.Final|
|org.jspecify|jspecify|1.0.0|
|org.junit.jupiter|junit-jupiter-api|5.13.4|
|org.junit.jupiter|junit-jupiter-engine|5.13.4|
|org.junit.jupiter|junit-jupiter-params|5.13.4|
|org.junit.jupiter|junit-jupiter|5.13.4|
|org.junit.platform|junit-platform-commons|1.13.4|
|org.junit.platform|junit-platform-engine|1.13.4|
|org.junit.platform|junit-platform-launcher|1.13.4|
|org.latencyutils|LatencyUtils|2.0.3|
|org.mockito|mockito-core|5.20.0|
|org.mockito|mockito-junit-jupiter|5.20.0|
|org.objenesis|objenesis|3.3|
|org.opentest4j|opentest4j|1.3.0|
|org.ow2.asm|asm-analysis|9.9|
|org.ow2.asm|asm-commons|9.9|
|org.ow2.asm|asm-tree|9.9|
|org.ow2.asm|asm-util|9.9|
|org.ow2.asm|asm|9.9|
|org.reactivestreams|reactive-streams|1.0.4|
|org.slf4j|slf4j-api|2.0.17|
|org.wildfly.common|wildfly-common|2.0.1|
|org.wildfly.security|wildfly-elytron-asn1|2.7.0.Final|
|org.wildfly.security|wildfly-elytron-auth-server|2.7.0.Final|
|org.wildfly.security|wildfly-elytron-auth|2.7.0.Final|
|org.wildfly.security|wildfly-elytron-base|2.7.0.Final|
|org.wildfly.security|wildfly-elytron-credential|2.7.0.Final|
|org.wildfly.security|wildfly-elytron-encryption|2.7.0.Final|
|org.wildfly.security|wildfly-elytron-keystore|2.7.0.Final|
|org.wildfly.security|wildfly-elytron-password-impl|2.7.0.Final|
|org.wildfly.security|wildfly-elytron-permission|2.7.0.Final|
|org.wildfly.security|wildfly-elytron-provider-util|2.7.0.Final|
|org.wildfly.security|wildfly-elytron-realm|2.7.0.Final|
|org.wildfly.security|wildfly-elytron-util|2.7.0.Final|
|org.wildfly.security|wildfly-elytron-x500-cert-util|2.7.0.Final|
|org.wildfly.security|wildfly-elytron-x500-cert|2.7.0.Final|
|org.wildfly.security|wildfly-elytron-x500|2.7.0.Final|
|org.yaml|snakeyaml|2.5|


## Lista licenze in uso


 * agpl_v3     : GNU Affero General Public License (AGPL) version 3.0
 * apache_v2   : Apache License version 2.0
 * bsd_2       : BSD 2-Clause License
 * bsd_3       : BSD 3-Clause License
 * cddl_v1     : COMMON DEVELOPMENT AND DISTRIBUTION LICENSE (CDDL) Version 1.0
 * epl_only_v1 : Eclipse Public License - v 1.0
 * epl_only_v2 : Eclipse Public License - v 2.0
 * epl_v1      : Eclipse Public + Distribution License - v 1.0
 * epl_v2      : Eclipse Public License - v 2.0 with Secondary License
 * eupl_v1_1   : European Union Public License v1.1
 * fdl_v1_3    : GNU Free Documentation License (FDL) version 1.3
 * gpl_v1      : GNU General Public License (GPL) version 1.0
 * gpl_v2      : GNU General Public License (GPL) version 2.0
 * gpl_v3      : GNU General Public License (GPL) version 3.0
 * lgpl_v2_1   : GNU General Lesser Public License (LGPL) version 2.1
 * lgpl_v3     : GNU General Lesser Public License (LGPL) version 3.0
 * mit         : MIT-License


# Supporto

Mantainer del progetto è [Engineering Ingegneria Informatica S.p.A.](https://www.eng.it/).

# Contributi

Se interessati a contribuire alla crescita del progetto potete scrivere all'indirizzo email <a href="mailto:areasviluppoparer@regione.emilia-romagna.it">areasviluppoparer@regione.emilia-romagna.it</a>.

# Credits

Progetto di proprietà di [Regione Emilia-Romagna](https://www.regione.emilia-romagna.it/) sviluppato a cura di [Engineering Ingegneria Informatica S.p.A.](https://www.eng.it/).

# Licenza

Questo progetto è rilasciato sotto licenza GNU Affero General Public License v3.0 or later ([LICENSE.txt](LICENSE.txt)).
