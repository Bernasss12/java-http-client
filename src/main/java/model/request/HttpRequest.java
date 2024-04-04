package model.request;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public record HttpRequest(
        HttpRequestType type,
        String path,
        String version,
        Map<String, String> headers,
        String content
) {

    public static HttpRequest fromStream(InputStream input) throws IOException {
        // TODO Different kinds of requests might require different kinds or parsing.
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        StringBuilder builder = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null && !line.isEmpty()) {
            builder.append(line).append("\r\n");
        }

        String request = builder.toString();

        String[] lines = request.split("\r\n");
        String[] requestLine = lines[0].split(" ");
        HttpRequestType requestType = HttpRequestType.valueOf(requestLine[0]);
        String path = requestLine[1];
        String version = requestLine[2];
        Map<String, String> headers = new HashMap<>();
        for (int i = 1; i < lines.length; i++) {
            String[] header = lines[i].split(": ");
            headers.put(header[0], header[1]);
        }
        // TODO For other types of requests there might be content here...
        return new HttpRequest(requestType, path, version, headers, "");
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
}
