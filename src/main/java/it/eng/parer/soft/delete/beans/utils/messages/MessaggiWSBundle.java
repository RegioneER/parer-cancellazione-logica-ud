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
 * (written by Francesco Fioravanti)
 */
// Last update: 2018-02-08 17:52:30.148111
package it.eng.parer.soft.delete.beans.utils.messages;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quarkus.arc.Arc;
import it.eng.parer.soft.delete.beans.MessaggiWSCache;
import it.eng.parer.soft.delete.beans.utils.UUIDMdcLogUtil;

public class MessaggiWSBundle {

    private static Logger log = LoggerFactory.getLogger(MessaggiWSBundle.class);

    public static final String DEFAULT_LOCALE = "it";

    private MessaggiWSBundle() {
        throw new IllegalStateException("Utility class");
    }

    /*
     * Metodi statici, implementazione causata dalla necessità di mantenere invariata l'interfaccia
     * della classe originale: un normalissimo Bundle con un file di properties
     */
    public static String getString(String key) {
        switch (key) {
        case MessaggiWSBundle.ERR_666:
            return getDefaultErrorMessage(key);
        case MessaggiWSBundle.ERR_666P:
            return getDefaultErrorMessage(key);
        default:
            // l'operazione di StringEscapeUtils.unescapeJava viene svolta nel singleton
            return lookupCacheRef().getString(key);
        }
    }

    public static String getString(String key, Object... params) {
        switch (key) {
        case MessaggiWSBundle.ERR_666:
            return getDefaultErrorMessage(key, params);
        case MessaggiWSBundle.ERR_666P:
            return getDefaultErrorMessage(key, params);
        default:
            // l'operazione di StringEscapeUtils.unescapeJava viene svolta nel singleton
            return lookupCacheRef().getString(key, params);
        }
    }

    private static MessaggiWSCache lookupCacheRef() {
        return Arc.container().instance(MessaggiWSCache.class).get();
    }

    private static String getDefaultErrorMessage(String key, Object... params) {
        // get or generate uuid
        final String uuid = UUIDMdcLogUtil.getUuid();
        // log original message
        log.atError().log("Risposta originale : {}", lookupCacheRef().getString(key, params));
        return lookupCacheRef().getString(MessaggiWSBundle.WS_GENERIC_ERROR_UUID, uuid);
    }

    // ERRORI IMPREVISTI TEMPLATE (ossia da restituire all'utente a fronte degli
    // errori imprevisti)
    public static final String WS_GENERIC_ERROR_UUID = "WS-GENERIC-ERROR-UUID";
    // Le righe che seguono verranno mostrate raggruppate in Netbeans
    //
    // Nota: queste righe sono state create con uno script Python, che per ora
    // non è pubblico. Non appena sarà gradevole da vedere verrà integrato
    // con il resto dei sorgenti
    //
    // <editor-fold defaultstate="collapsed" desc="COSTANTI DI ERRORE">
    /**
     * Errore imprevisto: {0}
     */
    public static final String ERR_666 = "666";

    /**
     * Errore imprevisto nella persistenza: {0}
     */
    public static final String ERR_666P = "666P";

    /**
     * Errore imprevisto su servizio (Network problems): {0}
     */
    public static final String ERR_666N = "666N";

    /**
     * Errore nella struttura della chiamata al Web service: {0}
     */
    public static final String WS_CHECK = "WS-CHECK";

    /**
     * Errore: XML malformato nel blocco di dati generali. Eccezione:{0}
     */
    public static final String FAS_XSD_001_001 = "FAS_XSD-001-001";

    /**
     * Errore di validazione del blocco di dati generali. Eccezione:{0}
     */
    public static final String FAS_XSD_001_002 = "FAS_XSD-001-002";

    /**
     * Il campo {0} di tipo data breve, non è valorizzato. Un campo di tipo data breve deve essere
     * espresso nella forma aaaa-mm-gg
     */
    public static final String FAS_XSD_001_003 = "FAS_XSD-001-003";

    /**
     * Per questo servizio il tag {0} è obbligatorio
     */
    public static final String FAS_XSD_002_001 = "FAS_XSD-002-001";

    /**
     * Per questo servizio il tag {0} non è supportato
     */
    public static final String FAS_XSD_002_002 = "FAS_XSD-002-002";

    /**
     * L''ambiente non è valorizzato o il valore indicato {0} non è definito nel sistema
     */
    public static final String FAS_CONFIG_001_001 = "FAS_CONFIG-001-001";

    /**
     * L''ente {0} non è valorizzato o è valorizzato con un valore non definito
     */
    public static final String FAS_CONFIG_001_002 = "FAS_CONFIG-001-002";

    /**
     * La struttura {0} non è valorizzata o è valorizzata con un valore non definito
     */
    public static final String FAS_CONFIG_001_003 = "FAS_CONFIG-001-003";

    /**
     * La struttura {0} è un template e non è abilitata al versamento
     */
    public static final String FAS_CONFIG_001_004 = "FAS_CONFIG-001-004";

