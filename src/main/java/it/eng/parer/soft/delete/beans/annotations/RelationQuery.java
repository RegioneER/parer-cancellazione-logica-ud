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

package it.eng.parer.soft.delete.beans.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Definisce una query ottimizzata per recuperare tutti i figli di una relazione parent-child.
 *
 * <p>
 * L'annotazione deve essere posta direttamente sul field {@code @ManyToOne} o {@code @OneToOne}
 * dell'entity figlia. La {@code parentClass} è inferita automaticamente dal tipo del field,
 * eliminando ridondanza e possibili disallineamenti.
 *
 * <p>
 * <b>Annotazione singola</b>: una sola {@code @RelationQuery} sul field — si applica ai livelli
 * specificati in {@code levels} (o a tutti se {@code levels={}}).
 *
 * <p>
 * <b>Annotazioni multiple</b>: più {@code @RelationQuery} sullo stesso field (grazie a
 * {@code @Repeatable}) — permette query diverse per livelli diversi. Java le conserva nel container
 * {@code @RelationQueries}, che viene risolto in modo trasparente dal motore tramite
 * {@code getAnnotationsByType}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Repeatable(RelationQueries.class)
public @interface RelationQuery {

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
     * Livelli dell'albero a cui questa query si applica.
     * <ul>
     * <li>{@code levels = {}} (default, array vuoto): la query si applica a <b>tutti i livelli</b>
     * in cui questa relazione parent-child viene incontrata durante la visita BFS.</li>
     * <li>{@code levels = {2}}: la query si applica <b>solo al livello 2</b>; agli altri livelli
     * viene usata la query generata dinamicamente.</li>
     * <li>{@code levels = {2, 4}}: la query si applica ai livelli 2 e 4.</li>
     * </ul>
     *
     * <p>
     * <b>Priorità</b>: se per la stessa relazione parent-child esistono due annotazioni, una con
     * livello specifico e una generica ({@code levels={}}), il livello specifico ha la precedenza.
     *
     * <p>
     * <b>Stessa relazione a livelli multipli</b>: se un'entity figlia compare come figlia della
     * stessa parent a livelli diversi (es. 2 e 5), con {@code levels={}} la stessa query
     * ottimizzata viene usata in entrambi i casi. Per query diverse per livello, è possibile
     * definire più annotazioni {@code @RelationQuery} sullo stesso field con {@code levels}
     * distinti.
     */
    int[] levels() default {};
}
