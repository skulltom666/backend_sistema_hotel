package com.hotel.sistemahotelero.unitarias;

import com.hotel.sistemahotelero.huespedes.Huesped;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class HuespedTest {

    @Test
    @DisplayName("CP-U14:Los datos del huesped se guardan correctamente")
    void datosDelHuesped() {
        Huesped huesped = new Huesped();
        huesped.setId(10L);
        huesped.setDni("87654321");
        huesped.setNombres("Juan");
        huesped.setApellidos("Perez");
        huesped.setTelefono("987654321");
        huesped.setEmail("juan@correo.com");

        assertEquals(10L, huesped.getId());
        assertEquals("87654321", huesped.getDni());
        assertEquals("Juan", huesped.getNombres());
        assertEquals("Perez", huesped.getApellidos());
        assertEquals("987654321", huesped.getTelefono());
        assertEquals("juan@correo.com", huesped.getEmail());
        assertNull(huesped.getUsuario());
    }
}