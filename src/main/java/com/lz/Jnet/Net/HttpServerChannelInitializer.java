package com.lz.Jnet.Net;

import com.ek.game.core.config.ThreadPoolExecutorConfig;
import com.ek.game.core.service.socket.client.SocketClient;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.util.concurrent.DefaultEventExecutorGroup;

public class HttpServerChannelInitializer extends ChannelInitializer<SocketChannel> {


    HttpServerChannelInitializer(){
    }
    @Override
    protected void initChannel(SocketChannel socketChannel) {
        ChannelPipeline channelPipLine = socketChannel.pipeline();
        channelPipLine.addLast(new HttpServerCodec());
        channelPipLine.addLast(new HttpObjectAggregator(1024 * 1024));
        int threadSize = threadPoolExecutorConfig.getMaxPoolSize();
        channelPipLine.addLast(new DefaultEventExecutorGroup(threadSize), new HttpServerHandler(socketClient,filter));
    }
}