package org.ybonfire.remoting;

import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Objects;

/**
 * 地址信息
 *
 * @author yuanbo
 * @date 2022-12-15 15:44
 */
@EqualsAndHashCode
public class Endpoint {
    private final String host;
    private final int port;

    private Endpoint(final String host, final int port) {
        if (StringUtils.isBlank(host)) {
            throw new IllegalArgumentException(String.format("Illegal host value: %s", host));
        }
        if (port < 0 || port > 65535) {
            throw new IllegalArgumentException(
                String.format("Illegal port value: %d, which should between 0 and 65535.", port));
        }
        this.host = host;
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public static Endpoint of(final String ip, final int port) {
        return new Endpoint(ip, port);
    }

    public static SocketAddress toSocketAddress(final Endpoint endpoint) {
        return new InetSocketAddress(endpoint.getHost(), endpoint.getPort());
    }

    public static Endpoint from(final SocketAddress address) {
        if (Objects.isNull(address)) {
            return null;
        }

        return from(address.toString());
    }

    public static Endpoint from(final String address) {
        if (StringUtils.isBlank(address)) {
            return null;
        }

        final String[] arr = StringUtils.split(address, ':');

        if (arr == null || arr.length < 2) {
            return null;
        }

        try {
            int port = Integer.parseInt(arr[1]);
            return Endpoint.of(arr[0], port);
        } catch (Exception ignored) {
            return null;
        }
    }

    @Override
    public String toString() {
        return "[" + host + ":" + port + "]";
    }
}
