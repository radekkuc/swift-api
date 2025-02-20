package com.example.swift_api.controller;

import com.example.swift_api.model.SwiftCode;
import com.example.swift_api.repository.SwiftCodeRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/swift-codes")
public class SwiftCodeController {
    private final SwiftCodeRepository swiftCodeRepository;

    @Autowired
    public SwiftCodeController(SwiftCodeRepository swiftCodeRepository){
        this.swiftCodeRepository = swiftCodeRepository;
    }

    @GetMapping("/{swiftCode}")
    public ResponseEntity<?> getSwiftCodesDetails(@PathVariable String swiftCode){
        if (swiftCode == null || swiftCode.length() < 8 || swiftCode.length() > 11) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponse("Invalid SWIFT code format"));
        }

        Optional<SwiftCode> swiftCodeOptional = swiftCodeRepository.findById(swiftCode);

        if(swiftCodeOptional.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse("SWIFT code not found"));
        }

        SwiftCode swiftCodeData = swiftCodeOptional.get();
        if(swiftCodeData.isHeadquarter()){
            List<SwiftCode> branches = swiftCodeRepository.findByHqSwiftCode(swiftCodeData.getSwiftCode());
            return ResponseEntity.ok(new HeadquartersResponse(swiftCodeData, branches));
        }
        else{
            return ResponseEntity.ok(new BranchResponse(swiftCodeData));
        }
    }

    @GetMapping("/country/{countryISO2code}")
    public ResponseEntity<?> getSwiftCodeByCountry(@PathVariable String countryISO2code){
        List<SwiftCode> swiftCodes = swiftCodeRepository.findByCountryISO2IgnoreCase(countryISO2code);

        if(swiftCodes.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse("No SWIFT codes found for the given country"));
        }

        String countryName = swiftCodes.get(0).getCountryName();

        return ResponseEntity.ok(new CountrySwiftCodeResponse(countryISO2code.toUpperCase(), countryName, swiftCodes));
    }

    @PostMapping
    public ResponseEntity<?> addSwiftCode(@Valid @RequestBody SwiftCodeRequest request){
        Optional<SwiftCode> existingSwiftCode = swiftCodeRepository.findById(request.getSwiftCode());

        if(existingSwiftCode.isPresent()){
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new MessageResponse("SWIFT code already exists in database"));
        }

        SwiftCode newSwiftCode = new SwiftCode(
                request.getSwiftCode(),
                request.getBankName(),
                request.getCountryISO2(),
                request.getCountryName(),
                request.getAddress()
        );

        swiftCodeRepository.save(newSwiftCode);
        return ResponseEntity.status(HttpStatus.CREATED).body(new MessageResponse("SWIFT code added successfully"));
    }

    @DeleteMapping("/{swiftCode}")
    @Transactional
    public ResponseEntity<?> deleteSwiftCode(@PathVariable String swiftCode){
        Optional<SwiftCode> existingSwiftCode = swiftCodeRepository.findById(swiftCode);

        if(existingSwiftCode.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse("SWIFT code not found"));
        }

        swiftCodeRepository.deleteById(swiftCode);
        return ResponseEntity.ok(new MessageResponse("SWIFT code deleted successfully"));
    }

    public static class CountrySwiftCodeResponse{
        private String countryISO2;
        private String countryName;
        private List<SwiftCodeResponse> swiftCodes;

        public CountrySwiftCodeResponse(String countryISO2, String countryName, List<SwiftCode> swiftCodeList) {
            this.countryISO2 = countryISO2;
            this.countryName = countryName;
            this.swiftCodes = swiftCodeList.stream().map(SwiftCodeResponse::new).collect(Collectors.toList());
        }

        public CountrySwiftCodeResponse(){}

        public String getCountryISO2() {
            return countryISO2;
        }

        public String getCountryName() {
            return countryName;
        }

        public List<SwiftCodeResponse> getSwiftCodes() {
            return swiftCodes;
        }

        public void setCountryISO2(String countryISO2) {
            this.countryISO2 = countryISO2;
        }

        public void setCountryName(String countryName) {
            this.countryName = countryName;
        }

        public void setSwiftCodes(List<SwiftCodeResponse> swiftCodes) {
            this.swiftCodes = swiftCodes;
        }
    }

    public static class SwiftCodeRequest{

        @NotBlank(message = "SWIFT code is required")
        @Size(min = 8, max = 11, message = "SWIFT code must be between 8 and 11 characters")
        private String swiftCode;

        @NotBlank(message = "Address is required")
        private String address;

        @NotBlank(message = "Bank name is required")
        private String bankName;

        @NotBlank(message = "Country ISO2 code is required")
        @Size(min = 2, max = 2, message = "Country ISO2 code must be exactly 2 characters")
        private String countryISO2;

        @NotBlank(message = "Country name is required")
        private String countryName;

        public String getSwiftCode() {
            return swiftCode;
        }

        public String getAddress() {
            return address;
        }

        public String getBankName() {
            return bankName;
        }

        public String getCountryISO2() {
            return countryISO2;
        }

        public String getCountryName() {
            return countryName;
        }

        public void setSwiftCode(String swiftCode) {
            this.swiftCode = swiftCode;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public void setBankName(String bankName) {
            this.bankName = bankName;
        }

        public void setCountryISO2(String countryISO2) {
            this.countryISO2 = countryISO2;
        }

        public void setCountryName(String countryName) {
            this.countryName = countryName;
        }
    }

    static class MessageResponse{
        private String message;

        public MessageResponse(String message){
            this.message = message;
        }

        public String getMessage(){
            return message;
        }
    }

    static class SwiftCodeResponse{
        private String address;
        private String bankName;
        private String countryISO2;
        private boolean isHeadquarter;
        private String swiftCode;


        public SwiftCodeResponse(SwiftCode swiftCode){
            this.address = swiftCode.getAddress();
            this.bankName = swiftCode.getBankName();
            this.countryISO2 = swiftCode.getCountryISO2();
            this.isHeadquarter = swiftCode.isHeadquarter();
            this.swiftCode = swiftCode.getSwiftCode();
        }

        public SwiftCodeResponse(){}


        public String getAddress() {
            return address;
        }

        public String getBankName() {
            return bankName;
        }

        public String getCountryISO2() {
            return countryISO2;
        }

        public boolean isHeadquarter() {
            return isHeadquarter;
        }

        public String getSwiftCode() {
            return swiftCode;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public void setBankName(String bankName) {
            this.bankName = bankName;
        }

        public void setCountryISO2(String countryISO2) {
            this.countryISO2 = countryISO2;
        }

        public void setHeadquarter(boolean headquarter) {
            isHeadquarter = headquarter;
        }

        public void setSwiftCode(String swiftCode) {
            this.swiftCode = swiftCode;
        }
    }


    static class HeadquartersResponse {
        private final String address;
        private final String bankName;
        private final String countryISO2;
        private final String countryName;
        private final String swiftCode;
        private final boolean isHeadquarter;
        private final List<BranchResponse> branches;

        public HeadquartersResponse(SwiftCode hq, List<SwiftCode> branchList){
            this.address = hq.getAddress();
            this.bankName = hq.getBankName();
            this.countryISO2 = hq.getCountryISO2();
            this.countryName = hq.getCountryName();
            this.swiftCode = hq.getSwiftCode();
            this.isHeadquarter = hq.isHeadquarter();
            this.branches = branchList.stream().map(BranchResponse::new).collect(Collectors.toList());
        }

        public String getAddress() {
            return address;
        }

        public List<BranchResponse> getBranches() {
            return branches;
        }

        public String getCountryName() {
            return countryName;
        }

        public String getCountryISO2() {
            return countryISO2;
        }

        public String getBankName() {
            return bankName;
        }

        public String getSwiftCode() {
            return swiftCode;
        }

        public boolean isHeadquarter() {
            return isHeadquarter;
        }
    }


    static class BranchResponse{
        private final String address;
        private final String bankName;
        private final String countryISO2;
        private final String countryName;
        private final String swiftCode;
        private final boolean isHeadquarter;


        public BranchResponse(SwiftCode branch){
            this.address = branch.getAddress();
            this.bankName = branch.getBankName();
            this.countryISO2 = branch.getCountryISO2();
            this.countryName = branch.getCountryName();
            this.swiftCode = branch.getSwiftCode();
            this.isHeadquarter = branch.isHeadquarter();

        }

        public String getAddress() {
            return address;
        }

        public String getBankName() {
            return bankName;
        }

        public String getCountryISO2() {
            return countryISO2;
        }

        public String getCountryName() {
            return countryName;
        }

        public boolean isHeadquarter() {
            return isHeadquarter;
        }

        public String getSwiftCode() {
            return swiftCode;
        }

        @Override
        public String toString() {
            return "BranchResponse{" +
                    "swiftCode='" + swiftCode + '\'' +
                    ", bankName='" + bankName + '\'' +
                    ", address='" + address + '\'' +
                    ", countryISO2='" + countryISO2 + '\'' +
                    ", countryName='" + countryName + '\'' +
                    ", isHeadquarter=" + isHeadquarter +
                    '}';
        }

    }
}

