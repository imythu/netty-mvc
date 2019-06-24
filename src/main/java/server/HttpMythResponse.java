package server;


import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

/**
 * @author myth
 */
public class HttpMythResponse {

    private ChannelHandlerContext ctx;
    private FullHttpResponse response;


    public HttpMythResponse(ChannelHandlerContext ctx) {
        this.ctx = ctx;
        response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
    }

    public void write(Object content) {
        setContentType();
        ByteBuf byteBuf = Unpooled.copiedBuffer(content.toString(), CharsetUtil.UTF_8);
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, byteBuf.readableBytes());
        response.content().writeBytes(byteBuf);
        ctx.write(response);
    }

    public void setHeader(String headerName, Object content) {
        response.headers().set(headerName, content);
    }

    public void flush() {
        try {
            ctx.flush();
            ctx.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void writeAndFlush(Object content) {
        write(content);
        flush();
    }

    private void setContentType() {
        if (response.headers().get(HttpHeaderNames.CONTENT_TYPE) == null) {
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=utf-8");
        }
    }
}
