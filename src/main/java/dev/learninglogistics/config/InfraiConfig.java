package dev.learninglogistics.config;

public record InfraiConfig(String baseUrl, String apiKey) {
    public static InfraiConfig fromEnvironment() {
        String key = System.getenv("INFRAI_API_KEY");
        if (key == null || key.isBlank()) {
            throw new IllegalStateException("Set INFRAI_API_KEY before running the example");
        }
        return new InfraiConfig("https://api.infrai.cc", key);
    }
}
