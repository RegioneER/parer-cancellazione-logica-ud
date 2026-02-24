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
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package it.eng.parer.soft.delete.beans.utils.messages;

import java.util.List;

import it.eng.parer.soft.delete.jpa.entity.DecErrSacer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;

@SuppressWarnings("unchecked")
@ApplicationScoped
@Transactional
public class MessaggiWSHelper {

    EntityManager entityManager;

    @Inject
    public MessaggiWSHelper(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public List<DecErrSacer> caricaListaErrori() {
        final String queryStr = "SELECT e FROM DecErrSacer e ";
        Query query = entityManager.createQuery(queryStr);

        return query.getResultList();
    }

    @Transactional(value = TxType.REQUIRED)
    public DecErrSacer caricaDecErrore(String cdErrore) {
        final String queryStr = "SELECT e FROM DecErrSacer e " + "where e.cdErr = :cdErr";
        Query query = entityManager.createQuery(queryStr);
        query.setParameter("cdErr", cdErrore);
        return (DecErrSacer) query.getSingleResult();
    }

}
