package com.example.mssqll.repository;

import com.example.mssqll.models.*;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Repository
public class ConnectionFeeCustomRepository {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<ConnectionFee> fetchConnectionFees(Map<String, String> filters) {
        StringBuilder sql = new StringBuilder("SELECT cf.*, " + "et.id AS et_id, et.date AS et_date, et.send_date AS et_send_date, et.file_name AS et_file_name, et.status AS et_status,\n " + "tp.id AS tp_id, tp.first_name AS tp_first_name, tp.last_name AS tp_last_name, tp.email AS tp_email, tp.role AS tp_role, tp.created_at AS tp_created_at, tp.updated_at AS tp_updated_at,\n " + "cp.id AS cp_id, cp.first_name AS cp_first_name, cp.last_name AS cp_last_name, cp.email AS cp_email, cp.role AS cp_role, cp.created_at AS cp_created_at, cp.updated_at AS cp_updated_at,\n " + "STRING_AGG(CONVERT(VARCHAR, cfp.canceled_project), ', ') AS canceled_projects\n " + "FROM connection_fees cf\n " + "LEFT JOIN extraction_task et ON cf.extraction_task_id = et.id\n " + "LEFT JOIN users tp ON cf.transfer_person = tp.id\n " + "LEFT JOIN users cp ON cf.change_person = cp.id\n " + "LEFT JOIN connection_fee_canceled_project cfp ON cf.id = cfp.connection_fee_id\n ");

        List<Object> paramValues = new ArrayList<>();
        List<String> whereClauses = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");

        for (Map.Entry<String, String> entry : filters.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (value == null) continue;

            switch (key) {
                case "orderN":
                    whereClauses.add("cf.ordern LIKE ?");
                    paramValues.add("%" + value + "%");
                    break;
                case "region":
                    whereClauses.add("cf.region = ?");
                    paramValues.add(value);
                    break;
                case "serviceCenter":
                    whereClauses.add("cf.service_center = ?");
                    paramValues.add(value);
                    break;
                case "projectID":
                    whereClauses.add("cf.project_id LIKE ?");
                    paramValues.add("%" + value + "%");
                    break;
                case "withdrawType":
                    whereClauses.add("cf.withdraw_type LIKE ?");
                    paramValues.add("%" + value + "%");
                    break;
                case "purpose":
                    whereClauses.add("cf.purpose LIKE ?");
                    paramValues.add("%" + value + "%");
                    break;
                case "description":
                    whereClauses.add("cf.description LIKE ?");
                    paramValues.add("%" + value + "%");
                    break;
                case "note":
                    whereClauses.add("cf.note LIKE ?");
                    paramValues.add("%" + value + "%");
                    break;
                case "tax":
                    whereClauses.add("cf." + key + "_id" + " LIKE ?");
                    paramValues.add("%" + value + "%");
                    break;

                case "status":
                    whereClauses.add("cf.status = ?");
                    paramValues.add(value);
                    break;

                case "clarificationDateStart":
                    whereClauses.add("cf.clarification_date >= ?");
                    paramValues.add(LocalDateTime.parse(value, formatter));
                    break;

                case "clarificationDateEnd":
                    whereClauses.add("cf.clarification_date <= ?");
                    paramValues.add(LocalDateTime.parse(value, formatter));
                    break;
                case "changeDateStart":
                    whereClauses.add("cf.change_date >= ?");
                    paramValues.add(LocalDateTime.parse(value));
                    break;

                case "changeDateEnd":
                    whereClauses.add("cf.change_date <= ?");
                    paramValues.add(LocalDateTime.parse(value));
                    break;

                case "transferDateStart":
                    whereClauses.add("cf.transfer_date >= ?");
                    paramValues.add(LocalDateTime.parse(value));
                    break;

                case "transferDateEnd":
                    whereClauses.add("cf.transfer_date <= ?");
                    paramValues.add(LocalDateTime.parse(value));
                    break;

                case "extractionDateStart":
                    whereClauses.add("cf.extraction_date >= ?");
                    paramValues.add(LocalDate.parse(value));
                    break;

                case "extractionDateEnd":
                    whereClauses.add("cf.extraction_date <= ?");
                    paramValues.add(LocalDate.parse(value));
                    break;

                case "totalAmountStart":
                    whereClauses.add("cf.total_amount >= ?");
                    paramValues.add(Double.parseDouble(value));
                    break;

                case "totalAmountEnd":
                    whereClauses.add("cf.total_amount <= ?");
                    paramValues.add(Double.parseDouble(value));
                    break;

                case "extractionId":
                case "history_id":
                    whereClauses.add("cf." + key + " = ?");
                    paramValues.add(value);
                    break;

                case "file":
                    whereClauses.add("et.file_name LIKE ?");
                    paramValues.add("%" + value + "%");
                    break;

                case "download":
                    whereClauses.add("cf.status != 'REMINDER'");
                    break;
            }
        }

        if (!filters.containsKey("status")) {
            whereClauses.add("cf.status != 'SOFT_DELETED'");
        }

