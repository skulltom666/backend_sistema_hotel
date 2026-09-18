# Pruebas Unitarias — Tabla de Casos de Prueba

Tabla de diseño de los casos de prueba unitarios implementados en `src/test/java/com/hotel/sistemahotelero/unitarias/`.

| ID | Nombre CP | Datos de entrada | Resultado esperado |
|----|-----------|------------------|--------------------|
| CP-U01 | Check-in valido | Habitación 101 en DISPONIBLE; POST `/api/operaciones/checkin` con DNI 87654321 y adelanto 10.0 | 200 OK "Check-In registrado exitosamente."; operación ACTIVA, historial DISPONIBLE→OCUPADO, habitación OCUPADA |
| CP-U02 | Check-out valido | Habitación 1 OCUPADA con huésped; `realizarCheckOut(1, 50.0, "YAPE", "2h")` | Habitación pasa a SUCIO; huésped y `fechaCheckIn` en null; se guarda venta y se publica evento |
| CP-U03 | Check-out invalido | Habitación 1 en DISPONIBLE; `realizarCheckOut(1, 250.0, "EFECTIVO", "2h")` | Lanza `IllegalStateException`; no se guarda venta ni se publica evento |
| CP-U04 | Inicio de limpieza | Habitación 1 en SUCIO; `iniciarLimpieza(1)` | Estado pasa a EN_LIMPIEZA; se guarda y publica evento |
| CP-U05 | Limpieza invalida | Habitación 1 en DISPONIBLE; `finalizarLimpieza(1)` | Lanza `IllegalStateException`; no se guarda |
| CP-U06 | Fin de limpieza | Habitación 1 en EN_LIMPIEZA; `finalizarLimpieza(1)` | Estado pasa a DISPONIBLE; se guarda y publica evento |
| CP-U07 | Datos basicos del usuario | Usuario id=1, nombre=Ana, email=ana@hotel.com, dni=12345678, password=clave123, rol=ADMINISTRADOR | Todos los campos se guardan y devuelven igual |
| CP-U08 | Huesped que no existe devuelve 404 | `buscarHuespedPorDni("99999999")` con repositorio vacío | HTTP 404 Not Found |
| CP-U09 | Pre-checkout calcula horas, total y saldo | Habitación 1 (horasMin=1, precioMin=50, horaExtra=15), operación ACTIVA con 2 h de estadía, adelanto 10.0 | horasTotales=2, totalAPagar=65.0, adelanto=10.0, saldoPendiente=55.0 |
| CP-U10 | Pre-checkout sin operacion activa | Habitación 1 existe, sin operación ACTIVA | HTTP 400 "No se encontró ningún Check-In activo." |
| CP-U11 | Buscar huesped existente por DNI | `findByDni("87654321")` devuelve huésped Juan | HTTP 200 OK con los datos del huésped |
| CP-U12 | Generar token y extraer el usuario | Usuario `ana@hotel.com`; `generarToken(usuario)` | Token no vacío; `extractUsername(token)` = ana@hotel.com |
| CP-U13 | Token valido para el usuario correcto | Token generado para `ana@hotel.com`; `isTokenValid(token, usuario)` | true |
| CP-U14 | Token invalido para otro usuario | Token de `ana@hotel.com`; `isTokenValid(token, otro@hotel.com)` | false |
| CP-U15 | Token mal formado lanza excepcion | `extractUsername("token.invalido")` | Lanza excepción |
| CP-U16 | Datos del huesped se guardan correctamente | Huesped id=10, dni=87654321, nombres=Juan, apellidos=Perez, telefono=987654321, email=juan@correo.com | Todos los campos se guardan y devuelven igual; usuario null |
| CP-U17 | Datos del cambio de estado | `EstadoHabitacionEvent(habitacionId=5, DISPONIBLE→OCUPADO)` | getters devuelven 5, DISPONIBLE, OCUPADO y el source correcto |