package com.hotel.sistemahotelero.modules.hotel.service;

import com.hotel.sistemahotelero.modules.auth.dto.HotelDTO;
import com.hotel.sistemahotelero.modules.auth.repository.UserRepository;
import com.hotel.sistemahotelero.modules.hotel.dto.HotelRequest;
import com.hotel.sistemahotelero.modules.hotel.dto.RegistrarHotelRequest;
import com.hotel.sistemahotelero.modules.hotel.repository.HotelRepository;
import com.hotel.sistemahotelero.security.tenant.TenantContext;
import com.hotel.sistemahotelero.shared.enums.SubscriptionPlan;
import com.hotel.sistemahotelero.shared.exceptions.BusinessException;
import com.hotel.sistemahotelero.shared.persistence.Hotel;
import com.hotel.sistemahotelero.shared.persistence.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class HotelService {

    private final HotelRepository hotelRepository;
    private final UserRepository userRepository;

    /**
     * Obtiene todos los hoteles del tenant actual
     */
    public List<Hotel> getHotelsByTenant() {
        String tenantId = TenantContext.getCurrentTenant();
        log.debug("Obteniendo hoteles para tenant: {}", tenantId);
        return hotelRepository.findAllByTenant(tenantId);
    }

    /**
     * Obtiene un hotel por ID verificando que pertenezca al tenant actual
     */
    public Hotel getHotelById(Long hotelId) {
        String tenantId = TenantContext.getCurrentTenant();
        return hotelRepository.findByIdAndTenant(hotelId, tenantId)
                .orElseThrow(() -> new BusinessException("Hotel no encontrado"));
    }

    /**
     * Crea un nuevo hotel
     */
    @Transactional
    public Hotel createHotel(HotelRequest request) {
        String tenantId = TenantContext.getCurrentTenant();

        // Verificar límite de hoteles según plan
        User user = userRepository.findByRuc(tenantId)
                .orElseThrow(() -> new BusinessException("Usuario no encontrado"));

        long currentHotels = hotelRepository.countByTenant(tenantId);
        if (currentHotels >= user.getMaxHotels()) {
            throw new BusinessException("Has alcanzado el límite de hoteles de tu plan (" + user.getMaxHotels() + ")");
        }

        Hotel hotel = new Hotel();
        hotel.setTenantId(tenantId);
        hotel.setNombre(request.getNombre());
        hotel.setDireccion(request.getDireccion());
        hotel.setTelefono(request.getTelefono());
        hotel.setEmail(request.getEmail());
        hotel.setWebsite(request.getWebsite());
        hotel.setUser(user);
        hotel.setTotalRooms(0);
        hotel.setTotalFloors(0);
        hotel.setUbicacionLat(request.getUbicacionLat());
        hotel.setUbicacionLng(request.getUbicacionLng());
        hotel.setHorarioApertura(request.getHorarioApertura());
        hotel.setHorarioCierre(request.getHorarioCierre());
        hotel.setDescripcion(request.getDescripcion());

        Hotel savedHotel = hotelRepository.save(hotel);
        log.info("Hotel creado para tenant: {}, hotel: {}", tenantId, savedHotel.getNombre());

        return savedHotel;
    }

    /**
     * Actualiza un hotel existente
     */
    @Transactional
    public Hotel updateHotel(Long hotelId, HotelRequest request) {
        String tenantId = TenantContext.getCurrentTenant();

        Hotel hotel = hotelRepository.findByIdAndTenant(hotelId, tenantId)
                .orElseThrow(() -> new BusinessException("Hotel no encontrado"));

        // Actualizar campos
        hotel.setNombre(request.getNombre());
        hotel.setDireccion(request.getDireccion());
        hotel.setTelefono(request.getTelefono());
        hotel.setEmail(request.getEmail());
        hotel.setWebsite(request.getWebsite());
        hotel.setUbicacionLat(request.getUbicacionLat());
        hotel.setUbicacionLng(request.getUbicacionLng());
        hotel.setHorarioApertura(request.getHorarioApertura());
        hotel.setHorarioCierre(request.getHorarioCierre());
        hotel.setDescripcion(request.getDescripcion());

        Hotel updatedHotel = hotelRepository.save(hotel);
        log.info("Hotel actualizado: {}, tenant: {}", updatedHotel.getNombre(), tenantId);

        return updatedHotel;
    }

    /**
     * Elimina un hotel (soft delete)
     */
    @Transactional
    public void deleteHotel(Long hotelId) {
        String tenantId = TenantContext.getCurrentTenant();

        Hotel hotel = hotelRepository.findByIdAndTenant(hotelId, tenantId)
                .orElseThrow(() -> new BusinessException("Hotel no encontrado"));

        // Verificar que no tenga pisos o habitaciones
        if (!hotel.getFloors().isEmpty()) {
            throw new BusinessException("No se puede eliminar el hotel porque tiene pisos asociados");
        }

        hotelRepository.softDeleteByIdAndTenant(hotelId, tenantId);
        log.info("Hotel eliminado: {}, tenant: {}", hotel.getNombre(), tenantId);
    }

    /**
     * Obtiene el conteo de hoteles del tenant actual
     */
    public long countHotelsByTenant() {
        String tenantId = TenantContext.getCurrentTenant();
        return hotelRepository.countByTenant(tenantId);
    }

    /**
     * Verifica si un hotel pertenece al tenant actual
     */
    public boolean existsByIdAndTenant(Long hotelId) {
        String tenantId = TenantContext.getCurrentTenant();
        return hotelRepository.existsByIdAndTenant(hotelId, tenantId);
    }

    /**
     * Obtiene todos los hoteles con sus pisos y habitaciones
     */
    public List<Hotel> getHotelsWithDetails() {
        String tenantId = TenantContext.getCurrentTenant();
        return hotelRepository.findAllWithDetailsByTenant(tenantId);
    }

    /**
     * Actualiza el conteo de pisos y habitaciones de un hotel
     */
    @Transactional
    public void updateHotelCounts(Long hotelId) {
        String tenantId = TenantContext.getCurrentTenant();

        Hotel hotel = hotelRepository.findByIdAndTenant(hotelId, tenantId)
                .orElseThrow(() -> new BusinessException("Hotel no encontrado"));

        int totalFloors = hotel.getFloors().size();
        int totalRooms = hotel.getFloors().stream()
                .mapToInt(floor -> floor.getRooms().size())
                .sum();

        hotel.setTotalFloors(totalFloors);
        hotel.setTotalRooms(totalRooms);
        hotelRepository.save(hotel);

        log.info("Conteos actualizados para hotel: {}, pisos: {}, habitaciones: {}",
                hotel.getNombre(), totalFloors, totalRooms);
    }

    /**
     * Obtiene los hoteles del usuario actual (contrato frontend: /api/hoteles/mio)
     */
    public List<HotelDTO> hotelesDelUsuario() {
        String tenantId = TenantContext.getCurrentTenant();
        User user = userRepository.findByRuc(tenantId)
                .orElseThrow(() -> new BusinessException("Usuario no encontrado"));

        return hotelRepository.findAllByTenant(tenantId).stream()
                .map(h -> HotelDTO.of(h, user.getSubscriptionPlan()))
                .toList();
    }

    /**
     * Crea un hotel desde el formulario frontend (contrato: /api/hoteles/registrar)
     */
    @Transactional
    public HotelDTO crearHotelFront(RegistrarHotelRequest request) {
        String tenantId = TenantContext.getCurrentTenant();

        User user = userRepository.findByRuc(tenantId)
                .orElseThrow(() -> new BusinessException("Usuario no encontrado"));

        long currentHotels = hotelRepository.countByTenant(tenantId);
        if (currentHotels >= user.getMaxHotels()) {
            throw new BusinessException("Has alcanzado el límite de hoteles de tu plan (" + user.getMaxHotels() + ")");
        }

        // Si el frontend mandó un plan (suscripción), aplicarlo
        SubscriptionPlan plan = HotelDTO.mapPlanFromFront(request.getPlan());
        if (plan != user.getSubscriptionPlan()) {
            user.setSubscriptionPlan(plan);
            user.setMaxHotels(plan.getMaxHotels());
            user.setMaxFloors(plan.getMaxFloors());
            user.setMaxRooms(plan.getMaxRooms());
            userRepository.save(user);
        }

        Hotel hotel = new Hotel();
        hotel.setTenantId(tenantId);
        hotel.setNombre(request.getNombre());
        hotel.setDireccion(request.getDireccion());
        hotel.setTelefono(request.getTelefono());
        hotel.setUser(user);
        hotel.setTotalRooms(0);
        hotel.setTotalFloors(0);

        Hotel savedHotel = hotelRepository.save(hotel);
        log.info("Hotel registrado por frontend: {}, tenant: {}", savedHotel.getNombre(), tenantId);

        return HotelDTO.of(savedHotel, user.getSubscriptionPlan());
    }

    /**
     * Busca hoteles por nombre
     */
    public List<Hotel> searchHotelsByName(String nombre) {
        String tenantId = TenantContext.getCurrentTenant();
        return hotelRepository.findByNombreContainingAndTenant(nombre, tenantId);
    }
}