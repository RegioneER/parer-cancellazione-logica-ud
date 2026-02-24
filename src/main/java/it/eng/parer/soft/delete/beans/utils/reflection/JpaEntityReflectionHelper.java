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

package it.eng.parer.soft.delete.beans.utils.reflection;

import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Column;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Classe di utilità per la reflection delle entity JPA. Fornisce metodi per l'introspezione delle
 * entità e delle loro relazioni, con meccanismi di cache per ottimizzare le performance.
 */
public class JpaEntityReflectionHelper {

    // Cache thread-safe per i metadati delle entità
    private static final Map<Class<?>, Field> idFieldCache = new ConcurrentHashMap<>();
    private static final Map<Class<?>, List<Field>> oneToManyFieldsCache = new ConcurrentHashMap<>();
    private static final Map<String, List<Field>> manyToOneFieldsCache = new ConcurrentHashMap<>();
    private static final Map<Class<?>, List<Field>> oneToOneFieldsCache = new ConcurrentHashMap<>();
    private static final Map<String, Field> inverseOneToOneFieldCache = new ConcurrentHashMap<>();

    private JpaEntityReflectionHelper() {
        // Costruttore privato per evitare istanziazione
    }

    /**
     * Ottiene il campo annotato con @Id per la classe specificata.
     *
     * @param clazz La classe dell'entità JPA
     *
     * @return Il campo ID o null se non trovato
     */
    public static Field getIdField(Class<?> clazz) {
        return idFieldCache.computeIfAbsent(clazz, cls -> {
            for (Field field : cls.getDeclaredFields()) {
                if (field.isAnnotationPresent(Id.class)) {
                    return field;
                }
            }
            return null;
        });
    }

    /**
     * Ottiene tutti i campi annotati con @OneToMany per la classe specificata.
     *
     * @param clazz La classe dell'entità JPA
     *
     * @return Lista dei campi OneToMany
     */
    public static List<Field> getOneToManyFields(Class<?> clazz) {
        return oneToManyFieldsCache.computeIfAbsent(clazz, cls -> {
            List<Field> fields = new ArrayList<>();
            for (Field field : cls.getDeclaredFields()) {
                if (field.isAnnotationPresent(OneToMany.class)) {
                    fields.add(field);
                }
            }
            return fields;
        });
    }

    /**
     * Ottiene tutti i campi ManyToOne che collegano childClass a parentClass.
     *
     * @param childClass  La classe figlio
     * @param parentClass La classe padre
     *
     * @return Lista dei campi ManyToOne che collegano childClass a parentClass
     */
    public static List<Field> getManyToOneFields(Class<?> childClass, Class<?> parentClass) {
        String key = childClass.getName() + "->" + parentClass.getName();
        return manyToOneFieldsCache.computeIfAbsent(key, k -> {
            List<Field> fields = new ArrayList<>();
            for (Field field : childClass.getDeclaredFields()) {
                if (field.isAnnotationPresent(ManyToOne.class)) {
                    Class<?> type = field.getType();
                    if (parentClass.equals(type)) {
                        fields.add(field);
                    }
                }
            }
            return fields;
        });
    }

    /**
     * Ottiene tutti i campi OneToOne per la classe specificata.
     *
     * @param clazz La classe dell'entità JPA
     *
     * @return Lista dei campi OneToOne
     */
    public static List<Field> getOneToOneFields(Class<?> clazz) {
        return oneToOneFieldsCache.computeIfAbsent(clazz, cls -> {
            List<Field> fields = new ArrayList<>();
            for (Field field : cls.getDeclaredFields()) {
                if (field.isAnnotationPresent(jakarta.persistence.OneToOne.class) && field
                        .isAnnotationPresent(jakarta.persistence.PrimaryKeyJoinColumn.class)) { // Include
                    // solo
                    // campi
                    // con
                    // PrimaryKeyJoinColumn
                    fields.add(field);
                }
            }
            return fields;
        });
    }

