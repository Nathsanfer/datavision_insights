package com.cti.backend_java.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties (prefix = "app")
public record AppProperties (Jwt jwt, Cors cors, Admin admin, Upload upload, Etl etl) {
    public record Jwt(String secret, long expirationMs) {}
    public record Cors(List<String> origins) {}
    public record Admin(String nome, String email, String senha) {}
    public record Upload(int maxLinhas) {}
    public record Etl(String url, String key) {}
}
