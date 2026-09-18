package com.hotel.sistemahotelero.modules.auth.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotel.sistemahotelero.modules.auth.dto.RucValidationResponse;
import com.hotel.sistemahotelero.shared.exceptions.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class RucValidationService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private static final String API_URL = "https://dniruc.apisperu.com/api/v1/ruc";
    private static final String TOKEN = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJlbWFpbCI6InNrdWxsdG9tNjY2QGdtYWlsLmNvbSJ9.u1nx3-MkpswiXUWEyjw1UbFSIYu2VyaUQjRE25LIYNg";

    public RucValidationResponse validateRuc(String ruc) {
        try {
            if (ruc == null || ruc.length() != 11) {
                throw new BusinessException("El RUC debe tener 11 dígitos");
            }

            String url = String.format("%s/%s?token=%s", API_URL, ruc, TOKEN);
            log.info("Validando RUC: {}", ruc);

            String response = restTemplate.getForObject(url, String.class);
            RucValidationResponse rucResponse = objectMapper.readValue(response, RucValidationResponse.class);

            if (rucResponse.getRuc() == null) {
                throw new BusinessException("RUC no encontrado en SUNAT");
            }

            log.info("RUC validado correctamente: {} - {}", ruc, rucResponse.getRazonSocial());
            return rucResponse;

        } catch (HttpClientErrorException.NotFound e) {
            throw new BusinessException("RUC no encontrado en SUNAT");
        } catch (HttpClientErrorException.TooManyRequests e) {
            throw new BusinessException("Demasiadas peticiones, intente más tarde");
        } catch (Exception e) {
            log.error("Error validando RUC: {}", e.getMessage());
            throw new BusinessException("Error al validar el RUC: " + e.getMessage());
        }
    }
}