    /**
     * La struttura {0} non \u00e8 valida alla data corrente e non accetta versamenti
     */
    public static final String FAS_CONFIG_001_005 = "FAS_CONFIG-001-005";

    /**
     * Il valore {0} indicato nel tag &lt;UserID&gt; non coincide con l''utente indicato nella
     * chiamata al WS
     */
    public static final String FAS_CONFIG_002_001 = "FAS_CONFIG-002-001";

    /**
     * La password dell''utente {0} è scaduta
     */
    public static final String FAS_CONFIG_002_002 = "FAS_CONFIG-002-002";

    /**
     * Errore di autenticazione: {0}
     */
    public static final String FAS_CONFIG_002_003 = "FAS_CONFIG-002-003";

    /**
     * L''utente {0} non è attivo. Autenticazione fallita
     */
    public static final String FAS_CONFIG_002_004 = "FAS_CONFIG-002-004";

    /**
     * L''utente {0} non è autorizzato alla funzione {1}
     */
    public static final String FAS_CONFIG_002_005 = "FAS_CONFIG-002-005";

    /**
     * L''utente {0} non è abilitato entro la struttura versante
     */
    public static final String FAS_CONFIG_002_006 = "FAS_CONFIG-002-006";

    /**
     * Nella chiamata al WS, il login name deve essere dichiarato
     */
    public static final String FAS_CONFIG_002_007 = "FAS_CONFIG-002-007";

    /**
     * Nella chiamata al WS è necessario indicare la versione di riferimento
     */
    public static final String FAS_CONFIG_003_001 = "FAS_CONFIG-003-001";

    /**
     * La versione {0} non è supportata.
     */
    public static final String FAS_CONFIG_003_002 = "FAS_CONFIG-003-002";

    /**
     * Il valore {0} indicato nel tag &lt;Versione&gt; non coincide con la versione indicata nella
     * chiamata al WS
     */
    public static final String FAS_CONFIG_003_003 = "FAS_CONFIG-003-003";

    /**
     * Il soggetto produttore con codice {0} è valorizzato con un valore non definito
     */
    public static final String FAS_CONFIG_004_001 = "FAS_CONFIG-004-001";

    /**
     * Il soggetto produttore con denominazione {0} è valorizzato con un valore non definito
     */
    public static final String FAS_CONFIG_004_002 = "FAS_CONFIG-004-002";

    /**
     * Il soggetto produttore non è supportato in questa release
     */
    public static final String FAS_CONFIG_004_003 = "FAS_CONFIG-004-003";

    /**
     * I dati specifici non sono supportati in questa release
     */
    public static final String FAS_CONFIG_005_001 = "FAS_CONFIG-005-001";

    /**
     * Il parametro {0} non \u00e8 stato configurato
     */
    public static final String FAS_CONFIG_006_001 = "FAS_CONFIG-006-001";

    /**
     * I sotto fascicoli non sono supportati in questa release
     */
    public static final String FAS_CONFIG_007_001 = "FAS_CONFIG-007-001";

    /**
     * Fascicolo {0}:la chiave indicata corrisponde ad un fascicolo già presente nel sistema
     */
    public static final String FASC_001_001 = "FASC-001-001";

    /**
     * Fascicolo {0}: il tipo {1} non è presente entro la struttura versante o è disattivo
     */
    public static final String FASC_002_001 = "FASC-002-001";

    /**
     * Fascicolo {0}: l'utente versatore non è abilitato al tipo fascicolo {1}
     */
    public static final String FASC_002_002 = "FASC-002-002";

    /**
     * Fascicolo {0}: il tipo fascicolo {1} non è valido nell'anno {2}
     */
    public static final String FASC_002_003 = "FASC-002-003";

    /**
     * Fascicolo {0}:il numero di unità documentarie indicato nel contenuto sintetico {1} non
     * coincide con il numero di unità documentarie elencate nel contenuto analitico
     */
    public static final String FASC_003_001 = "FASC-003-001";

    /**
     * Fascicolo {0}:è necessario valorizzare il tag &lt;ContenutoAnaliticoUnitaDocumentarie&gt;
     */
    public static final String FASC_003_002 = "FASC-003-002";

    /**
     * Fascicolo {0}: l''unità documentaria {1} indicata nel contenuto non è presente in Sacer o è
     * stata annullata.
     */
    public static final String FASC_003_003 = "FASC-003-003";

    /**
     * Fascicolo {0}: verificare che la posizione indicata nelle unità documentarie sia univoca
     */
    public static final String FASC_003_004 = "FASC-003-004";

    /**
     * Fascicolo {0}: verificare che le seguenti chiavi {1} indicate delle unità documentarie siano
     * univoche
     */
    public static final String FASC_003_005 = "FASC-003-005";

    /**
     * Fascicolo {0}:il numero di sotto fascicoli indicato nel contenuto sintetico {1} non coincide
     * con il numero di fascicoli elencati nel contenuto
     */
    public static final String FASC_003_XXX_NOTIMPLYET = "FASC-003-XXX_NOTIMPLYET";

