package com.hotel.sistemahotelero.modules.auth.controller;

import com.hotel.sistemahotelero.modules.auth.dto.*;
import com.hotel.sistemahotelero.modules.auth.service.AuthService;
import com.hotel.sistemahotelero.shared.dto.ApiResponse;
import com.hotel.sistemahotelero.shared.enums.SubscriptionPlan;
import com.hotel.sistemahotelero.shared.exceptions.BusinessException;
import com.hotel.sistemahotelero.shared.persistence.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    /**
     * Registra un nuevo usuario (contrato frontend: /api/auth/registro)
     */
    @PostMapping({"/registro", "/register"})
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        try {
            AuthResponse response = authService.register(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Error en registro: {}", e.getMessage());
            throw new BusinessException(e.getMessage());
        }
    }

    /**
     * Autentica un usuario (contrato frontend: email + password, RUC opcional)
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error en login: {}", e.getMessage());
            throw new BusinessException(e.getMessage());
        }
    }

    @PostMapping("/login-by-email")
    public ResponseEntity<AuthResponse> loginByEmail(@Valid @RequestBody LoginByEmailRequest request) {
        try {
            AuthResponse response = authService.loginByEmail(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error en login: {}", e.getMessage());
            throw new BusinessException(e.getMessage());
        }
    }

    /**
     * Valida un RUC con SUNAT
     */
    @GetMapping("/validate-ruc/{ruc}")
    public ResponseEntity<ApiResponse<RucValidationResponse>> validateRuc(@PathVariable String ruc) {
        try {
            RucValidationResponse response = authService.validateRuc(ruc);
            return ResponseEntity.ok(ApiResponse.success("RUC validado correctamente", response));
        } catch (Exception e) {
            log.error("Error validando RUC: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        }
    }

    /**
     * Verifica si existe un usuario por RUC
     */
    @GetMapping("/check/{ruc}")
    public ResponseEntity<ApiResponse<Boolean>> checkUserExists(@PathVariable String ruc) {
        try {
            boolean exists = authService.existsByRuc(ruc);
            return ResponseEntity.ok(ApiResponse.success("Verificación completada", exists));
        } catch (Exception e) {
            log.error("Error verificando usuario: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * Obtiene un usuario por RUC
     */
    @GetMapping("/user/{ruc}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<User>> getUserByRuc(@PathVariable String ruc) {
        try {
            User user = authService.findByRuc(ruc);
            return ResponseEntity.ok(ApiResponse.success("Usuario obtenido exitosamente", user));
        } catch (Exception e) {
            log.error("Error obteniendo usuario: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage(), HttpStatus.NOT_FOUND.value()));
        }
    }

    /**
     * Actualiza el plan de suscripción de un usuario
     */
    @PutMapping("/subscription/{ruc}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<User>> updateSubscription(
            @PathVariable String ruc,
            @RequestParam SubscriptionPlan plan) {
        try {
            User user = authService.updateSubscription(ruc, plan);
            return ResponseEntity.ok(ApiResponse.success("Plan actualizado exitosamente", user));
        } catch (Exception e) {
            log.error("Error actualizando plan: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        }
    }

    /**
     * Actualiza los datos de un usuario
     */
    @PutMapping("/user/{ruc}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<User>> updateUser(
            @PathVariable String ruc,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String apellido,
            @RequestParam(required = false) String telefono,
            @RequestParam(required = false) String direccion) {
        try {
            User user = authService.updateUser(ruc, nombre, apellido, telefono, direccion);
            return ResponseEntity.ok(ApiResponse.success("Usuario actualizado exitosamente", user));
        } catch (Exception e) {
            log.error("Error actualizando usuario: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        }
    }

    /**
     * Deshabilita un usuario
     */
    @PostMapping("/{ruc}/disable")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> disableUser(@PathVariable String ruc) {
        try {
            authService.disableUser(ruc);
            return ResponseEntity.ok(ApiResponse.success("Usuario deshabilitado exitosamente", null));
        } catch (Exception e) {
            log.error("Error deshabilitando usuario: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        }
    }

    /**
     * Habilita un usuario
     */
    @PostMapping("/{ruc}/enable")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> enableUser(@PathVariable String ruc) {
        try {
            authService.enableUser(ruc);
            return ResponseEntity.ok(ApiResponse.success("Usuario habilitado exitosamente", null));
        } catch (Exception e) {
            log.error("Error habilitando usuario: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        }
    }

    /**
     * Cambia la contraseña de un usuario
     */
    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @RequestParam String ruc,
            @RequestParam String oldPassword,
            @RequestParam String newPassword) {
        try {
            authService.changePassword(ruc, oldPassword, newPassword);
            return ResponseEntity.ok(ApiResponse.success("Contraseña actualizada exitosamente", null));
        } catch (Exception e) {
            log.error("Error cambiando contraseña: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        }
    }

    /**
     * Obtiene todos los usuarios (solo ADMIN)
     */
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<User>>> getAllUsers() {
        try {
            List<User> users = authService.getAllUsers();
            return ResponseEntity.ok(ApiResponse.success("Usuarios obtenidos exitosamente", users));
        } catch (Exception e) {
            log.error("Error obteniendo usuarios: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * Obtiene usuarios por plan de suscripción
     */
    @GetMapping("/users/plan/{plan}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<User>>> getUsersByPlan(@PathVariable SubscriptionPlan plan) {
        try {
            List<User> users = authService.getUsersByPlan(plan);
            return ResponseEntity.ok(ApiResponse.success("Usuarios por plan obtenidos exitosamente", users));
        } catch (Exception e) {
            log.error("Error obteniendo usuarios por plan: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * Obtiene el conteo de usuarios
     */
    @GetMapping("/count")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Long>> countUsers() {
        try {
            long count = authService.countUsers();
            return ResponseEntity.ok(ApiResponse.success("Conteo de usuarios obtenido exitosamente", count));
        } catch (Exception e) {
            log.error("Error obteniendo conteo de usuarios: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }
}