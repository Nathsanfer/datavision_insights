package com.cti.backend_java.security;

import com.cti.backend_java.models.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.io.Decoders;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service // Responsável por assinar e validar JWTs.
public class JwtService {
    private final SecretKey chave;
    private final long expiracaoMinutos;

    public JwtService(@Value("${app.jwt.secret-base64}") String segredoBase64,
                      @Value("${app.jwt.expiration-minutes:60}") long expiracaoMinutos) {
        // HS256 exige ao menos 256 bits (32 bytes) de chave aleatória.
        this.chave = Keys.hmacShaKeyFor(Decoders.BASE64.decode(segredoBase64));
        this.expiracaoMinutos = expiracaoMinutos;
    }

    public String gerar(Usuario usuario) {
        Instant agora = Instant.now();
        return Jwts.builder()
                .subject(usuario.getEmail()) // Identidade incluída no token.
                .issuedAt(Date.from(agora))
                .expiration(Date.from(agora.plusSeconds(expiracaoMinutos * 60)))
                .signWith(chave, Jwts.SIG.HS256) // Assinatura HMAC SHA-256.
                .compact();
    }

    public String extrairEmailValidado(String token) {
        // Rejeita assinaturas inválidas e tokens expirados; obtém o subject.
        Claims claims = Jwts.parser().verifyWith(chave).build()
                .parseSignedClaims(token).getPayload();
        return claims.getSubject();
    }
}
