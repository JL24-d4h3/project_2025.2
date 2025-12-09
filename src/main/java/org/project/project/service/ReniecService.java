package org.project.project.service;

import org.project.project.model.dto.ReniecResponseDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;

/**
 * Servicio para integración con API de RENIEC (Decolecta)
 * Permite validar DNIs y obtener datos oficiales de ciudadanos peruanos
 */
@Service
public class ReniecService {
    
    private final RestTemplate restTemplate;
    
    @Value("${reniec.api.url}")
    private String apiUrl;
    
    @Value("${reniec.api.token}")
    private String apiToken;
    
    public ReniecService() {
        this.restTemplate = new RestTemplate();
    }
    
    /**
     * Consulta datos de una persona en RENIEC usando su DNI
     * @param dni - DNI de 8 dígitos numéricos
     * @return ReniecResponseDTO con datos oficiales, o null si el DNI no existe o hay error
     */
    public ReniecResponseDTO consultarDni(String dni) {
        try {
            // 1. Validar formato DNI (8 dígitos numéricos)
            if (dni == null || !dni.matches("\\d{8}")) {
                System.out.println("❌ [RENIEC] Formato de DNI inválido: " + dni);
                return null;
            }
            
            // 2. Construir URL de la API
            String url = apiUrl + "?numero=" + dni;
            System.out.println("🔍 [RENIEC] Consultando API: " + url);
            
            // 3. Configurar headers con autenticación Bearer
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/json");
            headers.set("Authorization", "Bearer " + apiToken);
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            // 4. Realizar petición GET a la API de RENIEC
            ResponseEntity<ReniecResponseDTO> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                ReniecResponseDTO.class
            );
            
            // 5. Verificar respuesta exitosa
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                ReniecResponseDTO data = response.getBody();
                System.out.println("✅ [RENIEC] DNI encontrado: " + dni + " - " + data.getFullName());
                return data;
            }
            
            System.out.println("⚠️ [RENIEC] Respuesta vacía para DNI: " + dni);
            return null;
            
        } catch (HttpClientErrorException.NotFound e) {
            System.out.println("❌ [RENIEC] DNI NO ENCONTRADO en RENIEC: " + dni);
            return null;
            
        } catch (HttpClientErrorException.Unauthorized e) {
            System.err.println("❌ [RENIEC] Token inválido o expirado. Verifica la configuración.");
            return null;
            
        } catch (ResourceAccessException e) {
            System.err.println("❌ [RENIEC] Timeout o error de conexión con la API: " + e.getMessage());
            return null;
            
        } catch (Exception e) {
            System.err.println("❌ [RENIEC] Error inesperado consultando API: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
