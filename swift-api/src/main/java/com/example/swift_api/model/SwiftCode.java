package com.example.swift_api.model;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "swift_codes")
public class SwiftCode {

    @Id
    private String swiftCode;

    @Column(nullable = false)
    private String bankName;

    @Column(nullable = false)
    private String countryISO2;

    @Column(nullable = false)
    private String countryName;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private boolean isHeadquarter;

    @Column(name = "hq_swift_code")
    private String hqSwiftCode;

    public SwiftCode() {}

    public SwiftCode(String swiftCode, String bankName, String countryISO2,  String countryName, String address){
        this.swiftCode = swiftCode;
        this.bankName = bankName;
        this.countryISO2 = countryISO2;
        this.countryName = countryName;
        this.address = address;
        this.isHeadquarter = swiftCode.endsWith("XXX");
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getSwiftCode() {
        return swiftCode;
    }

    public void setSwiftCode(String swiftCode) {
        this.swiftCode = swiftCode;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public String getCountryISO2() {
        return countryISO2;
    }

    public void setCountryISO2(String countryISO2) {
        this.countryISO2 = countryISO2;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public boolean isHeadquarter() {
        return isHeadquarter;
    }

    public void setHeadquarter(boolean headquarter) {
        isHeadquarter = headquarter;
    }

    public String getHqSwiftCode() {
        return hqSwiftCode;
    }

    public void setHqSwiftCode(String hqSwiftCode) {
        this.hqSwiftCode = hqSwiftCode;
    }

}
