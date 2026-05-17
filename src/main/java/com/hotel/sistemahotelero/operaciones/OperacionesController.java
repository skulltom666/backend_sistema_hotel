package com.hotel.sistemahotelero.operaciones;

import com.hotel.sistemahotelero.configuracion.EstadoHabitacion;
import com.hotel.sistemahotelero.configuracion.Habitacion;
import com.hotel.sistemahotelero.configuracion.HabitacionRepository;
import com.hotel.sistemahotelero.huespedes.Huesped;
import com.hotel.sistemahotelero.huespedes.HuespedRepository;
import com.hotel.sistemahotelero.trazabilidad.HistorialEstado;
import com.hotel.sistemahotelero.trazabilidad.HistorialEstadoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneId; // 👈 IMPORTANTE: Añadimos ZoneId
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/operaciones")
public class OperacionesController {

    @Autowired
    private HabitacionService habitacionService;

    @Autowired
    private HuespedRepository huespedRepository;

    @Autowired
    private HabitacionRepository habitacionRepository;

    @Autowired
    private OperacionRepository operacionRepository;

    @Autowired
    private HistorialEstadoRepository historialRepository;

    // Definimos la zona horaria de Perú de forma global para no repetirla
    private final ZoneId zonaPeru = ZoneId.of("America/Lima");

