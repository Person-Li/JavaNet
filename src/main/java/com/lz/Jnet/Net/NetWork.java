package com.lz.Jnet.Net;

import com.lz.Jnet.Config.NameConfig;
import com.lz.Jnet.Factory.ThreadNameFactory;
import com.lz.Jnet.Util.Out;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class NetWork {
    private boolean isRunning=false;
    private NioEventLoopGroup bossGroup,workerGroup;
    private final ThreadNameFactory bossThreadNameFactory,workerThreadNameFactory;
    private final ChannelInitializer channelInitializer;

    private int serverPort;


    public NetWork(){
        bossThreadNameFactory=new ThreadNameFactory(NameConfig.FRAME_NAME+"BOSS");
        workerThreadNameFactory=new ThreadNameFactory(NameConfig.FRAME_NAME+"WORKER");
    }
    public String getName(){
        return "HttpServer";
    }

    public void start() throws InterruptedException {
        synchronized (this) {
            if (!isRunning) {
                isRunning = true;
                bossGroup = new NioEventLoopGroup(1, bossThreadNameFactory);
                workerGroup = new NioEventLoopGroup(0, workerThreadNameFactory);
                ServerBootstrap serverBootstrap = new ServerBootstrap();
                serverBootstrap = serverBootstrap.group(bossGroup, workerGroup);
                serverBootstrap.channel(NioServerSocketChannel.class)
                        .option(ChannelOption.SO_BACKLOG, 1024)
                        .childOption(ChannelOption.SO_REUSEADDR, true) //重用地址
                        .childOption(ChannelOption.SO_RCVBUF, 65536)
                        .childOption(ChannelOption.SO_SNDBUF, 65536)
                        .childOption(ChannelOption.TCP_NODELAY, true)
                        .childOption(ChannelOption.SO_KEEPALIVE, true)
                        .childOption(ChannelOption.ALLOCATOR, new PooledByteBufAllocator(false))  // heap buf 's better
                        .childOption(ChannelOption.CONNECT_TIMEOUT_MILLIS, 1000)
                        .handler(new LoggingHandler(LogLevel.INFO))
                        .childHandler(channelInitializer);
                // bootstrap.setOption("child.tcpNoDelay", Boolean.valueOf(true));
                // bootstrap.setOption("child.keepAlive", Boolean.valueOf(true));
                // bootstrap.setOption("child.reuseAddress", Boolean.valueOf(true));
                // bootstrap.setOption("child.connectTimeoutMillis", Integer.valueOf(100));
                ChannelFuture serverChannelFuture = serverBootstrap.bind(serverPort).sync();

                serverChannelFuture.channel().closeFuture().addListener(ChannelFutureListener.CLOSE);
                Out.outBlue(String.format("Thread[%s] Service[%s] startup port[%s]", Thread.currentThread().getName(), getName(),serverPort));
            }
        }
    }
}
