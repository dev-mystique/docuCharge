package com.example.mssqll.controller;


import com.example.mssqll.dto.response.BusinessUnitResponseDto;
import com.example.mssqll.models.BusinessUnit;
import com.example.mssqll.service.BusinessUnitService;
import com.example.mssqll.utiles.resonse.ApiResponseUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/business-units")
public class BusinessUnitController {

    @Autowired
    private BusinessUnitService businessUnitService;

    @PostMapping
    public ApiResponseUnit<BusinessUnitResponseDto> createBusinessUnit(@RequestBody BusinessUnit businessUnit) {
        BusinessUnit savedUnit = businessUnitService.save(businessUnit);
        BusinessUnitResponseDto dto = businessUnitService.convertToDto(savedUnit);
        return ApiResponseUnit.<BusinessUnitResponseDto>builder()
                .success(true)
                .message("Business Unit created successfully")
                .data(dto)
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponseUnit<BusinessUnitResponseDto> getBusinessUnitById(@PathVariable Long id) {
        Optional<BusinessUnit> businessUnit = businessUnitService.findById(id);
        if (businessUnit.isPresent()) {
            BusinessUnitResponseDto dto = businessUnitService.convertToDto(businessUnit.get());
            return ApiResponseUnit.<BusinessUnitResponseDto>builder()
                    .success(true)
                    .message("Business Unit found")
                    .data(dto)
                    .build();
        } else {
            return ApiResponseUnit.<BusinessUnitResponseDto>builder()
                    .success(false)
                    .message("Business Unit not found")
                    .build();
        }
    }

    @GetMapping
    public ApiResponseUnit<List<BusinessUnitResponseDto>> getAllBusinessUnits() {
        List<BusinessUnit> businessUnits = businessUnitService.findAll();
        List<BusinessUnitResponseDto> dtoList = businessUnitService.convertToDtoList(businessUnits);
        return ApiResponseUnit.<List<BusinessUnitResponseDto>>builder()
                .success(true)
                .message("Business Units retrieved successfully")
                .data(dtoList)
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponseUnit<Void> deleteBusinessUnit(@PathVariable Long id) {
        businessUnitService.deleteById(id);
        return ApiResponseUnit.<Void>builder()
                .success(true)
                .message("Business Unit deleted successfully")
                .build();
    }

    @GetMapping("/by-parent/{parentId}")
    public ApiResponseUnit<List<BusinessUnit>> getBusinessUnitsByParent(@PathVariable Long parentId) {
        BusinessUnit parent = new BusinessUnit();
        parent.setId(parentId);
        List<BusinessUnit> units = businessUnitService.getBusinessUnitsByParent(parent);
        return ApiResponseUnit.<List<BusinessUnit>>builder()
                .success(true)
                .message("Business Units fetched successfully")
                .data(units)
                .build();
    }

    @GetMapping("/roots")
    public ApiResponseUnit<List<BusinessUnit>> getRootBusinessUnits() {
        List<BusinessUnit> rootUnits = businessUnitService.getRootBusinessUnits();
        return ApiResponseUnit.<List<BusinessUnit>>builder()
                .success(true)
                .message("Root Business Units fetched successfully")
                .data(rootUnits)
                .build();
    }
    @GetMapping("/unit-key/{unit_type_key}")
    public ResponseEntity<?> getByKey(@PathVariable Integer unit_type_key) {
        return ResponseEntity.ok().body(businessUnitService.getBykey(unit_type_key));
    }

}