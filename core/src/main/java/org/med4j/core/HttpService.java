package org.med4j.core;

import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class HttpService implements ProtobufService {
    private static final String DEFAULT_URL = "http://localhost:9921";
    private static final MediaType MEDIA_TYPE = MediaType.parse("application/x-protobuf");
    static final String METHOD_KEY = "method";
    static final String PATH_KEY = "path";

    private OkHttpClient client = new OkHttpClient();
    private String baseUrl;

    public HttpService() {
        this(DEFAULT_URL);
    }

    public HttpService(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    @Override
    public <T extends Message> Request<T> getRequest(final Message requestMessage, final Map<String, String> params, final Class<T> responseType) {
        if (!params.containsKey(METHOD_KEY) || !params.containsKey(PATH_KEY)) {
            throw new RuntimeException("method and/or path is null");
        }

        return new Request<T>() {
            @Override
            protected T doSend() throws IOException {
                RequestBody requestBody = RequestBody.create(MEDIA_TYPE, requestMessage.toByteArray());
                String method = params.get(METHOD_KEY);
                okhttp3.Request.Builder requestBuilder = new okhttp3.Request.Builder()
                        .url(baseUrl + params.get(PATH_KEY));
                if (method.equals("GET")) {
                    requestBuilder = requestBuilder.get();
                } else {
                    requestBuilder = requestBuilder.method(method, requestBody);
                }

                okhttp3.Request request = requestBuilder.build();

                Response response = client.newCall(request).execute();

                ResponseBody body = response.body();
                if (body == null) {
                    throw new IOException("body is null");
                }

                Message.Builder builder;
                try {
                    builder = (Message.Builder) responseType.getMethod("newBuilder").invoke(null);
                } catch(NoSuchMethodException e) {
                    throw new RuntimeException(e);
                } catch(IllegalAccessException e) {
                    throw new RuntimeException(e);
                } catch(InvocationTargetException e) {
                    throw new RuntimeException(e);
                }

                JsonFormat.parser().merge(body.string(), builder);
                return responseType.cast(builder.build());
            }
        };
    }

    @Override
    public void close() throws IOException {

    }
}
