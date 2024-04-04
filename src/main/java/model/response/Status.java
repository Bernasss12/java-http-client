package model.response;

public enum Status {
    OK(200, "OK"),
    CREATED(201, "Created"),
    NOT_FOUND(404, "Not Found");

    final int code;
    final String message;

    Status(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public static Status fromCode(int code) {
        for (Status status : Status.values()) {
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
