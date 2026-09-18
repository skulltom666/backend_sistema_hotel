package com.hotel.sistemahotelero.func;

import com.hotel.sistemahotelero.modules.auth.repository.UserRepository;
import com.hotel.sistemahotelero.modules.hotel.repository.FloorRepository;
import com.hotel.sistemahotelero.modules.hotel.repository.HotelRepository;
import com.hotel.sistemahotelero.modules.hotel.repository.RoomRepository;
import com.hotel.sistemahotelero.shared.enums.RoomStatus;
import com.hotel.sistemahotelero.shared.enums.SubscriptionPlan;
import com.hotel.sistemahotelero.shared.persistence.Floor;
import com.hotel.sistemahotelero.shared.persistence.Hotel;
import com.hotel.sistemahotelero.shared.persistence.Room;
import com.hotel.sistemahotelero.shared.persistence.User;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas funcionales - Persistencia de los datos.
 *
 * Verifican que las entidades del dominio se persisten, recuperan, actualizan y borran
 * correctamente sobre la base H2 real, incluyendo jerarquias en cascada, aislamiento
 * multi-tenant y los valores que JPA asigna automaticamente (@PrePersist / @PreUpdate).
 */
@SpringBootTest
@DisplayName("Persistencia de los datos")
class HotelPersistenceTest {

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FloorRepository floorRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("Guardar y recuperar un hotel de la base de datos")
    void testPersistenciaReal() {
        User user = crearUsuario("20111111111", "persistencia@test.com");

        Hotel hotel = nuevoHotel(user, "Hotel Persistente");

        Hotel guardado = hotelRepository.saveAndFlush(hotel);
        Long id = guardado.getId();

        entityManager.clear();

        Optional<Hotel> encontrado = hotelRepository.findById(id);
        assertTrue(encontrado.isPresent(), "El hotel debería haberse guardado correctamente");
        assertEquals("Hotel Persistente", encontrado.get().getNombre());
        assertEquals(user.getRuc(), encontrado.get().getTenantId());
    }

//    @Test
//    @DisplayName("CP-F03: la persistencia sobrevive a un flush y a la limpieza del contexto")
//    void testPersistenciaTrasReiniciarContexto() {
//        User user = crearUsuario("20122222222", "persistencia2@test.com");
//
//        Hotel hotel = nuevoHotel(user, "Hotel Indestructible");
//
//        Long id = hotelRepository.saveAndFlush(hotel).getId();
//
//        hotelRepository.flush();
//        entityManager.clear();
//
//        Optional<Hotel> recuperado = hotelRepository.findById(id);
//        assertTrue(recuperado.isPresent());
//        assertEquals("Hotel Indestructible", recuperado.get().getNombre());
//        assertEquals(user.getRuc(), recuperado.get().getTenantId());
//    }

    @Test
    @Transactional
    @DisplayName("Ciclo completo crear, leer, actualizar y borrar lógico")
    void cicloCompletoCRUD() {
        User user = crearUsuario("20133333333", "crud@test.com");

        Hotel hotel = nuevoHotel(user, "Hotel CRUD");
        Long id = hotelRepository.saveAndFlush(hotel).getId();
        entityManager.clear();

        // READ: recuperar por id y tenant
        Optional<Hotel> leido = hotelRepository.findByIdAndTenant(id, user.getRuc());
        assertTrue(leido.isPresent(), "El hotel debe poder leerse por id y tenant");
        assertEquals("Hotel CRUD", leido.get().getNombre());

        // UPDATE: modificar campos y re-persistir
        Hotel paraActualizar = leido.get();
        paraActualizar.setNombre("Hotel CRUD Renovado");
        paraActualizar.setTelefono("999888777");
        hotelRepository.saveAndFlush(paraActualizar);
        entityManager.clear();

        Optional<Hotel> actualizado = hotelRepository.findByIdAndTenant(id, user.getRuc());
        assertTrue(actualizado.isPresent(), "El hotel actualizado debe seguir visible");
        assertEquals("Hotel CRUD Renovado", actualizado.get().getNombre());
        assertEquals("999888777", actualizado.get().getTelefono());
        assertNotNull(actualizado.get().getUpdatedAt());

        // DELETE logico (soft delete por tenant): deja de ser visible
        hotelRepository.softDeleteByIdAndTenant(id, user.getRuc());
        entityManager.clear();

        assertFalse(hotelRepository.findByIdAndTenant(id, user.getRuc()).isPresent(),
                "Tras el borrado logico el hotel no debe ser visible por tenant");
        assertFalse(hotelRepository.existsByIdAndTenant(id, user.getRuc()));

        // El registro fisico debe quedar con enabled = false
        Optional<Hotel> bruto = hotelRepository.findById(id);
        assertTrue(bruto.isPresent(), "El registro fisico debe conservarse");
        assertEquals(Boolean.FALSE, bruto.get().getEnabled(), "Debe quedar deshabilitado");
    }

