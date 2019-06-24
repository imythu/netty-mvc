package server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author myth
 */
public class HttpMythHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private static final List<String> STATIC_RESOURCES = new ArrayList<>();
    private static final List<String> METHODS = new ArrayList<>();
    private static final String URL_SEPARATOR = "/";
    private static String rootUrl;
    private static HttpStaticResourcesResponse staticResourcesResponse;

    public HttpMythHandler(String rootUrl) {
        HttpMythHandler.rootUrl = rootUrl;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest fullHttpRequest) {
        setStaticResources();
        setMethods();
        String url = fullHttpRequest.uri();
        fullHttpRequest.headers().set(HttpHeaderNames.ACCEPT_CHARSET, CharsetUtil.UTF_8)
                .set(HttpHeaderNames.ACCEPT_ENCODING, CharsetUtil.UTF_8);
        String method = fullHttpRequest.method().name();
        if (METHODS.indexOf(method) < 0) {
            System.out.println("不支持此方法"+method);
            writeStatus(fullHttpRequest, ctx, "405");
            return;
        }
        int position = url.lastIndexOf(".") + 1;
        try {
            int index = STATIC_RESOURCES.indexOf(url.substring(position));
            if (index >= 0 || url.endsWith(URL_SEPARATOR)) {
                System.out.println("请求静态资源");
                if (url.endsWith(URL_SEPARATOR) && url.length() == 1) {
                    url = "/index.html";
                }
                if (url.endsWith(URL_SEPARATOR) && url.length() != 1) {
                    url = url + "/index.html";
                }
                if (staticResourcesResponse != null) {
                } else {
                    staticResourcesResponse = new HttpStaticResourcesResponse();
                }
                staticResourcesResponse.writeAndFlush(ctx, rootUrl + url);
            } else {
                System.out.println("请求动态资源");
                HttpMythResponse response = new HttpMythResponse(ctx);
                HttpMythRequest request = new HttpMythRequest(fullHttpRequest);
                response.setHeader(HttpHeaderNames.SET_COOKIE.toString(),
                        "mythId="+UUID.randomUUID().toString().replaceAll("-",""));

                HttpMythDispatcher dispatcher = new HttpMythDispatcher(request, response);
                if (dispatcher.doDispatcher(request, response) == -1) {
                    writeStatus(fullHttpRequest, ctx, "500");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            ctx.writeAndFlush("500").addListener(ChannelFutureListener.CLOSE);
        }
    }

    private void setStaticResources() {
        STATIC_RESOURCES.add("html");
        STATIC_RESOURCES.add("js");
        STATIC_RESOURCES.add("css");
        STATIC_RESOURCES.add("gif");
        STATIC_RESOURCES.add("jpg");
        STATIC_RESOURCES.add("ico");
    }

    private void setMethods() {
        METHODS.add("GET");
        METHODS.add("GET".toLowerCase());
        METHODS.add("POST");
        METHODS.add("POST".toLowerCase());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
//        super.channelReadComplete(ctx);
    }

    private void writeStatus(FullHttpRequest request, ChannelHandlerContext ctx, String statusCode) {
        ByteBuf responseContent = Unpooled.copiedBuffer(statusCode, CharsetUtil.UTF_8);
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain, charset=utf-8");
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, responseContent.readableBytes());
        response.content().writeBytes(responseContent);
        ctx.writeAndFlush(response);
        ctx.close();
    }
}
