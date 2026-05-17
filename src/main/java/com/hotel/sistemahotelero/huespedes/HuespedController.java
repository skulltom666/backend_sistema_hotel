package com.hotel.sistemahotelero.huespedes;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/huespedes")
@CrossOrigin(origins = "http://localhost:4200")
public class HuespedController {

    @Autowired
    private HuespedRepository huespedRepository;

    // Listar todos los huéspedes
    @GetMapping
    public List<Huesped> listarHuespedes() {
        return huespedRepository.findAll();
    }

    // 🔍 BUSCAR POR DNI (Esta es la que usa tu autollenado en Angular)
    @GetMapping("/buscar/{dni}")
    public ResponseEntity<Huesped> buscarPorDni(@PathVariable String dni) {
        return huespedRepository.findByDni(dni) // Usamos el nuevo método del Repository
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Guardar nuevo huésped
    @PostMapping
    public Huesped guardarHuesped(@RequestBody Huesped huesped) {
        return huespedRepository.save(huesped);
    }

    // Actualizar huésped
    @PutMapping("/{dni}")
    public ResponseEntity<Huesped> actualizarHuesped(@PathVariable String dni, @RequestBody Huesped detallesHuesped) {
        return huespedRepository.findByDni(dni)
                .map(huesped -> {
                    huesped.setNombres(detallesHuesped.getNombres());
                    huesped.setApellidos(detallesHuesped.getApellidos());
                    huesped.setTelefono(detallesHuesped.getTelefono());
                    // 🚨 Aquí usamos el nuevo nombre 'dni'
                    huesped.setDni(detallesHuesped.getDni());
                    return ResponseEntity.ok(huespedRepository.save(huesped));
                })
                .orElse(ResponseEntity.notFound().build());
    }
}