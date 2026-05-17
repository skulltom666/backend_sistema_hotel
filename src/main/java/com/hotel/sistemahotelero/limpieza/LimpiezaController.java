package com.hotel.sistemahotelero.limpieza;

import com.hotel.sistemahotelero.configuracion.Habitacion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/habitaciones") // Alineado con tu RoomService de Angular
public class LimpiezaController {

    @Autowired
    private LimpiezaService limpiezaService;

    // 1. Iniciar Limpieza
    // Ruta en Angular: /api/habitaciones/{id}/limpieza-inicio
    @PostMapping("/{habitacionId}/limpieza-inicio")
    public Habitacion iniciarLimpieza(@PathVariable Long habitacionId) { // Cambiado a Long
        return limpiezaService.iniciarLimpieza(habitacionId);
    }

    // 2. Finalizar Limpieza
    // Ruta en Angular: /api/habitaciones/{id}/limpieza-fin
    @PostMapping("/{habitacionId}/limpieza-fin")
    public Habitacion finalizarLimpieza(@PathVariable Long habitacionId) { // Cambiado a Long
        return limpiezaService.finalizarLimpieza(habitacionId);
    }
}