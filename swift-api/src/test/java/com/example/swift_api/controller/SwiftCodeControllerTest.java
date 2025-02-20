package com.example.swift_api.controller;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import com.example.swift_api.model.SwiftCode;
import com.example.swift_api.repository.SwiftCodeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SwiftCodeControllerTest {

    @Mock
    private SwiftCodeRepository swiftCodeRepository;  //Mocked database

    @InjectMocks
    private SwiftCodeController swiftCodeController; //Controller being tested

    @Test
    void testGetSwiftCodeDetails_HQ_Success(){
        SwiftCode mockSwiftCode = new SwiftCode("HQTESTXXX", "HQ Bank", "US",
                "United States", "123 HQ St");

        when(swiftCodeRepository.findById("HQTESTXXX")).thenReturn(Optional.of(mockSwiftCode));
        when(swiftCodeRepository.findByHqSwiftCode("HQTESTXXX")).thenReturn(List.of(
                new SwiftCode("BRANCH1", "Branch 1", "US",
                        "United States", "456 Branch St")));

        ResponseEntity<?> response = swiftCodeController.getSwiftCodesDetails("HQTESTXXX");

        SwiftCodeController.HeadquartersResponse responseBody = (SwiftCodeController.HeadquartersResponse) response.getBody();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(responseBody);
        assertEquals("HQTESTXXX", responseBody.getSwiftCode()); //Check HQ SWIFT code
        assertFalse(responseBody.getBranches().isEmpty()); //Ensure there are branches
        assertEquals("BRANCH1", responseBody.getBranches().get(0).getSwiftCode());

    }

    //SWIFT code does not exist (404 Not Found)
    @Test
    void testGetSwiftCodeDetails_NotFound(){

        when(swiftCodeRepository.findById("UNKNOWN123")).thenReturn(Optional.empty());

        ResponseEntity<?> response = swiftCodeController.getSwiftCodesDetails("UNKNOWN123");

        SwiftCodeController.MessageResponse responseBody = (SwiftCodeController.MessageResponse) response.getBody();
        assert responseBody != null;
        assertEquals("SWIFT code not found", responseBody.getMessage());
    }

    @Test
    void testGetSwiftCodeDetails_InvalidFormat() {

        String[] invalidSwiftCodes = {"SHORT", "TOOLONGSWIFTCODE", "", null};

        for (String invalidCode : invalidSwiftCodes) {
            ResponseEntity<?> response = swiftCodeController.getSwiftCodesDetails(invalidCode);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(),
                    "Expected 400 BAD REQUEST for invalid SWIFT code: " + invalidCode);

            SwiftCodeController.MessageResponse responseBody = (SwiftCodeController.MessageResponse) response.getBody();
            assertNotNull(responseBody);
            assertEquals("Invalid SWIFT code format", responseBody.getMessage(),
                    "Unexpected message for SWIFT code: " + invalidCode);
        }
    }


    @Test
    void testGetSwiftCodeDetails_Branch_Success() {

        SwiftCode mockBranchSwiftCode = new SwiftCode("BRANCH123", "Branch Bank", "US",
                "United States", "456 Branch St");

        when(swiftCodeRepository.findById("BRANCH123")).thenReturn(Optional.of(mockBranchSwiftCode));

        ResponseEntity<?> response = swiftCodeController.getSwiftCodesDetails("BRANCH123");

        if (!(response.getBody() instanceof SwiftCodeController.BranchResponse)) {
            fail("Expected a BranchResponse, but got: " + Objects.requireNonNull(response.getBody()).getClass().getName());
        }

        SwiftCodeController.BranchResponse responseBody =
                (SwiftCodeController.BranchResponse) response.getBody();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("BRANCH123", responseBody.getSwiftCode());
        assertEquals("Branch Bank", responseBody.getBankName());
        assertEquals("456 Branch St", responseBody.getAddress());
    }

    @Test
    void testAddSwiftCode_Success(){
        when(swiftCodeRepository.findById("NEW123")).thenReturn(Optional.empty());

        SwiftCodeController.SwiftCodeRequest request = new SwiftCodeController.SwiftCodeRequest();
        request.setSwiftCode("NEW123");
        request.setBankName("New Bank");
        request.setCountryISO2("GB");
        request.setCountryName("United Kingdom");
        request.setAddress("123 Test St");

        ResponseEntity<?> response = swiftCodeController.addSwiftCode(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(swiftCodeRepository, times(1)).save(any(SwiftCode.class));

        SwiftCodeController.MessageResponse responseBody = (SwiftCodeController.MessageResponse) response.getBody();
        assertNotNull(responseBody);
        assertEquals("SWIFT code added successfully", responseBody.getMessage());
    }

    @Test
    void testAddSwiftCode_Conflict(){
        SwiftCode existingSwiftCode = new SwiftCode("DUPLICATE", "Bank", "FR",
                "France", "123 Street");

        when(swiftCodeRepository.findById("DUPLICATE")).thenReturn(Optional.of(existingSwiftCode));

        SwiftCodeController.SwiftCodeRequest request = new SwiftCodeController.SwiftCodeRequest();
        request.setSwiftCode("DUPLICATE");
        request.setBankName("Bank");
        request.setCountryISO2("FR");
        request.setCountryName("France");
        request.setAddress("123 Street");


        ResponseEntity<?> response = swiftCodeController.addSwiftCode(request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());

        verify(swiftCodeRepository, never()).save(any(SwiftCode.class));

        SwiftCodeController.MessageResponse responseBody = (SwiftCodeController.MessageResponse)response.getBody();
        assert responseBody != null;
        assertEquals("SWIFT code already exists in database", responseBody.getMessage());
    }

    @Test
    void testDeleteSwiftCode_Success(){
        SwiftCode mockSwiftCode = new SwiftCode("DELETE123", "Bank", "DE",
                "Germany", "789 Street");

        when(swiftCodeRepository.findById("DELETE123")).thenReturn(Optional.of(mockSwiftCode));

        ResponseEntity<?> response = swiftCodeController.deleteSwiftCode("DELETE123");

        assertEquals(HttpStatus.OK, response.getStatusCode());

        verify(swiftCodeRepository, times(1)).deleteById("DELETE123");

        SwiftCodeController.MessageResponse responseBody = (SwiftCodeController.MessageResponse) response.getBody();
        assert responseBody != null;
        assertEquals("SWIFT code deleted successfully", responseBody.getMessage());
    }

    @Test
    void testDeleteSwiftCode_NotFound(){
        when(swiftCodeRepository.findById("NOTEXIST")).thenReturn(Optional.empty());

        ResponseEntity<?> response = swiftCodeController.deleteSwiftCode("NOTEXIST");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        verify(swiftCodeRepository, never()).deleteById(anyString());

        SwiftCodeController.MessageResponse responseBody = (SwiftCodeController.MessageResponse) response.getBody();
        assert responseBody != null;
        assertEquals("SWIFT code not found", responseBody.getMessage());
    }

    @Test
    void testGetSwiftCodeByCountry_Success(){
        List<SwiftCode> mockSwiftCodes = List.of(
                new SwiftCode("BANKUS1", "Bank US1", "US",
                        "United States", "123 St"),
                new SwiftCode("BANKUS2", "Bank US2", "US",
                        "United States", "456 St")
        );

        when(swiftCodeRepository.findByCountryISO2IgnoreCase("US")).thenReturn(mockSwiftCodes);

        ResponseEntity<?> response = swiftCodeController.getSwiftCodeByCountry("US");

        assertEquals(HttpStatus.OK, response.getStatusCode());

        SwiftCodeController.CountrySwiftCodeResponse responseBody = (SwiftCodeController.CountrySwiftCodeResponse) response.getBody();
        assert responseBody != null;
        assertEquals(2, responseBody.getSwiftCodes().size() );
        assertEquals("BANKUS1", responseBody.getSwiftCodes().get(0).getSwiftCode());
        assertEquals("BANKUS2", responseBody.getSwiftCodes().get(1).getSwiftCode());
    }

    @Test
    void testGetSwiftCodeByCountry_NotFound(){
        when(swiftCodeRepository.findByCountryISO2IgnoreCase("XX")).thenReturn(List.of());

        ResponseEntity<?> response = swiftCodeController.getSwiftCodeByCountry("XX");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        SwiftCodeController.MessageResponse responseBody = (SwiftCodeController.MessageResponse) response.getBody();
        assert responseBody != null;
        assertEquals("No SWIFT codes found for the given country", responseBody.getMessage());
    }
}