    /**
     * Controllo non implementato.
     *
     * Fascicolo {0}: il fascicolo {1} indicato nel contenuto non è presente in Sacer o è stato
     * annullato.
     */
    public static final String FASC_003_YYY_NOTIMPLYET = "FASC-003-YYY_NOTIMPLYET";

    /**
     * Sulla struttura versante non è definito un titolario di classificazione attivo alla data di
     * apertura del fascicolo
     */
    public static final String FASC_004_001 = "FASC-004-001";

    /**
     * Nel SIP di Versamento del Fascicolo non è stato indicato l''indice di classificazione o
     * l''indice non è coerente con le voci di classificazione descritte
     */
    public static final String FASC_004_002 = "FASC-004-002";

    /**
     * Non esiste una voce di titolario attiva alla data di apertura del fascicolo coincidente con
     * l''indice di classificazione {0}
     */
    public static final String FASC_004_003 = "FASC-004-003";

    /**
     * La descrizione della voce di classificazione + codice voce classificazione non coincide con
     * la descrizione presente sul titolario
     */
    public static final String FASC_004_004 = "FASC-004-004";

    /**
     * Fascicolo {0}: il codice indicato non rispetta i requisiti di formato: {1}
     */
    public static final String FASC_005_001 = "FASC-005-001";

    /**
     * Fascicolo {0}: sul periodo di validità non è definito il formato numero
     */
    public static final String FASC_005_002 = "FASC-005-002";

    /**
     * Il fascicolo {0} da collegare al fascicolo oggetto di versamento non è presente nel sistema
     */
    public static final String FASC_006_001 = "FASC-006-001";

    /**
     * Fascicolo {0}: la descrizione del fascicolo da collegare supera il limite previsto.
     */
    public static final String FASC_006_002 = "FASC-006-002";

    /**
     * Fascicolo {0}: se presente un profilo archivistico, è necessario indicare la versione
     */
    public static final String FAS_PF_ARCH_001_001 = "FAS_PF_ARCH-001-001";

    /**
     * Fascicolo {0}: La versione {1} dell''xsd di profilo archivistico del fascicolo non è presente
     * o è disattiva
     */
    public static final String FAS_PF_ARCH_001_002 = "FAS_PF_ARCH-001-002";

    /**
     * Fascicolo {0}: il tipo fascicolo {1} non ha associato un xsd di profilo archivistico nel
     * periodo di validità vigente nell''anno del fascicolo.
     */
    public static final String FAS_PF_ARCH_001_003 = "FAS_PF_ARCH-001-003";

    /**
     * Fascicolo {0}: il tipo fascicolo {1} ha associato un xsd di profilo archivistico e non è
     * stato dichiarato nell''XML di Versamento
     */
    public static final String FAS_PF_ARCH_001_004 = "FAS_PF_ARCH-001-004";

    /**
     * Fascicolo {0}: il tipo fascicolo {1} ha associato un xsd di profilo specifico e non è stato
     * dichiarato nell''XML di Versamento
     */
    public static final String FAS_PF_SPEC_001_001 = "FAS_PF_SPEC-001-001";

    /**
     * Fascicolo {0}: La versione {1} dell''xsd di profilo specifico del fascicolo non è presente o
     * è disattiva
     */
    public static final String FAS_PF_SPEC_001_002 = "FAS_PF_SPEC-001-002";

    /**
     * Fascicolo {0}: se presente un profilo specifico, è necessario indicarne la versione
     */
    public static final String FAS_PF_SPEC_001_003 = "FAS_PF_SPEC-001-003";

    /**
     * Fascicolo {0}: il tipo fascicolo {1} non ha associato un xsd di profilo specifico nel periodo
     * di validità vigente nell''anno del fascicolo.
     */
    public static final String FAS_PF_SPEC_001_004 = "FAS_PF_SPEC-001-004";

    /**
     * Fascicolo {0}: il tipo fascicolo {1} ha associato un xsd di profilo normativo e non è stato
     * dichiarato nell''XML di Versamento
     */
    public static final String FAS_PF_NORM_001_001 = "FAS_PF_NORM-001-001";

    /**
     * Fascicolo {0}: La versione {1} dell'xsd di profilo normativo del fascicolo non \u00E8
     * presente o \u00E8 disattiva.
     */
    public static final String FAS_PF_NORM_001_002 = "FAS_PF_NORM-001-002";

    /**
     * Fascicolo {0}: se presente un profilo normativo, è necessario indicarne la versione
     */
    public static final String FAS_PF_NORM_001_003 = "FAS_PF_NORM-001-003";

    /**
     * Fascicolo {0}: il tipo fascicolo {1} non ha associato un xsd di profilo normativo nel periodo
     * di validit\u00E0 vigente nell''anno del fascicolo
     */
    public static final String FAS_PF_NORM_001_004 = "FAS_PF_NORM-001-004";

