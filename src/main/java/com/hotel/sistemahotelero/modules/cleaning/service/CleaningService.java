package com.hotel.sistemahotelero.modules.cleaning.service;

import com.hotel.sistemahotelero.modules.cleaning.dto.CleaningRequest;
import com.hotel.sistemahotelero.modules.cleaning.repository.CleaningRepository;
import com.hotel.sistemahotelero.modules.hotel.repository.RoomRepository;
import com.hotel.sistemahotelero.security.tenant.TenantContext;
import com.hotel.sistemahotelero.shared.enums.CleaningStatus;
import com.hotel.sistemahotelero.shared.enums.RoomStatus;
import com.hotel.sistemahotelero.shared.exceptions.BusinessException;
import com.hotel.sistemahotelero.shared.persistence.Cleaning;
import com.hotel.sistemahotelero.shared.persistence.Room;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CleaningService {

    private final CleaningRepository cleaningRepository;
    private final RoomRepository roomRepository;

    /**
     * Obtiene todas las tareas de limpieza del tenant actual
     */
    public List<Cleaning> getCleaningsByTenant() {
        String tenantId = TenantContext.getCurrentTenant();
        log.debug("Obteniendo tareas de limpieza para tenant: {}", tenantId);
        return cleaningRepository.findAllByTenant(tenantId);
    }

    /**
     * Obtiene una tarea de limpieza por ID
     */
    public Cleaning getCleaningById(Long cleaningId) {
        String tenantId = TenantContext.getCurrentTenant();
        return cleaningRepository.findByIdAndTenant(cleaningId, tenantId)
                .orElseThrow(() -> new BusinessException("Tarea de limpieza no encontrada"));
    }

    /**
     * Crea una nueva tarea de limpieza
     */
    @Transactional
    public Cleaning createCleaning(CleaningRequest request) {
        String tenantId = TenantContext.getCurrentTenant();

        Room room = roomRepository.findByIdAndTenant(request.getRoomId(), tenantId)
                .orElseThrow(() -> new BusinessException("Habitación no encontrada"));

        // Verificar que la habitación esté en estado de limpieza
        if (room.getStatus() != RoomStatus.LIMPIEZA) {
            throw new BusinessException("La habitación no está en estado de limpieza");
        }

        // Verificar que no haya una tarea de limpieza pendiente
        boolean hasPending = cleaningRepository.existsPendingByRoomAndTenant(request.getRoomId(), tenantId);
        if (hasPending) {
            throw new BusinessException("Ya existe una tarea de limpieza pendiente para esta habitación");
        }

        Cleaning cleaning = new Cleaning();
        cleaning.setTenantId(tenantId);
        cleaning.setRoom(room);
        cleaning.setScheduledDate(request.getScheduledDate() != null ? request.getScheduledDate() : LocalDateTime.now());
        cleaning.setStatus(CleaningStatus.PENDIENTE);
        cleaning.setAssignedTo(request.getAssignedTo());
        cleaning.setObservations(request.getObservations());

        Cleaning savedCleaning = cleaningRepository.save(cleaning);
        log.info("Tarea de limpieza creada para habitación: {}, tenant: {}",
                room.getRoomNumber(), tenantId);

        return savedCleaning;
    }

    /**
     * Crea una tarea de limpieza automática
     */
    @Transactional
    public void autoCreateCleaningForRoom(Long roomId) {
        String tenantId = TenantContext.getCurrentTenant();

        Room room = roomRepository.findByIdAndTenant(roomId, tenantId)
                .orElseThrow(() -> new BusinessException("Habitación no encontrada"));

        // Verificar si ya hay una tarea de limpieza pendiente
        boolean hasPending = cleaningRepository.existsPendingByRoomAndTenant(roomId, tenantId);
        if (hasPending) {
            log.debug("Ya existe una tarea de limpieza pendiente para la habitación: {}", roomId);
            return;
        }

        Cleaning cleaning = new Cleaning();
        cleaning.setTenantId(tenantId);
        cleaning.setRoom(room);
        cleaning.setScheduledDate(LocalDateTime.now());
        cleaning.setStatus(CleaningStatus.PENDIENTE);
        cleaning.setObservations("Limpieza automática generada después de check-out");

        cleaningRepository.save(cleaning);
        log.info("Tarea de limpieza automática creada para habitación: {}, tenant: {}",
                room.getRoomNumber(), tenantId);
    }

    /**
     * Completa una tarea de limpieza
     */
    @Transactional
    public Cleaning completeCleaning(Long cleaningId) {
        String tenantId = TenantContext.getCurrentTenant();

        Cleaning cleaning = cleaningRepository.findByIdAndTenant(cleaningId, tenantId)
                .orElseThrow(() -> new BusinessException("Tarea de limpieza no encontrada"));

        if (cleaning.getStatus() == CleaningStatus.COMPLETADO) {
            throw new BusinessException("La tarea de limpieza ya fue completada");
        }

        if (cleaning.getStatus() == CleaningStatus.CANCELADO) {
            throw new BusinessException("La tarea de limpieza fue cancelada");
        }

        cleaning.setStatus(CleaningStatus.COMPLETADO);
        cleaning.setCompletedDate(LocalDateTime.now());

        // Actualizar estado de la habitación a DISPONIBLE
        Room room = cleaning.getRoom();
        room.setStatus(RoomStatus.DISPONIBLE);
        roomRepository.save(room);

        Cleaning savedCleaning = cleaningRepository.save(cleaning);
        log.info("Limpieza completada para habitación: {}, tenant: {}",
                room.getRoomNumber(), tenantId);

        return savedCleaning;
    }

    /**
     * Cancela una tarea de limpieza
     */
    @Transactional
    public Cleaning cancelCleaning(Long cleaningId) {
        String tenantId = TenantContext.getCurrentTenant();

        Cleaning cleaning = cleaningRepository.findByIdAndTenant(cleaningId, tenantId)
                .orElseThrow(() -> new BusinessException("Tarea de limpieza no encontrada"));

        if (cleaning.getStatus() == CleaningStatus.COMPLETADO) {
            throw new BusinessException("No se puede cancelar una tarea ya completada");
        }

        cleaning.setStatus(CleaningStatus.CANCELADO);
        Cleaning savedCleaning = cleaningRepository.save(cleaning);

        log.info("Limpieza cancelada para habitación: {}, tenant: {}",
                cleaning.getRoom().getRoomNumber(), tenantId);

        return savedCleaning;
    }

    /**
     * Obtiene todas las tareas de limpieza pendientes
     */
    public List<Cleaning> getPendingCleanings() {
        String tenantId = TenantContext.getCurrentTenant();
        return cleaningRepository.findByStatusAndTenant(CleaningStatus.PENDIENTE, tenantId);
    }

    /**
     * Obtiene las tareas de limpieza de una habitación
     */
    public List<Cleaning> getCleaningsByRoom(Long roomId) {
        String tenantId = TenantContext.getCurrentTenant();
        return cleaningRepository.findByRoomAndTenant(roomId, tenantId);
    }

    /**
     * Actualiza el estado de una tarea de limpieza
     */
    @Transactional
    public Cleaning updateCleaningStatus(Long cleaningId, CleaningStatus status) {
        String tenantId = TenantContext.getCurrentTenant();

        Cleaning cleaning = cleaningRepository.findByIdAndTenant(cleaningId, tenantId)
                .orElseThrow(() -> new BusinessException("Tarea de limpieza no encontrada"));

        if (cleaning.getStatus() == CleaningStatus.COMPLETADO && status != CleaningStatus.COMPLETADO) {
            throw new BusinessException("No se puede modificar una tarea ya completada");
        }

        cleaning.setStatus(status);

        if (status == CleaningStatus.COMPLETADO) {
            cleaning.setCompletedDate(LocalDateTime.now());
            // Actualizar habitación a DISPONIBLE
            Room room = cleaning.getRoom();
            room.setStatus(RoomStatus.DISPONIBLE);
            roomRepository.save(room);
        }

        Cleaning savedCleaning = cleaningRepository.save(cleaning);
        log.info("Estado de limpieza actualizado a {} para habitación: {}, tenant: {}",
                status, cleaning.getRoom().getRoomNumber(), tenantId);

        return savedCleaning;
    }

    /**
     * Asigna una tarea de limpieza a una persona
     */
    @Transactional
    public Cleaning assignCleaning(Long cleaningId, String assignedTo) {
        String tenantId = TenantContext.getCurrentTenant();

        Cleaning cleaning = cleaningRepository.findByIdAndTenant(cleaningId, tenantId)
                .orElseThrow(() -> new BusinessException("Tarea de limpieza no encontrada"));

        cleaning.setAssignedTo(assignedTo);
        cleaning.setStatus(CleaningStatus.EN_PROGRESO);

        Cleaning savedCleaning = cleaningRepository.save(cleaning);
        log.info("Limpieza asignada a {} para habitación: {}, tenant: {}",
                assignedTo, cleaning.getRoom().getRoomNumber(), tenantId);

        return savedCleaning;
    }

    /**
     * Obtiene las tareas de limpieza asignadas a una persona
     */
    public List<Cleaning> getCleaningsAssignedTo(String assignedTo, CleaningStatus status) {
        String tenantId = TenantContext.getCurrentTenant();
        if (status != null) {
            return cleaningRepository.findByAssignedToAndStatusAndTenant(assignedTo, status, tenantId);
        }
        return cleaningRepository.findByAssignedToAndTenant(assignedTo, tenantId);
    }

    /**
     * Obtiene las tareas de limpieza atrasadas
     */
    public List<Cleaning> getOverdueCleanings() {
        String tenantId = TenantContext.getCurrentTenant();
        LocalDateTime now = LocalDateTime.now();
        List<CleaningStatus> pendingStatuses = Arrays.asList(CleaningStatus.PENDIENTE, CleaningStatus.EN_PROGRESO);

        return cleaningRepository.findPendingCleanings(now, pendingStatuses, tenantId);
    }

    /**
     * Obtiene las tareas de limpieza por rango de fechas
     */
    public List<Cleaning> getCleaningsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        String tenantId = TenantContext.getCurrentTenant();
        return cleaningRepository.findByDateRangeAndTenant(startDate, endDate, tenantId);
    }

    /**
     * Cuenta las tareas de limpieza pendientes
     */
    public long countPendingCleanings() {
        String tenantId = TenantContext.getCurrentTenant();
        return cleaningRepository.countByStatusAndTenant(CleaningStatus.PENDIENTE, tenantId);
    }

    /**
     * Actualiza una tarea de limpieza
     */
    @Transactional
    public Cleaning updateCleaning(Long cleaningId, CleaningRequest request) {
        String tenantId = TenantContext.getCurrentTenant();

        Cleaning cleaning = cleaningRepository.findByIdAndTenant(cleaningId, tenantId)
                .orElseThrow(() -> new BusinessException("Tarea de limpieza no encontrada"));

        if (cleaning.getStatus() == CleaningStatus.COMPLETADO) {
            throw new BusinessException("No se puede modificar una tarea ya completada");
        }

        if (request.getScheduledDate() != null) {
            cleaning.setScheduledDate(request.getScheduledDate());
        }
        if (request.getAssignedTo() != null) {
            cleaning.setAssignedTo(request.getAssignedTo());
        }
        if (request.getObservations() != null) {
            cleaning.setObservations(request.getObservations());
        }

        return cleaningRepository.save(cleaning);
    }

    /**
     * Elimina una tarea de limpieza (soft delete)
     */
    @Transactional
    public void deleteCleaning(Long cleaningId) {
        String tenantId = TenantContext.getCurrentTenant();

        Cleaning cleaning = cleaningRepository.findByIdAndTenant(cleaningId, tenantId)
                .orElseThrow(() -> new BusinessException("Tarea de limpieza no encontrada"));

        if (cleaning.getStatus() == CleaningStatus.EN_PROGRESO) {
            throw new BusinessException("No se puede eliminar una tarea en progreso");
        }

        cleaningRepository.softDeleteByIdAndTenant(cleaningId, tenantId);
        log.info("Tarea de limpieza eliminada: {}, tenant: {}", cleaningId, tenantId);
    }

    /**
     * Obtiene el conteo de tareas de limpieza por estado
     */
    public long countByStatus(CleaningStatus status) {
        String tenantId = TenantContext.getCurrentTenant();
        return cleaningRepository.countByStatusAndTenant(status, tenantId);
    }

    /**
     * Obtiene tareas de limpieza por estado
     */
    public List<Cleaning> getCleaningsByStatus(CleaningStatus status) {
        String tenantId = TenantContext.getCurrentTenant();
        return cleaningRepository.findByStatusAndTenant(status, tenantId);
    }

    /**
     * Obtiene tareas de limpieza completadas en un rango de fechas
     */
    public List<Cleaning> getCompletedCleaningsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        String tenantId = TenantContext.getCurrentTenant();
        return cleaningRepository.findByCompletedDateRangeAndStatusAndTenant(
                startDate, endDate, CleaningStatus.COMPLETADO, tenantId);
    }

    /**
     * Obtiene estadísticas de limpieza por estado
     */
    public List<Object[]> getCleaningStatistics() {
        String tenantId = TenantContext.getCurrentTenant();
        return cleaningRepository.countGroupByStatus(tenantId);
    }
}