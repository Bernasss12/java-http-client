package model.request;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
        while ((current = reader.readLine()) != null) {
            lines.add(current);
        }

        // Decompose first line as "REQ_TYPE PATH HTTP_VERSION"
        String[] request = lines.getFirst().split(" ");
        HttpRequestType requestType = HttpRequestType.valueOf(request[0]);
        String path = request[1];
        String version = request[2];

        // Iterate through remaining lines, start reading as headers, then after an empty line read as body.
        Map<String, String> headers = new HashMap<>();
        boolean readingContent = false;
        StringBuilder content = new StringBuilder();
        for (String line : lines) {
            if (!readingContent) {
                if (line.isEmpty()) {
                    readingContent = true;
                } else {
                    String[] header = line.split(": ");
                    headers.put(header[0], header[1]);
                }
            } else {
                content.append(line).append("\n");
            }
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
            builder.append("%s: %s".formatted(header.getKey(), header.getValue()));
        }
        if (!body.isBlank()) {
            builder.append("\n").append(body);
        }
        return builder.toString();
    }
}
