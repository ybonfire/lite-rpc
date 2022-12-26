package org.ybonfire.remoting.quickstart;

import org.ybonfire.remoting.Endpoint;
import org.ybonfire.remoting.RequestCodeEnum;
import org.ybonfire.remoting.client.IRemotingClient;
import org.ybonfire.remoting.client.NettyRemotingClient;
import org.ybonfire.remoting.client.RemotingClientOptions;
import org.ybonfire.remoting.protocol.request.IRemotingRequest;
import org.ybonfire.remoting.protocol.request.RemotingRequest;
import org.ybonfire.remoting.protocol.request.TestRequest;
import org.ybonfire.remoting.protocol.response.IRemotingResponse;

/**
 * 客户端示例
 *
 * @author yuanbo
 * @date 2022-12-22 13:29
 */
public class ClientExample {
    public static void main(String[] args) throws InterruptedException {
        final RemotingClientOptions options = RemotingClientOptions.builder().build();
        final IRemotingClient client = new NettyRemotingClient(options);
        Runtime.getRuntime().addShutdownHook(new Thread(client::shutdown));
        client.start();

        for (int i = 0; i < 100; ++i) {
            final IRemotingRequest<TestRequest> request =
                new RemotingRequest<>(RequestCodeEnum.TEST, new TestRequest());
            IRemotingResponse<?> response = client.invoke(Endpoint.from("0.0.0.0:4690"), request, 15 * 1000L);
            System.out.println(response.getStatus());
            Thread.sleep(1000L);
        }
    }
}
