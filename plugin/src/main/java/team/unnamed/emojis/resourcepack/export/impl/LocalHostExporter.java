package team.unnamed.emojis.resourcepack.export.impl;

import com.sun.net.httpserver.HttpExchange;
import org.jetbrains.annotations.Nullable;
import team.unnamed.creative.ResourcePack;
import team.unnamed.creative.file.FileTreeWriter;
import team.unnamed.creative.server.ResourcePackRequest;
import team.unnamed.creative.server.ResourcePackRequestHandler;
import team.unnamed.creative.server.ResourcePackServer;
import team.unnamed.emojis.resourcepack.UrlAndHash;
import team.unnamed.emojis.resourcepack.export.ResourceExporter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class LocalHostExporter implements ResourceExporter, ResourcePackRequestHandler {

    private final String address;
    private final int port;
    private @Nullable ResourcePackServer server;
    private @Nullable ResourcePack pack;

    public LocalHostExporter(String address, int port) {
        this.address = address;
        this.port = port;
    }

    @Override
    public @Nullable UrlAndHash export(FileTreeWriter writer) throws IOException {

        this.pack = ResourcePack.build(writer);

        if (this.server == null) {
            this.server = ResourcePackServer.builder()
                    .address(address, port)
                    .handler(this)
                    .build();
            this.server.start(); // TODO: Stop when??
        }

        // Example:
        // http://127.0.0.1:7270/f69deb4e77d2c6820b39652f63e6deceb87ba13d.zip
        return new UrlAndHash(
                "http://" + address + ':' + port + '/' + pack.hash() + ".zip",
                pack.hash()
        );
    }

    @Override
    public void onRequest(ResourcePackRequest request, HttpExchange exchange) throws IOException {

        if (pack == null) {
            byte[] response = "No resource pack exported yet".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(400, response.length);
            exchange.getResponseBody().write(response);
            return;
        }

        byte[] data = pack.bytes();
        exchange.getResponseHeaders().set("Content-Type", "application/zip");
        exchange.sendResponseHeaders(200, data.length);
        exchange.getResponseBody().write(data);
    }

}
