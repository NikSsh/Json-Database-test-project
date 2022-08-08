package server.requestANDresponse;

import com.google.gson.JsonObject;

public class Response {
    private String response, value, reason, type, key;

    Response (String response, String value, String reason, String type, String key) {
        this.response = response;
        this.value = value;
        this.reason = reason;
        this.type = type;
        this.key = key;
    }

    public static class ResponseBuilder {
        private String response, value, reason, type, key;

        public ResponseBuilder setResponse(String response) {
            this.response = response;
            return this;
        }

        public ResponseBuilder setValue(String value) {
            this.value = value;
            return this;
        }

        public ResponseBuilder setReason(String reason) {
            this.reason = reason;
            return this;
        }

        public ResponseBuilder setType(String type) {
            this.type = type;
            return this;
        }

        public ResponseBuilder setKey(String key) {
            this.key = key;
            return this;
        }

        public Response create() {
            return new Response(response, value, reason, type, key);
        }
    }
}
