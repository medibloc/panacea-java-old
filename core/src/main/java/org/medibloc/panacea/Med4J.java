package org.medibloc.panacea;

import org.medibloc.panacea.core.Med4JImpl;
import org.medibloc.panacea.core.Panacea;
import org.medibloc.panacea.core.ProtobufService;

public abstract class Med4J implements Panacea {

    public static Med4J create(ProtobufService service) {
        return new Med4JImpl(service);
    }

    /**
     * Shutdowns a Web3j instance and closes opened resources.
     */
    public abstract void shutdown();
}
