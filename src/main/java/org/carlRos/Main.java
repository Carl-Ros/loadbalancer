package org.carlRos;

import java.io.IOException;
import java.util.List;

public class Main {
    public static void main(String[] args)  {
        try {
            ApplicationServer server1 = new ApplicationServer(5501);
            ApplicationServer server2 = new ApplicationServer(5502);
            ApplicationServer server3 = new ApplicationServer(5503);

            LoadBalancer loadBalancer = new LoadBalancer(3500, List.of(server1.getInetSocketAddress(), server2.getInetSocketAddress(), server3.getInetSocketAddress()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}