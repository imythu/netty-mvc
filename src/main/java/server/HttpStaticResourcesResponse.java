package server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.spdy.SpdyHeaders;
import io.netty.handler.stream.ChunkedFile;
import io.netty.util.CharsetUtil;

import javax.activation.MimetypesFileTypeMap;
import java.io.*;
import java.net.URL;
import java.util.UUID;

import static java.lang.Character.LINE_SEPARATOR;

/**
 * @author myth
 */
public class HttpStaticResourcesResponse {
    private String url;

    public HttpStaticResourcesResponse() {
    }

    public void setContentType(FullHttpResponse response) {
        int index = url.lastIndexOf(".");
        String suffix = url.substring(index+1);
//        System.out.println(suffix);
        String common = "type; charset=utf-8";
        String content = "text/html; charset=utf-8";
        switch (suffix) {
            case "html":
                content = common.replace("type", "text/html");
                break;
            case "js":
                content = common.replace("type", "application/x-javascript");
                break;
            case "css":
                content = common.replace("type", "text/css");
                break;
            case "gif":
                content = common.replace("type", "image/gif");
                break;
            case "jpg":
                content = common.replace("type", "image/jpeg");
                break;
            case "ico":
                content = common.replace("type", "application/x-icon");
                break;
        }
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, content);
    }

    public void writeAndFlush(ChannelHandlerContext ctx, String url) throws Exception {
        this.url = url;
        System.out.println("请求资源: "+url);
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        ByteBuf bytes = Unpooled.buffer();
        setContentType(response);

        FileInputStream fileInputStream = new FileInputStream(url);
        bytes.writeBytes(fileInputStream, fileInputStream.available());
        response.content().writeBytes(bytes);

        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }
}
