package com.example.swift_api.repository;


import com.example.swift_api.model.SwiftCode;
import org.apache.coyote.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@TestPropertySource(locations = "classpath:application-test.properties")
class SwiftCodeRepositoryTest {

    @Autowired
    private SwiftCodeRepository swiftCodeRepository;

    @Test
    void testSaveAndFindById(){
        SwiftCode swiftCode = new SwiftCode("BANK123", "Test Bank", "US",
                "United States", "123 Test St");

        swiftCodeRepository.save(swiftCode);

        Optional<SwiftCode> found = swiftCodeRepository.findById("BANK123");
        assertTrue(found.isPresent());
        assertEquals("Test Bank", found.get().getBankName());
    }

    @Test
    void testFindByCountryISO2IgnoreCase(){
        SwiftCode swift1 = new SwiftCode("US001", "Bank USA 1", "US",
                "United States", "100 Main St");
        SwiftCode swift2 = new SwiftCode("US002", "Bank USA 2", "US",
                "United States", "200 Main St");
        SwiftCode swift3 = new SwiftCode("GB001", "Bank UK", "GB",
                "United Kingdom", "1 London St");

        swiftCodeRepository.saveAll(List.of(swift1, swift2, swift3));

        List<SwiftCode> found = swiftCodeRepository.findByCountryISO2IgnoreCase("US");

        assertEquals(2, found.size());
    }

    @Test
    void testFindByHqSwiftCode(){
        SwiftCode hqBank = new SwiftCode("HQ123", "Headquarters Bank", "US",
                "United States", "HQ St");
        SwiftCode branch1 = new SwiftCode("BRANCH1", "Branch 1", "US",
                "United States", "Branch 1 St");
        SwiftCode branch2 = new SwiftCode("BRANCH2", "Branch 2", "US",
                "United States", "Branch 2 St");

        branch1.setHqSwiftCode("HQ123");
        branch2.setHqSwiftCode("HQ123");

        swiftCodeRepository.saveAll(List.of(hqBank, branch1, branch2));

        List<SwiftCode> found = swiftCodeRepository.findByHqSwiftCode("HQ123");
        assertEquals(2, found.size());
    }

    @Test
    void testFindById_NotFound(){
        Optional<SwiftCode> result = swiftCodeRepository.findById("UNKNOWN");
        assertTrue(result.isEmpty());
    }
}


