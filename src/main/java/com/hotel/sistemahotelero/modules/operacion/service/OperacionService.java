package com.hotel.sistemahotelero.modules.operacion.service;

import com.hotel.sistemahotelero.modules.auth.repository.UserRepository;
import com.hotel.sistemahotelero.modules.booking.repository.BookingRepository;
import com.hotel.sistemahotelero.modules.hotel.repository.FloorRepository;
import com.hotel.sistemahotelero.modules.hotel.repository.HotelRepository;
import com.hotel.sistemahotelero.modules.hotel.repository.RoomRepository;
import com.hotel.sistemahotelero.modules.operacion.dto.*;
import com.hotel.sistemahotelero.modules.operacion.repository.HuespedRepository;
import com.hotel.sistemahotelero.modules.operacion.repository.MovimientoRepository;
import com.hotel.sistemahotelero.security.tenant.TenantContext;
import com.hotel.sistemahotelero.shared.enums.BookingStatus;
import com.hotel.sistemahotelero.shared.enums.RoomStatus;
import com.hotel.sistemahotelero.shared.exceptions.BusinessException;
import com.hotel.sistemahotelero.shared.persistence.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OperacionService {

    private final RoomRepository roomRepository;
    private final FloorRepository floorRepository;
    private final HotelRepository hotelRepository;
    private final BookingRepository bookingRepository;
    private final HuespedRepository huespedRepository;
    private final MovimientoRepository movimientoRepository;
    private final UserRepository userRepository;

    // ==================== HUÉSPEDES ====================

    public Optional<Huesped> buscarHuesped(String documento) {
        String tenantId = TenantContext.getCurrentTenant();
        return huespedRepository.findByDniAndTenantId(documento, tenantId);
    }

    @Transactional
    public Huesped guardarHuesped(HuespedDTO request) {
        String tenantId = TenantContext.getCurrentTenant();
        Huesped huesped = huespedRepository.findByDniAndTenantId(request.getDni(), tenantId)
                .orElseGet(() -> {
                    Huesped nuevo = new Huesped();
                    nuevo.setTenantId(tenantId);
                    nuevo.setDni(request.getDni());
                    return nuevo;
                });
        huesped.setNombre(request.getNombre());
        huesped.setApellido(request.getApellido());
        huesped.setTelefono(request.getTelefono());
        huesped.setEmail(request.getEmail());
        return huespedRepository.save(huesped);
    }

    // ==================== CHECK-IN ====================

    @Transactional
    public Booking checkIn(CheckInFrontRequest request) {
        String tenantId = TenantContext.getCurrentTenant();

        Room room = roomRepository.findByIdAndTenant(request.getIdHabitacion(), tenantId)
                .orElseThrow(() -> new BusinessException("Habitación no encontrada"));

        if (room.getStatus() != RoomStatus.DISPONIBLE) {
            throw new BusinessException("La habitación no está disponible para check-in");
        }

        if (bookingRepository.existsActiveBookingByRoomAndTenant(room.getId(), tenantId)) {
            throw new BusinessException("La habitación ya tiene una reserva activa");
        }

        CheckInFrontRequest.HuespedRequest huespedReq = request.getHuesped();

        // Find or create huésped por DNI
        Huesped huesped = null;
        if (huespedReq != null && huespedReq.getDni() != null && !huespedReq.getDni().isBlank()) {
            huesped = huespedRepository.findByDniAndTenantId(huespedReq.getDni(), tenantId)
                    .orElseGet(() -> {
                        Huesped nuevo = new Huesped();
                        nuevo.setTenantId(tenantId);
                        nuevo.setDni(huespedReq.getDni());
                        return nuevo;
                    });
            huesped.setNombre(huespedReq.getNombres());
            huesped.setApellido(huespedReq.getApellidos());
            huesped.setTelefono(huespedReq.getTelefono());
            huesped.setEmail(huespedReq.getEmail());
            huesped = huespedRepository.save(huesped);
        }

        String nombreCompleto = joinNombre(huespedReq);
        String dni = huespedReq != null ? huespedReq.getDni() : null;

        Booking booking = new Booking();
        booking.setTenantId(tenantId);
        booking.setRoom(room);
        booking.setGuestName(nombreCompleto);
        booking.setGuestDocument(dni);
        booking.setGuestEmail(huespedReq != null && huespedReq.getEmail() != null ? huespedReq.getEmail() : null);
        booking.setGuestPhone(huespedReq != null ? huespedReq.getTelefono() : null);
        booking.setCheckIn(LocalDateTime.now());
        booking.setStatus(BookingStatus.CHECK_IN);
        booking.setCheckedIn(true);
        booking.setCheckedOut(false);
        booking.setNumberOfGuests(1);
        booking.setObservations(request.getObservacion());
        booking.setSource("PRESENCIAL");
        booking.setPaidAmount(request.getAdelanto() != null ? request.getAdelanto().doubleValue() : 0.0);

        room.setStatus(RoomStatus.OCUPADO);
        roomRepository.save(room);

        Booking saved = bookingRepository.save(booking);
        registrarMovimiento(room, RoomStatus.DISPONIBLE, RoomStatus.OCUPADO, "Check-in registrado");

        log.info("Check-in frontend: room {}, guest {}", room.getRoomNumber(), nombreCompleto);
        return saved;
    }

    // ==================== PRE-CHECKOUT ====================

    public PreCheckoutDTO preCheckout(Long habitacionId) {
        String tenantId = TenantContext.getCurrentTenant();

        Room room = roomRepository.findByIdAndTenant(habitacionId, tenantId)
                .orElseThrow(() -> new BusinessException("Habitación no encontrada"));

        Booking booking = bookingRepository.findActiveByRoomAndTenant(habitacionId, tenantId)
                .stream().findFirst()
                .orElseThrow(() -> new BusinessException("No hay un huésped activo en esta habitación"));

        LocalDateTime now = LocalDateTime.now();
        long minutes = Duration.between(booking.getCheckIn(), now).toMinutes();
        int horasMin = room.getHorasMinimas() != null ? room.getHorasMinimas() : 1;
        int horas = (int) Math.max(Math.ceil(minutes / 60.0), horasMin);

        BigDecimal totalAPagar = calcularTotal(room, horas);
        BigDecimal adelanto = BigDecimal.valueOf(booking.getPaidAmount() != null ? booking.getPaidAmount() : 0.0);
        BigDecimal saldoPendiente = totalAPagar.subtract(adelanto).max(BigDecimal.ZERO);

        Hotel hotel = room.getFloor().getHotel();
        String[] partes = separarNombre(booking.getGuestName());

        return PreCheckoutDTO.builder()
                .habitacion(PreCheckoutDTO.HabitacionView.builder()
                        .id(room.getId())
                        .numero(room.getRoomNumber())
                        .piso(room.getFloor().getFloorNumber())
                        .hotel(PreCheckoutDTO.HotelView.builder()
                                .nombre(hotel.getNombre())
                                .ruc(hotel.getUser() != null ? hotel.getUser().getRuc() : null)
                                .direccion(hotel.getDireccion())
                                .build())
                        .build())
                .huesped(PreCheckoutDTO.HuespedView.builder()
                        .nombre(partes[0])
                        .apellido(partes[1])
                        .dni(booking.getGuestDocument())
                        .build())
                .fechaIngreso(booking.getCheckIn())
                .fechaSalida(now)
                .horasTotales(horas)
                .totalAPagar(totalAPagar)
                .adelanto(adelanto)
                .saldoPendiente(saldoPendiente)
                .build();
    }

    // ==================== CHECK-OUT ====================

    @Transactional
    public void checkOut(Long habitacionId, CheckOutFrontRequest request) {
        String tenantId = TenantContext.getCurrentTenant();

        Room room = roomRepository.findByIdAndTenant(habitacionId, tenantId)
                .orElseThrow(() -> new BusinessException("Habitación no encontrada"));

        Booking booking = bookingRepository.findActiveByRoomAndTenant(habitacionId, tenantId)
                .stream().findFirst()
                .orElseThrow(() -> new BusinessException("No hay un huésped activo en esta habitación"));

        booking.setCheckOut(LocalDateTime.now());
        booking.setStatus(BookingStatus.CHECK_OUT);
        booking.setCheckedOut(true);
        booking.setTotalAmount(request.getTotal() != null ? request.getTotal().doubleValue() : 0.0);
        booking.setPaymentMethod(request.getMetodoPago());
        bookingRepository.save(booking);

        RoomStatus anterior = room.getStatus();
        room.setStatus(RoomStatus.SUCIO);
        roomRepository.save(room);

        registrarMovimiento(room, anterior, RoomStatus.SUCIO, "Check-out y cobro registrado");
        log.info("Check-out frontend: room {}", room.getRoomNumber());
    }

    // ==================== HABITACIONES ====================

    public List<HabitacionDTO> habitacionesDeHotel(Long hotelId) {
        String tenantId = TenantContext.getCurrentTenant();
        hotelRepository.findByIdAndTenant(hotelId, tenantId)
                .orElseThrow(() -> new BusinessException("Hotel no encontrado"));

        return roomRepository.findByHotelAndTenant(hotelId, tenantId).stream()
                .map(room -> toHabitacionDTO(room, room.getFloor().getHotel().getId(), tenantId))
                .toList();
    }

    public HabitacionDTO habitacionDetalle(Long habitacionId) {
        String tenantId = TenantContext.getCurrentTenant();
        Room room = roomRepository.findByIdAndTenant(habitacionId, tenantId)
                .orElseThrow(() -> new BusinessException("Habitación no encontrada"));
        return toHabitacionDTO(room, room.getFloor().getHotel().getId(), tenantId);
    }

    @Transactional
    public Room crearHabitacion(CrearHabitacionRequest request) {
        String tenantId = TenantContext.getCurrentTenant();

        Hotel hotel = hotelRepository.findByIdAndTenant(request.getHotelId(), tenantId)
                .orElseThrow(() -> new BusinessException("Hotel no encontrado"));

        // Verificar límite del plan
        long currentRooms = roomRepository.findByHotelAndTenant(hotel.getId(), tenantId).size();
        if (hotel.getUser() != null && hotel.getUser().getMaxRooms() != null
                && currentRooms >= hotel.getUser().getMaxRooms()) {
            throw new BusinessException("Has alcanzado el límite de habitaciones de tu plan");
        }

        // Buscar o crear el piso
        int piso = request.getPiso() != null ? request.getPiso() : 1;
        Floor floor = floorRepository.findByHotelAndTenant(hotel.getId(), tenantId).stream()
                .filter(f -> f.getFloorNumber() != null && f.getFloorNumber() == piso)
                .findFirst()
                .orElseGet(() -> {
                    Floor nuevo = new Floor();
                    nuevo.setTenantId(tenantId);
                    nuevo.setFloorNumber(piso);
                    nuevo.setHotel(hotel);
                    return floorRepository.save(nuevo);
                });

        boolean exists = roomRepository.existsByFloorAndRoomNumberAndTenant(floor.getId(), request.getNumero(), tenantId);
        if (exists) {
            throw new BusinessException("Ya existe una habitación con ese número en este piso");
        }

        Room room = new Room();
        room.setTenantId(tenantId);
        room.setRoomNumber(request.getNumero());
        room.setCategory("Estandar");
        room.setCapacity(request.getLimitePersonas() != null ? request.getLimitePersonas() : 2);
        room.setDescription(request.getDescripcion());
        room.setStatus(RoomStatus.DISPONIBLE);
        room.setFloor(floor);
        room.setCamasSimples(request.getCamasSimples());
        room.setCamasDobles(request.getCamasDobles());
        room.setHorasMinimas(request.getHorasMinimas());
        room.setPrecioMinimo(request.getPrecioMinimo());
        room.setPrecio12Horas(request.getPrecio12Horas());
        room.setPrecio24Horas(request.getPrecio24Horas());
        room.setPrecioHoraExtra(request.getPrecioHoraExtra());

        Room saved = roomRepository.save(room);

        hotel.setTotalRooms(hotel.getTotalRooms() + 1);
        hotelRepository.save(hotel);

        registrarMovimiento(room, null, RoomStatus.DISPONIBLE, "Habitación creada");
        log.info("Habitación creada por frontend: hotel {}, piso {}, número {}", hotel.getId(), piso, request.getNumero());
        return saved;
    }

    @Transactional
    public void eliminarHabitacion(Long habitacionId) {
        String tenantId = TenantContext.getCurrentTenant();

        Room room = roomRepository.findByIdAndTenant(habitacionId, tenantId)
                .orElseThrow(() -> new BusinessException("Habitación no encontrada"));

        if (bookingRepository.existsActiveBookingByRoomAndTenant(habitacionId, tenantId)) {
            throw new BusinessException("No se puede eliminar la habitación porque tiene una reserva activa");
        }

        roomRepository.softDeleteByIdAndTenant(habitacionId, tenantId);

        Hotel hotel = room.getFloor().getHotel();
        if (hotel.getTotalRooms() != null && hotel.getTotalRooms() > 0) {
            hotel.setTotalRooms(hotel.getTotalRooms() - 1);
            hotelRepository.save(hotel);
        }

        log.info("Habitación eliminada: {}", habitacionId);
    }

    @Transactional
    public void cambiarEstado(Long habitacionId, EstadoRequest request) {
        String tenantId = TenantContext.getCurrentTenant();

        Room room = roomRepository.findByIdAndTenant(habitacionId, tenantId)
                .orElseThrow(() -> new BusinessException("Habitación no encontrada"));

        RoomStatus anterior = room.getStatus();
        RoomStatus nuevo = mapEstado(request.getEstado());

        room.setStatus(nuevo);
        roomRepository.save(room);

        registrarMovimiento(room, anterior, nuevo, "Cambio de estado");
        log.info("Estado habitación {}: {} -> {}", habitacionId, anterior, nuevo);
    }

    // ==================== HISTORIAL ====================

    public List<MovimientoHistorico> historialHotel(Long hotelId) {
        String tenantId = TenantContext.getCurrentTenant();
        return movimientoRepository.findByHotelAndTenant(hotelId, tenantId);
    }

    public List<MovimientoHistorico> historialHabitacion(Long habitacionId) {
        String tenantId = TenantContext.getCurrentTenant();
        return movimientoRepository.findByHabitacionAndTenant(habitacionId, tenantId);
    }

    // ==================== PERFIL ====================

    public PerfilDTO perfilActual() {
        String tenantId = TenantContext.getCurrentTenant();
        User user = usuarioActualOpcional(tenantId);

        Huesped huesped = null;
        if (user != null) {
            huesped = huespedRepository.findByUserIdAndTenantId(user.getId(), user.getRuc()).orElse(null);
        }

        return PerfilDTO.builder()
                .email(user != null ? user.getEmail() : null)
                .nombreUsuario(user != null ? user.getNombre() : null)
                .dni(huesped != null ? huesped.getDni() : null)
                .nombres(huesped != null ? huesped.getNombre() : null)
                .apellidos(huesped != null ? huesped.getApellido() : null)
                .telefono(user != null ? user.getTelefono() : null)
                .build();
    }

    @Transactional
    public String vincularPerfil(VincularRequest request) {
        String tenantId = TenantContext.getCurrentTenant();
        User user = usuarioActual(tenantId);

        if (request.getDni() == null || request.getDni().isBlank()) {
            throw new BusinessException("El DNI es obligatorio para vincular");
        }

        Huesped huesped = huespedRepository.findByDniAndTenantId(request.getDni(), tenantId)
                .orElseGet(() -> {
                    Huesped nuevo = new Huesped();
                    nuevo.setTenantId(tenantId);
                    nuevo.setDni(request.getDni());
                    return nuevo;
                });
        huesped.setNombre(request.getNombre());
        huesped.setApellido(request.getApellido());
        huesped.setTelefono(request.getTelefono());
        huesped.setUserId(user.getId());
        huespedRepository.save(huesped);

        if (request.getNombre() != null) user.setNombre(request.getNombre());
        if (request.getApellido() != null) user.setApellido(request.getApellido());
        if (request.getTelefono() != null) user.setTelefono(request.getTelefono());
        userRepository.save(user);

        log.info("Perfil vinculado para usuario {} y DNI {}", user.getEmail(), request.getDni());
        return "Perfil vinculado correctamente";
    }

    // ==================== HELPERS ====================

    private HabitacionDTO toHabitacionDTO(Room room, Long hotelId, String tenantId) {
        Booking activo = bookingRepository.findActiveByRoomAndTenant(room.getId(), tenantId)
                .stream().findFirst().orElse(null);

        HabitacionDTO.HuespedActualView huespedActual = null;
        if (activo != null) {
            String[] partes = separarNombre(activo.getGuestName());
            huespedActual = HabitacionDTO.HuespedActualView.builder()
                    .nombre(partes[0])
                    .apellidos(partes[1])
                    .dni(activo.getGuestDocument())
                    .build();
        }

        return HabitacionDTO.builder()
                .id(room.getId())
                .numero(room.getRoomNumber())
                .piso(room.getFloor() != null ? room.getFloor().getFloorNumber() : null)
                .estadoActual(mapEstadoActual(room.getStatus()))
                .limitePersonas(room.getCapacity())
                .camasSimples(room.getCamasSimples())
                .camasDobles(room.getCamasDobles())
                .horasMinimas(room.getHorasMinimas())
                .precioMinimo(room.getPrecioMinimo())
                .precio12Horas(room.getPrecio12Horas())
                .precio24Horas(room.getPrecio24Horas())
                .precioHoraExtra(room.getPrecioHoraExtra())
                .descripcion(room.getDescription())
                .huespedActual(huespedActual)
                .build();
    }

    private String mapEstadoActual(RoomStatus status) {
        if (status == RoomStatus.LIMPIEZA) {
            return "EN_LIMPIEZA";
        }
        return status != null ? status.name() : "DISPONIBLE";
    }

    private RoomStatus mapEstado(String estado) {
        if (estado == null) {
            return RoomStatus.DISPONIBLE;
        }
        String upper = estado.trim().toUpperCase();
        return switch (upper) {
            case "LIMPIEZA" -> RoomStatus.EN_LIMPIEZA;
            case "SUCIO" -> RoomStatus.SUCIO;
            case "OCUPADO" -> RoomStatus.OCUPADO;
            case "MANTENIMIENTO" -> RoomStatus.MANTENIMIENTO;
            case "RESERVADO" -> RoomStatus.RESERVADO;
            default -> RoomStatus.DISPONIBLE;
        };
    }

    /**
     * Cálculo de tarifas por horas del RoomioHub:
     * - hasta horasMinimas → precioMinimo
     * - entre horasMinimas y 12h → precioMinimo + horaExtra por cada hora extra
     * - entre 12h y 24h → como mínimo el precio de 12h
     * - >= 24h → precio de 24h + horaExtra por horas extras
     */
    private BigDecimal calcularTotal(Room room, int horas) {
        int horasMin = room.getHorasMinimas() != null ? room.getHorasMinimas() : 6;
        BigDecimal pMin = nz(room.getPrecioMinimo());
        BigDecimal p12 = nz(room.getPrecio12Horas());
        BigDecimal p24 = nz(room.getPrecio24Horas());
        BigDecimal pExtra = nz(room.getPrecioHoraExtra());

        if (horas <= horasMin) {
            return pMin;
        }
        if (horas < 12) {
            return pMin.add(pExtra.multiply(BigDecimal.valueOf(horas - horasMin)));
        }
        if (horas < 24) {
            BigDecimal base = pMin.add(pExtra.multiply(BigDecimal.valueOf(horas - horasMin)));
            return base.max(p12);
        }
        return p24.add(pExtra.multiply(BigDecimal.valueOf(horas - 24L)));
    }

    private BigDecimal nz(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }

    private String joinNombre(CheckInFrontRequest.HuespedRequest h) {
        if (h == null) {
            return "Huésped";
        }
        String n = h.getNombres() != null ? h.getNombres().trim() : "";
        String a = h.getApellidos() != null ? h.getApellidos().trim() : "";
        return (n + " " + a).trim().isEmpty() ? "Huésped" : (n + " " + a).trim();
    }

    private String[] separarNombre(String nombreCompleto) {
        if (nombreCompleto == null || nombreCompleto.isBlank()) {
            return new String[]{"Huésped", ""};
        }
        String[] partes = nombreCompleto.trim().split("\\s+", 2);
        return new String[]{partes[0], partes.length > 1 ? partes[1] : ""};
    }

    private void registrarMovimiento(Room room, RoomStatus anterior, RoomStatus nuevo, String observacion) {
        MovimientoHistorico m = new MovimientoHistorico();
        m.setTenantId(TenantContext.getCurrentTenant());
        m.setHotelId(room.getFloor().getHotel().getId());
        m.setHabitacionId(room.getId());
        m.setHabitacionNumero(room.getRoomNumber());
        m.setEstadoAnterior(anterior != null ? anterior.name() : "INICIO");
        m.setEstadoNuevo(nuevo != null ? nuevo.name() : "DISPONIBLE");
        m.setUsuarioEncargado(nombreUsuarioActual());
        m.setFechaHora(LocalDateTime.now());
        m.setObservacion(observacion);
        movimientoRepository.save(m);
    }

    private String nombreUsuarioActual() {
        User user = usuarioActualOpcional(TenantContext.getCurrentTenant());
        if (user != null && user.getNombre() != null) {
            return user.getNombre();
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getName() != null ? auth.getName() : "Sistema";
    }

    private User usuarioActual(String tenantId) {
        User user = usuarioActualOpcional(tenantId);
        if (user == null) {
            throw new BusinessException("Usuario no encontrado");
        }
        return user;
    }

    private User usuarioActualOpcional(String tenantId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth != null ? auth.getName() : null;
        if (email != null && !email.isBlank()) {
            return userRepository.findByEmail(email).orElse(null);
        }
        if (tenantId != null) {
            return userRepository.findByTenantId(tenantId).orElse(null);
        }
        return null;
    }
}