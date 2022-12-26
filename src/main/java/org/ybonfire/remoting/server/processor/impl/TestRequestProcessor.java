package org.ybonfire.remoting.server.processor.impl;

import org.ybonfire.remoting.RemoteResponseBuilder;
import org.ybonfire.remoting.common.connection.Connection;
import org.ybonfire.remoting.protocol.request.IRemotingRequest;
import org.ybonfire.remoting.protocol.request.TestRequest;
import org.ybonfire.remoting.server.processor.IRequestProcessor;

/**
 * TestRequest Processor For Test
 *
 * @author yuanbo
 * @date 2022-12-23 11:11
 */
public class TestRequestProcessor implements IRequestProcessor<TestRequest> {
    @Override
    public void process(IRemotingRequest<TestRequest> request, Connection connection) {
        connection.invokeOneway(RemoteResponseBuilder.success(request.getCommandId(), null));
    }
}