    /**
     * Fascicolo {0}: Errore nella verifica dei dati di profilo archivistico del fascicolo. {1}
     */
    public static final String FAS_PF_ARCH_002_001 = "FAS_PF_ARCH-002-001";

    /**
     * Fascicolo {0}: Errore nella verifica dei dati di profilo archivistico del fascicolo. Il
     * valore indicato nel tag {0} supera la lunghezza massima prevista
     */
    public static final String FAS_PF_ARCH_002_002 = "FAS_PF_ARCH-002-002";

    /**
     * Fascicolo {0}: Errore nella verifica dei dati di profilo specifico del fascicolo. {1}
     */
    public static final String FAS_PF_SPEC_002_001 = "FAS_PF_SPEC-002-001";

    /**
     * Fascicolo {0}: Errore nella verifica dei dati di profilo normativo del fascicolo. {1}
     */
    public static final String FAS_PF_NORM_002_001 = "FAS_PF_NORM-002-001";

    /**
     * Fascicolo {0}: il tipo fascicolo {1} non ha associato un xsd di profilo fascicolo standard
     * nel periodo di validità vigente nell''anno del fascicolo.
     */
    public static final String FAS_PF_GEN_001_001 = "FAS_PF_GEN-001-001";

    /**
     * Fascicolo {0}: La versione {1} dell''xsd di profilo generale del fascicolo non è presente o è
     * disattiva
     */
    public static final String FAS_PF_GEN_001_002 = "FAS_PF_GEN-001-002";

    /**
     * Fascicolo {0}: Necessario indicare la versione dell''xsd di profilo generale del fascicolo
     */
    public static final String FAS_PF_GEN_001_003 = "FAS_PF_GEN-001-003";

    /**
     * Fascicolo {0}: Errore nella verifica dei dati di profilo generale del fascicolo. {1}
     */
    public static final String FAS_PF_GEN_002_001 = "FAS_PF_GEN-002-001";

    /**
     * Fascicolo {0}: la data di apertura è più recente della data di chiusura del fascicolo
     */
    public static final String FAS_PF_GEN_003_001 = "FAS_PF_GEN-003-001";

    /**
     * Fascicolo {0}: il tipo conservazione è IN_ARCHIVIO e non è stata indicata la data di chiusura
     * del fascicolo
     */
    public static final String FAS_PF_GEN_003_002 = "FAS_PF_GEN-003-002";

    /**
     * Fascicolo {0}: l''unità documentaria {1} indicata come PrimoDocumentoNelFascicolo non è
     * presente nel contenuto analitico del fascicolo
     */
    public static final String FAS_PF_GEN_003_003 = "FAS_PF_GEN-003-003";

    /**
     * Fascicolo {0}: l''unità documentaria {1} indicata come UltimoDocumentoNelFascicolo non è
     * presente nel contenuto analitico del fascicolo
     */
    public static final String FAS_PF_GEN_003_004 = "FAS_PF_GEN-003-004";

    /**
     * Fascicolo {0}: è necessario indicare il tempo di conservazione
     */
    public static final String FAS_PF_GEN_003_005 = "FAS_PF_GEN-003-005";

    /**
     * Fascicolo {0}: informazioni su soggetto coinvolto mancanti, indicare Denominazione oppure
     * Cognome e Nome del soggetto
     */
    public static final String FAS_PF_GEN_003_006 = "FAS_PF_GEN-003-006";

    /**
     * Fascicolo {0}: informazioni su soggetto coinvolto mancanti, indicare sia il Cognome che il
     * Nome del soggetto
     */
    public static final String FAS_PF_GEN_003_007 = "FAS_PF_GEN-003-007";

    /**
     * Fascicolo {0}: informazioni su soggetto coinvolto errate, non è possibile indicare per un
     * soggetto sia il Cognome/Nome sia la Denominazione
     */
    public static final String FAS_PF_GEN_003_008 = "FAS_PF_GEN-003-008";

    /**
     * Fascicolo {0}: non è possibile inserire per lo stesso soggetto {1} il tipo di rapporto
     * indicato {2}
     */
    public static final String FAS_PF_GEN_003_009 = "FAS_PF_GEN-003-009";

    /**
     * Fascicolo {0}: il tipo conservazione VERSAMENTO_ANTICIPATO non è supportato dalla versione
     * del WS
     */
    public static final String FAS_PF_GEN_003_010 = "FAS_PF_GEN-003-010";

    /**
     * Fascicolo {0}: non è possibile inserire più di un codice IPA per lo stesso soggetto {1}
     */
    public static final String FAS_PF_GEN_003_011 = "FAS_PF_GEN-003-011";

    /**
     * Fascicolo {0}: evento {1} riporta una data di inizio pi\u00f9 recente della data di fine
     */
    public static final String FAS_PF_GEN_003_012 = "FAS_PF_GEN-003-012";

    /**
     * Fascicolo {0}: informazioni su soggetto coinvolto mancanti, indicare Denominazione oppure
     * Cognome e Nome del soggetto ({1})
     */
    public static final String FAS_PF_NORM_003_001 = "FAS_PF_NORM-003-001";

