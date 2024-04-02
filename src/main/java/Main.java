import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import model.HttpRequest;
import model.HttpResponse;
import model.HttpResponseContentType;
import model.HttpResponseStatus;

public class Main {

    public static void main(String[] args) {
        System.out.println("Logs from your program will appear here!");

        try (ServerSocket serverSocket = new ServerSocket(4221);) {
            serverSocket.setReuseAddress(true);
            System.out.println("Waiting for connections!");

            while (true) {
                Socket clientSocket = serverSocket.accept(); // Wait for connection from client.
                new Thread(
                        () -> handleClient(clientSocket)
                ).start();
            }

        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }
    }

    private static void handleClient(Socket clientSocket) {
        System.out.println("Connected!");
        try (InputStream in = clientSocket.getInputStream()) {
            try (OutputStream out = clientSocket.getOutputStream()) {
                HttpRequest request = HttpRequest.fromStream(in);
                if (request.path().equals("/")) {
                    out.write(new HttpResponse(
                            "HTTP/1.1",
                            HttpResponseStatus.OK,
                            null,
                            null
                    ).getBytes());
                } else if (request.path().equals("/user-agent")) {
                    out.write(new HttpResponse(
                            "HTTP/1.1",
                            HttpResponseStatus.OK,
                            HttpResponseContentType.TEXT_PLAIN,
                            request.headers().get("User-Agent")
                    ).getBytes());
                } else if (request.path().startsWith("/echo/")) {
                    String content = request.path().replace("/echo/", "");
                    out.write(new HttpResponse(
                            "HTTP/1.1",
                            HttpResponseStatus.OK,
                            HttpResponseContentType.TEXT_PLAIN,
                            content
                    ).getBytes());
                } else {
                    out.write(new HttpResponse(
                            "HTTP/1.1",
                            HttpResponseStatus.NOT_FOUNT,
                            null,
                            null
                    ).getBytes());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                System.out.println("Disconnected!");
            }
        }
    }

}
