package com.example.swift_api.repository;

import com.example.swift_api.model.SwiftCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SwiftCodeRepository extends JpaRepository<SwiftCode, String> {
    //SELECT * FROM swift_codes WHERE hq_swift_code = :hqSwiftCode;
    List<SwiftCode> findByHqSwiftCode(String hqSwiftCode);

    //SELECT * FROM swift_codes WHERE UPPER(countryISO2) = UPPER(:countryISO2);
    List<SwiftCode> findByCountryISO2IgnoreCase(String countryISO2);

}
