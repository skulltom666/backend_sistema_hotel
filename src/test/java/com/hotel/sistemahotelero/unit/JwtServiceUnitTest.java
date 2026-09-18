package com.hotel.sistemahotelero.unit;

import com.hotel.sistemahotelero.security.jwt.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas unitarias aisladas del componente de generacion y validacion de JWT.
 */
@DisplayName("Unitarias - JwtService (token de autenticacion)")
class JwtServiceUnitTest {

    private static final String SECRET = "roomiohubSecretKey2024With256BitsLength!roomiohub";
    private static final String TENANT = "20123456789";

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", SECRET);
        ReflectionTestUtils.setField(jwtService, "expiration", 86_400_000L);
    }

//    @Test
//    @DisplayName("CP-U21: genera token y extrae el subject (email)")
//    void generaToken_yExtraeSubject() {
//        UserDetails user = User.withUsername("user@test.com").password("x").roles("ADMIN").build();
//
//        String token = jwtService.generateToken(user, TENANT);
//
//        assertNotNull(token);
//        assertEquals("user@test.com", jwtService.extractUsername(token));
//    }

//    @Test
//    @DisplayName("CP-U22: extrae el tenantId del token")
//    void extraeTenantId_delToken() {
//        UserDetails user = User.withUsername("user@test.com").password("x").roles("ADMIN").build();
//
//        String token = jwtService.generateToken(user, TENANT);
//
//        assertEquals(TENANT, jwtService.extractTenantId(token));
//    }

    @Test
    @DisplayName("CP-U15: token valido para el mismo usuario e invalido para otro")
    void validaToken_segunUsuario() {
        UserDetails user = User.withUsername("user@test.com").password("x").roles("ADMIN").build();
        UserDetails otro = User.withUsername("otro@test.com").password("x").roles("ADMIN").build();

        String token = jwtService.generateToken(user, TENANT);

        assertTrue(jwtService.isTokenValid(token, user));
        assertFalse(jwtService.isTokenValid(token, otro));
    }

//    @Test
//    @DisplayName("CP-U24: extrae authorities y agrega el prefijo ROLE_")
//    void extraeAuthorities_conPrefijoRole() {
//        String token = jwtService.generateToken("recepcion@test.com", TENANT, "RECEPCION");
//
//        List<GrantedAuthority> authorities = jwtService.extractAuthorities(token);
//
//        assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_RECEPCION")));
//    }
}
