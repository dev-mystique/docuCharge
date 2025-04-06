package com.example.mssqll.service;

import com.example.mssqll.dto.response.ConnectionFeeChildrenDTO;
import com.example.mssqll.models.ConnectionFee;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PagedModel;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;


public interface ConnectionFeeService {

    PagedModel<ConnectionFee> getAllFee(int page, int size);

    Optional<ConnectionFee> getFee(Long id);

    List<ConnectionFee> saveFee(Long extractionTask);

    ConnectionFee save(ConnectionFee connectionFee);

    Optional<ConnectionFee> findById(Long id);

    ConnectionFee updateFee(Long connectionFeeId, ConnectionFee connectionFeeDetails);

    PagedModel<?> letDoFilter(Specification<ConnectionFee> spec, int page, int size, String sortBy, String sortDir);

    void deleteByTaskId(Long taskId);

    void softDeleteById(Long id);

    ByteArrayInputStream createExcel(List<ConnectionFee> connectionFees) throws IOException;

    void divideFee(Long feeId, Double[] arr);

    List<ConnectionFeeChildrenDTO> getFeesByParent(Long id);

    List<ConnectionFee> getDownloadDataBySpec(Specification<ConnectionFee> spec);

    Integer uploadHistory(MultipartFile file) throws IOException;
}