package com.hotel.sistemahotelero.modules.hotel.service;

import com.hotel.sistemahotelero.modules.hotel.dto.RoomRequest;
import com.hotel.sistemahotelero.modules.hotel.repository.FloorRepository;
import com.hotel.sistemahotelero.modules.hotel.repository.HotelRepository;
import com.hotel.sistemahotelero.modules.hotel.repository.RoomRepository;
import com.hotel.sistemahotelero.security.tenant.TenantContext;
import com.hotel.sistemahotelero.shared.enums.RoomStatus;
import com.hotel.sistemahotelero.shared.exceptions.BusinessException;
import com.hotel.sistemahotelero.shared.persistence.Floor;
import com.hotel.sistemahotelero.shared.persistence.Hotel;
import com.hotel.sistemahotelero.shared.persistence.Room;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoomService {

    private final RoomRepository roomRepository;
    private final FloorRepository floorRepository;
    private final HotelRepository hotelRepository;

    public List<Room> getRoomsByFloor(Long floorId) {
        String tenantId = TenantContext.getCurrentTenant();
        return roomRepository.findByFloorAndTenant(floorId, tenantId);
    }

    @Transactional
    public Room createRoom(Long floorId, RoomRequest request) {
        String tenantId = TenantContext.getCurrentTenant();

        Floor floor = floorRepository.findByIdAndTenant(floorId, tenantId)
                .orElseThrow(() -> new BusinessException("Piso no encontrado"));

        // Verificar límite de habitaciones
        Hotel hotel = floor.getHotel();
        long currentRooms = roomRepository.countByFloorAndTenant(floorId, tenantId);
        if (currentRooms >= hotel.getUser().getMaxRooms()) {
            throw new BusinessException("Has alcanzado el límite de habitaciones de tu plan");
        }

        // Verificar que no exista habitación con el mismo número en el piso
        boolean exists = roomRepository.existsByFloorAndRoomNumberAndTenant(floorId, request.getRoomNumber(), tenantId);
        if (exists) {
            throw new BusinessException("Ya existe una habitación con ese número en este piso");
        }

        Room room = new Room();
        room.setTenantId(tenantId);
        room.setRoomNumber(request.getRoomNumber());
        room.setCategory(request.getCategory());
        room.setCapacity(request.getCapacity());
        room.setPricePerNight(request.getPricePerNight());
        room.setDescription(request.getDescription());
        room.setStatus(RoomStatus.DISPONIBLE);
        room.setFloor(floor);

        Room savedRoom = roomRepository.save(room);

        // Actualizar total de habitaciones en el hotel
        hotel.setTotalRooms(hotel.getTotalRooms() + 1);
        hotelRepository.save(hotel);

        log.info("Habitación creada en piso {} para tenant {}", floorId, tenantId);
        return savedRoom;
    }

    @Transactional
    public Room updateRoom(Long roomId, RoomRequest request) {
        String tenantId = TenantContext.getCurrentTenant();

        Room room = roomRepository.findByIdAndTenant(roomId, tenantId)
                .orElseThrow(() -> new BusinessException("Habitación no encontrada"));

        room.setRoomNumber(request.getRoomNumber());
        room.setCategory(request.getCategory());
        room.setCapacity(request.getCapacity());
        room.setPricePerNight(request.getPricePerNight());
        room.setDescription(request.getDescription());

        return roomRepository.save(room);
    }

    @Transactional
    public Room updateRoomStatus(Long roomId, RoomStatus status) {
        String tenantId = TenantContext.getCurrentTenant();

        Room room = roomRepository.findByIdAndTenant(roomId, tenantId)
                .orElseThrow(() -> new BusinessException("Habitación no encontrada"));

        room.setStatus(status);
        return roomRepository.save(room);
    }

    public List<Room> getRoomsByStatus(Long hotelId, RoomStatus status) {
        String tenantId = TenantContext.getCurrentTenant();

        Hotel hotel = hotelRepository.findByIdAndTenant(hotelId, tenantId)
                .orElseThrow(() -> new BusinessException("Hotel no encontrado"));

        return roomRepository.findByStatusAndHotelAndTenant(status, hotelId, tenantId);
    }

    @Transactional
    public void deleteRoom(Long roomId) {
        String tenantId = TenantContext.getCurrentTenant();

        Room room = roomRepository.findByIdAndTenant(roomId, tenantId)
                .orElseThrow(() -> new BusinessException("Habitación no encontrada"));

        // Verificar que no tenga reservas activas
        boolean hasActiveBookings = roomRepository.hasActiveBookings(roomId);
        if (hasActiveBookings) {
            throw new BusinessException("No se puede eliminar la habitación porque tiene reservas activas");
        }

        roomRepository.delete(room);

        // Actualizar total de habitaciones en el hotel
        Hotel hotel = room.getFloor().getHotel();
        hotel.setTotalRooms(hotel.getTotalRooms() - 1);
        hotelRepository.save(hotel);

        log.info("Habitación eliminada para tenant {}", tenantId);
    }
}
