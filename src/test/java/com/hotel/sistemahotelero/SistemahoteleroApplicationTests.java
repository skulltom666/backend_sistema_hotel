package com.hotel.sistemahotelero;

import com.hotel.sistemahotelero.modules.auth.dto.RucValidationResponse;
import com.hotel.sistemahotelero.modules.auth.service.RucValidationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SistemahoteleroApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RucValidationService rucValidationService;

    @Test
    void contextLoads() {
        // Verifica que el ApplicationContext arranque correctamente
    }

    @Test
    void registrarYLoguearUsuario_FluJoCompleto() throws Exception {
        when(rucValidationService.validateRuc(anyString()))
                .thenReturn(springRucResponse("20123456789"));

        String registerJson = "{"
                + "\"email\":\"test7@hotel.com\","
                + "\"password\":\"123456\","
                + "\"nombre\":\"Usuario\","
                + "\"apellido\":\"Prueba\","
                + "\"rol\":\"ADMINISTRADOR\""
                + "}";

        mockMvc.perform(post("/api/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.rol").value("ADMINISTRADOR"));

        String loginJson = "{"
                + "\"email\":\"test7@hotel.com\","
                + "\"password\":\"123456\""
                + "}";

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.success").value(true));
    }

    private RucValidationResponse springRucResponse(String ruc) {
        RucValidationResponse response = new RucValidationResponse();
        response.setRuc(ruc);
        response.setRazonSocial("EMPRESA DE PRUEBA S.A.C.");
        response.setNombreComercial("EMPRESA DE PRUEBA");
        response.setEstado("ACTIVO");
        response.setCondicion("HABIDO");
        return response;
    }
}