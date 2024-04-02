package model;

public enum HttpResponseStatus {
    OK(200, "OK"),
    NOT_FOUNT(404, "Not Found");

    final int code;
    final String message;

    HttpResponseStatus(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public static HttpResponseStatus fromCode(int code) {
        for (HttpResponseStatus status : HttpResponseStatus.values()) {
            if (status.code == code) {
                return status;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "%d %s".formatted(code, message);
    }
}