    /**
     * Fascicolo {0}: informazioni su soggetto coinvolto mancanti, indicare sia il Cognome che il
     * Nome del soggetto ({1})
     */
    public static final String FAS_PF_NORM_003_002 = "FAS_PF_NORM-003-002";

    /**
     * Fascicolo {0}: informazioni su soggetto coinvolto errate, non è possibile indicare per un
     * soggetto sia il Cognome/Nome sia la Denominazione ({1})
     */
    public static final String FAS_PF_NORM_003_003 = "FAS_PF_NORM-003-003";

    /**
     * Fascicolo {0}: non è possibile inserire per lo stesso soggetto {1} il tipo di rapporto
     * indicato {2}
     */
    public static final String FAS_PF_NORM_003_004 = "FAS_PF_NORM-003-004";

    /**
     * Fascicolo {0}: non è possibile inserire più di un codice IPA per lo stesso soggetto {1}
     */
    public static final String FAS_PF_NORM_003_005 = "FAS_PF_NORM-003-005";

    /**
     * Fascicolo {0}: non consentita la tipologia di aggregazione {1}, da indicare quella di tipo
     * {2}
     */
    public static final String FAS_PF_NORM_003_006 = "FAS_PF_NORM-003-006";

    /**
     * Errore: XML malformato nel blocco di dati generali. Eccezione: {0}
     */
    public static final String XSD_001_001 = "XSD-001-001";

    /**
     * Errore di validazione del blocco di dati generali. Eccezione: {0}
     */
    public static final String XSD_001_002 = "XSD-001-002";

    /**
     * Controllare che i tag &lt;ID&gt; dei componenti e dei sottocomponenti siano stati valorizzati
     * correttamente. I valori devono essere univoci entro l''Unità Documentaria
     */
    public static final String XSD_002_001 = "XSD-002-001";

    /**
     * Controllare che i tag &lt;IDDocumento&gt; di ogni documento siano stati valorizzati
     * correttamente. I valori devono essere univoci entro l''Unità Documentaria
     */
    public static final String XSD_002_002 = "XSD-002-002";

    /**
     * Il campo {0} di tipo data breve, non è valorizzato. Un campo di tipo data breve deve essere
     * espresso nella forma aaaa-mm-gg
     */
    public static final String XSD_002_003 = "XSD-002-003";

    /**
     * Il numero di allegati dichiarato non corrisponde al numero di elementi &lt;Allegato&gt;
     */
    public static final String XSD_003_001 = "XSD-003-001";

    /**
     * Il numero di annessi dichiarato non corrisponde al numero di elementi &lt;Annesso&gt;
     */
    public static final String XSD_004_001 = "XSD-004-001";

    /**
     * Il numero di annotazioni dichiarate non corrisponde al numero di elementi &lt;Annotazione&gt;
     */
    public static final String XSD_005_001 = "XSD-005-001";

    /**
     * Il tipo di conservazione è MIGRAZIONE ma la versione del WS non lo supporta
     */
    public static final String XSD_006_001 = "XSD-006-001";

    /**
     * Il tipo di conservazione è MIGRAZIONE ma non è indicato il sistema di migrazione
     */
    public static final String XSD_006_002 = "XSD-006-002";

    /**
     * E'' stato indicato un sistema di migrazione ma il tipo di conservazione non è MIGRAZIONE
     */
    public static final String XSD_006_003 = "XSD-006-003";

    /**
     * Sono stati indicati dati specifici di migrazione, ma il tipo di conservazione non è
     * MIGRAZIONE
     */
    public static final String XSD_006_004 = "XSD-006-004";

    /**
     * Sono stati indicati dati specifici estesi, ma la versione del WS non li supporta
     */
    public static final String XSD_006_005 = "XSD-006-005";

    /**
     * Il tipo di aggiornamento specificato non è fra i valori ammessi
     */
    public static final String XSD_006_006 = "XSD-006-006";

    /**
     * {0} {1}: {2} E'' stato indicato il profilo {3} per ma la versione del WS non lo supporta
     */
    public static final String XSD_006_007 = "XSD-006-007";

    /**
     * Per questo servizio il tag {0} è obbligatorio
     */
    public static final String XSD_011_001 = "XSD-011-001";

    /**
     * Per questo servizio il tag {0} non è supportato
     */
    public static final String XSD_011_002 = "XSD-011-002";

    /**
     * E'' stato specificato il tag {0}, ma la versione indicata del WS non lo supporta
     */
    public static final String XSD_011_003 = "XSD-011-003";

    /**
     * L''Ambiente {0} non è presente nel sistema
     */
    public static final String UD_001_001 = "UD-001-001";

    /**
     * L''Ente {0} non è presente nel sistema
     */
    public static final String UD_001_002 = "UD-001-002";

    /**
     * La Struttura {0} non è presente nel sistema
     */
    public static final String UD_001_003 = "UD-001-003";

