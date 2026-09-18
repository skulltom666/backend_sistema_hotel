package com.hotel.sistemahotelero.unitarias;

import com.hotel.sistemahotelero.seguridad.JwtService;
import com.hotel.sistemahotelero.seguridad.Rol;
import com.hotel.sistemahotelero.seguridad.Usuario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private final JwtService jwtService = new JwtService();

    private Usuario usuario(String email) {
        Usuario usuario = new Usuario();
        usuario.setNombre("Ana");
        usuario.setEmail(email);
        usuario.setPassword("clave123");
        usuario.setRol(Rol.RECEPCIONISTA);
        return usuario;
    }

    @Test
    @DisplayName("CP-U10: Generar token y extraer el usuario")
    void generarTokenYExtraerUsuario() {
        Usuario usuario = usuario("ana@hotel.com");

        String token = jwtService.generarToken(usuario);

        assertNotNull(token);
        assertFalse(token.isBlank());
        assertEquals("ana@hotel.com", jwtService.extractUsername(token));
    }

    @Test
    @DisplayName("CP-U11: Token valido para el usuario correcto")
    void tokenValidoParaUsuarioCorrecto() {
        Usuario usuario = usuario("ana@hotel.com");
        String token = jwtService.generarToken(usuario);

        assertTrue(jwtService.isTokenValid(token, usuario));
    }

    @Test
    @DisplayName("CP-U12: Token invalido para otro usuario")
    void tokenInvalidoParaOtroUsuario() {
        Usuario emisor = usuario("ana@hotel.com");
        Usuario receptor = usuario("otro@hotel.com");
        String token = jwtService.generarToken(emisor);

        assertFalse(jwtService.isTokenValid(token, receptor));
    }

    @Test
    @DisplayName("CP-U13: Token mal formado lanza excepcion")
    void tokenMalFormadoLanzaExcepcion() {
        assertThrows(Exception.class, () -> jwtService.extractUsername("token.invalido"));
    }
}