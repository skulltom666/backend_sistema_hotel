package com.hotel.sistemahotelero.modules.auth.service;

import com.hotel.sistemahotelero.modules.auth.dto.*;
import com.hotel.sistemahotelero.modules.auth.repository.UserRepository;
import com.hotel.sistemahotelero.modules.hotel.repository.HotelRepository;
import com.hotel.sistemahotelero.security.jwt.JwtService;
import com.hotel.sistemahotelero.shared.enums.SubscriptionPlan;
import com.hotel.sistemahotelero.shared.exceptions.BusinessException;
import com.hotel.sistemahotelero.shared.persistence.Hotel;
import com.hotel.sistemahotelero.shared.persistence.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {


    private final UserRepository userRepository;
    private final HotelRepository hotelRepository;
    private final PasswordEncoder passwordEncoder; // ✅ Este bean ahora existe
    private final JwtService jwtService;
    private final RucValidationService rucValidationService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // RUC opcional: si no llega, se autogenera uno local para el tenant
        String ruc = request.getRuc();
        if (ruc == null || ruc.isBlank()) {
            ruc = generateRuc();
        }
        ruc = ruc.trim();

        if (userRepository.existsByRuc(ruc)) {
            throw new BusinessException("El RUC ya está registrado");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("El email ya está registrado");
        }

        // Obtener plan de suscripción
        SubscriptionPlan plan = request.getSubscriptionPlan();
        if (plan == null) {
            plan = SubscriptionPlan.BASICO;
        }

        // Crear usuario
        User user = new User();
        user.setTenantId(ruc);
        user.setRuc(ruc);
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNombre(request.getNombre());
        user.setApellido(request.getApellido());
        user.setTelefono(request.getTelefono());
        user.setRol(normalizeRol(request.getRol()));
        user.setSubscriptionPlan(plan);
        user.setMaxHotels(plan.getMaxHotels());
        user.setMaxFloors(plan.getMaxFloors());
        user.setMaxRooms(plan.getMaxRooms());
        user.setEnabled(true);

        // La validación SUNAT es un extra: si no responde, el registro continúa
        try {
            RucValidationResponse rucData = rucValidationService.validateRuc(ruc);
            user.setRazonSocial(rucData.getRazonSocial());
            user.setNombreComercial(rucData.getNombreComercial());
            user.setDireccion(rucData.getDireccion());
        } catch (Exception e) {
            log.warn("SUNAT no disponible para RUC {}: {}", ruc, e.getMessage());
        }

        User savedUser = userRepository.save(user);

        String token = jwtService.generateToken(savedUser.getEmail(), savedUser.getRuc(), savedUser.getRol());

        log.info("Usuario registrado con RUC: {}, Tenant ID: {}", savedUser.getRuc(), savedUser.getTenantId());

        return buildAuthResponse(savedUser, token, "Usuario registrado exitosamente");
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        log.info("Intento de login para email: {}", request.getEmail());

        // Buscar usuario por email (el RUC es opcional para el frontend RoomioHub)
        User user = userRepository.findByEmail(request.getEmail().trim())
                .orElseThrow(() -> new BusinessException("Credenciales inválidas"));

        // Si se envió RUC, validar que coincida
        if (request.getRuc() != null && !request.getRuc().isBlank()
                && !user.getRuc().equals(request.getRuc().trim())) {
            throw new BusinessException("Credenciales inválidas");
        }

        // Validar contraseña
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("Credenciales inválidas");
        }

        if (!user.isEnabled()) {
            throw new BusinessException("Usuario deshabilitado");
        }

        return buildAuthResponse(user, "Login exitoso");
    }

    @Transactional
    public AuthResponse loginByEmail(LoginByEmailRequest request) {
        log.info("Intento de login por email: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException("Usuario no encontrado"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("Contraseña incorrecta");
        }

        if (!user.isEnabled()) {
            throw new BusinessException("Usuario deshabilitado");
        }

        return buildAuthResponse(user, "Login exitoso");
    }

    public boolean existsByRuc(String ruc) {
        return userRepository.existsByRuc(ruc);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public User findByRuc(String ruc) {
        return userRepository.findByRuc(ruc)
                .orElseThrow(() -> new BusinessException("Usuario no encontrado"));
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("Usuario no encontrado"));
    }

    public RucValidationResponse validateRuc(String ruc) {
        return rucValidationService.validateRuc(ruc);
    }

    @Transactional
    public User updateSubscription(String ruc, SubscriptionPlan plan) {
        User user = findByRuc(ruc);
        user.setSubscriptionPlan(plan);
        user.setMaxHotels(plan.getMaxHotels());
        user.setMaxFloors(plan.getMaxFloors());
        user.setMaxRooms(plan.getMaxRooms());

        User updatedUser = userRepository.save(user);
        log.info("Plan actualizado para usuario {}: {}", ruc, plan);

        return updatedUser;
    }

    @Transactional
    public User updateUser(String ruc, String nombre, String apellido, String telefono, String direccion) {
        User user = findByRuc(ruc);

        if (nombre != null) user.setNombre(nombre);
        if (apellido != null) user.setApellido(apellido);
        if (telefono != null) user.setTelefono(telefono);
        if (direccion != null) user.setDireccion(direccion);

        return userRepository.save(user);
    }

    @Transactional
    public void disableUser(String ruc) {
        User user = findByRuc(ruc);
        user.setEnabled(false);
        userRepository.save(user);
        log.info("Usuario deshabilitado: {}", ruc);
    }

    @Transactional
    public void enableUser(String ruc) {
        User user = findByRuc(ruc);
        user.setEnabled(true);
        userRepository.save(user);
        log.info("Usuario habilitado: {}", ruc);
    }

    @Transactional
    public void changePassword(String ruc, String oldPassword, String newPassword) {
        User user = findByRuc(ruc);

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException("Contraseña actual incorrecta");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.info("Contraseña actualizada para usuario: {}", ruc);
    }

    public long countUsers() {
        return userRepository.count();
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<User> getUsersByPlan(SubscriptionPlan plan) {
        return userRepository.findBySubscriptionPlan(plan);
    }

    /**
     * Normaliza el rol del frontend al valor que espera el backend.
     */
    public static String normalizeRol(String rol) {
        if (rol == null || rol.isBlank()) {
            return "ADMINISTRADOR";
        }
        String upper = rol.toUpperCase();
        if (upper.contains("RECEPCION")) {
            return "RECEPCIONISTA";
        }
        return "ADMINISTRADOR";
    }

    /**
     * La authority que se guarda en el JWT para que funcione @PreAuthorize.
     */
    public static String rolToAuthority(String rol) {
        String normalized = normalizeRol(rol);
        return normalized.equals("RECEPCIONISTA") ? "RECEPCION" : "ADMIN";
    }

    private AuthResponse buildAuthResponse(User user, String message) {
        // El tenant del usuario actual es su RUC
        String token = jwtService.generateToken(
                org.springframework.security.core.userdetails.User
                        .builder()
                        .username(user.getEmail())
                        .password(user.getPassword())
                        .roles(rolToAuthority(user.getRol()))
                        .build(),
                user.getRuc()
        );
        return buildAuthResponse(user, token, message);
    }

    private AuthResponse buildAuthResponse(User user, String token, String message) {
        List<HotelDTO> hoteles = hotelRepository.findAllByTenant(user.getRuc()).stream()
                .map(h -> HotelDTO.of(h, user.getSubscriptionPlan()))
                .toList();

        return AuthResponse.builder()
                .token(token)
                .nombre(user.getNombre() != null ? user.getNombre()
                        : (user.getRazonSocial() != null ? user.getRazonSocial()
                        : user.getEmail().split("@")[0]))
                .rol(normalizeRol(user.getRol()))
                .hoteles(hoteles)
                .message(message)
                .success(true)
                .build();
    }

    /**
     * Genera un RUC local de 11 dígitos (para registros sin validación SUNAT).
     */
    private String generateRuc() {
        String ruc;
        int attempts = 0;
        do {
            ruc = String.format("20%09d", ThreadLocalRandom.current().nextInt(1_000_000_000));
            attempts++;
        } while (userRepository.existsByRuc(ruc) && attempts < 50);
        return ruc;
    }
}