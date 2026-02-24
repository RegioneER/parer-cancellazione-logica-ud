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
package it.eng.parer.soft.delete.beans.exceptions.xml;

import org.apache.commons.lang3.StringUtils;

import jakarta.xml.bind.JAXBException;

public class XmlReqUnmarshalException extends JAXBException implements IXmlReqValidationException {

    private static final long serialVersionUID = 4261618786078845479L;
    private final String xmlReq;

    public XmlReqUnmarshalException(String message) {
        super(message);
        this.xmlReq = StringUtils.EMPTY;
    }

    public XmlReqUnmarshalException(String message, String errorCode, Throwable exception,
            String xmlReq) {
        super(message, errorCode, exception);
        this.xmlReq = xmlReq;
    }

    public XmlReqUnmarshalException(String message, String errorCode, String xmlReq) {
        super(message, errorCode);
        this.xmlReq = xmlReq;
    }

    public XmlReqUnmarshalException(String message, Throwable exception, String xmlReq) {
        super(message, exception);
        this.xmlReq = xmlReq;
    }

    public XmlReqUnmarshalException(Throwable exception, String xmlReq) {
        super(exception);
        this.xmlReq = xmlReq;
    }

    public String getXmlReq() {
        return xmlReq;
    }

}
