package com.hotel.sistemahotelero;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SistemahoteleroApplication {

	public static void main(String[] args) {
		SpringApplication.run(SistemahoteleroApplication.class, args);
	}

	// Se eliminó el Bean iniciarDatosPrueba ya que el registro
	// de habitaciones ahora se hace de forma dinámica desde el sistema.
}