    /**
     * Nella chiamata al WS, il login name deve essere dichiarato
     */
    public static final String UD_001_004 = "UD-001-004";

    /**
     * Il valore [{0}] indicato nel tag &lt;UserID&gt; non coincide con l''utente indicato nella
     * chiamata al WS
     */
    public static final String UD_001_005 = "UD-001-005";

    /**
     * La password dell''utente {0} è scaduta
     */
    public static final String UD_001_006 = "UD-001-006";

    /**
     * L''utente {0} è disattivato. Autenticazione fallita
     */
    public static final String UD_001_007 = "UD-001-007";

    /**
     * L''utente {0} non è autorizzato alla funzione {1}
     */
    public static final String UD_001_008 = "UD-001-008";

    /**
     * L''utente {0} non è abilitato entro la struttura versante
     */
    public static final String UD_001_009 = "UD-001-009";

    /**
     * Nella chiamata al WS, è necessario indicare la versione di riferimento
     */
    public static final String UD_001_010 = "UD-001-010";

    /**
     * La versione [{0}] indicata non è supportata
     */
    public static final String UD_001_011 = "UD-001-011";

    /**
     * Errore di autenticazione: {0}
     */
    public static final String UD_001_012 = "UD-001-012";

    /**
     * Il valore [{0}] indicato nel tag &lt;Versione&gt; non coincide con la versione indicata nella
     * chiamata al WS
     */
    public static final String UD_001_013 = "UD-001-013";

    /**
     * Il nome del sistema di migrazione {0} non è presente nel sistema
     */
    public static final String UD_001_014 = "UD-001-014";

    /**
     * La Struttura {0} è un template e non è abilitata al versamento
     */
    public static final String UD_001_015 = "UD-001-015";

    /**
     * L'utente non è abilitato al tipo di unità documentaria {0} oppure al registro {1}
     */
    public static final String UD_001_016 = "UD-001-016";

    /**
     * L'utente non è abilitato al tipo documento {0}
     */
    public static final String UD_001_017 = "UD-001-017";

    /**
     * Per la struttura {0} deve essere definito il "Tipo di unita documentaria sconosciuta", il
     * "Registro sconosciuto" ed il Tipo documento principale sconosciuto"
     */
    public static final String UD_001_018 = "UD-001-018";

    /**
     * La struttura {0} non \u00e8 valida alla data corrente e non accetta versamenti
     */
    public static final String UD_001_019 = "UD-001-019";

    /**
     * Unità Documentaria {0}: la chiave indicata corrisponde ad una Unità Documentaria già presente
     * nel sistema
     */
    public static final String UD_002_001 = "UD-002-001";

    /**
     * Unità Documentaria {0}: la tipologia {1} non è presente entro la struttura versante
     */
    public static final String UD_003_001 = "UD-003-001";

    /**
     * Unità Documentaria {0}: il tipo registro {1} non è presente entro la struttura versante
     */
    public static final String UD_003_002 = "UD-003-002";

    /**
     * Unità Documentaria {0}: il tipo registro {1} non è associato alla tipologia di unità
     * documentaria {2}
     */
    public static final String UD_003_003 = "UD-003-003";

    /**
     * Unità Documentaria {0}: l''anno {2} non è valido per il tipo registro {1}
     */
    public static final String UD_003_004 = "UD-003-004";

    /**
     * Unità Documentaria {0}: esiste più di una tipologia di unità documentaria {1} attiva alla
     * data corrente entro la struttura versante. E’ necessario correggere la configurazione della
     * struttura prima di ripetere il versamento.
     */
    public static final String UD_003_005 = "UD-003-005";

    /**
     * Unità Documentaria {0}: l''Unità Documentaria {1} da collegare alla UD oggetto di versamento
     * non è presente nel sistema
     */
    public static final String UD_004_001 = "UD-004-001";

    /**
     * Unità Documentaria {0}: il registro dell''unità documentaria {1} da collegare alla UD oggetto
     * di versamento non è presente nella struttura o non è attivo
     */
    public static final String UD_004_003 = "UD-004-003";

    /**
     * Unità Documentaria {0}: l''anno dell''unità documentaria {1} da collegare alla UD oggetto di
     * versamento non è compreso nei periodi di validità del registro{2}
     */
    public static final String UD_004_004 = "UD-004-004";

    /**
     * Unità Documentaria {0}: il numero dell''unità documentaria {1} da collegare alla UD oggetto
     * di versamento non rispetta le regole configurate sul registro nel periodo di validità: {2}
     */
    public static final String UD_004_005 = "UD-004-005";

    /**
     * Unità Documentaria {0}: la descrizione delle unità documentarie da collegare supera il limite
     * previsto
     */
    public static final String UD_004_006 = "UD-004-006";

    /**
     * Unità Documentaria {0}: la chiave indicata non corrisponde a nessuna Unità Documentaria
     * presente nel sistema
     */
    public static final String UD_005_001 = "UD-005-001";

