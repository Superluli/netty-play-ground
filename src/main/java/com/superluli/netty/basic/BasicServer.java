package com.superluli.netty.basic;

import java.net.InetSocketAddress;
import java.util.Calendar;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.buffer.LittleEndianHeapChannelBuffer;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

public class BasicServer {

	public static void main(String[] args) {

		ServerBootstrap bs = new ServerBootstrap();

		bs.setFactory(new NioServerSocketChannelFactory(Executors
				.newCachedThreadPool(), Executors.newCachedThreadPool()));

		bs.getPipeline().addLast("MyHandler", new MyHandler());
		bs.bind(new InetSocketAddress(8888));
		System.out.println("Netty Server");
	}

	static class MyHandler extends SimpleChannelHandler {

		@Override
		public void channelConnected(ChannelHandlerContext ctx,
				ChannelStateEvent e) throws Exception {

			ChannelBuffer time = ChannelBuffers.dynamicBuffer(2);
			String message = Calendar.getInstance().getTime().toString() + "\n";
			time.writeBytes(message.getBytes());
			e.getChannel().write(time).addListener(new ChannelFutureListener() {

				@Override
				public void operationComplete(ChannelFuture future)
						throws Exception {
					future.getChannel().close();
				}
			});
		}

		@Override
		public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
				throws Exception {

			ChannelBuffer readBuffer = (ChannelBuffer) e.getMessage();
			byte[] bytes = new byte[readBuffer.readableBytes()];
			readBuffer.readBytes(bytes);
			System.out.println("Message : " + new String(bytes) + "\n");
			ChannelBuffer writeBuffer = new LittleEndianHeapChannelBuffer(100);
			writeBuffer.writeBytes(bytes);
			e.getChannel().write(writeBuffer);
		}

		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
				throws Exception {
			ChannelBuffer writeBuffer = new LittleEndianHeapChannelBuffer(100);
			writeBuffer.writeBytes("E\n".getBytes());
			e.getChannel().write(writeBuffer);
		}
	}
}
