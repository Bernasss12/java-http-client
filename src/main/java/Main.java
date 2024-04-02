import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import model.HttpRequest;
import model.HttpResponse;
import model.HttpResponseContentType;
import model.HttpResponseStatus;

public class Main {

    public static void main(String[] args) {
        // You can use print statements as follows for debugging, they'll be visible when running tests.
        System.out.println("Logs from your program will appear here!");

        ServerSocket serverSocket = null;
        Socket clientSocket = null;

        try {
            serverSocket = new ServerSocket(4221);
            serverSocket.setReuseAddress(true);
            System.out.println("Waiting for connection!");
            clientSocket = serverSocket.accept(); // Wait for connection from client.
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
            }
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }
    }
}
