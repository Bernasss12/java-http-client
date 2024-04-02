package model;

public record HttpResponse(
        String version,
        HttpResponseStatus status,
        HttpResponseContentType contentType,
        Object content

) {

    public byte[] getBytes() {
        StringBuilder builder = new StringBuilder("%s %s\r\n".formatted(version, status));
        if (content != null) {
            String contentString = content.toString();
            builder.append("Content-Type: %s\r\n".formatted(contentType.message));
            builder.append("Content-Length: %d\r\n".formatted(contentString.length()));
            builder.append("\r\n");
            builder.append(content);
        }
        builder.append("\r\n");
        return builder.toString().getBytes();
    }
}
