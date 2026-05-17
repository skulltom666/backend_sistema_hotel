package com.hotel.sistemahotelero.seguridad;

import com.hotel.sistemahotelero.huespedes.Huesped;
import com.hotel.sistemahotelero.huespedes.HuespedRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/perfil")
public class PerfilController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private HuespedRepository huespedRepository;

    // 🚨 NUEVO MÉTODO: Permite cargar los datos al iniciar la pestaña
    @GetMapping("/actual")
    public ResponseEntity<?> obtenerPerfilActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario usuario = usuarioRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Map<String, Object> response = new HashMap<>();
        response.put("nombreUsuario", usuario.getNombre());
        response.put("email", usuario.getEmail());
        response.put("dni", usuario.getDni()); // 👈 Esto es lo que Angular usará para el auto-completado

        return ResponseEntity.ok(response);
    }

    @PostMapping("/vincular")
    public ResponseEntity<?> vincularIdentidad(@RequestBody Map<String, String> datos) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario usuarioActual = usuarioRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Sesión no válida"));

        String dniCandidato = datos.get("dni");

        // 1. Evitar duplicados (Doble identidad)
        Optional<Usuario> duplicado = usuarioRepository.findAll().stream()
                .filter(u -> dniCandidato.equals(u.getDni()) && !u.getId().equals(usuarioActual.getId()))
                .findFirst();

        if (duplicado.isPresent()) {
            return ResponseEntity.badRequest().body("Este DNI ya está vinculado a otra cuenta de usuario.");
        }

        // 2. Vincular DNI a la cuenta de Usuario
        usuarioActual.setDni(dniCandidato);
        usuarioRepository.save(usuarioActual);

        // 3. Sincronizar/Crear el registro en la tabla Huesped
        Huesped huesped = huespedRepository.findByDni(dniCandidato).orElse(new Huesped());
        huesped.setDni(dniCandidato);
        huesped.setNombres(datos.get("nombre"));
        huesped.setApellidos(datos.get("apellido"));
        huesped.setTelefono(datos.get("telefono"));
        huesped.setEmail(usuarioActual.getEmail());
        huesped.setUsuario(usuarioActual);

        huespedRepository.save(huesped);

        return ResponseEntity.ok("Perfil vinculado exitosamente.");
    }
}