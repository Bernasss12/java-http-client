package model.request;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record HttpRequest(
        HttpRequestType type,
        String path,
        String version,
        Map<String, String> headers,
        String body
) {

    public static HttpRequest fromStream(InputStream input) throws IOException {
        // TODO Different kinds of requests might require different kinds or parsing.
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        List<String> lines = new ArrayList<>();

        String current;
        while ((current = reader.readLine()) != null && !current.isEmpty()) {
            lines.add(current);
        }

        // Decompose first line as "REQ_TYPE PATH HTTP_VERSION"
        String[] header = lines.getFirst().split(" ");
        HttpRequestType requestType = HttpRequestType.valueOf(header[0]);
        String path = header[1];
        String version = header[2];
        lines.removeFirst();

        System.out.println("Reading headers!");
        // Iterate through remaining header lines!
        Map<String, String> headers = new HashMap<>();
        for (String line : lines) {
            String[] headerField = line.split(": ");
            headers.put(headerField[0], headerField[1]);
        }
        System.out.println("Read headers!");

        // If there is defined body, read it.
        StringBuilder content = new StringBuilder();
        if (headers.containsKey("Content-Length")) {
            System.out.println("Reading body!");
            int expectedLength = Integer.parseInt(headers.get("Content-Length"));
            while (reader.ready()) {
                content.append((char) reader.read());
            }
            System.out.printf("Body read, %d characters out of %d expected!%n", content.length(), expectedLength);
        }

        return new HttpRequest(requestType, path, version, headers, content.toString());
    }

    /**
     * Check if the provided request type and match string matches the current HttpRequest instance.
     *
     * @param requestType The HttpRequestType to compare with the current request type.
     * @param match       The match string to compare with the current request path.
     * @return true if the request type and match string matches the current HttpRequest, false otherwise.
     */
    public boolean matches(HttpRequestType requestType, String match) {
        if (requestType != type()) return false;
        if (match.endsWith("*") && path().startsWith(match.substring(0, match.length() - 2))) {
            System.out.printf("Partial match %s %s %s%n", requestType, match, path);
            return true;
        } else if (path().equals(match)) {
            System.out.printf("Full match %s %s %s%n", requestType, match, path);
            return true;
        } else return false;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("%s %s %s\n".formatted(type, path, version));
        for (Map.Entry<String, String> header: headers.entrySet()) {
            builder.append("%s: %s%n".formatted(header.getKey(), header.getValue()));
        }
        if (!body.isBlank()) {
            builder.append("\n").append(body).append("\n");
        }
        return builder.toString();
    }
}
