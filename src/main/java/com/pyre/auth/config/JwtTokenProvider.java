package com.pyre.auth.config;

import com.pyre.auth.enumeration.UserRoleEnum;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.*;
import org.springframework.util.StringUtils;


import java.security.Key;
import java.util.Date;
import java.util.UUID;


@Component
public class JwtTokenProvider {

    private final UserDetailsService userDetailsService;
    private final static String ROLE = "role";
    private final static String ID = "id";

    private final Key secretKey;

    private final long tokenTime;

    private final long refreshTime;

    public JwtTokenProvider(
            @Value("${jwt.secret.key}")
            String key,
            @Value("${jwt.time.access}")
            long tokenTime,
            @Value("${jwt.time.refresh}")
            long refreshTime,
            UserDetailsService userDetailsService
    ) {
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(key));
        this.tokenTime = tokenTime;
        this.refreshTime = refreshTime;
        this.userDetailsService = userDetailsService;
    }

    public String createToken(String email, UserRoleEnum role, UUID id) {
        Claims claims = Jwts.claims().setSubject(email);
        claims.put(ROLE,role.getKey());
        claims.put(ID, id.toString());
        Date now =new Date();
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + tokenTime))
                .signWith(SignatureAlgorithm.HS256,secretKey)
                .compact();
    }

    public String CreateRefreshToken(String email) {
        Claims claims = Jwts.claims().setSubject(email);
        Date now =new Date();
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + refreshTime))
                .signWith(SignatureAlgorithm.HS256,secretKey)
                .compact();
    }

    public String getEmail(String token){
        return Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody().getSubject();
    }

    public Authentication getAuthentication(String token){
        UserDetails userDetails = userDetailsService.loadUserByUsername(this.getEmail(token));
        return new UsernamePasswordAuthenticationToken(userDetails,"",userDetails.getAuthorities());
    }
    public UserRoleEnum getRole(String token) {
        return UserRoleEnum.valueOf(Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody().get("role", String.class));
    }
    public UUID getId(String token) {
        return UUID.fromString(Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody().get("id", String.class));
    }

    public String resolveToken(HttpServletRequest req){
        String token = req.getHeader("AUTHORIZATION");
        if (StringUtils.hasText(token) && token.startsWith("Bearer ")) {
            return token.substring(7);
        } else if (StringUtils.hasText(token)) {
            return token;
        }
        return null;
    }

    public boolean validateToken(String jwtToken, HttpServletRequest req) {
        try {
            if (jwtToken.isEmpty()) throw new JwtException("empty jwtToken");
            Jws<Claims> claimsJws = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(jwtToken);
            return !claimsJws.getBody().getExpiration().before(new Date());
        } catch (JwtException e) {
            if (jwtToken.isEmpty()) {
                req.setAttribute("exception", "토큰이 비어 있습니다.");
            }
            else req.setAttribute("exception", "토큰이 유효하지 않습니다.");
            return false;
        }
    }
}