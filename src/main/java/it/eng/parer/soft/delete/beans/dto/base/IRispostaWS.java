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

/*
 * To change this template, choose Tools | Templates and open the template in the editor.
 */
package it.eng.parer.soft.delete.beans.dto.base;

import java.io.Serializable;

import it.eng.parer.soft.delete.beans.utils.AvanzamentoWs;

/**
 *
 * @author DiLorenzo_F
 */
public interface IRispostaWS extends Serializable {

    public enum SeverityEnum {

        OK, WARNING, ERROR
    }

    public enum ErrorTypeEnum {

        NOERROR, WS_DATA, WS_SIGNATURE, DB_FATAL
    }

    String getErrorCode();

    String getErrorMessage();

    ErrorTypeEnum getErrorType();

    SeverityEnum getSeverity();

    AvanzamentoWs getAvanzamento();

    void setErrorCode(String errorCode);

    void setErrorMessage(String errorMessage);

    void setErrorType(ErrorTypeEnum errorType);

    void setSeverity(SeverityEnum severity);

    void setAvanzamento(AvanzamentoWs avanzamento);

    void setEsitoWsErrBundle(String errCode, Object... params);

    void setEsitoWsErrBundle(String errCode);

    void setEsitoWsWarnBundle(String errCode, Object... params);

    void setEsitoWsWarnBundle(String errCode);

    void setEsitoWsError(String errCode, String errMessage);

    void setEsitoWsWarning(String errCode, String errMessage);
}
