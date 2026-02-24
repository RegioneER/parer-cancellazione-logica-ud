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

package it.eng.parer.soft.delete.beans.utils;

import static it.eng.parer.soft.delete.beans.utils.Costanti.WS_SOFT_DELETE_VRSN;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import it.eng.parer.soft.delete.beans.dto.RispostaWSInvioRichiestaCancellazioneLogica;
import it.eng.parer.soft.delete.beans.dto.base.BlockingFakeSession;
import it.eng.parer.soft.delete.beans.dto.InvioRichiestaCancellazioneLogicaExt;
import it.eng.parer.ws.xml.esitoRichSoftDelete.EsitoRichiestaCancellazioneLogica;
import it.eng.parer.ws.xml.richSoftDelete.RichiestaCancellazioneLogica;

/**
 *
 * @author Fioravanti_F
 */
public class VerificaVersione {

    boolean migrazione = false;
    boolean datiSpecExt = false;
    boolean profiliUdXsd = false;
    boolean parseOk = true;
    EsitiVerfica tmpEsitiVerfica = null;

    public class EsitiVerfica {

        //
        ArrayList<Object> params = null;
        //
        boolean flgErrore = false;
        //
        boolean flgWarning = false;
        String messaggio;
        String codErrore;

        public String getCodErrore() {
            return codErrore;
        }

        public void setCodErrore(String codErrore) {
            this.codErrore = codErrore;
        }

        public boolean isFlgErrore() {
            return flgErrore;
        }

        public void setFlgErrore(boolean flgErrore) {
            this.flgErrore = flgErrore;
        }

        public String getMessaggio() {
            return messaggio;
        }

        public void setMessaggio(String messaggio) {
            this.messaggio = messaggio;
        }

        /**
         * @return the flgWarning
         */
        public boolean isFlgWarning() {
            return flgWarning;
        }

        /**
         * @param flgWarning the flgWarning to set
         */
        public void setFlgWarning(boolean flgWarning) {
            this.flgWarning = flgWarning;
        }

        /**
         * @return the params
         */
        public List<Object> getParams() {
            if (params == null) {
                params = new ArrayList<>();
            }
            return params;
        }

    }

    public EsitiVerfica getEsitiVerfica() {
        return tmpEsitiVerfica;
    }

    /*
     * Logica non implementata (per il momento) utile se introdotti nuovi campi per diversificare la
     * gestione su SIP
     */
    public void verifica(RispostaWSInvioRichiestaCancellazioneLogica rispostaWS,
            InvioRichiestaCancellazioneLogicaExt richiestaCancellazioneLogicaExt,
            BlockingFakeSession syncFakeSession) {
        @SuppressWarnings("unused")
        RichiestaCancellazioneLogica parsedFascicolo = syncFakeSession
                .getRichiestaCancellazioneLogica();
        @SuppressWarnings("unused")
        EsitoRichiestaCancellazioneLogica myEsito = rispostaWS
                .getEsitoRichiestaCancellazioneLogica();
        parseOk = true;

        tmpEsitiVerfica = new EsitiVerfica();

        /**
         * #################################### TODO : logica da implementare
         * ####################################
         */
        this.generaMessaggioErrore();
        //
        this.generaMessaggioWarning();
    }

    // al momento non implementato (-> eventualmente lo fosse da cambiare l'XSD in risposta)
    private void generaMessaggioErrore(Object... params) {
        if (!parseOk) {
            tmpEsitiVerfica.setFlgErrore(true);
            throw new UnsupportedOperationException(
                    "Operazione non supportata: generaMessaggioErrore");
        }
    }

    // al momento non implementato (-> eventualmente lo fosse da cambiare l'XSD in risposta)
    private void generaMessaggioWarning(Object... params) {
        if (!parseOk) {
            tmpEsitiVerfica.setFlgWarning(true);
            throw new UnsupportedOperationException(
                    "Operazione non supportata: generaMessaggioWarning");
        }
    }

    public static String elabWsKey(String versioniWsName) {
        return ParametroApplDB.VERSIONI_WS_PREFIX.concat(versioniWsName);
    }

    public static List<String> getWsVersionList(String versioniWsName,
            Map<String, String> mapWsVersion) {
        // key name on map
        String versioniWsKey = elabWsKey(versioniWsName);
        if (mapWsVersion == null || !mapWsVersion.containsKey(versioniWsKey)) {
            return new ArrayList<>();// empty list
        } else {
            return Arrays.asList(mapWsVersion.get(versioniWsKey).split("\\|")); // NOTA
            // :
            // separator
            // on code
        }
    }

    public static String latestVersion(String versioniWsName, Map<String, String> mapWsVersion) {
        List<String> versioniWs = getWsVersionList(versioniWsName, mapWsVersion);
        if (versioniWs.isEmpty()) {
            /**
             * Di norma questo caso non dovrebbe mai verificarsi in quanto all'atto
             * dell'inizializzazione del ws la mappa contenente i valori è già stata testata @link
             * ControlliWsService.caricaVersioniWSDefault
             */
            return WS_SOFT_DELETE_VRSN; // default
        }
        Collections.sort(versioniWs, new Comparator<String>() {
            @Override
            public int compare(String v1, String v2) {
                String[] v1nodot = v1.split("\\."); // NOTA : dot sep on code
                String[] v2nodot = v2.split("\\."); // NOTA : dot sep on code
                int major1 = major(v1nodot);
                int major2 = major(v2nodot);
                if (major1 == major2) {
                    return minor(v1nodot).compareTo(minor(v2nodot));
                }
                return major1 > major2 ? 1 : -1;
            }

            private int major(String[] version) {
                return Integer.parseInt(version[0]);
            }

            private Integer minor(String[] version) {
                // right padding 0 from right (comparable digits)
                return version.length > 1
                        ? Integer.parseInt(StringUtils.rightPad(version[1], 4, "0"))
                        : 0;
            }

        });

        return versioniWs.get(versioniWs.size() - 1);// the last one
    }

}
