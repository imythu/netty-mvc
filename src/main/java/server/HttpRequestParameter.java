package server;

import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author myth
 */
public class HttpRequestParameter {

    private Map<String, String> parametersMap;

    public Map<String, String> getParametersMap() {
        return parametersMap;
    }

    public HttpRequestParameter(FullHttpRequest request) throws IOException {
        initGetParametersMap(request);
    }

    public void initGetParametersMap(FullHttpRequest request) throws IOException {
        parametersMap = new LinkedHashMap<>();
        HttpMethod method = request.method();
        if (method == HttpMethod.GET) {
            QueryStringDecoder decoder = new QueryStringDecoder(request.uri());
            decoder.parameters().entrySet().forEach( entry -> {
                // entry.getValue()是一个List, 只取第一个元素
                parametersMap.put(entry.getKey(), entry.getValue().get(0));
            });
        }else if (method == HttpMethod.POST) {
            HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(request);
            decoder.offer(request);
            List<InterfaceHttpData> datas = decoder.getBodyHttpDatas();
            for (InterfaceHttpData data : datas) {
                Attribute attribute = (Attribute) data;
                parametersMap.put(attribute.getName(), attribute.getValue());
            }
        }
    }
}
