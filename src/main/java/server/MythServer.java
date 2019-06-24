package server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.stream.ChunkedWriteHandler;

import javax.net.ssl.SSLEngine;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * @author myth
 */
public class MythServer {
    private static final int port = 4567;
    public void start(String[] args) throws Exception {
        String rootUrl = System.getProperty("user.dir").replace("\\", "/")+"/resources";
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        EventLoopGroup eventExecutors = new NioEventLoopGroup();
        serverBootstrap.group(eventExecutors)
                .channel(NioServerSocketChannel.class)
                .localAddress(port)
                .option(ChannelOption.SO_BACKLOG, 100)
                .handler(new LoggingHandler(LogLevel.DEBUG))
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline()
                                .addLast(new HttpResponseEncoder())
                                .addLast(new HttpRequestDecoder())
                                .addLast(new HttpObjectAggregator(65536))
                                .addLast(new ChunkedWriteHandler())
                                .addLast(new HttpMythHandler(rootUrl))
                                .addLast(new ExceptionHandler());
                    }
                });
        ChannelFuture future = serverBootstrap.bind().sync();
        HttpMythDispatcher.runDispatcher();
//        startTimer();
        System.out.println("已启动：根地址为：imyth.top:"+port);
        future.channel().closeFuture().sync();
        eventExecutors.shutdownGracefully().sync();
    }

    public static void startTimer() {
        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                try {
                    HttpMythDispatcher.runDispatcher();
                } catch (Exception e) {
                    System.out.println("定时自动扫描注入出现异常，时间"+new Date());
                    e.printStackTrace();
                }
            }
        };
        timer.schedule(timerTask, 180000);
    }
}
