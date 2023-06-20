package com.projecki.fusion.util;

import com.google.common.base.Preconditions;
import com.google.common.net.InetAddresses;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;

@SuppressWarnings("UnstableApiUsage")
public final class AddressUtil {

    private AddressUtil() {}

    /**
     * Attempts to parse an IP address of the form {@code 127.0.0.1:25565}. The returned
     * {@link InetSocketAddress} is not resolved.
     *
     * @param ip the IP to parse
     * @return the parsed address
     */
    public static InetSocketAddress parseAddress(String ip) {
        Preconditions.checkNotNull(ip, "ip");
        URI uri = URI.create("tcp://" + ip);
        if (uri.getHost() == null) {
            throw new IllegalStateException("Invalid hostname/IP " + ip);
        }

        int port = uri.getPort() == -1 ? 25565 : uri.getPort();
        try {
            InetAddress ia = InetAddresses.forUriString(uri.getHost());
            return new InetSocketAddress(ia, port);
        } catch (IllegalArgumentException e) {
            return InetSocketAddress.createUnresolved(uri.getHost(), port);
        }
    }
}