    /**
     * Unità Documentaria {0}: L''unità documentaria non è contenuta in alcun volume di
     * conservazione
     */
    public static final String UD_005_002 = "UD-005-002";

    /**
     * Unità Documentaria {0}: L''indice AIP dell''unità documentaria deve ancora essere prodotto
     */
    public static final String UD_005_003 = "UD-005-003";

    /**
     * Unità Documentaria {0}: Il Rapporto di Versamento dell''unità documentaria non è stato
     * prodotto
     */
    public static final String UD_005_004 = "UD-005-004";

    /**
     * Unità Documentaria {0}: Nel sistema é già presente una unità documentaria con lo stesso
     * numero normalizzato prodotto
     */
    public static final String UD_005_005 = "UD-005-005";

    /**
     * Unità Documentaria {0}: la chiave indicata non rispetta i requisiti di formato; {1}
     */
    public static final String UD_007_001 = "UD-007-001";

    /**
     * Unità Documentaria {0}: la lunghezza di &lt;registro&gt;-&lt;anno&gt;-&lt;numero
     * normalizzato&gt; supera i 100 caratteri e non consente la normalizzazione
     */
    public static final String UD_007_002 = "UD-007-002";

    /**
     * Unità Documentaria {0}: l''URN calcolato per i componenti di questa unità documentaria supera
     * i 254 caratteri
     */
    public static final String UD_007_003 = "UD-007-003";

    /**
     * Unità Documentaria {0}: non sono stati trovati componenti firmati digitalmente
     */
    public static final String UD_008_001 = "UD-008-001";

    /**
     * Unità Documentaria {0}: impossibile memorizzare i dati relativi al certificato di firma. Il
     * versamento deve essere ripetuto.
     */
    public static final String UD_008_002 = "UD-008-002";

    /**
     * Unità Documentaria {0}: conservazione NON FISCALE nonostante il registro sia fiscalmente
     * rilevante
     */
    public static final String UD_009_001 = "UD-009-001";

    /**
     * Unità Documentaria {0}: conservazione FISCALE nonostante il registro NON sia fiscalmente
     * rilevante
     */
    public static final String UD_009_002 = "UD-009-002";

    /**
     * Unità Documentaria {0}: Errore nel calcolo della Substruttura: {1}
     */
    public static final String UD_011_001 = "UD-011-001";

    /**
     * Unità Documentaria {0}: Il dato specifico {1} dell''Unità Documentaria è nullo: impossibile
     * determinare la Substruttura
     */
    public static final String UD_011_002 = "UD-011-002";

    /**
     * Unità Documentaria {0}: Il dato specifico {1} del Documento Principale dell''Unità
     * Documentaria è nullo: impossibile determinare la Substruttura
     */
    public static final String UD_011_003 = "UD-011-003";

    /**
     * Unità Documentaria {0}: Il dato specifico {1} dell''Unità Documentaria non individua una
     * Substruttura valida
     */
    public static final String UD_011_004 = "UD-011-004";

    /**
     * Unità Documentaria {0}: Il dato specifico {1} del Documento Principale dell''Unità
     * Documentaria non individua una Substruttura valida
     */
    public static final String UD_011_005 = "UD-011-005";

    /**
     * Unità Documentaria {0}: L''unità documentaria è sottoposta a sequestro
     */
    public static final String UD_012_001 = "UD-012-001";

    /**
     * Unità Documentaria {0}: L''unità documentaria è stata annullata
     */
    public static final String UD_012_002 = "UD-012-002";

    /**
     * Unità Documentaria {0}: L''unità documentaria è inserita in almeno una serie; per aggiungere
     * il documento è necessario richiedere l''aggiornamento della serie
     */
    public static final String UD_013_001 = "UD-013-001";

    /**
     * Per unità documentaria {0} non è abilitato l'aggiornamento dei metadati
     */
    public static final String UD_013_002 = "UD-013-002";

    /**
     * Unità Documentaria {0} ha stato di conservazione {1} e, quindi, non può essere modificata
     */
    public static final String UD_013_003 = "UD-013-003";

    /**
     * Unità Documentaria {0}: L''oggetto è un dato di profilo configurato come obbligatorio
     */
    public static final String UD_014_001 = "UD-014-001";

    /**
     * Unità Documentaria {0}: La data dell''unità documentaria è un dato di profilo configurato
     * come obbligatorio
     */
    public static final String UD_014_002 = "UD-014-002";

    /**
     * L’unità Documentaria {0} ha tipo diverso da {1}
     */
    public static final String UD_015_001 = "UD-015-001";

    /**
     * Aggiornamento UD: Controllo HASH SIP
     */
    public static final String UD_015_002 = "UD-015-002";

    /**
     * Per almeno un documento da aggiornare si è rilevato almeno un errore
     */
    public static final String UD_016_001 = "UD-016-001";

    /**
     * Per almeno un componente da aggiornare si è rilevato almeno un errore
     */
    public static final String UD_016_002 = "UD-016-002";

