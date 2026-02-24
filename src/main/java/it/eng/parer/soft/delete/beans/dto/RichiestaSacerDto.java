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
import java.util.Objects;

/**
 *
 * @author DiLorenzo_F
 */
public class RichiestaSacerDto {

    private BigDecimal idStrut;
    private BigDecimal idRichAnnulVers;

    public RichiestaSacerDto(BigDecimal idStrut, BigDecimal idRichAnnulVers) {
        this.idStrut = idStrut;
        this.idRichAnnulVers = idRichAnnulVers;
    }

    public BigDecimal getIdStrut() {
        return idStrut;
    }

    public void setIdStrut(BigDecimal idStrut) {
        this.idStrut = idStrut;
    }

    public BigDecimal getIdRichAnnulVers() {
        return idRichAnnulVers;
    }

    public void setIdRichAnnulVers(BigDecimal idRichAnnulVers) {
        this.idRichAnnulVers = idRichAnnulVers;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        RichiestaSacerDto that = (RichiestaSacerDto) o;
        return Objects.equals(idStrut, that.idStrut)
                && Objects.equals(idRichAnnulVers, that.idRichAnnulVers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idStrut, idRichAnnulVers);
    }
}
