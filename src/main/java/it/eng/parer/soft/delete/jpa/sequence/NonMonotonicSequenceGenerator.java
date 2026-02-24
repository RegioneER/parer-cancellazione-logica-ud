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

/**
 *
 */
package it.eng.parer.soft.delete.jpa.sequence;

import java.io.Serializable;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Properties;

import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.generator.GeneratorCreationContext;
import org.hibernate.id.OptimizableGenerator;
import org.hibernate.id.enhanced.SequenceStyleGenerator;

/*
 * A generator that produces non-monotonic values by prepending a random number to a sequence value.
 */
public class NonMonotonicSequenceGenerator extends SequenceStyleGenerator {

    private static final long serialVersionUID = 1L;

    private static final String FORMAT = "%d%d";
    private SecureRandom rand;
    private static final int MAX = 9999;
    private static final int MIN = 1000;
    // config params
    private String sequenceName;
    private String initialValue;
    private String incrementSize;

    public NonMonotonicSequenceGenerator(NonMonotonicSequence config)
            throws NoSuchAlgorithmException {
        // secure random generator
        rand = SecureRandom.getInstance("NativePRNGNonBlocking");
        // read config
        sequenceName = config.sequenceName();
        initialValue = Integer.toString(config.startWith());
        incrementSize = Integer.toString(config.incrementBy());
    }

    @Override
    public void configure(GeneratorCreationContext creationContext, Properties parameters)
            throws MappingException {
        parameters.setProperty(SequenceStyleGenerator.SEQUENCE_PARAM, sequenceName);
        parameters.setProperty(OptimizableGenerator.INCREMENT_PARAM, incrementSize);
        parameters.setProperty(OptimizableGenerator.INITIAL_PARAM, initialValue);
        super.configure(creationContext, parameters);
    }

    private int random() {
        return (rand.nextInt((MAX - MIN) + 1) + MIN);
    }

    @Override
    public Serializable generate(SharedSessionContractImplementor session, Object object)
            throws HibernateException {
        return Long.parseLong(String.format(FORMAT, random(), super.generate(session, object)));
    }

}