    /**
     * Almeno un collegamento da aggiornare presenta un errore
     */
    public static final String UD_016_003 = "UD-016-003";

    /**
     * Un fascicolo secondario coincide con il fascicolo principale precedentemente versato
     */
    public static final String UD_017_001 = "UD-017-001";

    /**
     * Il parametro {0} non \u00e8 stato configurato
     */
    public static final String UD_018_001 = "UD-018-001";

    /**
     * Lo xml ricevuto non è coerente con lo xsd previsto
     */
    public static final String RICH_SOFT_DELETE_001 = "RICH-SOFT-DELETE-001";

    /**
     * L''utente specificato nell''xml non coincide con quello che ha attivato il servizio
     */
    public static final String RICH_SOFT_DELETE_002 = "RICH-SOFT-DELETE-002";

    /**
     * L''utente che ha attivato il servizio non è abilitato alla struttura versante oppure non è
     * autorizzato ad attivare il servizio
     */
    public static final String RICH_SOFT_DELETE_003 = "RICH-SOFT-DELETE-003";

    /**
     * La versione dell''xml di richiesta specificata nell''xml non coincide con quello ricevuto
     * nell''attivazione del web service
     */
    public static final String RICH_SOFT_DELETE_005 = "RICH-SOFT-DELETE-005";

    /**
     * Nessuna unità documentaria definita nella richiesta è elaborabile
     */
    public static final String RICH_SOFT_DELETE_006 = "RICH-SOFT-DELETE-006";

    /**
     * Nessun annullamento definito nella richiesta è elaborabile
     */
    public static final String RICH_SOFT_DELETE_007 = "RICH-SOFT-DELETE-007";

    /**
     * Nessuna restituzione definita nella richiesta è elaborabile
     */
    public static final String RICH_SOFT_DELETE_008 = "RICH-SOFT-DELETE-008";

    /**
     * Nessuno scarto definito nella richiesta è elaborabile
     */
    public static final String RICH_SOFT_DELETE_009 = "RICH-SOFT-DELETE-009";

    /**
     * Alcune unità documentarie definite nella richiesta non sono elaborabili
     */
    public static final String RICH_SOFT_DELETE_010 = "RICH-SOFT-DELETE-010";

    /**
     * Alcuni annullamenti definiti nella richiesta non sono elaborabili
     */
    public static final String RICH_SOFT_DELETE_011 = "RICH-SOFT-DELETE-011";

    /**
     * Alcune restituzioni definite nella richiesta non sono elaborabili
     */
    public static final String RICH_SOFT_DELETE_012 = "RICH-SOFT-DELETE-012";

    /**
     * Alcuni scarti definiti nella richiesta non sono elaborabili
     */
    public static final String RICH_SOFT_DELETE_013 = "RICH-SOFT-DELETE-013";

    /**
     * Le richieste di cancellazione logica specificate nella richiesta devono avere stesso Tipo
     * Richiesta
     */
    public static final String RICH_SOFT_DELETE_017 = "RICH-SOFT-DELETE-017";

    /**
     * Per almeno un annullamento definito nella richiesta è presente il Tipo Registro ma non è
     * previsto
     */
    public static final String RICH_SOFT_DELETE_018 = "RICH-SOFT-DELETE-018";

    /**
     * Per almeno una restituzione definita nella richiesta è presente il Tipo Registro ma non è
     * previsto
     */
    public static final String RICH_SOFT_DELETE_019 = "RICH-SOFT-DELETE-019";

    /**
     * Per almeno uno scarto definito nella richiesta è presente il Tipo Registro ma non è previsto
     */
    public static final String RICH_SOFT_DELETE_020 = "RICH-SOFT-DELETE-020";

    /**
     * Per almeno una unità documentaria definita nella richiesta è necessario specificare il Tipo
     * Registro
     */
    public static final String RICH_SOFT_DELETE_021 = "RICH-SOFT-DELETE-021";

    /**
     * Per almeno una annullamento definito nella richiesta è necessario specificare l''ID Richiesta
     * Sacer
     */
    public static final String RICH_SOFT_DELETE_022 = "RICH-SOFT-DELETE-022";

    /**
     * Per almeno una restituzione definita nella richiesta è necessario specificare l''ID Richiesta
     * Sacer
     */
    public static final String RICH_SOFT_DELETE_023 = "RICH-SOFT-DELETE-023";

    /**
     * Per almeno uno scarto definito nella richiesta è necessario specificare l''ID Richiesta Sacer
     */
    public static final String RICH_SOFT_DELETE_024 = "RICH-SOFT-DELETE-024";

    /**
     * Per almeno una unità documentaria definita nella richiesta è presente l''ID Richiesta Sacer
     * ma non è previsto
     */
    public static final String RICH_SOFT_DELETE_025 = "RICH-SOFT-DELETE-025";

    /**
     * Il valore del TipoCancellazione è incompatibile col TipoRichiesta
     */
    public static final String RICH_SOFT_DELETE_026 = "RICH-SOFT-DELETE-026";

    // </editor-fold>

}
