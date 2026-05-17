package com.hotel.sistemahotelero.seguridad;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // 🚨 ESTA ES LA LÍNEA QUE FALTA PARA EL LOGIN 🚨
    // Permite buscar al usuario por su correo electrónico
    Optional<Usuario> findByEmail(String email);

    // 🚨 ESTA ES LA LÍNEA PARA LA VINCULACIÓN CON EL HUÉSPED 🚨
    // Permite buscar al usuario por su DNI
    Optional<Usuario> findByDni(String dni);
}