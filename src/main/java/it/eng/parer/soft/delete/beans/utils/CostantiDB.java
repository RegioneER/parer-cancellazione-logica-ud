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
package it.eng.parer.soft.delete.beans.utils;

public class CostantiDB {

    //
    public enum TiXmlRichSoftDelete {

        RICHIESTA, RISPOSTA
    }

    //
    public enum TipiUsoDatiSpec {

        MIGRAZ, VERS
    }

    public enum TiUsoModelloXsd {
        MIGRAZ, VERS
    }

    public enum TiModelloXsd {
        PROFILO_GENERALE_FASCICOLO, PROFILO_ARCHIVISTICO_FASCICOLO, PROFILO_SPECIFICO_FASCICOLO,
        FASCICOLO, SELF_DESCRIPTION_MORE_INFO,
        // FILE_GROUP_FILE_MORE_INFO,
        UNISYNCRO, PROFILO_NORMATIVO_FASCICOLO
    }

    public enum StatoConservazioneUnitaDoc {

        ANNULLATA, AIP_DA_GENERARE, AIP_GENERATO, AIP_IN_AGGIORNAMENTO, IN_ARCHIVIO, IN_CUSTODIA,
        IN_VOLUME_DI_CONSERVAZIONE, PRESA_IN_CARICO, VERSAMENTO_IN_ARCHIVIO, AIP_FIRMATO
    }

    public enum TipoGravitaErrore {

        ERRORE, WARNING
    }

    public enum TipiEsitoVerificaHash {

        POSITIVO, NEGATIVO, DISABILITATO, // nel caso la verifica hash non fosse da fare o
        // nel caso
        // di mancata
        // identificazione dell'algoritmo
        NON_EFFETTUATO // nel caso di hash forzato nel versamento MM
    }

    //
    public enum StatoVrsFascicoloKo {

        NON_VERIFICATO, VERIFICATO, NON_RISOLUBILE, RISOLTO
    }

    public enum TipiHash {

        SCONOSCIUTO("SCONOSCIUTO", -1), MD5("MD5", 16), SHA_1("SHA-1", 20), SHA_224("SHA-224", 28),
        SHA_256("SHA-256", 32), SHA_384("SHA-384", 48), SHA_512("SHA-512", 64);

        private String desc;
        private int lenght;

        private TipiHash(String ds, int ln) {
            desc = ds;
            lenght = ln;
        }

        public String descrivi() {
            return desc;
        }

        public int lunghezza() {
            return lenght;
        }

        public static TipiHash evaluateByLenght(int lenght) {
            for (TipiHash hash : values()) {
                if (hash.lunghezza() == lenght) {
                    return hash;
                }
            }
            return SCONOSCIUTO;
        }

        public static TipiHash evaluateByDesc(String desc) {
            for (TipiHash hash : values()) {
                if (hash.descrivi().equals(desc)) {
                    return hash;
                }
            }
            return SCONOSCIUTO;
        }
    }

    public enum TipiEncBinari {

        SCONOSCIUTO("SCONOSCIUTO"), HEX_BINARY("hexBinary"), BASE64("Base64");

        private String desc;

        private TipiEncBinari(String ds) {
            desc = ds;
        }

        public String descrivi() {
            return desc;
        }

        public static TipiEncBinari evaluateByDesc(String desc) {
            for (TipiEncBinari bin : values()) {
                /*
                 * equalsIgnoreCase = dato che non esiste una codifica "forte" il chiamante (e.g.
                 * ping che utilizza BASE64 e non Base64 come sui ws, ma non ha importanza la
                 * sintassi quanto la semantica ...)
                 */
                if (bin.descrivi().equalsIgnoreCase(desc)) {
                    return bin;
                }
            }
            return TipiEncBinari.SCONOSCIUTO;
        }
    }

    //
    public class Flag {

        private Flag() {
            throw new IllegalStateException("Utility class");
        }

        public static final String TRUE = "1";
        public static final String FALSE = "0";
    }

    //
    public enum TiRichSoftDelete {
        // ELENCO,
        UD, ANNULLAMENTO, RESTITUZIONE, SCARTO
    }

    public enum TiItemRichSoftDelete {
        // ELENCO_VERS,
        UNI_DOC, ANNUL_VERS, REST_ARCH, SCARTO_ARCH
    }

    public enum TipoCreazioneRichSoftDelete {
        // SINCRONA,
        ASINCRONA
    }

    public enum StatoRichSoftDelete {

        PRESA_IN_CARICO, ACQUISITA, EVASA, ACQUISIZIONE_FALLITA, ERRORE
    }

    public enum StatoItemRichSoftDelete {

        ELABORATO, NON_ELABORABILE, DA_ELABORARE, ERRORE_ELABORAZIONE
    }

    public enum TipoErrRichSoftDelete {

        ITEM_GIA_PRESENTE, ITEM_IN_CORSO_DI_ELAB, ITEM_IN_CORSO_DI_ANNUL,
        ITEM_IN_CORSO_DI_REST_ARCH, ITEM_IN_CORSO_DI_SCARTO, ITEM_NON_ANNULLATO,
        ITEM_NON_RESTITUITO, ITEM_NON_ESISTE, STATO_CONSERV_NON_AMMESSO;

        public static String[] getStatiControlloItem() {
            // <<<<<<< Ritorna tutti gli stati tranne ITEM_NON_ESISTE e
            // ITEM_GIA_PRESENTE - DA
            // MODIFICARE IN CASO DI
            // AGGIUNTE

            return new String[] {
                    TipoErrRichSoftDelete.ITEM_IN_CORSO_DI_ELAB.name(),
                    TipoErrRichSoftDelete.ITEM_IN_CORSO_DI_ANNUL.name(),
                    TipoErrRichSoftDelete.ITEM_IN_CORSO_DI_REST_ARCH.name(),
                    TipoErrRichSoftDelete.ITEM_IN_CORSO_DI_SCARTO.name(),
                    TipoErrRichSoftDelete.STATO_CONSERV_NON_AMMESSO.name() };
        }
    }

}
