package com.hotel.sistemahotelero.unitarias;

import com.hotel.sistemahotelero.seguridad.Rol;
import com.hotel.sistemahotelero.seguridad.Usuario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import static org.junit.jupiter.api.Assertions.*;

class UsuarioTest {

    private Usuario usuarioConRol(Rol rol) {
        Usuario usuario = new Usuario();
        usuario.setNombre("Ana");
        usuario.setEmail("ana@hotel.com");
        usuario.setDni("12345678");
        usuario.setPassword("clave123");
        usuario.setRol(rol);
        return usuario;
    }

    @Test
    @DisplayName("CP-U07: Los datos basicos del usuario se guardan correctamente")
    void datosBasicosDelUsuario() {
        Usuario usuario = usuarioConRol(Rol.ADMINISTRADOR);
        usuario.setId(1L);

        assertEquals(1L, usuario.getId());
        assertEquals("Ana", usuario.getNombre());
        assertEquals("ana@hotel.com", usuario.getEmail());
        assertEquals("12345678", usuario.getDni());
        assertEquals("clave123", usuario.getPassword());
        assertEquals(Rol.ADMINISTRADOR, usuario.getRol());
    }


}