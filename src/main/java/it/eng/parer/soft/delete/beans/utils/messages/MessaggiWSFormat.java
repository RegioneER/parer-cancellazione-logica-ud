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
package it.eng.parer.soft.delete.beans.utils.messages;

import static it.eng.parer.soft.delete.beans.utils.converter.DateUtilsConverter.convert;

import java.text.MessageFormat;
import java.text.Normalizer;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import it.eng.parer.soft.delete.beans.dto.base.CSChiave;
import it.eng.parer.soft.delete.beans.dto.base.CSVersatore;
import it.eng.parer.soft.delete.beans.utils.Costanti;

/**
 *
 * @author Fioravanti_F
 */
public class MessaggiWSFormat {

    private MessaggiWSFormat() {
        throw new IllegalStateException("Utility class");
    }

    // MEV#16490
    public static String formattaUrnPartVersatore(CSVersatore versatore) {
        return formattaUrnPartVersatore(versatore, false, Costanti.UrnFormatter.VERS_FMT_STRING);
    }

    public static String formattaUrnPartVersatore(CSVersatore versatore, boolean toNormalize,
            String fmtUsed) {
        if (!toNormalize) {
            return MessageFormat.format(fmtUsed,
                    StringUtils.isNotBlank(versatore.getSistemaConservazione())
                            ? versatore.getSistemaConservazione()
                            : versatore.getAmbiente(),
                    versatore.getEnte(), versatore.getStruttura());
        } else {
            return MessageFormat.format(fmtUsed,
                    StringUtils.isNotBlank(versatore.getSistemaConservazione())
                            ? MessaggiWSFormat.normalizingKey(versatore.getSistemaConservazione())
                            : MessaggiWSFormat.normalizingKey(versatore.getAmbiente()),
                    MessaggiWSFormat.normalizingKey(versatore.getEnte()),
                    MessaggiWSFormat.normalizingKey(versatore.getStruttura()));
        }
    }

    public static String formattaUrnPartUnitaDoc(CSChiave chiave) {
        return formattaUrnPartUnitaDoc(chiave, false, Costanti.UrnFormatter.UD_FMT_STRING);
    }

    public static String formattaUrnPartUnitaDoc(CSChiave chiave, boolean toNormalize,
            String fmtUsed) {
        if (!toNormalize) {
            return MessageFormat.format(fmtUsed, chiave.getTipoRegistro(),
                    chiave.getAnno().toString(), chiave.getNumero());
        } else {
            return MessageFormat.format(fmtUsed,
                    MessaggiWSFormat.normalizingKey(chiave.getTipoRegistro()),
                    chiave.getAnno().toString(),
                    MessaggiWSFormat.normalizingKey(chiave.getNumero()));
        }
    }

    //
    public static String formattaUrnPartComponente(String urnBase, long ordinePresentazione,
            String fmtUsed, String padfmtUsed) {
        return MessageFormat.format(fmtUsed, urnBase,
                String.format(padfmtUsed, ordinePresentazione));
    }

    public static Long formattaKeyPartAnnoMeseVers(Date dtVersamento) {
        Calendar date = Calendar.getInstance();
        date.setTime(dtVersamento);
        int year = date.get(Calendar.YEAR);
        int month = date.get(Calendar.MONTH) + 1;
        return (long) year * 100 + month;
    }

    public static Long formattaKeyPartAnnoMeseVers(ZonedDateTime dtVersamento) {
        return formattaKeyPartAnnoMeseVers(convert(dtVersamento));
    }

    public static String formattaBaseUrnDoc(String versatore, String unitaDoc, String documento,
            String fmtUsed) {
        return MessageFormat.format(fmtUsed, versatore, unitaDoc, documento);
    }

    //

    public static String formattaUrnIndiceSip(String urnBase, String fmtUsed) {
        return MessageFormat.format(fmtUsed, urnBase);
    }

    public static String formattaUrnPiSip(String urnBase, String fmtUsed) {
        return MessageFormat.format(fmtUsed, urnBase);
    }

    public static String formattaUrnEsitoVers(String urnBase, String fmtUsed) {
        return MessageFormat.format(fmtUsed, urnBase);
    }

    public static String formattaUrnRappVers(String urnBase, String fmtUsed) {
        return MessageFormat.format(fmtUsed, urnBase);
    }

    public static String formattaUrnSip(String urnBase, String fmtUsed) {
        return MessageFormat.format(fmtUsed, urnBase);
    }

    public static String formattaUrnIndiceSipUpd(String urnBase, String fmtUsed) {
        return MessageFormat.format(fmtUsed, urnBase);
    }

    /* AWS : RULES FOR CD_KEY_FILE SU COMPONENTE */
    /*
     * Nota importante : la regola scelta si basa sulle dinaniche previste per gli URN ma a
     * differenza di quest'ultime anziché riportare il numero (che può contentere caratteri
     * speciali) utilizza di base l'ID del componete
     *
     * Su extra si possono aggiungere ulteriori parametri (in coda) sulla base del formatter
     * (fmtUsed) che viene passato al metodo.
     */
    public static String formattaComponenteCdKeyFile(CSVersatore versatore, CSChiave chiave,
            long idComponente, Optional<Object> extra, String fmtUsed) {
        return MessageFormat.format(fmtUsed,
                StringUtils.isNotBlank(versatore.getSistemaConservazione())
                        ? MessaggiWSFormat.normalizingKey(versatore.getSistemaConservazione())
                        : MessaggiWSFormat.normalizingKey(versatore.getAmbiente()),
                MessaggiWSFormat.normalizingKey(versatore.getEnte()),
                MessaggiWSFormat.normalizingKey(versatore.getStruttura()),
                MessaggiWSFormat.normalizingKey(chiave.getTipoRegistro()),
                chiave.getAnno().toString(), String.valueOf(idComponente),
                extra.isPresent() ? String.valueOf(extra.get()) : StringUtils.EMPTY);
    }

    /*
     * Restituisce una stringa normalizzata secondo le regole cel codice UD normalizzato sostituendo
     * tutti i caratteri accentati con i corrispondenti non accentati e ammettendo solo lettere,
     * numeri, '.', '-' e '_'. Tutto il resto viene convertito in '_'.
     */
    public static String normalizingKey(String value) {
        return Normalizer.normalize(value, Normalizer.Form.NFD).replace(" ", "_")
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .replaceAll("[^A-Za-z0-9\\. _-]", "_");
    }

}
