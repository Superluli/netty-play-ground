package com.superluli.netty.stream;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

public class StreamServer {

	public static void main(String[] args) {

		ServerBootstrap bs = new ServerBootstrap();

		bs.setFactory(new NioServerSocketChannelFactory(Executors
				.newCachedThreadPool(), Executors.newCachedThreadPool()));

		/*
		 * Create a new pipeline for each channel
		 */
		bs.setPipelineFactory(new MyPipelineFactory());
		bs.bind(new InetSocketAddress(8888));
		System.out.println("Netty Server");
	}

	static class MyPipelineFactory implements ChannelPipelineFactory {

		@Override
		public ChannelPipeline getPipeline() throws Exception {

			ChannelPipeline pipeline = Channels.pipeline();
			pipeline.addFirst("handler", new MyFrameHandler());
			return pipeline;
		}
	}

	static class MyFrameHandler extends SimpleChannelHandler {

		static final int FRAME_SIZE = 10;

		/*
		 * Buffer shared by consecutive events on the same channel
		 */
		ChannelBuffer readBuffer = ChannelBuffers.dynamicBuffer();

		@Override
		public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
				throws Exception {

			ChannelBuffer buffer = (ChannelBuffer) e.getMessage();
			/*
			 * Incoming buffer size is unknown
			 */
			readBuffer.writeBytes(buffer);

			/*
			 * Again, readableBytes is unknown, maybe 1, maybe n times bigger than FRAME_SIZE
			 */
			while (readBuffer.readableBytes() >= FRAME_SIZE) {
				byte[] arr = new byte[FRAME_SIZE];
				readBuffer.readBytes(arr);
				System.out.println("Framed message : " + Arrays.toString(arr));
			}

		}

		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
				throws Exception {
			e.getCause().printStackTrace();
		}
	}
}
