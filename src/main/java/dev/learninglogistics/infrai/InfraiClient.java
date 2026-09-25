package dev.learninglogistics.infrai;

import dev.learninglogistics.config.InfraiConfig;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

public final class InfraiClient {
    private final InfraiConfig config;
    private final HttpClient http;

    public InfraiClient(InfraiConfig config) {
        this.config = config;
        this.http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    }

    public String requestPhoneCode(String phone, String locale) throws IOException, InterruptedException {
        return post("/v1/auth/phone/send_code", Map.of("phone", phone, "purpose", "login", "locale", locale));
    }

    public String sendOtp(String to) throws IOException, InterruptedException {
        return post("/v1/sms/otp", Map.of("to", to));
    }

    public String verifyPhoneLogin(String phone, String code) throws IOException, InterruptedException {
        return post("/v1/auth/phone/verify", Map.of("phone", phone, "code", code, "login", true));
    }

    private String post(String path, Map<String, ?> body) throws IOException, InterruptedException {
        for (int attempt = 0; attempt < 4; attempt++) {
            HttpRequest request = HttpRequest.newBuilder(URI.create(config.baseUrl() + path))
                    .timeout(Duration.ofSeconds(20))
                    .header("Authorization", "Bearer " + config.apiKey())
                    .header("Content-Type", "application/json")
                    .method("POST", HttpRequest.BodyPublishers.ofString(Json.object(body)))
                    .build();
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            Map<String, String> envelope = Json.topLevelFields(response.body());
            if (response.statusCode() == 429) {
                Thread.sleep(retryDelayMillis(response, attempt));
                continue;
            }
            if ("false".equals(envelope.get("ok"))) {
                Map<String, String> error = Json.topLevelFields(envelope.getOrDefault("error", "{}"));
                throw new InfraiException(error.getOrDefault("code", "REQUEST_REJECTED"),
                        error.getOrDefault("message", response.body()), response.statusCode());
            }
            if (response.statusCode() >= 500) throw new IOException("Infrai transport status " + response.statusCode());
            if (!"true".equals(envelope.get("ok"))) throw new IOException("Invalid Infrai envelope");
            return envelope.getOrDefault("data", "null");
        }
        throw new IOException("Infrai retry budget exhausted");
    }

    private long retryDelayMillis(HttpResponse<?> response, int attempt) {
        return response.headers().firstValue("Retry-After")
                .map(value -> {
                    try { return Long.parseLong(value) * 1000L; }
                    catch (NumberFormatException ignored) { return 250L << attempt; }
                }).orElse(250L << attempt);
    }
}
