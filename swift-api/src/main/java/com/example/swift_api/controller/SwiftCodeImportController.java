package com.example.swift_api.controller;


import com.example.swift_api.service.SwiftCodeImportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/v1/swift-codes/import")
public class SwiftCodeImportController {

    private final SwiftCodeImportService importService;

    public SwiftCodeImportController(SwiftCodeImportService importService) {
        this.importService = importService;
    }

    @PostMapping
    public ResponseEntity<String> uploadFile(@RequestParam("file")MultipartFile file){
        if(file.isEmpty()){
            return ResponseEntity.badRequest().body("Please upload valid excel file");
        }
        importService.importSwiftCodes(file);
        return ResponseEntity.ok("File uploaded and processed successfully");
    }


}
