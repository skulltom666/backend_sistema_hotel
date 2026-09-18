package com.hotel.sistemahotelero.integracion;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotel.sistemahotelero.modules.auth.dto.RucValidationResponse;
import com.hotel.sistemahotelero.modules.auth.repository.UserRepository;
import com.hotel.sistemahotelero.modules.auth.service.RucValidationService;
import com.hotel.sistemahotelero.modules.hotel.repository.HotelRepository;
import com.hotel.sistemahotelero.shared.enums.SubscriptionPlan;
import com.hotel.sistemahotelero.shared.persistence.Hotel;
import com.hotel.sistemahotelero.shared.persistence.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas de integracion de seguridad y autenticacion basada en JWT
 * (Spring Boot Test + MockMvc). Valida el flujo registro/login/token y el
 * acceso a endpoints protegidos.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Integracion - Seguridad y autenticacion JWT")
class JwtSecurityIntegrationTest {

    private static final String TENANT = "20000000001";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private RucValidationService rucValidationService;

    @Test
    @DisplayName("CP-I06: registro via API retorna token y respuesta exitosa")
    void registro_retornaTokenYRespuestaExitosa() throws Exception {
        when(rucValidationService.validateRuc(anyString())).thenReturn(rucResponse());

        String body = "{\"ruc\":\"" + TENANT + "\",\"email\":\"seg1@test.com\",\"password\":\"123456\","
                + "\"nombre\":\"Seg\",\"rol\":\"ADMINISTRADOR\"}";

        mockMvc.perform(post("/api/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.rol").value("ADMINISTRADOR"));
    }

    @Test
    @DisplayName("CP-I07: login entrega token JWT que autoriza un endpoint protegido (200)")
    void login_tokenAccedeEndpointProtegido() throws Exception {
        seedUser("20900000001", "seg2@test.com", "ADMINISTRADOR");

        String token = login("seg2@test.com", "123456");

        mockMvc.perform(get("/api/hotels")
                        .header("Authorization", "Bearer " + token)
                        .header("X-Tenant-ID", "20900000001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("CP-I08: endpoint protegido sin token es rechazado (401/403)")
    void endpointProtegido_sinToken_esRechazado() throws Exception {
        mockMvc.perform(get("/api/hotels"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("CP-I09: token malformado es rechazado (401/403)")
    void tokenMalformado_esRechazado() throws Exception {
        mockMvc.perform(get("/api/hotels")
                        .header("Authorization", "Bearer token.invalido.falso"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("CP-I10: token de RECEPCIONISTA no accede a endpoint solo ADMIN (403)")
    void tokenRecepcionista_noAccedeEndpointAdmin() throws Exception {
        seedUser("20900000002", "recepcion@test.com", "RECEPCIONISTA");

        String token = login("recepcion@test.com", "123456");

        mockMvc.perform(get("/api/auth/users")
                        .header("Authorization", "Bearer " + token)
                        .header("X-Tenant-ID", "20900000002"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("CP-I11: token emitido en registro autoriza endpoint protegido (mapeo de roles)")
    void tokenDeRegistro_autorizaEndpointProtegido() throws Exception {
        when(rucValidationService.validateRuc(anyString())).thenReturn(rucResponse());

        String body = "{\"ruc\":\"20900000003\",\"email\":\"seg3@test.com\",\"password\":\"123456\","
                + "\"nombre\":\"Seg\",\"rol\":\"ADMINISTRADOR\"}";

        String response = mockMvc.perform(post("/api/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String token = objectMapper.readTree(response).get("token").asText();

        mockMvc.perform(get("/api/hotels")
                        .header("Authorization", "Bearer " + token)
                        .header("X-Tenant-ID", "20900000003"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("CP-I12: separacion de datos por tenant (cada usuario solo ve sus hoteles)")
    void separacionDeDatos_porTenant() throws Exception {
        seedUser("20900000004", "tenantA@test.com", "ADMINISTRADOR");
        User userB = seedUser("20900000005", "tenantB@test.com", "ADMINISTRADOR");

        User userA = userRepository.findByRuc("20900000004").orElseThrow();
        Hotel hotelA = new Hotel();
        hotelA.setTenantId("20900000004");
        hotelA.setNombre("Hotel A");
        hotelA.setUser(userA);
        hotelRepository.save(hotelA);

        String tokenA = login("tenantA@test.com", "123456");
        String tokenB = login("tenantB@test.com", "123456");

        mockMvc.perform(get("/api/hotels")
                        .header("Authorization", "Bearer " + tokenA)
                        .header("X-Tenant-ID", "20900000004"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));

        mockMvc.perform(get("/api/hotels")
                        .header("Authorization", "Bearer " + tokenB)
                        .header("X-Tenant-ID", "20900000005"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));

        org.junit.jupiter.api.Assertions.assertNotNull(userB);
    }

    private User seedUser(String ruc, String email, String rol) {
        User user = new User();
        user.setRuc(ruc);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode("123456"));
        user.setNombre("Usuario " + rol);
        user.setRol(rol);
        user.setSubscriptionPlan(SubscriptionPlan.EMPRESARIAL);
        user.setEnabled(true);
        return userRepository.save(user);
    }

    private String login(String email, String password) throws Exception {
        String body = "{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}";
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("token").asText();
    }

    private RucValidationResponse rucResponse() {
        RucValidationResponse response = new RucValidationResponse();
        response.setRazonSocial("EMPRESA DE PRUEBA S.A.C.");
        response.setNombreComercial("EMPRESA DE PRUEBA");
        response.setEstado("ACTIVO");
        response.setCondicion("HABIDO");
        return response;
    }
}

