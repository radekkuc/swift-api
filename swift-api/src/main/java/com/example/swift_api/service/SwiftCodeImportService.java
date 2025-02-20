package com.example.swift_api.service;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import com.example.swift_api.model.SwiftCode;
import com.example.swift_api.repository.SwiftCodeRepository;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

@Service
public class SwiftCodeImportService {
    private final SwiftCodeRepository swiftCodeRepository;

    public SwiftCodeImportService(SwiftCodeRepository swiftCodeRepository) {
        this.swiftCodeRepository = swiftCodeRepository;
    }

    public void importSwiftCodes(MultipartFile file){
        if(file.isEmpty()){
            throw new RuntimeException("Uploaded file is empty");
        }
        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)){

            Sheet sheet = workbook.getSheetAt(0);
            List<SwiftCode> swiftCodes = new ArrayList<>();
            Map<String, String> hqMap = new HashMap<>();

            for(Row row : sheet){
                if(row.getRowNum() == 0) continue;

                SwiftCode swiftCode = new SwiftCode();

                String countryISO2 = getCellValue(row, 0);
                String swiftCodeValue = getCellValue(row, 1);
                String bankName = getCellValue(row, 3);
                String address = getCellValue(row, 4);
                String countryName = getCellValue(row, 6);

                if (countryISO2 == null) countryISO2 = "UNKNOWN";
                if (swiftCodeValue == null) {
                    System.out.println("Skipping row " + row.getRowNum() + ": SWIFT code is missing.");
                    continue;
                }
                if (bankName == null) bankName = "UNKNOWN BANK";
                if (address == null) address = "UNKNOWN ADDRESS";
                if (countryName == null) countryName = "UNKNOWN COUNTRY";

                boolean isHeadquarter = swiftCodeValue.endsWith("XXX");
                swiftCode.setHeadquarter(isHeadquarter);

                if(isHeadquarter && swiftCodeValue.length() >= 8){
                    hqMap.put(swiftCodeValue.substring(0,8), swiftCodeValue);
                }

                swiftCode.setCountryISO2(countryISO2);
                swiftCode.setSwiftCode(swiftCodeValue);
                swiftCode.setBankName(bankName);
                swiftCode.setAddress(address);
                swiftCode.setCountryName(countryName);

                swiftCodes.add(swiftCode);
            }

            for(SwiftCode code : swiftCodes){
                if(!code.isHeadquarter() && code.getSwiftCode().length() >= 8){
                    String hqCode = hqMap.get(code.getSwiftCode().substring(0, 8));
                    if (hqCode != null) {
                        code.setHqSwiftCode(hqCode);
                    }
                }
            }

            swiftCodeRepository.saveAll(swiftCodes);
        } catch (IOException e) {
            throw new RuntimeException("Failed to import SWIFT codes from Excel file", e);
        }
    }

    private String getCellValue(Row row, int cellIndex) {
        if (row == null || row.getCell(cellIndex) == null) {
            return null;
        }

        Cell cell = row.getCell(cellIndex);
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                return String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return null;
        }
    }

}
