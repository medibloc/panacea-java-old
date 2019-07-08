package org.medibloc.panacea.core;

import com.google.protobuf.Message;

import java.io.IOException;
import java.util.Map;

public interface ProtobufService {

    /**
     * Get Panacea Request Object
     *
     * @param requestMessage protobuf request message
     * @param params additional informations need for make Request(ex. HTTP method, HTTP url path)
     * @param responseType protobuf response message type
     * @param <T> protobuf response message
     * @return panacea Request object
     * @throws IOException thrown if failed to perform a request
     */
    <T extends Message> Request<T> getRequest(Message requestMessage, Map<String, String> params, Class<T> responseType);

    /**
     * Closes resources used by the service.
     *
     * @throws IOException thrown if a service failed to close all resources
     */
    void close() throws IOException;
}
