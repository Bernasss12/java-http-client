import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import model.request.HttpRequest;
import model.request.HttpRequestType;
import model.response.ContentType;
import model.response.HttpResponse;
import model.response.Status;

/**
 * The Main class is the entry point of the program. It contains the main method which is responsible for running the program.
 * The program starts by printing a log message and then proceeds to handle incoming client connections.
 */
public class Main {

    static Path directory;

    public static void main(String[] args) {
        System.out.println("Logs from your program will appear here!");

        int index = arrayIndexOf(args, "--directory");

        if (index != -1) {
            File dir = new File(args[index + 1]);
            if (dir.exists() && dir.isDirectory()) {
                directory = Paths.get(args[index + 1]);
                System.out.println("Initialized with given directory: " + directory.toAbsolutePath());
            }
        }

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
                System.out.println("Reading request...");
                HttpRequest request = HttpRequest.fromStream(in);
                System.out.println("Got: " + request);
                if (request.matches(HttpRequestType.GET, "/")) {
                    out.write(new HttpResponse(
                            "HTTP/1.1",
                            Status.OK,
                            null,
                            null
                    ).getBytes());
                } else if (request.matches(HttpRequestType.GET, "/user-agent")) {
                    out.write(new HttpResponse(
                            "HTTP/1.1",
                            Status.OK,
                            ContentType.Text.PLAIN,
                            request.headers().get("User-Agent")
                    ).getBytes());
                } else if (request.matches(HttpRequestType.GET, "/echo/*")) {
                    String content = request.path().replace("/echo/", "");
                    out.write(new HttpResponse(
                            "HTTP/1.1",
                            Status.OK,
                            ContentType.Text.PLAIN,
                            content
                    ).getBytes());
                } else if (request.matches(HttpRequestType.GET, "/files/*")) {
                    if (directory == null) {
                        out.write(new HttpResponse(
                                "HTTP/1.1",
                                Status.NOT_FOUND,
                                null,
                                "Directory cannot be null"
                        ).getBytes());
                        return;
                    }

                    File file = Paths.get(directory.toAbsolutePath().toString(), remaining("/files/*", request.path())).toFile();
                    System.out.println("Looking for file: " + file.toPath().toAbsolutePath());

                    if (!file.exists()) {
                        out.write(new HttpResponse(
                                "HTTP/1.1",
                                Status.NOT_FOUND,
                                ContentType.Text.PLAIN,
                                "File \"%s\" was not found."
                        ).getBytes());
                        return;
                    }

                    out.write(
                            new HttpResponse(
                                    "HTTP/1.1",
                                    Status.OK,
                                    ContentType.Application.OCTET_STREAM,
                                    Files.readString(file.toPath())
                            ).getBytes()
                    );
                } else if (request.matches(HttpRequestType.POST, "/files/*")) {
                    if (directory == null) {
                        out.write(new HttpResponse(
                                "HTTP/1.1",
                                Status.NOT_FOUND,
                                ContentType.Text.PLAIN,
                                "Directory cannot be null"
                        ).getBytes());
                        return;
                    }

                    File file = Paths.get(directory.toAbsolutePath().toString(), remaining("/files/*", request.path())).toFile();
                    System.out.println("Writing to file: " + file.toPath().toAbsolutePath());

                    file.createNewFile();

                    try (FileWriter writer = new FileWriter(file)) {
                        writer.write(request.body());
                        out.write(
                                new HttpResponse(
                                        "HTTP/1.1",
                                        Status.CREATED,
                                        ContentType.Null,
                                        null
                                ).getBytes()
                        );
                    } catch (Exception e) {
                        System.out.println("Error while writing: " + e.getMessage());
                    }

                    out.write(new HttpResponse(
                            "HTTP/1.1",
                            Status.NOT_FOUND,
                            ContentType.Text.PLAIN,
                            "Error writing file."
                    ).getBytes());

                } else {
                    out.write(new HttpResponse(
                            "HTTP/1.1",
                            Status.NOT_FOUND,
                            ContentType.Null,
                            null
                    ).getBytes());
                }
            }
        } catch (IOException e) {
            System.out.println("Problem when reading or writing on the streams.");
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.out.println("Problem disconnecting gracefully!");
            } finally {
                System.out.println("Disconnected!");
            }
        }
    }

    /**
     * Finds the index of the first occurrence of the specified object in the given array.
     *
     * @param <T>    the type of the array elements
     * @param array  the array to search in
     * @param object the object to search for
     * @return the index of the first occurrence of the object, or -1 if not found
     */
    private static <T> int arrayIndexOf(T[] array, T object) {
        int index = 0;
        for (T obj : array) {
            if (obj.equals(object)) {
                return index;
            }
            index++;
        }
        return -1;
    }

    /**
     * Replaces the specified substring in the given path with an empty string.
     * This expects the provided string to end with * and will ignore that correctly.
     *
     * @param match the substring to be replaced
     * @param path  the path to perform the replacement on
     * @return the modified path with the specified substring removed
     */
    private static String remaining(String match, String path) {
        return path.replace(match.substring(0, match.length() - 2), "");
    }

}
