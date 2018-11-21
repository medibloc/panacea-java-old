package org.med4j;

import org.med4j.core.Med4JImpl;
import org.med4j.core.Panacea;
import org.med4j.core.ProtobufService;

public interface Med4J extends Panacea {

    public static Med4J create(ProtobufService service) {
        return new Med4JImpl(service);
    }

    /**
     * Shutdowns a Web3j instance and closes opened resources.
     */
    void shutdown();
}
