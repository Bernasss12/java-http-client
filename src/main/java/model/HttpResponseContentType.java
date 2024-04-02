package model;

public enum HttpResponseContentType {
    TEXT_PLAIN("text/plain");

    final String message;

    HttpResponseContentType(String message) {
        this.message = message;
    }
}
