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

		// bs.getPipeline().addLast("MyHandler", new MyHandler());

		bs.getPipeline().addLast("1", new TestHandler("1", true));
		bs.getPipeline().addLast("2", new TestHandler("2", true));
		bs.getPipeline().addLast("3", new TestHandler("3", false));
		bs.getPipeline().addFirst("4", new TestHandler("4", false));
		bs.getPipeline().addFirst("5", new TestHandler("5", false));

		bs.bind(new InetSocketAddress(8888));
		System.out.println("Netty Server");
	}

	static class TestHandler extends SimpleChannelHandler {

		private String id;
		boolean isUpStream;

		public TestHandler(String id, boolean isUpStream) {
			this.id = id;
			this.isUpStream = isUpStream;
		}

		@Override
		public void channelConnected(ChannelHandlerContext ctx,
				ChannelStateEvent e) throws Exception {
			System.out.println(this.id);
			System.out.println(ctx);
			if (this.isUpStream) {
				ctx.sendUpstream(e);
			} else {
				ctx.sendDownstream(e);
			}
		}
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
