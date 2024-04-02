import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Objects;

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
            try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
                try (OutputStream out = clientSocket.getOutputStream()) {
                    String[] request = in.readLine().split(" "); // Ignore the client input
                    if (Objects.equals(request[1], "/")) {
                        out.write("HTTP/1.1 200 OK\r\n\r\n".getBytes());
                    } else {
                        out.write("HTTP/1.1 404 Not Found\r\n\r\n".getBytes());
                    }
                }
            }
            System.out.println("accepted new connection");
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }
    }
}
