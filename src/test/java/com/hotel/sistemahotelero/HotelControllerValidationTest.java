package com.hotel.sistemahotelero;

import com.hotel.sistemahotelero.modules.auth.dto.RucValidationResponse;
import com.hotel.sistemahotelero.modules.auth.repository.UserRepository;
import com.hotel.sistemahotelero.modules.auth.service.RucValidationService;
import com.hotel.sistemahotelero.modules.hotel.repository.HotelRepository;
import com.hotel.sistemahotelero.shared.persistence.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class HotelControllerValidationTest {

    private static final String TENANT = "20123456789";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HotelRepository hotelRepository;

    @MockBean
    private RucValidationService rucValidationService;

    @BeforeEach
    void setup() {
        userRepository.findByRuc(TENANT).orElseGet(() -> {
            User usuario = new User();
            usuario.setRuc(TENANT);
            usuario.setTenantId(TENANT);
            usuario.setEmail("admin@test.com");
            usuario.setPassword("$2a$10$7EqJtq98hPqEX7fNZaFWoOhiZ7UjcDzDl2S1PqGZ4W5T5Xq5YbM8u");
            usuario.setRol("ADMINISTRADOR");
            usuario.setNombre("Administrador de Prueba");
            return userRepository.save(usuario);
        });

        when(rucValidationService.validateRuc(anyString()))
                .thenReturn(springRucResponse(TENANT));
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = "ADMIN")
    void registrarHotel_NombreVacio_RetornaError400() throws Exception {
        String json = "{\"nombre\": \"\"}";

        mockMvc.perform(post("/api/hoteles/registrar")
                        .with(csrf())
                        .header("X-Tenant-ID", TENANT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = "ADMIN")
    void registrarHotel_NombreValido_Retorna201() throws Exception {
        String json = "{\"nombre\": \"Hotel Paraíso\", \"direccion\": \"Av. Lima 123\", \"telefono\": \"999999999\"}";

        mockMvc.perform(post("/api/hoteles/registrar")
                        .with(csrf())
                        .header("X-Tenant-ID", TENANT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Hotel Paraíso"))
                .andExpect(jsonPath("$.direccion").value("Av. Lima 123"));

        // Verificar que realmente se guardó en la BD con su tenant
        boolean existe = hotelRepository.findAllByTenant(TENANT).stream()
                .anyMatch(h -> h.getNombre().equals("Hotel Paraíso"));
        org.junit.jupiter.api.Assertions.assertTrue(existe, "El hotel debería quedar persistido");
    }

    private RucValidationResponse springRucResponse(String ruc) {
        RucValidationResponse response = new RucValidationResponse();
        response.setRuc(ruc);
        response.setRazonSocial("EMPRESA DE PRUEBA S.A.C.");
        response.setEstado("ACTIVO");
        response.setCondicion("HABIDO");
        return response;
    }
}