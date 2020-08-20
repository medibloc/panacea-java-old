package org.medibloc.panacea.did;

import org.medibloc.panacea.crypto.ECKeyPair;
import org.medibloc.panacea.crypto.Keys;

public class Did {
    private String id;

    public Did(NetworkId networkId, DidKey key, String password) throws Exception {
        ECKeyPair keyPair = key.getKeyPair(password);
        //TODO: don't use compressPubKey()
        this.id = String.format("did:panacea:%s:%s", networkId, Keys.compressPubKey(keyPair.getPubKey()));
    }

    @Override
    public String toString() {
        return "Did{" +
                "id='" + id + '\'' +
                '}';
    }

    public enum NetworkId {
        MAINNET("mainnet"), TESTNET("testnet");

        private final String id;

        private NetworkId(String id) {
            this.id = id;
        }

        @Override
        public String toString() {
            return id;
        }
    }
}
