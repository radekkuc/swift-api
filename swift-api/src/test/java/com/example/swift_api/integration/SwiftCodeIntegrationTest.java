package com.example.swift_api.integration;


import com.example.swift_api.controller.SwiftCodeController;
import com.example.swift_api.model.SwiftCode;
import com.example.swift_api.repository.SwiftCodeRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.coyote.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;


import static org.junit.jupiter.api.Assertions.*;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;
import java.util.Objects;


@ExtendWith(org.springframework.test.context.junit.jupiter.SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SwiftCodeIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private SwiftCodeRepository swiftCodeRepository;

    private String baseUrl;

    @BeforeEach
    void setup(){
        baseUrl = "http://localhost:" + port + "/v1/swift-codes";
        swiftCodeRepository.deleteAll();
    }

    @Test
    void testAddAndRetrieveValidSwiftCode() throws JsonProcessingException {
        SwiftCodeController.SwiftCodeRequest request = new SwiftCodeController.SwiftCodeRequest();
        request.setSwiftCode("TEST1234");
        request.setAddress("123 Test St");
        request.setBankName("Test Bank");
        request.setCountryISO2("US");
        request.setCountryName("United States");


        ResponseEntity<String> postResponse = restTemplate.postForEntity(baseUrl, request, String.class);
        ObjectMapper objectMapper = new ObjectMapper();
        String actualMessage = objectMapper.readTree(postResponse.getBody()).get("message").asText();
        assertEquals(HttpStatus.CREATED, postResponse.getStatusCode());

        ResponseEntity<SwiftCode> getResponse = restTemplate.getForEntity(baseUrl + "/TEST1234", SwiftCode.class);
        assertEquals("SWIFT code added successfully", actualMessage);
        assertEquals(HttpStatus.OK, getResponse.getStatusCode());

        SwiftCode found = getResponse.getBody();
        assertNotNull(found);
        assertEquals("Test Bank", found.getBankName());
    }

    @Test
    void testGetNonExistentSwiftCode() throws JsonProcessingException {
        ResponseEntity<String> response = restTemplate.getForEntity(baseUrl + "/INVALID123", String.class);

        ObjectMapper objectMapper = new ObjectMapper();
        String actualMessage = objectMapper.readTree(response.getBody()).get("message").asText();
        assertEquals("SWIFT code not found", actualMessage);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testGetSwiftCodeByCountry() {
        swiftCodeRepository.saveAll(List.of(
                new SwiftCode("BANKUS1", "Bank 1", "US", "United States", "123 St"),
                new SwiftCode("BANKUS2", "Bank 2", "US", "United States", "456 St"),
                new SwiftCode("BANKGB1", "Bank UK", "GB", "United Kingdom", "789 St")
        ));

        ResponseEntity<SwiftCodeController.CountrySwiftCodeResponse> response =
                restTemplate.getForEntity(baseUrl + "/country/US", SwiftCodeController.CountrySwiftCodeResponse.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        SwiftCodeController.CountrySwiftCodeResponse countryResponse = response.getBody();
        assertNotNull(countryResponse);
        assertEquals("US", countryResponse.getCountryISO2());
        assertEquals("United States", countryResponse.getCountryName());
        assertEquals(2, countryResponse.getSwiftCodes().size());
    }

    @Test
    void testAddDuplicateSwiftCode() throws JsonProcessingException {
        SwiftCodeController.SwiftCodeRequest request = new SwiftCodeController.SwiftCodeRequest();
        request.setSwiftCode("DUPLUS33XXX");
        request.setAddress("456 Duplicate St");
        request.setBankName("Duplicate Bank");
        request.setCountryISO2("US");
        request.setCountryName("United States");

        ResponseEntity<String> firstResponse = restTemplate.postForEntity(baseUrl, request, String.class);
        assertEquals(HttpStatus.CREATED, firstResponse.getStatusCode());

        ResponseEntity<String> secondResponse = restTemplate.postForEntity(baseUrl, request, String.class);

        ObjectMapper objectMapper = new ObjectMapper();
        String actualMessage = objectMapper.readTree(secondResponse.getBody()).get("message").asText();

        assertEquals(HttpStatus.CONFLICT, secondResponse.getStatusCode());
        assertEquals("SWIFT code already exists in database", actualMessage);

    }

    @Test
    void testAddInvalidSwiftCodeFormat() throws JsonProcessingException {
        SwiftCodeController.SwiftCodeRequest request = new SwiftCodeController.SwiftCodeRequest();
        request.setSwiftCode("1234INVALIDSWIFTCODEFORMAT");
        request.setAddress("789 Invalid St");
        request.setBankName("Invalid Bank");
        request.setCountryISO2("US");
        request.setCountryName("United States");

        ResponseEntity<String> response = restTemplate.postForEntity(baseUrl, request, String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testAddSwiftCodeWithMissingFields() throws JsonProcessingException {
        SwiftCodeController.SwiftCodeRequest request = new SwiftCodeController.SwiftCodeRequest();
        request.setSwiftCode("MISSINGUS33");

        ResponseEntity<String> response = restTemplate.postForEntity(baseUrl, request, String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testDeleteExistingSwiftCode() throws JsonProcessingException {
        SwiftCode swiftCode = new SwiftCode("DELETEME", "DeleteBank", "US",
                "United States", "123 Delete St");

        swiftCodeRepository.save(swiftCode);

        ResponseEntity<String> deleteResponse = restTemplate.exchange(baseUrl + "/DELETEME", HttpMethod.DELETE,
                null, String.class);
        ObjectMapper objectMapper = new ObjectMapper();
        String actualMessage = objectMapper.readTree(deleteResponse.getBody()).get("message").asText();
        assertEquals("SWIFT code deleted successfully", actualMessage);
        assertEquals(HttpStatus.OK, deleteResponse.getStatusCode());

        ResponseEntity<String> getResponse = restTemplate.getForEntity(baseUrl + "/DELETEME", String.class);
        assertEquals(HttpStatus.NOT_FOUND, getResponse.getStatusCode());
    }

    @Test
    void testDeleteNonExistentSwiftCode() throws JsonProcessingException {
        ResponseEntity<String> deleteResponse = restTemplate.exchange(baseUrl + "/DELETEME", HttpMethod.DELETE,
                null, String.class);

        ObjectMapper objectMapper = new ObjectMapper();
        String actualMessage = objectMapper.readTree(deleteResponse.getBody()).get("message").asText();
        assertEquals(HttpStatus.NOT_FOUND, deleteResponse.getStatusCode());
        assertEquals("SWIFT code not found", actualMessage);

    }
}
