package server;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;

import java.io.IOException;
import java.util.Map;

public class HttpMythRequest {
    private HttpMythSession session;
    private FullHttpRequest request;
    private HttpRequestParameter parameter;

    public HttpMythRequest(FullHttpRequest request) throws IOException {
        this.request = request;
        session = new HttpMythSession(request.headers().get(HttpHeaderNames.COOKIE));
        parameter = new HttpRequestParameter(request);
    }

    public String getParameter(String parameterName) {
        try {
            return getParameterMap().get(parameterName);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("出现异常");
            return null;
        }
    }

    public Map<String, String> getParameterMap() throws IOException {
        return parameter.getParametersMap();
    }

    public HttpMythSession getSession() {
        return session;
    }

    public String getUrl() {
        String url = request.uri();
            int end = request.uri().indexOf("?");
            if (end == -1) return url;
            url = request.uri().substring(0,end);
        return url;
    }
}
