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

package it.eng.parer.soft.delete.beans.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.eng.parer.soft.delete.beans.IEntityHierarchyBuilder;
import it.eng.parer.soft.delete.beans.IEntityRelationResolver;
import it.eng.parer.soft.delete.beans.IStreamSource;
import it.eng.parer.soft.delete.beans.dto.EntityNode;
import it.eng.parer.soft.delete.beans.dto.EntityRelation;
import it.eng.parer.soft.delete.beans.exceptions.AppGenericPersistenceException;
import it.eng.parer.soft.delete.beans.streams.SimpleStreamSource;
import it.eng.parer.soft.delete.beans.streams.StreamSourceFactory;
import it.eng.parer.soft.delete.beans.utils.reflection.JpaEntityReflectionHelper;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Classe helper responsabile della costruzione della gerarchia di entità.
 */
@ApplicationScoped
public class EntityHierarchyBuilder implements IEntityHierarchyBuilder {

    private static final Logger log = LoggerFactory.getLogger(EntityHierarchyBuilder.class);

    private final IEntityRelationResolver entityRelationResolver;

    private final StreamSourceFactory streamSourceFactory;

    // Set per tracciare le entità già esplorate
    private Set<String> processedForDiscovery;

    // Set per tracciare i nodi esplorati a ciascun livello
    private Set<String> exploredAtLevel;

    // Set per tracciare le relazioni scoperte
    private Set<String> discoveredRelations;

    // Set per tracciare le relazioni che contengono nodi duplicati
    private Set<String> relationsWithDuplicateNodes;

    // Mappa per tenere traccia dei nodi per livello
    private Map<Integer, List<EntityNode>> nodesByLevel;

    // Mappa delle relazioni scoperte per livello
    private Map<Integer, List<EntityRelation>> relationsByLevel;

    @Inject
    public EntityHierarchyBuilder(IEntityRelationResolver entityRelationResolver,
            StreamSourceFactory streamSourceFactory) {
        this.entityRelationResolver = entityRelationResolver;
        this.streamSourceFactory = streamSourceFactory;
    }

    /**
     * Costruisce una mappa di sorgenti di stream per ciascun livello della gerarchia
     */
    public Map<Integer, IStreamSource> buildHierarchy(Class<?> rootClass, Object rootId)
            throws AppGenericPersistenceException {

        // Inizializza le strutture dati
        initialize(rootClass, rootId);

        // Esplora la gerarchia livello per livello
        exploreLevels();

        // Costruisce e restituisce gli stream sources
        return buildStreamSources();
    }

    /**
     * Inizializza le strutture dati necessarie
     */
    private void initialize(Class<?> rootClass, Object rootId) {
        // Reset delle strutture dati
        processedForDiscovery = new HashSet<>();
        exploredAtLevel = new HashSet<>();
        discoveredRelations = new HashSet<>();
        relationsWithDuplicateNodes = new HashSet<>();
        nodesByLevel = new HashMap<>();
        relationsByLevel = new HashMap<>();

        // Inizializzazione con il nodo root
        Field rootIdField = JpaEntityReflectionHelper.getIdField(rootClass);
        EntityNode rootNode = new EntityNode(rootClass, rootId, rootIdField, 0);

        nodesByLevel.put(0, List.of(rootNode));
        processedForDiscovery.add(rootClass.getName() + ":" + rootId);
        exploredAtLevel.add(rootClass.getName() + ":" + rootId + "@0");
    }

    /**
     * Esplora la gerarchia livello per livello
     */
    private void exploreLevels() throws AppGenericPersistenceException {
        int currentLevel = 0;

        while (nodesByLevel.containsKey(currentLevel)
                && !nodesByLevel.get(currentLevel).isEmpty()) {
            exploreLevel(currentLevel);
            currentLevel++;
        }
    }

    /**
     * Esplora un singolo livello della gerarchia
     */
    private void exploreLevel(int level) throws AppGenericPersistenceException {
        List<EntityNode> currentLevelNodes = nodesByLevel.get(level);
        List<EntityNode> nextLevelNodes = new ArrayList<>();

        // Raggruppa i nodi del livello corrente per tipo di entità
        Map<Class<?>, List<EntityNode>> nodesByType = groupNodesByType(currentLevelNodes);

        // Per ogni tipo di entità, trova tutti i figli in batch
        for (Map.Entry<Class<?>, List<EntityNode>> entry : nodesByType.entrySet()) {
            Class<?> parentClass = entry.getKey();
            List<EntityNode> parentNodes = entry.getValue();

            // Estrai tutti gli ID dei nodi padre di questo tipo
            List<Object> parentIds = parentNodes.stream().map(EntityNode::getEntityId).toList();

            // Trova tutti i figli per questo batch di padri
            entityRelationResolver.findBatchChildEntities(parentClass, parentIds, level,
                    processedForDiscovery,
                    childNode -> processChildNode(childNode, nextLevelNodes));
        }

        // Prepara per il prossimo livello se necessario
        if (!nextLevelNodes.isEmpty()) {
            nodesByLevel.put(level + 1, nextLevelNodes);
        }
    }

