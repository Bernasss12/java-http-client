package model.response;

public interface ContentType {
    String getResponseText();

    enum Text implements ContentType {
        PLAIN("plain");

        private final String type;

        Text(String type) {this.type = type;}

        @Override
        public String getResponseText() {
            return "text/" + type;
        }
    }

    enum Application implements ContentType {
        OCTET_STREAM("octet-stream");

        private final String type;

        Application(String type) {this.type = type;}

        @Override
        public String getResponseText() {
            return "application/" + type;
        }
    }
}