    @PostMapping("/checkin")
    public ResponseEntity<?> confirmarCheckIn(@RequestBody CheckInDTO payload) {
        try {
            // 1. Lógica del Huésped (Buscamos si ya existe por DNI)
            Huesped huespedRecibido = payload.getHuesped();
            Huesped huespedFinal;
            Optional<Huesped> huespedExistente = huespedRepository.findByDni(huespedRecibido.getDni());

            if (huespedExistente.isPresent()) {
                huespedFinal = huespedExistente.get();
            } else {
                huespedFinal = huespedRepository.save(huespedRecibido);
            }

            // 2. Buscamos la habitación
            Habitacion habitacion = habitacionRepository.findById(payload.getIdHabitacion())
                    .orElseThrow(() -> new RuntimeException("Habitación no encontrada"));

            // 3. Guardamos la Operación de Check-In
            Operacion nuevaOp = new Operacion();
            nuevaOp.setHabitacion(habitacion);
            nuevaOp.setHuesped(huespedFinal);
            nuevaOp.setAdelanto(payload.getAdelanto() != null ? payload.getAdelanto() : 0.0);

            // 🚨 CORRECCIÓN: Usamos la zona horaria de Perú
            nuevaOp.setFechaIngreso(LocalDateTime.now(zonaPeru));
            nuevaOp.setEstado("ACTIVO");
            operacionRepository.save(nuevaOp);

            // 4. GUARDAMOS EL HISTORIAL PARA EL DASHBOARD
            HistorialEstado historial = new HistorialEstado();
            historial.setHabitacionId(habitacion.getId());
            historial.setEstadoAnterior("DISPONIBLE");
            historial.setEstadoNuevo("OCUPADO");

            // 🚨 CORRECCIÓN: Usamos la zona horaria de Perú
            historial.setFechaHora(LocalDateTime.now(zonaPeru));
            historial.setUsuarioEncargado("Recepcionista"); // Placeholder
            historialRepository.save(historial);

            // 5. Actualizamos el estado de la Habitación
            habitacion.setEstadoActual(EstadoHabitacion.OCUPADO);
            habitacion.setHuespedActual(huespedFinal);
            habitacionRepository.save(habitacion);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Check-In registrado exitosamente.");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/huesped/{dni}")
    public ResponseEntity<?> buscarHuespedPorDni(@PathVariable String dni) {
        Optional<Huesped> huesped = huespedRepository.findByDni(dni);
        if (huesped.isPresent()) {
            return ResponseEntity.ok(huesped.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{habitacionId}/pre-checkout")
    public ResponseEntity<?> obtenerDetalleCheckOut(@PathVariable Long habitacionId) {
        try {
            Habitacion hab = habitacionRepository.findById(habitacionId)
                    .orElseThrow(() -> new RuntimeException("La habitación no existe."));

            Operacion opActiva = operacionRepository.findAll().stream()
                    .filter(o -> o.getHabitacion() != null &&
                            o.getHabitacion().getId().equals(habitacionId) &&
                            "ACTIVO".equals(o.getEstado()))
                    .findFirst()
                    .orElse(null);

            if (opActiva == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("No se encontró ningún Check-In activo.");
            }

            // 🚨 CORRECCIÓN: El cálculo de salida también debe usar la hora de Perú
            LocalDateTime ahora = LocalDateTime.now(zonaPeru);

            long horasTranscurridas = java.time.Duration.between(opActiva.getFechaIngreso(), ahora).toHours();
            // Cobramos mínimo 1 hora aunque salga a los 5 minutos
            if (horasTranscurridas == 0) horasTranscurridas = 1;

            double total = calcularPrecio(hab, horasTranscurridas);
            double adelanto = opActiva.getAdelanto() != null ? opActiva.getAdelanto() : 0.0;

            Map<String, Object> response = new HashMap<>();
            response.put("huesped", opActiva.getHuesped());
            response.put("habitacion", hab);
            response.put("fechaIngreso", opActiva.getFechaIngreso().toString());
            response.put("fechaSalida", ahora.toString());
            response.put("horasTotales", horasTranscurridas);
            response.put("totalAPagar", total);
            response.put("adelanto", adelanto);
            response.put("saldoPendiente", total - adelanto);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error al calcular: " + e.getMessage());
        }
    }

    private double calcularPrecio(Habitacion hab, long horas) {
        int horasMin = hab.getHorasMinimas() != null ? hab.getHorasMinimas() : 1;
        double pMin = hab.getPrecioMinimo() != null ? hab.getPrecioMinimo() : 0.0;
        double p12h = hab.getPrecio12Horas() != null ? hab.getPrecio12Horas() : 0.0;
        double p24h = hab.getPrecio24Horas() != null ? hab.getPrecio24Horas() : 0.0;
        double pExtra = hab.getPrecioHoraExtra() != null ? hab.getPrecioHoraExtra() : 0.0;

        if (horas <= horasMin) return pMin;
        if (horas <= 12) return Math.min(pMin + (horas - horasMin) * pExtra, p12h > 0 ? p12h : Double.MAX_VALUE);
        if (horas <= 24) return Math.min(p12h + (horas - 12) * pExtra, p24h > 0 ? p24h : Double.MAX_VALUE);
        return (horas / 24 * p24h) + (horas % 24 * pExtra);
    }

    @PostMapping("/{habitacionId}/check-out")
    public Habitacion realizarCheckOut(@PathVariable Long habitacionId, @RequestBody Map<String, Object> payload) {
        Double monto = Double.valueOf(payload.get("total").toString());
        String medioPago = (String) payload.get("metodoPago");
        String tiempoEstadia = (String) payload.get("tiempoUso");

        // 🚨 LA SOLUCIÓN: Buscamos la operación que estaba ACTIVA y la cerramos.
        Operacion opActiva = operacionRepository.findAll().stream()
                .filter(o -> o.getHabitacion() != null &&
                        o.getHabitacion().getId().equals(habitacionId) &&
                        "ACTIVO".equals(o.getEstado()))
                .findFirst()
                .orElse(null);

        if (opActiva != null) {
            opActiva.setEstado("FINALIZADO"); // La marcamos como cerrada
            operacionRepository.save(opActiva); // Actualizamos la base de datos
        }

        // Finalmente, liberamos la habitación y guardamos la venta
        return habitacionService.realizarCheckOut(habitacionId, monto, medioPago, tiempoEstadia);
    }
}