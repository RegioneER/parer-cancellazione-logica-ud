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

package it.eng.parer.soft.delete.jpa.entity.constraint;

/**
 * ARO_RICHIESTA_RA's constraint
 *
 * @author DiLorenzo_F
 */
public final class AroRichiestaRa {

    private AroRichiestaRa() {
    }

    /**
     * Stato richiesta
     *
     * TI_STATO IN
     * ('CALCOLO_AIP_IN_CORSO','IN_ATTESA_ESTRAZIONE','ESTRAZIONE_IN_CORSO','ESTRATTO','VERIFICATO','ERRORE','ANNULLATO')
     *
     */
    public enum AroRichiestaTiStato {
        CALCOLO_AIP_IN_CORSO, IN_ATTESA_ESTRAZIONE, ESTRAZIONE_IN_CORSO, ESTRATTO, VERIFICATO,
        ERRORE, ANNULLATO, RESTITUITO
    }
}
