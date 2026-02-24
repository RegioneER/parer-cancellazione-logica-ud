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

package it.eng.parer.soft.delete.beans.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Definisce una query ottimizzata per recuperare tutti i figli di una relazione parent-child. Può
 * essere ripetuta per specificare query diverse per la stessa relazione a livelli diversi.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Repeatable(RelationQueries.class)
public @interface RelationQuery {
    /**
     * Classe del parent per cui questa query è ottimizzata
     */
    Class<?> parentClass();

    /**
     * Query JPQL che recupera tutti i figli di questa relazione. Usa :rootId come parametro per
     * l'ID root dell'albero. La query deve selezionare almeno l'ID del figlio e l'ID del parent.
     */
    String query();

    /**
     * Nome del parametro per l'ID root
     */
    String rootIdParam() default "rootId";

    /**
     * Nome del parametro per l'ID del parent specifico
     */
    String parentIdParam() default "parentId";

    /**
     * Nome del parametro per la lista degli ID parent quando si usa la modalità batch. Se non
     * specificato, verrà utilizzato parentIdParam + "s" come convenzione
     */
    String parentIdsParam() default "parentIds";

    /**
     * Livelli dell'albero a cui questa query si applica. Un array vuoto significa che si applica a
     * tutti i livelli.
     */
    int[] levels() default {};
}
