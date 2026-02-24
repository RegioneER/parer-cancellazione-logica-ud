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

package it.eng.parer.soft.delete.beans.dto;

import java.math.BigDecimal;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author Bonora_L
 */
public class UnitaDocDto {

    private BigDecimal idStrut;
    private String registro;
    private BigDecimal anno;
    private String numero;

    public UnitaDocDto(BigDecimal idStrut, String registro, BigDecimal anno, String numero) {
        this.idStrut = idStrut;
        this.registro = registro;
        this.anno = anno;
        this.numero = numero;
    }

    public BigDecimal getIdStrut() {
        return idStrut;
    }

    public void setIdStrut(BigDecimal idStrut) {
        this.idStrut = idStrut;
    }

    public String getRegistro() {
        return registro;
    }

    public void setRegistro(String registro) {
        this.registro = registro;
    }

    public BigDecimal getAnno() {
        return anno;
    }

    public void setAnno(BigDecimal anno) {
        this.anno = anno;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    @Override
    public int hashCode() {
        return 7;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final UnitaDocDto other = (UnitaDocDto) obj;
        if (this.getIdStrut().compareTo(other.getIdStrut()) != 0) {
            return false;
        }
        if (this.getAnno().compareTo(other.getAnno()) != 0) {
            return false;
        }
        if (!StringUtils.equals(this.getRegistro(), other.getRegistro())) {
            return false;
        }
        if (!StringUtils.equals(this.getNumero(), other.getNumero())) {
            return false;
        }

        return true;
    }

}
