package com.example.swift_api.controller;

import com.example.swift_api.service.SwiftCodeImportService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SwiftCodeImportControllerTest {

    @Mock
    private SwiftCodeImportService importService;

    @InjectMocks
    private SwiftCodeImportController importController;

    @Test
    void testUploadFile_Success(){
        MultipartFile mockFile = new MockMultipartFile(
                "file", "test.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                new byte[]{1, 2, 3, 4} // Fake content
        );

        doNothing().when(importService).importSwiftCodes(mockFile);

        ResponseEntity<?> response = importController.uploadFile(mockFile);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("File uploaded and processed successfully", response.getBody());

        verify(importService, times(1)).importSwiftCodes(mockFile);
    }

    @Test
    void testUploadFile_EmptyFile() {
        MultipartFile emptyFile = new MockMultipartFile(
                "file", "empty.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                new byte[]{} // Empty content
        );

        ResponseEntity<String> response = importController.uploadFile(emptyFile);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode()); // Expecting 400 BAD REQUEST
        assertEquals("Please upload valid excel file", response.getBody());

        verify(importService, never()).importSwiftCodes(any(MultipartFile.class));
    }
}
