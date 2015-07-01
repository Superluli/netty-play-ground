package com.superluli.netty.basic;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;

public class BasicClient {
	public static void main(String[] args) {

		ClientBootstrap bs = new ClientBootstrap();
		ChannelFactory cf = new NioClientSocketChannelFactory(
				Executors.newCachedThreadPool(),
				Executors.newCachedThreadPool());
		bs.setFactory(cf);

		bs.getPipeline().addLast("handler", new SimpleChannelHandler() {

			@Override
			public void messageReceived(ChannelHandlerContext ctx,
					MessageEvent e) throws Exception {

				System.out.println("Message Received!");
				ChannelBuffer readBuffer = (ChannelBuffer) e.getMessage();
				byte[] bytes = new byte[readBuffer.readableBytes()];
				readBuffer.readBytes(bytes);
				System.out.println("Message : " + new String(bytes));
				e.getChannel().close().addListener(new ChannelFutureListener() {

					@Override
					public void operationComplete(ChannelFuture future)
							throws Exception {
						System.out.println("channel closed");
					}
				});
				;
			}

			@Override
			public void exceptionCaught(ChannelHandlerContext ctx,
					ExceptionEvent e) throws Exception {
				e.getCause().printStackTrace();
			}
		});

		ChannelFuture f = bs.connect(new InetSocketAddress(8888));
		System.out.println("Netty Client");
		f.awaitUninterruptibly();
		f.getChannel().getCloseFuture().awaitUninterruptibly();
		cf.releaseExternalResources();
	}
}
