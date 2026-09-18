package com.hotel.sistemahotelero.modules.cleaning.repository;

import com.hotel.sistemahotelero.shared.enums.CleaningStatus;
import com.hotel.sistemahotelero.shared.persistence.Cleaning;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CleaningRepository extends JpaRepository<Cleaning, Long> {

    // ==================== MÉTODOS BÁSICOS ====================

    @Query("SELECT c FROM Cleaning c WHERE c.tenantId = :tenantId AND c.enabled = true")
    List<Cleaning> findAllByTenant(@Param("tenantId") String tenantId);

    @Query("SELECT c FROM Cleaning c WHERE c.id = :id AND c.tenantId = :tenantId AND c.enabled = true")
    Optional<Cleaning> findByIdAndTenant(@Param("id") Long id, @Param("tenantId") String tenantId);

    // ==================== MÉTODOS POR HABITACIÓN ====================

    @Query("SELECT c FROM Cleaning c WHERE c.room.id = :roomId AND c.tenantId = :tenantId AND c.enabled = true")
    List<Cleaning> findByRoomAndTenant(@Param("roomId") Long roomId, @Param("tenantId") String tenantId);

    @Query("SELECT c FROM Cleaning c WHERE c.room.id = :roomId AND c.tenantId = :tenantId AND c.status = :status AND c.enabled = true")
    List<Cleaning> findByRoomAndStatusAndTenant(@Param("roomId") Long roomId,
                                                @Param("status") CleaningStatus status,
                                                @Param("tenantId") String tenantId);

    // ==================== MÉTODOS POR ESTADO ====================

    @Query("SELECT c FROM Cleaning c WHERE c.tenantId = :tenantId AND c.status = :status AND c.enabled = true")
    List<Cleaning> findByStatusAndTenant(@Param("status") CleaningStatus status,
                                         @Param("tenantId") String tenantId);

    @Query("SELECT c FROM Cleaning c WHERE c.tenantId = :tenantId AND c.status IN :statuses AND c.enabled = true")
    List<Cleaning> findByStatusInAndTenant(@Param("statuses") List<CleaningStatus> statuses,
                                           @Param("tenantId") String tenantId);

    @Query("SELECT COUNT(c) FROM Cleaning c WHERE c.tenantId = :tenantId AND c.status = :status AND c.enabled = true")
    long countByStatusAndTenant(@Param("status") CleaningStatus status,
                                @Param("tenantId") String tenantId);

    // ==================== MÉTODOS POR PERSONA ASIGNADA ====================

    @Query("SELECT c FROM Cleaning c WHERE c.tenantId = :tenantId AND c.assignedTo = :assignedTo AND c.enabled = true")
    List<Cleaning> findByAssignedToAndTenant(@Param("assignedTo") String assignedTo,
                                             @Param("tenantId") String tenantId);

    @Query("SELECT c FROM Cleaning c WHERE c.tenantId = :tenantId AND c.assignedTo = :assignedTo AND c.status = :status AND c.enabled = true")
    List<Cleaning> findByAssignedToAndStatusAndTenant(@Param("assignedTo") String assignedTo,
                                                      @Param("status") CleaningStatus status,
                                                      @Param("tenantId") String tenantId);

    @Query("SELECT c FROM Cleaning c WHERE c.tenantId = :tenantId AND c.assignedTo = :assignedTo AND c.status IN :statuses AND c.enabled = true")
    List<Cleaning> findByAssignedToAndStatusInAndTenant(@Param("assignedTo") String assignedTo,
                                                        @Param("statuses") List<CleaningStatus> statuses,
                                                        @Param("tenantId") String tenantId);

    // ==================== MÉTODOS POR FECHAS ====================

    @Query("SELECT c FROM Cleaning c WHERE c.tenantId = :tenantId AND c.scheduledDate BETWEEN :startDate AND :endDate AND c.enabled = true")
    List<Cleaning> findByDateRangeAndTenant(@Param("startDate") LocalDateTime startDate,
                                            @Param("endDate") LocalDateTime endDate,
                                            @Param("tenantId") String tenantId);

    @Query("SELECT c FROM Cleaning c WHERE c.tenantId = :tenantId AND c.scheduledDate <= :date AND c.status IN :statuses AND c.enabled = true")
    List<Cleaning> findPendingCleanings(@Param("date") LocalDateTime date,
                                        @Param("statuses") List<CleaningStatus> statuses,
                                        @Param("tenantId") String tenantId);

    @Query("SELECT c FROM Cleaning c WHERE c.tenantId = :tenantId AND c.completedDate BETWEEN :startDate AND :endDate AND c.status = :status AND c.enabled = true")
    List<Cleaning> findByCompletedDateRangeAndStatusAndTenant(@Param("startDate") LocalDateTime startDate,
                                                              @Param("endDate") LocalDateTime endDate,
                                                              @Param("status") CleaningStatus status,
                                                              @Param("tenantId") String tenantId);

    // ==================== MÉTODOS DE VERIFICACIÓN ====================

    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Cleaning c " +
            "WHERE c.room.id = :roomId AND c.tenantId = :tenantId AND c.status IN :pendingStatuses AND c.enabled = true")
    boolean existsPendingByRoomAndTenant(@Param("roomId") Long roomId,
                                         @Param("tenantId") String tenantId,
                                         @Param("pendingStatuses") List<CleaningStatus> pendingStatuses);

    default boolean existsPendingByRoomAndTenant(Long roomId, String tenantId) {
        return existsPendingByRoomAndTenant(roomId, tenantId,
                List.of(CleaningStatus.PENDIENTE, CleaningStatus.EN_PROGRESO));
    }

    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Cleaning c " +
            "WHERE c.room.id = :roomId AND c.tenantId = :tenantId AND c.status = :status AND c.enabled = true")
    boolean existsByRoomAndStatusAndTenant(@Param("roomId") Long roomId,
                                           @Param("status") CleaningStatus status,
                                           @Param("tenantId") String tenantId);

    // ==================== MÉTODOS DE CONTEO ====================

    @Query("SELECT COUNT(c) FROM Cleaning c WHERE c.tenantId = :tenantId AND c.status IN :statuses AND c.enabled = true")
    long countByStatusInAndTenant(@Param("statuses") List<CleaningStatus> statuses,
                                  @Param("tenantId") String tenantId);

    @Query("SELECT COUNT(c) FROM Cleaning c WHERE c.tenantId = :tenantId AND c.assignedTo = :assignedTo AND c.status = :status AND c.enabled = true")
    long countByAssignedToAndStatusAndTenant(@Param("assignedTo") String assignedTo,
                                             @Param("status") CleaningStatus status,
                                             @Param("tenantId") String tenantId);

    // ==================== MÉTODOS DE ACTUALIZACIÓN ====================

    @Modifying
    @Query("UPDATE Cleaning c SET c.enabled = false WHERE c.id = :id AND c.tenantId = :tenantId")
    void softDeleteByIdAndTenant(@Param("id") Long id, @Param("tenantId") String tenantId);

    @Modifying
    @Query("UPDATE Cleaning c SET c.status = :status, c.completedDate = :completedDate " +
            "WHERE c.id = :id AND c.tenantId = :tenantId")
    void updateStatusAndCompletedDate(@Param("id") Long id,
                                      @Param("status") CleaningStatus status,
                                      @Param("completedDate") LocalDateTime completedDate,
                                      @Param("tenantId") String tenantId);

    // ==================== MÉTODOS DE BÚSQUEDA AVANZADA ====================

    @Query("SELECT c FROM Cleaning c WHERE c.tenantId = :tenantId AND c.status = :status " +
            "AND c.scheduledDate <= :date AND c.enabled = true")
    List<Cleaning> findOverdueCleanings(@Param("date") LocalDateTime date,
                                        @Param("status") CleaningStatus status,
                                        @Param("tenantId") String tenantId);

    @Query("SELECT c FROM Cleaning c WHERE c.tenantId = :tenantId AND c.observations LIKE %:observation% AND c.enabled = true")
    List<Cleaning> findByObservationsContainingAndTenant(@Param("observation") String observation,
                                                         @Param("tenantId") String tenantId);

    @Query("SELECT DISTINCT c.assignedTo FROM Cleaning c WHERE c.tenantId = :tenantId AND c.assignedTo IS NOT NULL AND c.enabled = true")
    List<String> findDistinctAssignedToByTenant(@Param("tenantId") String tenantId);

    // ==================== MÉTODOS PARA REPORTES ====================

    @Query("SELECT c.status, COUNT(c) FROM Cleaning c WHERE c.tenantId = :tenantId AND c.enabled = true GROUP BY c.status")
    List<Object[]> countGroupByStatus(@Param("tenantId") String tenantId);

    @Query("SELECT FUNCTION('DATE', c.scheduledDate), COUNT(c) FROM Cleaning c " +
            "WHERE c.tenantId = :tenantId AND c.scheduledDate BETWEEN :startDate AND :endDate AND c.enabled = true " +
            "GROUP BY FUNCTION('DATE', c.scheduledDate)")
    List<Object[]> countByDateRangeGroupByDate(@Param("startDate") LocalDateTime startDate,
                                               @Param("endDate") LocalDateTime endDate,
                                               @Param("tenantId") String tenantId);
}