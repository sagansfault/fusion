package com.projecki.fusion.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public final class NetworkingUtil {

    private static CompletableFuture<String> ipFuture = null;

    private NetworkingUtil() {
        throw new UnsupportedOperationException("This is a static utility class");
    }

    /**
     * Get the Ip of this machine
     * Uses AWS to get the external Ip of this machine
     *
     * @return future that completes with this machine's Ip
     */
    public static CompletableFuture<String> getIp() {

        if (ipFuture == null) {
            ipFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    URL url = new URL("http://checkip.amazonaws.com");
                    BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
                    return in.readLine();
                } catch (IOException e) {
                    throw new CompletionException(e);
                }
            });
        }

        return ipFuture;
    }

}
