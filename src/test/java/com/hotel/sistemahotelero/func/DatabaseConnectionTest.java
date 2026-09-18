package com.hotel.sistemahotelero.func;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas funcionales - Conexion a la base de datos.
 *
 * Verifican que el DataSource del entorno de pruebas apunte a H2 en memoria, que la
 * conexion sea valida, que el esquema creado por JPA contenga las tablas del dominio y
 * que la base soporte operaciones reales de escritura y lectura con SQL nativo.
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Funcional - Conexion a la base de datos")
public class DatabaseConnectionTest {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private Environment environment;

    @Test
    @DisplayName("Abre una conexion valida al DataSource de pruebas")
    public void testDatabaseConnection() throws Exception {
        System.out.println("URL configurada: " + environment.getProperty("spring.datasource.url"));

        try (Connection conn = dataSource.getConnection()) {
            assertNotNull(conn, "La conexion no debe ser nula");
            assertFalse(conn.isClosed(), "La conexion debe estar abierta");
            System.out.println("Conexión exitosa a la base de datos de pruebas");
            System.out.println("JDBC URL: " + conn.getMetaData().getURL());
        }
    }

    @Test
    @DisplayName("La base de datos de pruebas es H2 en memoria")
    void laBaseDeDatosDePruebas_esH2EnMemoria() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData md = conn.getMetaData();

            String producto = md.getDatabaseProductName();
            assertEquals("H2", producto, "El gestor de pruebas debe ser H2");
            assertTrue(md.getURL().startsWith("jdbc:h2:mem:"),
                    "Debe estar en modo memoria. URL real: " + md.getURL());
            assertTrue(md.supportsTransactions(), "H2 debe soportar transacciones");
        }
    }

    @Test
    @DisplayName("Contiene todas las tablas del dominio")
    void elEsquema_contieneLasTablasDelDominio() throws SQLException {
        String[] tablasEsperadas = {
                "users", "hotels", "floors", "rooms",
                "bookings", "huespedes", "movimientos_historicos", "cleanings"
        };

        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData md = conn.getMetaData();
            for (String tabla : tablasEsperadas) {
                try (ResultSet rs = md.getTables(null, "PUBLIC", tabla.toUpperCase(),
                        new String[]{"TABLE"})) {
                    assertTrue(rs.next(),
                            "Debe existir la tabla PUBLIC." + tabla.toUpperCase()
                                    + " en el esquema de pruebas");
                }
            }
        }
    }

    @Test
    @DisplayName("Lectura y escritura directa sobre SQL nativo")
    void lecturaYEscritura_directaSobreSQLNativo() throws SQLException {
        String ruc = "20444444444";
        String email = "sql@test.com";

        // ESCRITURA: insertar una fila real en la tabla users mediante SQL nativo
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement insert = conn.prepareStatement(
                    "INSERT INTO users (ruc, email, password, nombre, rol, tenant_id, enabled, created_at, updated_at) "
                            + "VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)")) {
                insert.setString(1, ruc);
                insert.setString(2, email);
                insert.setString(3, "hash");
                insert.setString(4, "Usuario SQL");
                insert.setString(5, "ADMINISTRADOR");
                insert.setString(6, ruc);
                insert.setBoolean(7, true);
                int filas = insert.executeUpdate();
                assertEquals(1, filas, "Debe insertar exactamente 1 fila");
            }

            // LECTURA: verificar que la fila queda persistida y es consultable
            try (PreparedStatement select = conn.prepareStatement(
                    "SELECT email, enabled FROM users WHERE ruc = ?")) {
                select.setString(1, ruc);
                try (ResultSet rs = select.executeQuery()) {
                    assertTrue(rs.next(), "Debe devolver la fila recien insertada");
                    assertEquals(email, rs.getString(1));
                    assertTrue(rs.getBoolean(2));
                }
            }
            conn.rollback();
        }

        // LIMPIEZA: eliminar el dato de prueba para no contaminar el resto de suites
        try (Connection conn = dataSource.getConnection();
             PreparedStatement delete = conn.prepareStatement("DELETE FROM users WHERE ruc = ?")) {
            delete.setString(1, ruc);
            delete.executeUpdate();
        }

        System.out.println("Lectura/escritura nativa sobre H2: OK");
    }
}