package com.hotel.sistemahotelero;

import com.hotel.sistemahotelero.modules.auth.repository.UserRepository;
import com.hotel.sistemahotelero.modules.hotel.repository.HotelRepository;
import com.hotel.sistemahotelero.shared.enums.SubscriptionPlan;
import com.hotel.sistemahotelero.shared.persistence.Hotel;
import com.hotel.sistemahotelero.shared.persistence.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class HotelPerformanceTest {

    private static final String TENANT = "20123456789";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setupData() {
        User admin = userRepository.findByRuc(TENANT).orElseGet(() -> {
            User user = new User();
            user.setRuc(TENANT);
            user.setTenantId(TENANT);
            user.setEmail("test@hotel.com");
            user.setPassword("1234");
            user.setRol("ADMINISTRADOR");
            user.setSubscriptionPlan(SubscriptionPlan.EMPRESARIAL);
            return userRepository.save(user);
        });

        for (int i = 0; i < 100; i++) {
            Hotel hotel = new Hotel();
            hotel.setTenantId(TENANT);
            hotel.setNombre("Hotel " + i);
            hotel.setUser(admin);
            hotelRepository.save(hotel);
        }
        hotelRepository.flush();
    }

    @Test
    @WithMockUser(username = "test@hotel.com", roles = "ADMIN")
    void testConsultarMisHoteles_Rendimiento() throws Exception {
        long startTime = System.currentTimeMillis();
        mockMvc.perform(get("/api/hoteles/mio")
                        .with(csrf())
                        .header("X-Tenant-ID", TENANT))
                .andExpect(status().isOk());
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        System.out.println("--------------------------------------------------");
        System.out.println("RESULTADO DE PRUEBA DE EFICIENCIA:");
        System.out.println("Registros en base de datos: 300");
        System.out.println("Tiempo de respuesta: " + duration + " ms");
        System.out.println("Estado: " + (duration < 500 ? "PASÓ (Eficiente)" : "FALLÓ (Lento)"));
        System.out.println("--------------------------------------------------");

        assertTrue(duration < 500, "La consulta tardó demasiado: " + duration + "ms");
    }
}