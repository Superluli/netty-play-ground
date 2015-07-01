package com.superluli.netty.stream;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;

public class StreamClient {

	public static void main(String[] args) throws Exception{
		ExecutorService executorService = Executors.newFixedThreadPool(100);
		for (int i = 0; i < 1000; i++) {
			executorService.execute(new ClientTask());
		}
		executorService.shutdown();
		executorService.awaitTermination(100, TimeUnit.SECONDS);
	}
	
	static class ClientTask implements Runnable {

		@Override
		public void run() {

			ClientBootstrap bs = new ClientBootstrap();
			ChannelFactory cf = new NioClientSocketChannelFactory(
					Executors.newCachedThreadPool(),
					Executors.newCachedThreadPool());
			bs.setFactory(cf);

			bs.getPipeline().addLast("handler", new SimpleChannelHandler() {
				@Override
				public void channelConnected(ChannelHandlerContext ctx,
						ChannelStateEvent e) throws Exception {
					ChannelBuffer buffer = ChannelBuffers.buffer(4);
					
					for(int i = 0; i < 50; i++){
						buffer.writeByte(i);
						e.getChannel().write(buffer);
						buffer.clear();
						Thread.sleep(1);
					}
				}
			});

			ChannelFuture f = bs.connect(new InetSocketAddress(8888));
			f.awaitUninterruptibly();
			f.getChannel().getCloseFuture().awaitUninterruptibly();
			cf.releaseExternalResources();
		}

	}
}
