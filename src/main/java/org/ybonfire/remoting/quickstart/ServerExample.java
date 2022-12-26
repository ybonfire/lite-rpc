package org.ybonfire.remoting.quickstart;

import org.ybonfire.remoting.RequestCodeEnum;
import org.ybonfire.remoting.server.IRemotingServer;
import org.ybonfire.remoting.server.NettyRemotingServer;
import org.ybonfire.remoting.server.RemotingServerOptions;
import org.ybonfire.remoting.server.processor.impl.TestRequestProcessor;

/**
 * 服务端示例
 *
 * @author yuanbo
 * @date 2022-12-22 13:29
 */
public class ServerExample {
    public static void main(String[] args) {
        final RemotingServerOptions options = RemotingServerOptions.builder().build();
        final IRemotingServer server = new NettyRemotingServer(4690, options);
        server.registerProcessor(RequestCodeEnum.TEST, new TestRequestProcessor());
        Runtime.getRuntime().addShutdownHook(new Thread(server::shutdown));
        server.start();
    }
}