    /**
     * Trova il campo OneToOne inverso in una relazione con @PrimaryKeyJoinColumn.
     *
     * @param childClass  La classe figlio
     * @param parentClass La classe padre
     *
     * @return Il campo inverso o null se non trovato
     */
    public static Field getInverseOneToOneField(Class<?> childClass, Class<?> parentClass) {
        String key = childClass.getName() + "->" + parentClass.getName();
        return inverseOneToOneFieldCache.computeIfAbsent(key, k -> {
            // 1. Cerca il campo @MapsId nel child che si riferisce al parent
            for (Field field : childClass.getDeclaredFields()) {
                if (field.isAnnotationPresent(jakarta.persistence.MapsId.class)
                        && field.isAnnotationPresent(jakarta.persistence.OneToOne.class)) {
                    Class<?> type = field.getType();
                    if (parentClass.equals(type)) {
                        // Trovato il campo inverso corretto
                        return field;
                    }
                }
            }

            // 2. Se non trova un @MapsId, verifica se esiste una relazione
            // @PrimaryKeyJoinColumn
            // nel parent
            for (Field field : parentClass.getDeclaredFields()) {
                if (field.isAnnotationPresent(jakarta.persistence.OneToOne.class) && field
                        .isAnnotationPresent(jakarta.persistence.PrimaryKeyJoinColumn.class)) {
                    Class<?> type = field.getType();
                    if (childClass.equals(type)) {
                        // Cerca ancora il campo inverso nel child se
                        // possibile
                        for (Field childField : childClass.getDeclaredFields()) {
                            if (childField.isAnnotationPresent(jakarta.persistence.OneToOne.class)
                                    && childField.getType().equals(parentClass)) {
                                return childField;
                            }
                        }
                    }
                }
            }

            return null;
        });
    }

    /**
     * Ottiene il tipo di parametro generico di un campo. Utile per determinare il tipo di entità in
     * una collezione.
     *
     * @param field Il campo da analizzare
     *
     * @return La classe del primo parametro generico o null
     */
    public static Class<?> getFirstActualTypeArgument(Field field) {
        Type genericType = field.getGenericType();
        if (genericType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) genericType;
            Type[] typeArguments = parameterizedType.getActualTypeArguments();
            if (typeArguments.length > 0) {
                return (Class<?>) typeArguments[0];
            }
        }
        return null;
    }

    /**
     * Ottiene il nome del campo mappedBy da un campo OneToMany.
     *
     * @param oneToManyField Il campo OneToMany
     *
     * @return Il nome del campo mappedBy o null
     */
    public static String getMappedByFieldName(Field oneToManyField) {
        OneToMany annotation = oneToManyField.getAnnotation(OneToMany.class);
        if (annotation != null) {
            return annotation.mappedBy();
        }
        return null;
    }

    /**
     * Ottiene il nome della tabella nel database corrispondente alla classe dell'entità.
     *
     * @param entityClass La classe dell'entità
     *
     * @return Il nome della tabella
     */
    public static String getTableName(Class<?> entityClass) {
        if (entityClass.isAnnotationPresent(Table.class)) {
            Table tableAnnotation = entityClass.getAnnotation(Table.class);
            if (!tableAnnotation.name().isEmpty()) {
                return tableAnnotation.name();
            }
            if (!tableAnnotation.schema().isEmpty()) {
                return tableAnnotation.schema() + "." + entityClass.getSimpleName().toUpperCase();
            }
        }
        // Fallback: usa il nome della classe convertito secondo la convenzione
        return camelCaseToSnakeCase(entityClass.getSimpleName()).toUpperCase();
    }

    /**
     * Ottiene il nome della colonna nel database corrispondente al campo dell'entità.
     *
     * @param field Il campo dell'entità
     *
     * @return Il nome della colonna
     */
    public static String getColumnName(Field field) {
        if (field == null) {
            return null;
        }

        if (field.isAnnotationPresent(Column.class)) {
            Column columnAnnotation = field.getAnnotation(Column.class);
            if (!columnAnnotation.name().isEmpty()) {
                return columnAnnotation.name();
            }
        } else if (field.isAnnotationPresent(JoinColumn.class)) {
            JoinColumn joinColumnAnnotation = field.getAnnotation(JoinColumn.class);
            if (!joinColumnAnnotation.name().isEmpty()) {
                return joinColumnAnnotation.name();
            }
        }

        // Fallback: converti il nome del campo secondo la convenzione
        return camelCaseToSnakeCase(field.getName()).toUpperCase();
    }

    /**
     * Converte una stringa da camelCase a SNAKE_CASE.
     *
     * @param camelCase La stringa in camelCase
     *
     * @return La stringa in snake_case
     */
    public static String camelCaseToSnakeCase(String camelCase) {
        return camelCase.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }

}
