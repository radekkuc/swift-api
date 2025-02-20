package com.example.swift_api.service;

import com.example.swift_api.repository.SwiftCodeRepository;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SwiftCodeImportServiceTest {
    @Mock
    private SwiftCodeRepository swiftCodeRepository;

    @InjectMocks
    private SwiftCodeImportService swiftCodeImportService;

    @Test
    void testImportSwiftCodes_Success() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        XSSFWorkbook workbook = new XSSFWorkbook();
        var sheet = workbook.createSheet("SWIFT Codes");

        var headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Country ISO2");
        headerRow.createCell(1).setCellValue("SWIFT Code");
        headerRow.createCell(3).setCellValue("Bank Name");
        headerRow.createCell(4).setCellValue("Address");
        headerRow.createCell(6).setCellValue("Country Name");

        var validRow = sheet.createRow(1);
        validRow.createCell(0).setCellValue("US");
        validRow.createCell(1).setCellValue("BANKUSXX");
        validRow.createCell(3).setCellValue("US Bank");
        validRow.createCell(4).setCellValue("123 Bank St");
        validRow.createCell(6).setCellValue("United States");

        workbook.write(out);
        workbook.close();

        MultipartFile mockFile = new MockMultipartFile("file", "swift_codes_invalid.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                new ByteArrayInputStream(out.toByteArray()));

        swiftCodeImportService.importSwiftCodes(mockFile);

        verify(swiftCodeRepository, times(1)).saveAll(any(List.class));
    }

    @Test
    void testImportSwiftCodes_EmptyFile(){
        MultipartFile emptyFile = new MockMultipartFile("file", "empty.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", new byte[]{});

        Exception exception = assertThrows(RuntimeException.class, () ->
                swiftCodeImportService.importSwiftCodes(emptyFile));

        System.out.println(exception.getMessage());
        assertTrue(exception.getMessage().contains("Uploaded file is empty"));
        verify(swiftCodeRepository, never()).saveAll(any(List.class));

    }

    @Test
    void testImportSwiftCodes_SkipInvalidRows() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        XSSFWorkbook workbook = new XSSFWorkbook();
        var sheet = workbook.createSheet("SWIFT Codes");

        var headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Country ISO2");
        headerRow.createCell(1).setCellValue("SWIFT Code");
        headerRow.createCell(3).setCellValue("Bank Name");
        headerRow.createCell(4).setCellValue("Address");
        headerRow.createCell(6).setCellValue("Country Name");

        var validRow = sheet.createRow(1);
        validRow.createCell(0).setCellValue("US");
        validRow.createCell(1).setCellValue("BANKUSXX");
        validRow.createCell(3).setCellValue("US Bank");
        validRow.createCell(4).setCellValue("123 Bank St");
        validRow.createCell(6).setCellValue("United States");

        var invalidRow = sheet.createRow(2);
        invalidRow.createCell(0).setCellValue("GB");
        invalidRow.createCell(1).setCellValue(""); //SWIFT Code missing
        invalidRow.createCell(3).setCellValue("UK Bank");
        invalidRow.createCell(4).setCellValue("456 Bank St");
        invalidRow.createCell(6).setCellValue("United Kingdom");

        workbook.write(out);
        workbook.close();

        MultipartFile mockFile = new MockMultipartFile("file", "swift_codes_invalid.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                new ByteArrayInputStream(out.toByteArray()));

        swiftCodeImportService.importSwiftCodes(mockFile);

        verify(swiftCodeRepository, times(1)).saveAll(any(List.class));
    }
}