    /**
     * Raggruppa i nodi per tipo di entità
     */
    private Map<Class<?>, List<EntityNode>> groupNodesByType(List<EntityNode> nodes) {
        Map<Class<?>, List<EntityNode>> nodesByType = new HashMap<>();
        for (EntityNode node : nodes) {
            nodesByType.computeIfAbsent(node.getEntityClass(), k -> new ArrayList<>()).add(node);
        }
        return nodesByType;
    }

    /**
     * Processa un nodo figlio
     */
    private void processChildNode(EntityNode childNode, List<EntityNode> nextLevelNodes) {
        // Crea una chiave che include entità e livello
        String entityKeyWithLevel = childNode.getEntityClass().getName() + ":"
                + childNode.getEntityId() + "@" + childNode.getLevel();

        // Aggiungi alla coda solo se non è già stato esplorato a questo livello
        if (!exploredAtLevel.contains(entityKeyWithLevel)) {
            nextLevelNodes.add(childNode);
            exploredAtLevel.add(entityKeyWithLevel);
        }

        // Registra la relazione parent-child
        if (childNode.hasParent()) {
            registerRelation(childNode);
        }
    }

    /**
     * Registra una relazione parent-child
     */
    private void registerRelation(EntityNode childNode) {
        String relationKey = childNode.getRelationKey();
        boolean isNodeDuplicate = childNode.isDuplicate();

        if (!discoveredRelations.contains(relationKey)) {
            // Prima volta che vediamo questa relazione
            discoveredRelations.add(relationKey);

            EntityRelation relation = new EntityRelation(childNode.getEntityClass(),
                    childNode.getParentClass(), childNode.getParentId(), childNode.getParentField(),
                    childNode.getIdField(), isNodeDuplicate);

            relationsByLevel.computeIfAbsent(childNode.getLevel(), k -> new ArrayList<>())
                    .add(relation);

            // Se il nodo è già duplicato, marca anche la relazione
            if (isNodeDuplicate) {
                relationsWithDuplicateNodes.add(relationKey);
                log.debug("Registrata relazione con duplicati: {} -> {} (livello {})",
                        childNode.getEntityClass().getSimpleName(),
                        childNode.getParentClass().getSimpleName(), childNode.getLevel());
            } else {
                log.debug("Registrata relazione: {} -> {} (livello {})",
                        childNode.getEntityClass().getSimpleName(),
                        childNode.getParentClass().getSimpleName(), childNode.getLevel());
            }
        } else if (isNodeDuplicate && !relationsWithDuplicateNodes.contains(relationKey)) {
            // La relazione esiste già, ma ora abbiamo trovato un nodo duplicato
            updateRelationAsDuplicate(childNode);
        }
    }

    /**
     * Aggiorna una relazione come contenente duplicati
     */
    private void updateRelationAsDuplicate(EntityNode childNode) {
        String relationKey = childNode.getRelationKey();
        relationsWithDuplicateNodes.add(relationKey);

        // Cerca nel livello corrente
        List<EntityRelation> relationsAtLevel = relationsByLevel.get(childNode.getLevel());
        if (relationsAtLevel != null) {
            boolean relationFound = false;
            for (EntityRelation relation : relationsAtLevel) {
                if (childNode.getEntityClass().equals(relation.getChildClass())
                        && childNode.getParentClass().equals(relation.getParentClass())
                        && childNode.getParentId().equals(relation.getParentId())) {

                    if (!relation.isDuplicate()) {
                        // Aggiorniamo il flag solo se necessario
                        log.info(
                                "Relazione aggiornata: {} -> {} ora contiene entità duplicate (livello {})",
                                childNode.getEntityClass().getSimpleName(),
                                childNode.getParentClass().getSimpleName(), childNode.getLevel());

                        // Crea una nuova relazione con flag=true
                        int index = relationsAtLevel.indexOf(relation);
                        relationsAtLevel.set(index, new EntityRelation(relation.getChildClass(),
                                relation.getParentClass(), relation.getParentId(),
                                relation.getParentField(), relation.getChildIdField(), true));
                    }
                    relationFound = true;
                    break;
                }
            }

            if (!relationFound) {
                log.warn("Relazione {} -> {} marcata come duplicato ma non trovata al livello {}",
                        childNode.getEntityClass().getSimpleName(),
                        childNode.getParentClass().getSimpleName(), childNode.getLevel());
            }
        }
    }

    /**
     * Costruisce gli stream sources per ogni livello
     */
    private Map<Integer, IStreamSource> buildStreamSources() {
        Map<Integer, IStreamSource> streamSources = new HashMap<>();

        // Primo elemento della mappa è il nodo root (livello 0)
        List<EntityNode> rootNodes = nodesByLevel.get(0);
        if (rootNodes != null && !rootNodes.isEmpty()) {
            EntityNode rootNode = rootNodes.get(0);
            streamSources.put(0, new SimpleStreamSource(rootNode.getEntityClass(),
                    rootNode.getEntityId(), rootNode.getIdField(), 0));
        }

        // Aggiungi tutti gli altri livelli
        for (Map.Entry<Integer, List<EntityRelation>> entry : relationsByLevel.entrySet()) {
            int level = entry.getKey();
            List<EntityRelation> relations = entry.getValue();

            if (!relations.isEmpty()) {
                streamSources.put(level,
                        streamSourceFactory.createRelationStream(level, relations));
            }
        }

        return streamSources;
    }
}