    @Test
    @DisplayName("Persistir en cascada Usuario -> Hotel -> Piso -> Habitacion")
    void persistirEnCascadaJerarquiaCompleta() {
        User user = crearUsuario("20144444444", "cascada@test.com");

        Hotel hotel = nuevoHotel(user, "Hotel Cascada");

        Floor piso = new Floor();
        piso.setFloorNumber(1);
        piso.setHotel(hotel);
        piso.setTenantId(user.getRuc());
        hotel.getFloors().add(piso);

        Room habitacion = new Room();
        habitacion.setRoomNumber("101");
        habitacion.setCategory("Estandar");
        habitacion.setCapacity(2);
        habitacion.setStatus(RoomStatus.DISPONIBLE);
        habitacion.setFloor(piso);
        habitacion.setTenantId(user.getRuc());
        habitacion.setHorasMinimas(6);
        habitacion.setPrecioMinimo(new BigDecimal("50"));
        habitacion.setPrecio12Horas(new BigDecimal("150"));
        habitacion.setPrecio24Horas(new BigDecimal("250"));
        habitacion.setPrecioHoraExtra(new BigDecimal("20"));
        piso.getRooms().add(habitacion);

        // Un solo saveAndFlush del hotel debe cascadear a todo el arbol
        hotelRepository.saveAndFlush(hotel);
        entityManager.clear();

        // El hotel quedo persistido y visible para su tenant
        Optional<Hotel> guardado = hotelRepository.findByIdAndTenant(hotel.getId(), user.getRuc());
        assertTrue(guardado.isPresent(), "El hotel debe persistirse");
        assertEquals("Hotel Cascada", guardado.get().getNombre());
        assertTrue(hotelRepository.countByTenant(user.getRuc()) == 1);

        // Los pisos y habitaciones persistieron gracias a la cascada
        List<Floor> pisos = floorRepository.findByHotelAndTenant(hotel.getId(), user.getRuc());
        assertEquals(1, pisos.size(), "El hotel debe tener 1 piso");
        assertEquals("Piso 1", pisos.get(0).getName(),
                "@PrePersist debe asignar el nombre del piso");

        List<Room> habitaciones = roomRepository.findByHotelAndTenant(hotel.getId(), user.getRuc());
        assertEquals(1, habitaciones.size(), "El piso debe tener 1 habitacion");
        assertEquals("101", habitaciones.get(0).getRoomNumber());
        assertEquals(RoomStatus.DISPONIBLE, habitaciones.get(0).getStatus(),
                "La habitacion debe persistir en estado DISPONIBLE");
    }

    @Test
    @DisplayName("Cada tenant solo ve sus propios hoteles")
    void aislamientoPorTenant() {
        User tenantA = crearUsuario("20155555551", "tenant.a@test.com");
        User tenantB = crearUsuario("20155555552", "tenant.b@test.com");

        Hotel hotelA = hotelRepository.saveAndFlush(nuevoHotel(tenantA, "Hotel A"));
        hotelRepository.saveAndFlush(nuevoHotel(tenantB, "Hotel B"));
        entityManager.clear();

        List<Hotel> hotelesA = hotelRepository.findAllByTenant(tenantA.getRuc());
        List<Hotel> hotelesB = hotelRepository.findAllByTenant(tenantB.getRuc());

        assertEquals(1, hotelesA.size(), "El tenant A debe ver solo su hotel");
        assertEquals("Hotel A", hotelesA.get(0).getNombre());
        assertEquals(1, hotelesB.size(), "El tenant B debe ver solo su hotel");
        assertEquals("Hotel B", hotelesB.get(0).getNombre());

        // La busqueda por nombre tambien respeta el tenant
        List<Hotel> buscados = hotelRepository.findByNombreContainingAndTenant("B", tenantA.getRuc());
        assertTrue(buscados.isEmpty(), "El tenant A no debe encontrar hoteles del tenant B");
        assertEquals(1, hotelRepository.findByNombreContainingAndTenant("Hotel", tenantA.getRuc()).size());

        // Referencias al id del otro tenant: el hotel B no existe para el tenant A
        assertFalse(hotelRepository.existsByIdAndTenant(hotelA.getId(), tenantB.getRuc()));
    }

//    @Test
//    @DisplayName("CP-F18: @PrePersist asigna tenantId, plan y limites automaticamente")
//    void valoresAutomaticosDeLosCallbacksJPA() {
//        User user = new User();
//        user.setRuc("20166666666");
//        user.setEmail("auto@test.com");
//        user.setPassword("123456");
//        user.setNombre("Usuario Auto");
//        user.setRol("ADMINISTRADOR");
//        // No se setechan tenantId, plan ni limites: deben salir de @PrePersist
//
//        User guardado = userRepository.saveAndFlush(user);
//
//        assertEquals(user.getRuc(), guardado.getTenantId(),
//                "tenantId debe autoasignarse con el RUC");
//        assertEquals(SubscriptionPlan.BASICO, guardado.getSubscriptionPlan(),
//                "El plan debe ser BASICO por defecto");
//        assertEquals(SubscriptionPlan.BASICO.getMaxHotels(), guardado.getMaxHotels());
//        assertEquals(SubscriptionPlan.BASICO.getMaxFloors(), guardado.getMaxFloors());
//        assertEquals(SubscriptionPlan.BASICO.getMaxRooms(), guardado.getMaxRooms());
//        assertNotNull(guardado.getCreatedAt());
//        assertNotNull(guardado.getUpdatedAt());
//        assertTrue(guardado.getEnabled());
//
//        // El hotel recien creado tambien hereda el tenant del usuario
//        Hotel hotel = hotelRepository.saveAndFlush(nuevoHotel(guardado, "Hotel Callback"));
//        assertEquals(guardado.getRuc(), hotel.getTenantId(),
//                "Hotel @PrePersist debe heredar el tenant del user");
//    }

    private User crearUsuario(String ruc, String email) {
        User user = new User();
        user.setRuc(ruc);
        user.setTenantId(ruc);
        user.setEmail(email);
        user.setPassword("123456");
        user.setNombre("Admin");
        user.setRol("ADMINISTRADOR");
        return userRepository.save(user);
    }

    private Hotel nuevoHotel(User user, String nombre) {
        Hotel hotel = new Hotel();
        hotel.setNombre(nombre);
        hotel.setUser(user);
        return hotel;
    }
}