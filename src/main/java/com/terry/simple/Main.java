package com.terry.simple;
import com.sun.net.httpserver.HttpServer;
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Objects;

public class Main {
    public static void main(String[] args) {
        PrometheusMeterRegistry prometheusRegistry =
                new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
        new ClassLoaderMetrics().bindTo(prometheusRegistry);
        new JvmMemoryMetrics().bindTo(prometheusRegistry);
        new JvmGcMetrics().bindTo(prometheusRegistry);
        new ProcessorMetrics().bindTo(prometheusRegistry);
        new JvmThreadMetrics().bindTo(prometheusRegistry);

        String envPort = System.getenv("L_PORT");
        int port = Objects.equals(envPort, "") ? Integer.parseInt(envPort) : 9999;

        try {
            InetAddress addr;
            addr = InetAddress.getLocalHost();
            byte[] hostname = addr.getHostName().getBytes();

            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext("/metrics", httpExchange -> {
                String response = prometheusRegistry.scrape();

                httpExchange.sendResponseHeaders(200, response.getBytes().length);
                try (OutputStream os = httpExchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
            });

            // trigger full gc
            server.createContext("/fujic", httpExchange -> {
                System.gc();
                httpExchange.sendResponseHeaders(200, hostname.length);
                try (OutputStream os = httpExchange.getResponseBody()) {
                    os.write(hostname);
                }
            });
            new Thread(server::start).start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        System.out.println("listen on port: " + port);
    }
}