package com.example.mssqll.repository;

import com.example.mssqll.models.ConnectionFee;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Repository
public class ConnectionFeeCustomRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public List<ConnectionFee[]> fetchConnectionFees(Map<String, String> filters) {
        StringBuilder sql = new StringBuilder("SELECT * FROM connection_fees cf ");
        List<String> whereClauses = new ArrayList<>();
        Map<String, Object> params = new HashMap<>();

        if (filters.containsKey("file")) {
            sql.append("JOIN extraction_task et ON cf.extraction_task_id = et.id ");
        }

        for (Map.Entry<String, String> entry : filters.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (value == null) continue;

            switch (key) {
                case "orderN":
                case "region":
                case "serviceCenter":
                case "projectID":
                case "withdrawType":
                case "purpose":
                case "description":
                case "note":
                case "tax":
                    whereClauses.add("cf." + key + " LIKE :" + key);
                    params.put(key, "%" + value + "%");
                    break;

                case "status":
                    whereClauses.add("cf.status = :status");
                    params.put("status", value);
                    break;

                case "clarificationDateStart":
                    whereClauses.add("cf.clarification_date >= :clarificationDateStart");
                    params.put("clarificationDateStart", LocalDateTime.parse(value.toString()));
                    break;

                case "clarificationDateEnd":
                    whereClauses.add("cf.clarification_date <= :clarificationDateEnd");
                    params.put("clarificationDateEnd", LocalDateTime.parse(value.toString()));
                    break;

                case "changeDateStart":
                    whereClauses.add("cf.change_date >= :changeDateStart");
                    params.put("changeDateStart", LocalDateTime.parse(value.toString()));
                    break;

                case "changeDateEnd":
                    whereClauses.add("cf.change_date <= :changeDateEnd");
                    params.put("changeDateEnd", LocalDateTime.parse(value.toString()));
                    break;

                case "transferDateStart":
                    whereClauses.add("cf.transfer_date >= :transferDateStart");
                    params.put("transferDateStart", LocalDateTime.parse(value.toString()));
                    break;

                case "transferDateEnd":
                    whereClauses.add("cf.transfer_date <= :transferDateEnd");
                    params.put("transferDateEnd", LocalDateTime.parse(value.toString()));
                    break;

                case "extractionDateStart":
                    whereClauses.add("cf.extraction_date >= :extractionDateStart");
                    params.put("extractionDateStart", LocalDate.parse(value.toString()));
                    break;

                case "extractionDateEnd":
                    whereClauses.add("cf.extraction_date <= :extractionDateEnd");
                    params.put("extractionDateEnd", LocalDate.parse(value.toString()));
                    break;

                case "totalAmountStart":
                    whereClauses.add("cf.total_amount >= :totalAmountStart");
                    params.put("totalAmountStart", Double.parseDouble(value.toString()));
                    break;

                case "totalAmountEnd":
                    whereClauses.add("cf.total_amount <= :totalAmountEnd");
                    params.put("totalAmountEnd", Double.parseDouble(value.toString()));
                    break;

                case "extractionId":
                case "history_id":
                    whereClauses.add("cf." + key + " = :" + key);
                    params.put(key, value);
                    break;

                case "file":
                    whereClauses.add("et.file_name LIKE :file");
                    params.put("file", "%" + value + "%");
                    break;

                case "download":
                    whereClauses.add("cf.status != 'REMINDER'");
                    break;

                default:
                    // skip unknown keys
                    break;
            }
        }

        if (!filters.containsKey("status")) {
            whereClauses.add("cf.status != 'SOFT_DELETED'");
        }

        if (!whereClauses.isEmpty()) {
            sql.append("WHERE ").append(String.join(" AND ", whereClauses));
        }

        Query query = entityManager.createNativeQuery(sql.toString());

        for (Map.Entry<String, Object> param : params.entrySet()) {
            query.setParameter(param.getKey(), param.getValue());
        }
        List<ConnectionFee[]> results = query.getResultList();
        System.out.println((results.get(0)).toString());
        return  results;
    }
}
