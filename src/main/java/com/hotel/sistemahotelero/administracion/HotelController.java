package com.hotel.sistemahotelero.administracion;

import com.hotel.sistemahotelero.seguridad.Usuario;
import com.hotel.sistemahotelero.seguridad.UsuarioRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hoteles")
@CrossOrigin(origins = "http://localhost:4200")
public class HotelController {

    private final HotelRepository hotelRepository;
    private final UsuarioRepository usuarioRepository;

    public HotelController(HotelRepository hotelRepository, UsuarioRepository usuarioRepository) {
        this.hotelRepository = hotelRepository;
        this.usuarioRepository = usuarioRepository;
    }

    // 1. Buscar los hoteles del usuario logueado
    @GetMapping("/mio")
    public ResponseEntity<List<Hotel>> obtenerMisHoteles() {
        // Extraemos el email del Token JWT automáticamente
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        List<Hotel> hoteles = hotelRepository.buscarMisHotelesDefinitivo(usuario);
        return ResponseEntity.ok(hoteles);
    }

    // 2. Registrar el hotel vinculado al usuario del Token
    @PostMapping("/registrar")
    public ResponseEntity<Hotel> registrarHotel(@RequestBody Hotel hotel) {
        // 🚨 IMPORTANTE: SecurityContextHolder solo funciona si Angular envía el TOKEN
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        if (email == null || email.equals("anonymousUser")) {
            return ResponseEntity.status(403).build(); // Si no hay token, fuera.
        }

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        System.out.println("🏨 Registrando hotel: " + hotel.getNombre() + " para: " + email);

        // Vinculamos el objeto Usuario real (el administrador) al hotel
        hotel.setAdministrador(usuario);
        hotel.setSuscripcionActiva(true);

        // Guardamos en la BD
        Hotel nuevoHotel = hotelRepository.save(hotel);

        return ResponseEntity.ok(nuevoHotel);
    }
}