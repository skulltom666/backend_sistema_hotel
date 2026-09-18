package com.hotel.sistemahotelero.unit;

import com.hotel.sistemahotelero.modules.auth.dto.LoginRequest;
import com.hotel.sistemahotelero.modules.auth.dto.RegisterRequest;
import com.hotel.sistemahotelero.modules.auth.dto.RucValidationResponse;
import com.hotel.sistemahotelero.modules.auth.repository.UserRepository;
import com.hotel.sistemahotelero.modules.auth.service.AuthService;
import com.hotel.sistemahotelero.modules.auth.service.RucValidationService;
import com.hotel.sistemahotelero.modules.hotel.repository.HotelRepository;
import com.hotel.sistemahotelero.security.jwt.JwtService;
import com.hotel.sistemahotelero.shared.enums.SubscriptionPlan;
import com.hotel.sistemahotelero.shared.exceptions.BusinessException;
import com.hotel.sistemahotelero.shared.persistence.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias aisladas (JUnit 5 + Mockito) del modulo de autenticacion
 * de usuarios: registro y login.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Unitarias - AuthService (autenticacion de usuarios)")
class AuthServiceUnitTest {

    private static final String TENANT = "20123456789";

    @Mock
    private UserRepository userRepository;
    @Mock
    private HotelRepository hotelRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private RucValidationService rucValidationService;

    @InjectMocks
    private AuthService authService;

    // ===================== REGISTRO =====================

    @Test
    @DisplayName("CP-U09: registro crea usuario con plan BASICO por defecto y genera token")
    void register_creaUsuarioConPlanBasicoYToken() {
        RegisterRequest request = new RegisterRequest();
        request.setRuc(TENANT);
        request.setEmail("nuevo@test.com");
        request.setPassword("secreto123");
        request.setNombre("Nuevo");
        request.setRol("ADMINISTRADOR");

        when(userRepository.existsByRuc(TENANT)).thenReturn(false);
        when(userRepository.existsByEmail("nuevo@test.com")).thenReturn(false);
        when(passwordEncoder.encode("secreto123")).thenReturn("hash");
        when(rucValidationService.validateRuc(TENANT)).thenReturn(new RucValidationResponse());
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(hotelRepository.findAllByTenant(TENANT)).thenReturn(List.of());
        when(jwtService.generateToken(eq("nuevo@test.com"), eq(TENANT), anyString())).thenReturn("jwt-token");

        var response = authService.register(request);

        assertEquals("jwt-token", response.getToken());
        assertEquals("ADMINISTRADOR", response.getRol());
        assertTrue(response.isSuccess());
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("CP-U10: registro con RUC duplicado lanza BusinessException")
    void register_rucDuplicado_lanzaBusinessException() {
        RegisterRequest request = new RegisterRequest();
        request.setRuc(TENANT);
        request.setEmail("nuevo@test.com");
        request.setPassword("secreto123");

        when(userRepository.existsByRuc(TENANT)).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class, () -> authService.register(request));
        assertTrue(ex.getMessage().contains("RUC ya"));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("CP-U11: registro con email duplicado lanza BusinessException")
    void register_emailDuplicado_lanzaBusinessException() {
        RegisterRequest request = new RegisterRequest();
        request.setRuc(TENANT);
        request.setEmail("duplicado@test.com");
        request.setPassword("secreto123");

        when(userRepository.existsByRuc(TENANT)).thenReturn(false);
        when(userRepository.existsByEmail("duplicado@test.com")).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class, () -> authService.register(request));
        assertTrue(ex.getMessage().contains("email ya"));
        verify(userRepository, never()).save(any(User.class));
    }

    // ===================== LOGIN =====================

    @Test
    @DisplayName("CP-U12: login con credenciales validas retorna token")
    void login_credencialesValidas_retornaToken() {
        User user = usuarioHabilitado("admin@test.com", "hash");
        LoginRequest request = new LoginRequest();
        request.setEmail("admin@test.com");
        request.setPassword("secreto123");

        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secreto123", "hash")).thenReturn(true);
        when(hotelRepository.findAllByTenant(TENANT)).thenReturn(List.of());
        when(jwtService.generateToken(any(UserDetails.class), eq(TENANT))).thenReturn("jwt-login");

        var response = authService.login(request);

        assertEquals("jwt-login", response.getToken());
        assertTrue(response.isSuccess());
    }

    @Test
    @DisplayName("CP-U13: login con contrasena incorrecta lanza BusinessException")
    void login_passwordIncorrecto_lanzaBusinessException() {
        User user = usuarioHabilitado("admin@test.com", "hash");
        LoginRequest request = new LoginRequest();
        request.setEmail("admin@test.com");
        request.setPassword("mala");

        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("mala", "hash")).thenReturn(false);

        assertThrows(BusinessException.class, () -> authService.login(request));
        verify(jwtService, never()).generateToken(any(UserDetails.class), anyString());
    }

    @Test
    @DisplayName("CP-U14: login con email no registrado lanza BusinessException")
    void login_emailNoRegistrado_lanzaBusinessException() {
        LoginRequest request = new LoginRequest();
        request.setEmail("nadie@test.com");
        request.setPassword("secreto123");

        when(userRepository.findByEmail("nadie@test.com")).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> authService.login(request));
    }

//    @Test
//    @DisplayName("CP-U19: login de usuario deshabilitado lanza BusinessException")
//    void login_usuarioDeshabilitado_lanzaBusinessException() {
//        User user = usuarioHabilitado("admin@test.com", "hash");
//        user.setEnabled(false);
//        LoginRequest request = new LoginRequest();
//        request.setEmail("admin@test.com");
//        request.setPassword("secreto123");
//
//        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(user));
//        when(passwordEncoder.matches("secreto123", "hash")).thenReturn(true);
//
//        BusinessException ex = assertThrows(BusinessException.class, () -> authService.login(request));
//        assertTrue(ex.getMessage().toLowerCase().contains("deshabilitado"));
//    }
//
//    @Test
//    @DisplayName("CP-U20: normalizar rol de recepcion retorna RECEPCIONISTA")
//    void normalizeRol_recepcion_retornaRecepcionista() {
//        assertEquals("RECEPCIONISTA", AuthService.normalizeRol("Recepcion"));
//        assertEquals("ADMINISTRADOR", AuthService.normalizeRol(null));
//        assertEquals("ADMINISTRADOR", AuthService.normalizeRol("Administrador"));
//        assertEquals("RECEPCION", AuthService.rolToAuthority("RECEPCIONISTA"));
//        assertEquals("ADMIN", AuthService.rolToAuthority("ADMINISTRADOR"));
//    }

    private User usuarioHabilitado(String email, String passwordHash) {
        User user = new User();
        user.setId(1L);
        user.setRuc(TENANT);
        user.setTenantId(TENANT);
        user.setEmail(email);
        user.setPassword(passwordHash);
        user.setNombre("Admin");
        user.setRol("ADMINISTRADOR");
        user.setSubscriptionPlan(SubscriptionPlan.BASICO);
        user.setEnabled(true);
        return user;
    }
}
