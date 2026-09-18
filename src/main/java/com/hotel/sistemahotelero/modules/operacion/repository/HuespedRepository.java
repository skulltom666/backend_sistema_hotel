package com.hotel.sistemahotelero.modules.operacion.repository;

import com.hotel.sistemahotelero.shared.persistence.Huesped;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HuespedRepository extends JpaRepository<Huesped, Long> {

    Optional<Huesped> findByDniAndTenantId(String dni, String tenantId);

    Optional<Huesped> findByUserIdAndTenantId(Long userId, String tenantId);

    List<Huesped> findAllByTenantId(String tenantId);
}