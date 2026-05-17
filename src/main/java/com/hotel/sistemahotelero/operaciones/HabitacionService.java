package com.hotel.sistemahotelero.operaciones;

import com.hotel.sistemahotelero.configuracion.*;
import com.hotel.sistemahotelero.seguridad.UsuarioRepository;
import com.hotel.sistemahotelero.trazabilidad.EstadoHabitacionEvent;
import com.hotel.sistemahotelero.ventas.Venta;
import com.hotel.sistemahotelero.ventas.VentaRepository;
import com.hotel.sistemahotelero.huespedes.HuespedRepository;
import com.hotel.sistemahotelero.huespedes.Huesped;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class HabitacionService {

    @Autowired
    private HuespedRepository huespedRepository;

    @Autowired
    private HabitacionRepository habitacionRepository;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * PROCESO COMPLETO (Usado por Angular):
     * Recibe CheckInRequest que tiene idHabitacion (Long) y DNI (String)
     */
    @Transactional
    public Habitacion procesarCheckInCompleto(CheckInRequest request) {
        // 1. Buscamos la habitación por su ID numérico (Long)
        Habitacion habitacion = habitacionRepository.findById(request.getIdHabitacion())
                .orElseThrow(() -> new RuntimeException("Habitación no encontrada"));

        // 2. Lógica del Huésped: Buscamos por DNI (String)
        // Importante: Usamos findByDni, NO findById
        String dniBusqueda = request.getHuesped().getDni();

        Huesped huesped = huespedRepository.findByDni(dniBusqueda)
                .orElseGet(() -> {
                    // Si no existe, creamos un nuevo objeto Huesped
                    Huesped nuevo = new Huesped();
                    nuevo.setDni(dniBusqueda); // Seteamos el String
                    nuevo.setNombres(request.getHuesped().getNombres());
                    nuevo.setApellidos(request.getHuesped().getApellidos());
                    nuevo.setTelefono(request.getHuesped().getTelefono());
                    return nuevo;
                });

        // 3. VINCULACIÓN: Buscamos en la tabla de usuarios usando el DNI (String)
        usuarioRepository.findByDni(dniBusqueda).ifPresent(usuario -> {
            huesped.setUsuario(usuario);
        });

        // Guardamos el huésped (Esto genera su ID Long automático si es nuevo)
        huespedRepository.save(huesped);

        // 4. Actualizamos la habitación
        EstadoHabitacion estadoAnterior = habitacion.getEstadoActual();
        habitacion.setEstadoActual(EstadoHabitacion.OCUPADO);
        habitacion.setHuespedActual(huesped);
        habitacion.setFechaCheckIn(LocalDateTime.now());

        Habitacion guardada = habitacionRepository.save(habitacion);

        // 5. Trazabilidad
        eventPublisher.publishEvent(new EstadoHabitacionEvent(this, habitacion.getId(), estadoAnterior, EstadoHabitacion.OCUPADO));

        return guardada;
    }

    /**
     * CHECK-OUT: Genera venta y libera habitación
     */
    @Transactional
    public Habitacion realizarCheckOut(Long habitacionId, Double monto, String medioPago, String tiempoEstadia) {
        Habitacion habitacion = habitacionRepository.findById(habitacionId)
                .orElseThrow(() -> new RuntimeException("Error: Habitación no encontrada"));

        if (habitacion.getEstadoActual() != EstadoHabitacion.OCUPADO) {
            throw new IllegalStateException("La habitación no está ocupada.");
        }

        Huesped huespedQueSale = habitacion.getHuespedActual();
        EstadoHabitacion anterior = habitacion.getEstadoActual();

        // Registrar la Venta
        Venta nuevaVenta = new Venta(habitacion, huespedQueSale, monto, medioPago, tiempoEstadia);
        ventaRepository.save(nuevaVenta);

        // Limpiar habitación
        habitacion.setEstadoActual(EstadoHabitacion.SUCIO);
        habitacion.setHuespedActual(null);
        habitacion.setFechaCheckIn(null);

        Habitacion guardada = habitacionRepository.save(habitacion);

        // Trazabilidad
        eventPublisher.publishEvent(new EstadoHabitacionEvent(this, habitacionId, anterior, EstadoHabitacion.SUCIO));

        return guardada;
    }

    /**
     * MÉTODO SIMPLE (Opcional): Por si tienes procesos que ya conocen el ID numérico del huésped
     */
    @Transactional
    public Habitacion realizarCheckIn(Long habitacionId, Long huespedId) {
        Habitacion habitacion = habitacionRepository.findById(habitacionId).orElseThrow();
        // Aquí sí usamos findById porque recibimos un Long huespedId
        Huesped huesped = huespedRepository.findById(huespedId).orElseThrow();

        EstadoHabitacion anterior = habitacion.getEstadoActual();
        habitacion.setEstadoActual(EstadoHabitacion.OCUPADO);
        habitacion.setHuespedActual(huesped);
        habitacion.setFechaCheckIn(LocalDateTime.now());

        Habitacion guardada = habitacionRepository.save(habitacion);
        eventPublisher.publishEvent(new EstadoHabitacionEvent(this, habitacionId, anterior, EstadoHabitacion.OCUPADO));
        return guardada;
    }
}