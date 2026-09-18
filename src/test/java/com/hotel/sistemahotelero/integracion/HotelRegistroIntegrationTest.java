package com.hotel.sistemahotelero.integracion;

import com.hotel.sistemahotelero.modules.auth.repository.UserRepository;
import com.hotel.sistemahotelero.modules.hotel.repository.HotelRepository;
import com.hotel.sistemahotelero.shared.persistence.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class HotelRegistroIntegrationTest {

    private static final String TENANT = "20123456789";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setup() {
        userRepository.findByRuc(TENANT).orElseGet(() -> {
            User usuario = new User();
            usuario.setRuc(TENANT);
            usuario.setTenantId(TENANT);
            usuario.setEmail("admin@test.com");
            usuario.setPassword("password");
            usuario.setRol("ADMINISTRADOR");
            usuario.setNombre("Administrador");
            return userRepository.save(usuario);
        });
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = "ADMIN")
    void registrarHotel_PersistenciaReal_Test() throws Exception {
        // 1. Arrange: Definir los datos
        String jsonHotel = "{\"nombre\": \"Hotel Gran Lujo\", \"direccion\": \"Av. Central 456\"}";

        // 2. Act: Realizar la petición
        mockMvc.perform(post("/api/hoteles/registrar")
                        .with(csrf())
                        .header("X-Tenant-ID", TENANT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonHotel))
                .andExpect(status().isCreated());

        // 3. Assert: Verificar que el hotel realmente se guardó en la BD física
        boolean existe = hotelRepository.findAllByTenant(TENANT).stream()
                .anyMatch(h -> h.getNombre().equals("Hotel Gran Lujo"));

        assertTrue(existe, "El hotel debería estar persistido en la base de datos");
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = "ADMIN")
    void testInteroperabilidad_FormatoJSON() throws Exception {
        // Crear un hotel primero para tener un id real
        String jsonHotel = "{\"nombre\": \"Hotel Central\"}";

        mockMvc.perform(post("/api/hoteles/registrar")
                        .with(csrf())
                        .header("X-Tenant-ID", TENANT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonHotel))
                .andExpect(status().isCreated());

        Long id = hotelRepository.findAllByTenant(TENANT).stream()
                .filter(h -> h.getNombre().equals("Hotel Central"))
                .findFirst()
                .orElseThrow()
                .getId();

        // El contrato JSON expone los datos dentro de "data" (ApiResponse)
        mockMvc.perform(get("/api/hotels/{id}", id)
                        .with(csrf())
                        .header("X-Tenant-ID", TENANT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nombre").value("Hotel Central"))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.success").value(true));
    }
}