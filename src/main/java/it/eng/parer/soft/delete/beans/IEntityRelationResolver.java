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

package it.eng.parer.soft.delete.beans;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import it.eng.parer.soft.delete.beans.dto.EntityNode;
import it.eng.parer.soft.delete.beans.exceptions.AppGenericPersistenceException;

/**
 * Interfaccia per la risoluzione delle relazioni tra entità.
 */
public interface IEntityRelationResolver {

    /**
     * Trova le entità figlie per un batch di nodi parent dello stesso tipo.
     *
     * @param parentClass       Classe dell'entità parent
     * @param parentIds         Lista di ID dei parent
     * @param parentLevel       Livello dei parent
     * @param processedEntities Set di entità già processate
     * @param childConsumer     Consumer per elaborare i nodi figli trovati
     *
     * @throws AppGenericPersistenceException in caso di errori durante la ricerca
     */
    void findBatchChildEntities(Class<?> parentClass, List<Object> parentIds, int parentLevel,
            Set<String> processedEntities, Consumer<EntityNode> childConsumer)
            throws AppGenericPersistenceException;

    /**
     * Elabora le associazioni OneToMany per un batch di entità parent dello stesso tipo
     *
     * @param parentClass       Classe dell'entità parent
     * @param parentIds         Lista di ID dei parent
     * @param parentIdField     Campo ID della classe parent
     * @param parentLevel       Livello dei parent
     * @param processedEntities Set di entità già processate
     * @param childConsumer     Consumer per elaborare i nodi figli trovati
     *
     * @throws AppGenericPersistenceException in caso di errori durante l'elaborazione
     */
    void processOneToManyRelations(Class<?> parentClass, List<Object> parentIds,
            Field parentIdField, int parentLevel, Set<String> processedEntities,
            Consumer<EntityNode> childConsumer) throws AppGenericPersistenceException;

    /**
     * Elabora le associazioni OneToOne per un batch di entità parent dello stesso tipo
     *
     * @param parentClass       Classe dell'entità parent
     * @param parentIds         Lista di ID dei parent
     * @param parentIdField     Campo ID della classe parent
     * @param parentLevel       Livello dei parent
     * @param processedEntities Set di entità già processate
     * @param childConsumer     Consumer per elaborare i nodi figli trovati
     *
     * @throws AppGenericPersistenceException in caso di errori durante l'elaborazione
     */
    void processOneToOneRelations(Class<?> parentClass, List<Object> parentIds, Field parentIdField,
            int parentLevel, Set<String> processedEntities, Consumer<EntityNode> childConsumer)
            throws AppGenericPersistenceException;

    /**
     * Processa le entità figlie in batch per tutti i parent forniti
     *
     * @param parentClass       Classe dell'entità parent
     * @param parentIds         Lista di ID dei parent
     * @param parentIdField     Campo ID della classe parent
     * @param childClass        Classe dell'entità figlio
     * @param childIdField      Campo ID della classe figlio
     * @param relationField     Campo di relazione nella classe figlio
     * @param parentLevel       Livello dei parent
     * @param processedEntities Set di entità già processate
     * @param childConsumer     Consumer per elaborare i nodi figli trovati
     *
     * @throws AppGenericPersistenceException in caso di errori durante l'elaborazione
     */
    void processBatchChildEntities(Class<?> parentClass, List<Object> parentIds,
            Field parentIdField, Class<?> childClass, Field childIdField, Field relationField,
            int parentLevel, Set<String> processedEntities, Consumer<EntityNode> childConsumer)
            throws AppGenericPersistenceException;
}