        if (!whereClauses.isEmpty()) {
            sql.append("WHERE ").append(String.join(" AND ", whereClauses));
        }
        sql.append(" group by cp.last_name, et.id, et.date,cp.id, tp.first_name,tp.last_name,cp.first_name,\n" + "         cp.role,cp.created_at,cp.updated_at,\n" + "         tp.email, cp.email, et.file_name, et.send_date, cf.id, change_date,\n" + "         clarification_date, description, extraction_date, extraction_id, first_withdraw_type,\n" + "         history_id, note, ordern, order_status, payment_order_sent_date,payment_order_sent_date_status, project_id, purpose,\n" + "         queue_number, region, cf.status, tax_id, total_amount, transfer_date,\n" + "         treasury_refund_date, withdraw_type, change_person, extraction_task_id, parent_id, transfer_person,\n" + "         service_center, extraction_id, tp.updated_at, tp.created_at, et.status, tp.role, tp.id");
        List<ConnectionFee> connectionFees = jdbcTemplate.query(sql.toString(), paramValues.toArray(), connectionFeeRowMapper());
        return connectionFees;
    }

    private RowMapper<ConnectionFee> connectionFeeRowMapper() {
        return (rs, rowNum) -> {
            ConnectionFee fee = new ConnectionFee();
            fee.setId(rs.getLong("id"));
            fee.setOrderStatus(safeFromOrdinal(rs.getInt("order_status")));
            fee.setStatus(Status.valueOf(rs.getString("status")));
            fee.setOrderN(rs.getString("orderN"));
            fee.setRegion(rs.getString("region"));
            fee.setServiceCenter(rs.getString("service_center"));
            fee.setQueueNumber(rs.getString("queue_number"));
            fee.setProjectID(rs.getString("project_id"));
            fee.setWithdrawType(rs.getString("withdraw_type"));
            fee.setClarificationDate(getLocalDateTime(rs, "clarification_date"));
            fee.setChangeDate(getLocalDateTime(rs, "change_date"));
            fee.setTransferDate(getLocalDateTime(rs, "transfer_date"));
            fee.setExtractionId(getNullableLong(rs, "extraction_id"));
            fee.setNote(rs.getString("note"));
            fee.setExtractionDate(getLocalDate(rs, "extraction_date"));
            fee.setTotalAmount(rs.getDouble("total_amount"));
            fee.setPurpose(rs.getString("purpose"));
            fee.setDescription(rs.getString("description"));
            fee.setTax(rs.getString("tax_id"));
            fee.setHistoryId(getNullableLong(rs, "history_id"));
            fee.setTreasuryRefundDate(getLocalDate(rs, "treasury_refund_date"));
            fee.setPaymentOrderSentDate(getLocalDate(rs, "payment_order_sent_date"));
            if (rs.getString("canceled_projects") != null) {
                fee.setCanceledProject(List.of(rs.getString("canceled_projects").split(",")));
            }
            // ExtractionTask
            if (rs.getObject("et_id") != null) {
                ExtractionTask task = new ExtractionTask();
                task.setId(rs.getLong("et_id"));
                task.setDate(getLocalDateTime(rs, "et_date"));
                task.setSendDate(getLocalDateTime(rs, "et_send_date"));
                task.setFileName(rs.getString("et_file_name"));
                task.setStatus(FileStatus.valueOf(rs.getString("et_status")));
                fee.setExtractionTask(task);
            }

            // Transfer Person
            if (rs.getObject("tp_id") != null) {
                User transferPerson = new User();
                transferPerson.setId(rs.getLong("tp_id"));
                transferPerson.setFirstName(rs.getString("tp_first_name"));
                transferPerson.setLastName(rs.getString("tp_last_name"));
                transferPerson.setEmail(rs.getString("tp_email"));
                transferPerson.setRole(Role.valueOf(rs.getString("tp_role")));
                transferPerson.setCreatedAt(getLocalDateTime(rs, "tp_created_at"));
                transferPerson.setUpdatedAt(getLocalDateTime(rs, "tp_updated_at"));
                fee.setTransferPerson(transferPerson);
            }

            // Change Person
            if (rs.getObject("cp_id") != null) {
                User changePerson = new User();
                changePerson.setId(rs.getLong("cp_id"));
                changePerson.setFirstName(rs.getString("cp_first_name"));
                changePerson.setLastName(rs.getString("cp_last_name"));
                changePerson.setEmail(rs.getString("cp_email"));
                changePerson.setRole(Role.valueOf(rs.getString("cp_role")));
                changePerson.setCreatedAt(getLocalDateTime(rs, "cp_created_at"));
                changePerson.setUpdatedAt(getLocalDateTime(rs, "cp_updated_at"));
                fee.setChangePerson(changePerson);
            }

            // Default empty lists
            fee.setCanceledOrders(Collections.emptyList());
            fee.setChildren(Collections.emptyList());

            return fee;
        };
    }

    private LocalDateTime getLocalDateTime(ResultSet rs, String columnLabel) throws SQLException {
        Timestamp timestamp = rs.getTimestamp(columnLabel);
        return timestamp != null ? timestamp.toLocalDateTime() : null;
    }

    private LocalDate getLocalDate(ResultSet rs, String columnLabel) throws SQLException {
        Date date = rs.getDate(columnLabel);
        return date != null ? date.toLocalDate() : null;
    }

    private Long getNullableLong(ResultSet rs, String columnLabel) throws SQLException {
        Object value = rs.getObject(columnLabel);
        return value != null ? ((Number) value).longValue() : null;
    }

    private static OrderStatus safeFromOrdinal(int ordinal) {
        OrderStatus[] values = OrderStatus.values();
        return (ordinal >= 0 && ordinal < values.length) ? values[ordinal] : null;
    }

    @Setter
    @Getter
    public static class CanceledProject {
        private Long id;
        private String projectNumber;
    }
}