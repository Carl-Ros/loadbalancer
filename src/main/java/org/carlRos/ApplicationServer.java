package org.carlRos;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ApplicationServer {
    private final ServerSocket server;

    public ApplicationServer(int port) throws IOException {
        server = new ServerSocket(port);
        start();
    }

    public InetSocketAddress getInetSocketAddress() {
        return new InetSocketAddress(server.getInetAddress(), server.getLocalPort());
    }

    private void greet(Socket clientSocket) throws IOException {
        String greeting = "Hello from " + getInetSocketAddress().toString();
        var response = OkHttpStringResponse(greeting);
        clientSocket.getOutputStream().write(response.getBytes(StandardCharsets.UTF_8));
    }

    private String OkHttpStringResponse(String message) {
        return "HTTP/1.1 200 OK\r\n" +
                "Content-Type: text/plain\r\n" +
                "Content-Length: " + message.length() + "\r\n" +
                "\r\n" +
                message;
    }

    private void start() {
        Thread.startVirtualThread(() -> {
            try {
                listen();
            } catch (IOException e) {
                throw new RuntimeException();
            }
        });
    }

    private void listen() throws IOException {
        System.out.println("Application server started on " + getInetSocketAddress().toString());
        while(!server.isClosed()) {
            var clientSocket = server.accept();

            Thread.startVirtualThread(() -> {
                try {
                    greet(clientSocket);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }
}
