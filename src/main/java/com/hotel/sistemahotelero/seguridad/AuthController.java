package com.hotel.sistemahotelero.seguridad;

import com.hotel.sistemahotelero.administracion.Hotel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(AuthenticationManager authenticationManager, JwtService jwtService,
                          UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        String token = jwtService.generarToken(usuario);

        // 1. Convertimos la lista de entidades Hotel a una lista de DTOs simples
        List<HotelDTO> hotelesPermitidos = usuario.getHoteles().stream()
                .map(hotel -> new HotelDTO(hotel.getId(), hotel.getNombre()))
                .collect(Collectors.toList());

        // 2. Devolvemos la respuesta con la LISTA de hoteles
        return ResponseEntity.ok(new AuthResponse(
                token,
                usuario.getNombre(),
                usuario.getRol().name(),
                hotelesPermitidos
        ));
    }

    @PostMapping("/registro")
    public ResponseEntity<AuthResponse> registrar(@RequestBody Usuario request) {
        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setNombre(request.getNombre());
        nuevoUsuario.setEmail(request.getEmail());
        nuevoUsuario.setDni(request.getDni());
        nuevoUsuario.setPassword(passwordEncoder.encode(request.getPassword()));
        nuevoUsuario.setRol(request.getRol());

        // Si en el registro se envían hoteles, los asignamos
        if (request.getHoteles() != null) {
            nuevoUsuario.setHoteles(request.getHoteles());
        }

        usuarioRepository.save(nuevoUsuario);

        String token = jwtService.generarToken(nuevoUsuario);

        List<HotelDTO> hotelesPermitidos = nuevoUsuario.getHoteles() != null ?
                nuevoUsuario.getHoteles().stream()
                .map(h -> new HotelDTO(h.getId(), h.getNombre()))
                .collect(Collectors.toList()) : List.of();

        return ResponseEntity.ok(new AuthResponse(
                token,
                nuevoUsuario.getNombre(),
                nuevoUsuario.getRol().name(),
                hotelesPermitidos
        ));
    }
}