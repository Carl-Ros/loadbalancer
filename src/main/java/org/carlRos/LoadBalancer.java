package org.carlRos;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.util.List;

public class LoadBalancer {
    private final ServerSocket server;
    private final List<InetSocketAddress> serverAddresses;
    private int nextServerIndex = 0;

    public LoadBalancer(int port, List<InetSocketAddress> serverAddresses) throws IOException {
        server = new ServerSocket(port);
        this.serverAddresses = serverAddresses;
        start();
    }

    private synchronized InetSocketAddress roundRobin() {
        var nextServerAddress = serverAddresses.get(nextServerIndex);
        nextServerIndex = (nextServerIndex + 1) % serverAddresses.size();
        return nextServerAddress;
    }

    private void loadBalance(Socket clientSocket) throws IOException {
        var nextServerAddress = roundRobin();

        try (var serverSocket = new Socket()) {
            serverSocket.connect(nextServerAddress);

            try (var clientOs = clientSocket.getOutputStream();
                 var clientIs = clientSocket.getInputStream();
                 var serverOs = serverSocket.getOutputStream();
                 var serverIs = serverSocket.getInputStream()) {
                passThrough(clientIs, serverOs);
                passThrough(serverIs, clientOs);
            }
        }
    }

    private void passThrough(InputStream is, OutputStream os) throws IOException {
        int bufferSize = 8096;
        byte[] buffer = new byte[bufferSize];
        int bytesRead = is.read(buffer, 0, bufferSize);
        os.write(buffer, 0, bytesRead);
    }

    public void start() {
        try {
            listen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void listen() throws IOException {
        System.out.println("Load balancer started on " + server.getInetAddress() + ":" + server.getLocalPort());
        while(!server.isClosed()) {
            var clientSocket = server.accept();
                Thread.startVirtualThread(() -> {
                    try {
                        loadBalance(clientSocket);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            clientSocket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
        }
    }
}
