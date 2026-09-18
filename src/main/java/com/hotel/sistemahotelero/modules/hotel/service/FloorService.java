package com.hotel.sistemahotelero.modules.hotel.service;


import com.hotel.sistemahotelero.modules.hotel.dto.FloorRequest;
import com.hotel.sistemahotelero.modules.hotel.repository.FloorRepository;
import com.hotel.sistemahotelero.modules.hotel.repository.HotelRepository;
import com.hotel.sistemahotelero.security.tenant.TenantContext;
import com.hotel.sistemahotelero.shared.exceptions.BusinessException;
import com.hotel.sistemahotelero.shared.persistence.Floor;
import com.hotel.sistemahotelero.shared.persistence.Hotel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FloorService {

    private final FloorRepository floorRepository;
    private final HotelRepository hotelRepository;

    public List<Floor> getFloorsByHotel(Long hotelId) {
        String tenantId = TenantContext.getCurrentTenant();
        return floorRepository.findByHotelAndTenant(hotelId, tenantId);
    }

    @Transactional
    public Floor createFloor(Long hotelId, FloorRequest request) {
        String tenantId = TenantContext.getCurrentTenant();

        Hotel hotel = hotelRepository.findByIdAndTenant(hotelId, tenantId)
                .orElseThrow(() -> new BusinessException("Hotel no encontrado"));

        Floor floor = new Floor();
        floor.setTenantId(tenantId);
        floor.setFloorNumber(request.getFloorNumber());
        floor.setName(request.getName());
        floor.setDescription(request.getDescription());
        floor.setHotel(hotel);

        Floor savedFloor = floorRepository.save(floor);

        // Actualizar total de pisos en el hotel
        hotel.setTotalFloors(hotel.getTotalFloors() + 1);
        hotelRepository.save(hotel);

        log.info("Piso creado en hotel {} para tenant {}", hotelId, tenantId);
        return savedFloor;
    }

    @Transactional
    public Floor updateFloor(Long hotelId, Long floorId, FloorRequest request) {
        String tenantId = TenantContext.getCurrentTenant();

        Floor floor = floorRepository.findByIdAndTenant(floorId, tenantId)
                .orElseThrow(() -> new BusinessException("Piso no encontrado"));

        if (!floor.getHotel().getId().equals(hotelId)) {
            throw new BusinessException("El piso no pertenece al hotel especificado");
        }

        floor.setFloorNumber(request.getFloorNumber());
        floor.setName(request.getName());
        floor.setDescription(request.getDescription());

        return floorRepository.save(floor);
    }

    @Transactional
    public void deleteFloor(Long hotelId, Long floorId) {
        String tenantId = TenantContext.getCurrentTenant();

        Floor floor = floorRepository.findByIdAndTenant(floorId, tenantId)
                .orElseThrow(() -> new BusinessException("Piso no encontrado"));

        if (!floor.getHotel().getId().equals(hotelId)) {
            throw new BusinessException("El piso no pertenece al hotel especificado");
        }

        // Verificar que no tenga habitaciones
        if (!floor.getRooms().isEmpty()) {
            throw new BusinessException("No se puede eliminar el piso porque tiene habitaciones asociadas");
        }

        floorRepository.delete(floor);

        // Actualizar total de pisos en el hotel
        Hotel hotel = floor.getHotel();
        hotel.setTotalFloors(hotel.getTotalFloors() - 1);
        hotelRepository.save(hotel);

        log.info("Piso eliminado para tenant {}", tenantId);
    }
}
