package com.example.mssqll.service.impl;


import com.example.mssqll.dto.response.ConnectionFeeChildrenDTO;
import com.example.mssqll.dto.response.ConnectionFeeResponseDto;
import com.example.mssqll.dto.response.UserResponseDto;
import com.example.mssqll.models.*;
import com.example.mssqll.repository.ConnectionFeeRepository;
import com.example.mssqll.repository.ExtractionRepository;
import com.example.mssqll.repository.ExtractionTaskRepository;
import com.example.mssqll.service.ConnectionFeeService;
import com.example.mssqll.utiles.exceptions.FileAlreadyTransferredException;
import com.example.mssqll.utiles.exceptions.ResourceNotFoundException;
import lombok.SneakyThrows;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PagedModel;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ConnectionFeeServiceImpl implements ConnectionFeeService {
    @Autowired
    private final ConnectionFeeRepository connectionFeeRepository;
    @Autowired
    private final ExtractionRepository extractionRepository;
    @Autowired
    private final ExtractionTaskRepository extractionTaskRepository;

    public ConnectionFeeServiceImpl(ConnectionFeeRepository connectionFeeRepository,
                                    ExtractionRepository extractionRepository, ExtractionTaskRepository extractionTaskRepository) {
        this.connectionFeeRepository = connectionFeeRepository;
        this.extractionRepository = extractionRepository;
        this.extractionTaskRepository = extractionTaskRepository;
    }

    @Override
    public PagedModel<ConnectionFee> getAllFee(int page, int size) {
        return new PagedModel<>(connectionFeeRepository.findAll(PageRequest.of(page, size)));
    }

    @Override
    public Optional<ConnectionFee> getFee(Long id) {
        return connectionFeeRepository.findById(id);
    }

    @Override
    public List<ConnectionFee> saveFee(Long extractionTask) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User userDetails = (User) authentication.getPrincipal();

        Optional<ExtractionTask> extractionTaskOptional = extractionTaskRepository.findById(extractionTask);
        if (extractionTaskOptional.isEmpty()) {
            throw new ResourceNotFoundException("Extraction task not found");
        }

        ExtractionTask extractionTask1 = extractionTaskOptional.get();
        if (extractionTask1.getStatus() == FileStatus.TRANSFERRED_GOOD ||
                extractionTask1.getStatus() == FileStatus.TRANSFERRED_WARNING) {
            throw new FileAlreadyTransferredException("file with id: " + extractionTask1.getId() + " already transferred");
        }

        List<Extraction> extractions = extractionRepository.findByExtractionTask(extractionTask1);
        if (extractionTask1.getStatus().equals(FileStatus.WARNING)) {
            extractionTask1.setStatus(FileStatus.TRANSFERRED_WARNING);
        } else {
            extractionTask1.setStatus(FileStatus.TRANSFERRED_GOOD);
        }
        extractionTask1.setSendDate(LocalDateTime.now());
        extractionTaskRepository.save(extractionTask1);

        List<ConnectionFee> fees = new ArrayList<>();
        for (Extraction extraction : extractions) {
            fees.add(
                    ConnectionFee.builder()
                            .orderStatus(OrderStatus.ORDER_INCOMPLETE)
                            .purpose(extraction.getPurpose())
                            .totalAmount(extraction.getTotalAmount())
                            .extractionDate(extraction.getDate())
                            .status(Status.TRANSFERRED)
                            .transferDate(LocalDateTime.now())
                            .extractionTask(extraction.getExtractionTask())
                            .description(extraction.getDescription())
                            .extractionId(extraction.getId())
                            .tax(extraction.getTax())
                            .transferPerson(userDetails)
                            .changePerson(userDetails)
                            .build()
            );
        }
        return connectionFeeRepository.saveAll(fees);
    }

    @Override
    public ConnectionFee save(ConnectionFee connectionFee) {
        connectionFee.setTransferDate(LocalDateTime.now());
        return connectionFeeRepository.save(connectionFee);
    }

    @Override
    public Optional<ConnectionFee> findById(Long id) {
        return connectionFeeRepository.findById(id);
    }

    @Override
    public ConnectionFee updateFee(Long connectionFeeId, ConnectionFee connectionFeeDetails) {
        ConnectionFee existingFee = connectionFeeRepository.findById(connectionFeeId)
                .orElseThrow(() -> new ResourceNotFoundException("ConnectionFee not found with id: " + connectionFeeId));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User userDetails = (User) authentication.getPrincipal();
        existingFee.setStatus(connectionFeeDetails.getStatus());
        existingFee.setRegion(connectionFeeDetails.getRegion());
        existingFee.setServiceCenter(connectionFeeDetails.getServiceCenter());
        System.out.println(existingFee.getFirstWithdrawType());
        if (existingFee.getFirstWithdrawType() == null) {
            System.out.println("uraaa");
            existingFee.setFirstWithdrawType(connectionFeeDetails.getWithdrawType());
        }
        existingFee.setWithdrawType(connectionFeeDetails.getWithdrawType());
        existingFee.setExtractionTask(connectionFeeDetails.getExtractionTask());
        existingFee.setClarificationDate(connectionFeeDetails.getClarificationDate());
        if (!Objects.equals(existingFee.getProjectID(), connectionFeeDetails.getProjectID())) {
            existingFee.setChangeDate(LocalDateTime.now());
            existingFee.setChangePerson(userDetails);
        }
        existingFee.setExtractionId(connectionFeeDetails.getExtractionId());
        existingFee.setNote(connectionFeeDetails.getNote());
        existingFee.setExtractionDate(connectionFeeDetails.getExtractionDate());
        existingFee.setTotalAmount(connectionFeeDetails.getTotalAmount());
        existingFee.setPurpose(connectionFeeDetails.getPurpose());
        existingFee.setDescription(connectionFeeDetails.getDescription());
        existingFee.setPaymentOrderSentDate(connectionFeeDetails.getPaymentOrderSentDate());//new
        existingFee.setTreasuryRefundDate(connectionFeeDetails.getTreasuryRefundDate());//new
        if (!Objects.equals(existingFee.getProjectID(), connectionFeeDetails.getProjectID())) {
            List<String> proj = existingFee.getCanceledProject();
            if (!proj.isEmpty()) {
                if (connectionFeeDetails.getProjectID() != proj.get(proj.size() - 1)) {
                    proj.add(existingFee.getProjectID());
                }
            } else {
                proj.add(existingFee.getProjectID());
            }
            existingFee.setCanceledProject(proj);
            existingFee.setProjectID(connectionFeeDetails.getProjectID());
        }
        if (connectionFeeDetails.getOrderStatus() == OrderStatus.CANCELED) {
            List<String> proj = existingFee.getCanceledProject();
            if (!proj.isEmpty()) {
                if (!Objects.equals(connectionFeeDetails.getProjectID(), proj.get(proj.size() - 1))) {
                    proj.add(existingFee.getProjectID());
                }
            } else {
                proj.add(existingFee.getProjectID());
            }

            existingFee.setCanceledProject(proj);
        }
        existingFee.setProjectID(connectionFeeDetails.getProjectID());

        if (!Objects.equals(existingFee.getOrderN(), connectionFeeDetails.getOrderN())) {
            List<String> newLst = existingFee.getCanceledOrders();
            newLst.add(existingFee.getOrderN());
            existingFee.setCanceledOrders(newLst);//new
            existingFee.setOrderN(connectionFeeDetails.getOrderN());
        }
        existingFee.setStatus(Status.TRANSFER_COMPLETE);
        if (connectionFeeDetails.getClarificationDate() == null) {
            existingFee.setClarificationDate(LocalDateTime.now());
        } else {
            existingFee.setClarificationDate(connectionFeeDetails.getClarificationDate());
        }
        existingFee.setOrderStatus(connectionFeeDetails.getOrderStatus());
        return connectionFeeRepository.save(existingFee);
    }

    @Override
    public void deleteByTaskId(Long taskId) {
        Optional<ExtractionTask> extractionTask = extractionTaskRepository.findById(taskId);

        if (extractionTask.isPresent()) {
            ExtractionTask extractionTask1 = extractionTask.get();
            extractionTask1.setStatus(FileStatus.SOFT_DELETED);
            connectionFeeRepository.updateStatusByExtractionTask(Status.SOFT_DELETED, extractionTask1);

        }
    }

    @Override
    public void softDeleteById(Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User userDetails = (User) authentication.getPrincipal();

        ConnectionFee connectionFee = connectionFeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ConnectionFee not found with id: " + id));

        ConnectionFee parent = connectionFee.getParent();
        List<ConnectionFee> connectionFees = connectionFeeRepository.findAllDescendants(parent.getId());

        // If the sum of all children equals the parent's total amount, mark it as TRANSFERRED
        if (Objects.equals(connectionFeeRepository.sumTotalAmountByParentId(parent), parent.getTotalAmount())) {
            Optional<ConnectionFee> reminderFeeOpt = connectionFeeRepository.findReminderChildByParentId(parent.getId());


            if (reminderFeeOpt.isEmpty()) {
                if (parent.getTotalAmount() - connectionFee.getTotalAmount() != 0) {
                    ConnectionFee reminderFee1 = new ConnectionFee();
                    reminderFee1.setParent(parent);
                    reminderFee1.setNote("ნაშთი");
                    reminderFee1.setStatus(Status.REMINDER);
                    reminderFee1.setChangePerson(userDetails);
                    reminderFee1.setTransferPerson(userDetails);
                    reminderFee1.setExtractionTask(parent.getExtractionTask());
                    reminderFee1.setOrderN("ნაშთი");
                    reminderFee1.setPurpose("ნაშთი");reminderFee1.setTotalAmount(connectionFee.getTotalAmount());
                    connectionFeeRepository.save(reminderFee1);
                }
            }
            connectionFee.setStatus(Status.SOFT_DELETED);
            parent.setStatus(Status.TRANSFERRED);
            connectionFeeRepository.save(parent);
            connectionFeeRepository.save(connectionFee);
            return;
        }

        // Check if the connectionFee is NOT a REMINDER
        System.out.println("heck if the connectionFee is NOT a REMINDER ");
        if (!connectionFee.getStatus().equals(Status.REMINDER)) {
            Optional<ConnectionFee> reminderFeeOpt = connectionFeeRepository.findReminderChildByParentId(parent.getId());

            if (reminderFeeOpt.isPresent()) {
                ConnectionFee reminderFee = reminderFeeOpt.get();

                // Add the deleted child's amount to the reminder fee
                reminderFee.setTotalAmount(reminderFee.getTotalAmount() + connectionFee.getTotalAmount());

                // If the new reminder amount equals the parent's total amount, delete the reminder
                if (reminderFee.getTotalAmount().equals(parent.getTotalAmount())) {
                    parent.setWithdrawType(parent.getFirstWithdrawType());
                    parent.setStatus(Status.TRANSFERRED);
                    connectionFeeRepository.save(parent);
                    connectionFeeRepository.delete(reminderFee);
                } else {
                    connectionFeeRepository.save(reminderFee);
                }
            }
            System.out.println("gamocda nashtze damatebas");

            // Soft delete the current connectionFee
            connectionFee.setStatus(Status.SOFT_DELETED);
            System.out.println("statusi shecvala " + connectionFee.getStatus());
            connectionFee.setChangePerson(userDetails);
            connectionFeeRepository.save(connectionFee);
        }
        // Handle case where only one child remains
        else if (connectionFees.size() == 1) {
            System.out.println("aqa mshvidoba");
            ConnectionFee lastChild = connectionFees.get(0);
            lastChild.setStatus(Status.SOFT_DELETED);
            lastChild.setChangePerson(userDetails);
            connectionFeeRepository.save(lastChild);

            // Update the parent's status
            if (parent.getProjectID() != null) {
                parent.setStatus(Status.TRANSFER_COMPLETE);
            } else {
                parent.setStatus(Status.TRANSFERRED);
            }
            connectionFeeRepository.save(parent);
        }
    }

    @Override
    public ByteArrayInputStream createExcel(List<ConnectionFee> connectionFees) throws IOException {
        String[] columns = {
                "ID", //0
                "ორდერის N",//1
                "რეგიონი",//2
                "სერვის ცენტრი",//3
                "პროექტის ნომერი",//4
                "გარკვევის თარიღი",//5
                "შეცვლის თარიღი",//6
                "შენიშვნა",//7
                "გადმოტანის თარიღი",//8
                "ჩარიცხვის თარიღი",//9
                "თანხა",//10
                "გადამხდელის იდენტიფიკატორი",//11
                "მიზანი",//12
                "აღწერა",//13
                "შემცველელი",//14
                "გაუქმებული პროექტები",//15
                "მშობელი"
        };

        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Connection Fees");

        Row headerRow = sheet.createRow(0);
        XSSFFont boldFont = workbook.createFont();
        boldFont.setBold(true);
        XSSFCellStyle boldCellStyle = workbook.createCellStyle();
        boldCellStyle.setFont(boldFont);

        for (int i = 0; i < columns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(boldCellStyle);
        }
        List<ConnectionFee> flatList = new ArrayList<>();

        for (ConnectionFee fee : connectionFees) {
            flatList.add(fee);
            flatList.addAll(fee.getChildren().stream()
                    .filter(child -> !child.getStatus().equals(Status.SOFT_DELETED))
                    .filter(child -> !child.getStatus().equals(Status.REMINDER))
                    .toList());
        }

        int rowIdx = 1;
        for (ConnectionFee connectionFee : flatList) {
            Row row = sheet.createRow(rowIdx);
            for (int i = 0; i < columns.length; i++) {
                Cell cell = row.createCell(i);
                cell.setCellValue(getCellValue(connectionFee, i));
            }
            rowIdx++;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();

        return new ByteArrayInputStream(out.toByteArray());
    }

    @SneakyThrows
    @Override
    public void divideFee(Long feeId, Double[] arr) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User userDetails = (User) authentication.getPrincipal();
        Optional<Double> arrSum = Arrays.stream(arr).reduce(Double::sum);
        Optional<ConnectionFee> connectionFee = connectionFeeRepository.findById(feeId);
        List<ConnectionFee> feeToAdd = new ArrayList<>();
        ConnectionFee connectionFeeCopy;
        ConnectionFee connectionFee1;

        if (connectionFee.isPresent()) {
            connectionFee1 = connectionFee.get();
            if (arrSum.isPresent()) {
                Double sum = arrSum.get();
                if (sum != 0.0) {
                    Double childSum = (connectionFeeRepository.sumTotalAmountByParentId(connectionFee1) != null)
                            ? connectionFeeRepository.sumTotalAmountByParentId(connectionFee1) : 0.0;
                    if (sum <= connectionFee1.getTotalAmount() && (childSum + sum) <= connectionFee1.getTotalAmount()) {
                        Optional<ConnectionFee> reminderChildOpt = connectionFeeRepository.findReminderChildByParentId(connectionFee1.getId());
                        boolean reminderUpdated = false;
                        if (reminderChildOpt.isPresent()) {
                            ConnectionFee reminderChild = reminderChildOpt.get();
                            double reminderAmount = reminderChild.getTotalAmount();
                            double newReminderAmount = reminderAmount - sum;

                            if (newReminderAmount >= 0) {
                                reminderChild.setTotalAmount(newReminderAmount);
                                connectionFeeRepository.save(reminderChild);
                                reminderUpdated = true;
                            } else {
                                throw new Exception("Insufficient amount in Reminder child for this operation.");
                            }
                        }
                        int childNum = 1;
                        double newElement = connectionFee1.getTotalAmount() - childSum - sum;
                        Double[] newArr = Arrays.copyOf(arr, arr.length + (reminderUpdated ? 0 : 1));
                        if (!reminderUpdated) {
                            newArr[newArr.length - 1] = newElement;
                        }
                        for (Double d : newArr) {
                            if (d == 0.0) {
                                continue;
                            }
                            connectionFeeCopy = new ConnectionFee(connectionFee1);
                            connectionFeeCopy.setTotalAmount(d);
                            connectionFeeCopy.setParent(connectionFee1);
                            connectionFeeCopy.setChangePerson(userDetails);
                            connectionFeeCopy.setTransferPerson(userDetails);
                            connectionFeeCopy.setOrderStatus(OrderStatus.ORDER_INCOMPLETE);
                            connectionFeeCopy.setStatus(Status.TRANSFERRED);

                            String parentQueueNumber = connectionFee1.getQueueNumber() != null
                                    ? connectionFee1.getQueueNumber()
                                    : String.valueOf(connectionFee1.getId());
                            connectionFeeCopy.setQueueNumber(parentQueueNumber + "-" + (connectionFeeRepository.childNumberByParentId(feeId) + childNum));
                            childNum++;
                            feeToAdd.add(connectionFeeCopy);
                        }

                        boolean isLastElement = feeToAdd.get(feeToAdd.size() - 1).getTotalAmount() == newElement;
                        boolean isFullSumMatch = (sum + childSum == connectionFee1.getTotalAmount());
                        if (!reminderUpdated && !isFullSumMatch && isLastElement) {
                            ConnectionFee lastFee = feeToAdd.get(feeToAdd.size() - 1);
                            lastFee.setNote("test2");
                            lastFee.setOrderN("ნაშთი");
                            lastFee.setDescription("ნაშთი");
                            lastFee.setPurpose("ნაშთი");
                            lastFee.setStatus(Status.REMINDER);
                        } else if (isFullSumMatch) {
                            connectionFeeRepository.deleteResidualEntriesByParentId(connectionFee1.getId());
                        }
                        connectionFee1.setStatus(Status.CANCELED);
                        connectionFee1.setWithdrawType("4 (ერთანი გადახდა, გადანაწილებული რამოდენიმე პროექტის საფასურად)");
                        connectionFeeRepository.save(connectionFee1);
                        connectionFeeRepository.saveAll(feeToAdd);
                    } else {
                        throw new Exception("Sum of elements must not be greater than parent amount");
                    }
                } else {
                    throw new Exception("Sum of array must be greater than 0");
                }
            } else {
                throw new Exception("Sum of elements must be a floating-point number");
            }
        } else {
            throw new ResourceNotFoundException("ConnectionFee not found with id: " + feeId);
        }
    }

    @Override
    public List<ConnectionFeeChildrenDTO> getFeesByParent(Long id) {
        Optional<ConnectionFee> connectionFee = connectionFeeRepository.findById(id);
        if (connectionFee.isPresent()) {
            List<ConnectionFee> fees = connectionFeeRepository.findAllDescendants(id);

            return fees.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
        } else {
            throw new ResourceNotFoundException("ConnectionFee not found with id: " + id);
        }
    }

    @Override
    public List<ConnectionFee> getDownloadDataBySpec(Specification<ConnectionFee> spec) {
        return connectionFeeRepository.findAll(spec);
    }

    private ConnectionFeeChildrenDTO convertToDto(ConnectionFee connectionFee) {
        ConnectionFeeChildrenDTO dto = new ConnectionFeeChildrenDTO();
        dto.setId(connectionFee.getId());
        dto.setOrderN(connectionFee.getOrderN());
        dto.setRegion(connectionFee.getRegion());
        dto.setServiceCenter(connectionFee.getServiceCenter());
        dto.setProjectID(connectionFee.getProjectID());
        dto.setWithdrawType(connectionFee.getWithdrawType());
        dto.setClarificationDate(connectionFee.getClarificationDate());
        dto.setChangeDate(connectionFee.getChangeDate());
        dto.setTransferDate(connectionFee.getTransferDate());
        dto.setExtractionId(connectionFee.getExtractionId());
        dto.setNote(connectionFee.getNote());
        dto.setExtractionDate(connectionFee.getExtractionDate());
        dto.setTotalAmount(connectionFee.getTotalAmount());
        dto.setPurpose(connectionFee.getPurpose());
        dto.setDescription(connectionFee.getDescription());
        dto.setTax(connectionFee.getTax());

        if (connectionFee.getChildren() != null) {
            dto.setChildren(connectionFee.getChildren().stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList()));
        }
        return dto;
    }

    private String getCellValue(ConnectionFee connectionFee, int columnIndex) {
        return switch (columnIndex) {
            case 0 -> String.valueOf(connectionFee.getId());
            case 1 -> connectionFee.getOrderN();
            case 2 -> connectionFee.getRegion();
            case 3 -> connectionFee.getServiceCenter();
            case 4 -> connectionFee.getProjectID();
            case 5 -> {
                if (connectionFee.getClarificationDate() != null) {
                    yield connectionFee.getClarificationDate().toString();
                }
                yield "";
            }
            case 6 -> {
                if (connectionFee.getChangeDate() != null) {
                    yield connectionFee.getChangeDate().toString();
                }
                yield "";
            }
            case 7 -> connectionFee.getNote();
            case 8 -> connectionFee.getTransferDate().toString();
            case 9 -> connectionFee.getExtractionDate().toString();
            case 10 -> connectionFee.getTotalAmount().toString();
            case 11 -> connectionFee.getTax();
            case 12 -> connectionFee.getPurpose();
            case 13 -> connectionFee.getDescription();
            case 14 ->
                    connectionFee.getChangePerson().getLastName() + " " + connectionFee.getChangePerson().getFirstName();
            case 15 -> connectionFee.getCanceledOrders().toString();
            case 16 -> {
                if (connectionFee.getParent() != null) {
                    yield connectionFee.getParent().getId().toString();
                }
                yield "";
            }

            default -> "";
        };
    }

    @Override
    public PagedModel<?> letDoFilter(Specification<ConnectionFee> spec, int page, int size, String sortBy, String sortDir) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        return new PagedModel<>(castToDtos(connectionFeeRepository.findAll(spec, PageRequest.of(page, size, sort))));
    }

    private Page<ConnectionFeeResponseDto> castToDtos(Page<ConnectionFee> page) {
        List<ConnectionFeeResponseDto> cfDtos = page.getContent()
                .stream()
                .map(this::castToDto)
                .collect(Collectors.toList());

        return new PageImpl<>(cfDtos, page.getPageable(), page.getTotalElements());
    }

    private ConnectionFeeResponseDto castToDto(ConnectionFee cf) {
        ConnectionFeeResponseDto cfd = baseCast(cf);
        List<ConnectionFeeResponseDto> emptyDto = new ArrayList<>();
        List<ConnectionFeeResponseDto> cfDtos = new ArrayList<>();
        if (!cf.getChildren().isEmpty()) {
            ConnectionFeeResponseDto cfChild1;
            for (ConnectionFee cfChild : cf.getChildren()) {
                cfChild1 = baseCast(cfChild);
                cfChild1.setChildren(emptyDto);
                cfDtos.add(cfChild1);
            }
        }
        cfd.setChildren(cfDtos);
        return cfd;
    }

    private ConnectionFeeResponseDto baseCast(ConnectionFee cf) {
        return ConnectionFeeResponseDto.builder()
                .id(cf.getId())
                .orderStatus(cf.getOrderStatus())
                .status(cf.getStatus())
                .orderN(cf.getOrderN())
                .region(cf.getRegion())
                .serviceCenter(cf.getServiceCenter())
                .queueNumber(cf.getQueueNumber())
                .projectID(cf.getProjectID())
                .withdrawType(cf.getWithdrawType())
                .clarificationDate(cf.getClarificationDate())
                .treasuryRefundDate(cf.getTreasuryRefundDate())
                .paymentOrderSentDate(cf.getPaymentOrderSentDate())
                .canceledOrders(cf.getCanceledOrders())
                .canceledProject(cf.getCanceledProject())
                .changeDate(cf.getChangeDate())
                .transferDate(cf.getTransferDate())
                .extractionDate(cf.getExtractionDate())
                .extractionTask(cf.getExtractionTask())
                .totalAmount(cf.getTotalAmount())
                .purpose(cf.getPurpose())
                .description(cf.getDescription())
                .tax(cf.getTax())
                .transferPerson(castUserToDto(cf.getTransferPerson()))
                .changePerson(castUserToDto(cf.getChangePerson()))
                .note(cf.getNote())
                .build();
    }

    private UserResponseDto castUserToDto(User user) {
        return UserResponseDto.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .email(user.getEmail())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    @Override
    public Integer uploadHistory(MultipartFile file) throws IOException {
        int batchSize = 50;
        List<ConnectionFee> batch = new ArrayList<>();
        LocalDateTime today = LocalDateTime.now();
        ExtractionTask task;
        try {
            task = extractionTaskRepository.save(new ExtractionTask(today, file.getOriginalFilename(), FileStatus.HISTORY));
        } catch (Exception e) {
            return 0;
        }
        Map<Integer, String> PAYMENT_MAPPING = new HashMap<>();
        PAYMENT_MAPPING.put(1, "1 (პირველი გადახდა)");
        PAYMENT_MAPPING.put(2, "2 (მეორე გადახდა)");
        PAYMENT_MAPPING.put(3, "3 (სრული საფასურის გადახდა)");
        PAYMENT_MAPPING.put(4, "4 (ერთანი გადახდა, გადანაწილებული რამოდენიმე პროექტის საფასურად)");
        PAYMENT_MAPPING.put(5, "5 (სავარაუდოდ არაა ახალი მიერთების საფასური)");
        PAYMENT_MAPPING.put(6, "6 (თანხის დაბრუნება)");
        PAYMENT_MAPPING.put(7, "7 (გადანაწილებული გადახდა / რამოდენიმეჯერ გადახდა)");
        PAYMENT_MAPPING.put(8, "8 (სააბონენტო ბარათზე თანხის დასმა)");
        PAYMENT_MAPPING.put(9, "9 (ხაზის მშენებლობა / არარეგულირებული პროექტები (პირველი ან სრული გადახდა))");
        PAYMENT_MAPPING.put(10, "10 (სისტემის ნებართვის საფასური)");
        PAYMENT_MAPPING.put(19, "19 (ხაზის მშენებლობა / არარეგულირებული პროექტები (მეორე გადახდა))");
        PAYMENT_MAPPING.put(11, "11 (სააბონენტო ბარათიდან თანხის გადმოტანა)");
        PAYMENT_MAPPING.put(12, "12 (ჯარიმის გადატანა)");
        PAYMENT_MAPPING.put(13, "13 (საპროექტო ტრასის შეტანხმება)");
        PAYMENT_MAPPING.put(14, "14 (ჰესები DDSH)");
        PAYMENT_MAPPING.put(15, "15 (ჰესები DDNA)");

        Map<String, OrderStatus> ORDER_STATUS_MAPPING = new HashMap<>();
        ORDER_STATUS_MAPPING.put("გაუქმებული", OrderStatus.CANCELED);
        ORDER_STATUS_MAPPING.put("დასასრულებელი", OrderStatus.ORDER_INCOMPLETE);
        ORDER_STATUS_MAPPING.put("დასრულებული", OrderStatus.ORDER_COMPLETE);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User userDetails = (User) authentication.getPrincipal();
        List<ConnectionFee> connectionFees = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
        int rowNum = 0;
        try {
            Workbook workbook = new XSSFWorkbook(file.getInputStream());
            Sheet sheet = workbook.getSheetAt(0);

            for (int i = 2; i <= sheet.getLastRowNum(); i++) { // Start from row 2 to skip headers
                Row row = sheet.getRow(i);
                if (getDoubleCellValue(row.getCell(7)) == null ||
                        getDoubleCellValue(row.getCell(7)) == 0.0) {
                    continue;
                }
                rowNum = i;
                //  Skip empty rows
                if (isRowEmpty(row)) {
                    continue;
                }

                ConnectionFee fee = new ConnectionFee();
                try {
                    fee.setId(getLongCellValue(row.getCell(0)));//1 აიდი
                    fee.setOrderN(getStringCellValue(row.getCell(1)));//2 ორდეირს ნომერი
                    fee.setRegion(getStringCellValue(row.getCell(2)));//3 რეგიონი
                    fee.setServiceCenter(getStringCellValue(row.getCell(3)));//4 მომსახურების ცენტრი
                    fee.setProjectID(getStringCellValue(row.getCell(4)));//5 პროექტის ნომერი
                    Integer paymentType = (int) row.getCell(5).getNumericCellValue();//6 ტიპი
                    fee.setWithdrawType(PAYMENT_MAPPING.getOrDefault(paymentType, "Unknown payment type"));

                    // Date Handling
                    try {
                        LocalDate extractionDate = null;
                        if (row.getCell(6) != null && !row.getCell(6).toString().trim().isEmpty()) {
                            if (DateUtil.isCellDateFormatted(row.getCell(6))) {
                                extractionDate = row.getCell(6).getLocalDateTimeCellValue().toLocalDate();
                            } else {
                                try {
                                    extractionDate = LocalDate.parse(row.getCell(6).toString(), formatter);
                                } catch (Exception dateEx) {
                                    // Log specific error for invalid date format
                                    logRowError(row, 6, dateEx);
                                    extractionDate = null; // Set to null if parsing fails
                                }
                            }
                        }

                        // Set extractionDate to the fee object
                        fee.setExtractionDate(extractionDate);

                    } catch (Exception e) {
                        // Catch any other unexpected errors
                        logRowError(row, 6, e);
                        fee.setExtractionDate(null); // Set to null if there is any error
                    }

                    fee.setTotalAmount(getDoubleCellValue(row.getCell(7)));//8 ბრუნვა
                    fee.setPurpose(getStringCellValue(row.getCell(8)));//9 დანიშნულება
                    fee.setDescription(getStringCellValue(row.getCell(9))); //10 დამატებითი ინფირმაცია
                    fee.setTax(getStringCellValue(row.getCell(10)));//
                    fee.setNote(getStringCellValue(row.getCell(13)));// 13 შენიშვნა
                    System.out.println(fee.getNote() + " note");

                    // Clarification Date
                    try {
                        if (row.getCell(11) != null && !row.getCell(11).toString().isEmpty()) {
                            LocalDate clarificationDate = LocalDate.parse(row.getCell(11).toString(), formatter);
                            fee.setClarificationDate(clarificationDate.atStartOfDay());
                        }
                    } catch (Exception e) {
                        logRowError(row, 11, e);
                        fee.setClarificationDate(null);
                    }
                    // Treasury Refund Date
                    try {
                        if (row.getCell(14) != null && !row.getCell(14).toString().isEmpty()) {
                            LocalDate treasuryRefundDate = LocalDate.parse(row.getCell(14).toString(), formatter);
                            fee.setTreasuryRefundDate(treasuryRefundDate);
                        }
                    } catch (Exception e) {
                        logRowError(row, 14, e);
                        fee.setTreasuryRefundDate(null);
                    }
                    // Payment Order Sent Date
                    try {
                        if (row.getCell(15) != null && !row.getCell(15).toString().isEmpty() && row.getCell(15).toString().matches("\\d{2}-[A-Za-z]{3}-\\d{4}")) {
                            LocalDate paymentOrderSentDate = LocalDate.parse(row.getCell(15).toString(), formatter);
                            fee.setPaymentOrderSentDate(paymentOrderSentDate);
                        } else {
                            fee.setPaymentOrderSentDate(null);
                        }
                    } catch (Exception e) {
                        logRowError(row, 15, e);
                        fee.setPaymentOrderSentDate(null);
                    }

                    // order status
                    try {
                        fee.setOrderStatus(ORDER_STATUS_MAPPING.get(row.getCell(22).toString()));
                    } catch (Exception e) {
                        fee.setOrderStatus(null);
                        logRowError(row, 22, e);
                    }

                    List<String> canceledProjects = new ArrayList<>();
                    canceledProjects.add(getStringCellValue(row.getCell(16)));
                    canceledProjects.add(getStringCellValue(row.getCell(17)));
                    canceledProjects.add(getStringCellValue(row.getCell(18)));
                    fee.setCanceledProject(canceledProjects);

                    fee.setStatus(Status.TRANSFERRED);
                    fee.setTransferDate(LocalDateTime.now());
                    fee.setExtractionId(0L);
                    fee.setTransferPerson(userDetails);
                    fee.setChangePerson(userDetails);
                    fee.setExtractionTask(task);
                    batch.add(fee);
                    if ((i + 1) % batchSize == 0) {
                        connectionFeeRepository.saveAll(batch);
                        connectionFeeRepository.flush();  // Ensures data is written to the database
                        batch.clear();  // Clear batch to free memory
                    }

                } catch (Exception e) {
                    logFullRow(row, e);
                }
            }
            System.out.println("Processed " + connectionFees.size() + " records");
            return connectionFees.size();

        } catch (Exception e) {
            System.err.println("Critical error reading file :" + file.getOriginalFilename() + "row: " + rowNum);
            e.printStackTrace();
            return 0;
        }
    }

    private void logRowError(Row row, int columnIndex, Exception e) {
        System.out.println("❌ Error processing row " + (row.getRowNum() + 1) +
                " at column " + (columnIndex + 1) + ": " + e.getMessage());
    }

    private void logFullRow(Row row, Exception e) {
        StringBuilder rowContent = new StringBuilder();
        for (int j = 0; j < row.getLastCellNum(); j++) {
            Cell cell = row.getCell(j, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
            rowContent.append("Col ").append(j + 1).append(": ").append(cell.toString()).append(" | ");
        }
        System.out.println("🚨 Error processing row " + (row.getRowNum() + 1) +
                ": " + e.getMessage());
        System.out.println("Row content: " + rowContent.toString());
    }

    private boolean isRowEmpty(Row row) {
        if (row == null) {
            return true;
        }
        for (int j = 0; j < row.getLastCellNum(); j++) {
            Cell cell = row.getCell(j, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
            if (cell.getCellType() != CellType.BLANK && !cell.toString().trim().isEmpty()) {
                return false;
            }
            if (row.getCell(7) == null || row.getCell(7).toString().trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    // Helper Methods
    private static String getStringCellValue(Cell cell) {
        if (cell == null) return null;
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return null;
        }
    }

    private static Long getLongCellValue(Cell cell) {
        if (cell == null) return null;
        if (cell.getCellType() == CellType.NUMERIC) {
            return (long) cell.getNumericCellValue();
        } else if (cell.getCellType() == CellType.STRING) {
            try {
                return Long.parseLong(cell.getStringCellValue().trim());
            } catch (NumberFormatException e) {
                return null; // Or throw an exception if this is critical
            }
        }
        return null;
    }

    private static Double getDoubleCellValue(Cell cell) {
        if (cell == null) return null;
        if (cell.getCellType() == CellType.NUMERIC) {
            return cell.getNumericCellValue();
        } else if (cell.getCellType() == CellType.STRING) {
            try {
                return Double.parseDouble(cell.getStringCellValue().trim());
            } catch (NumberFormatException e) {
                return null; // Or throw an exception if this is critical
            }
        }
        return null;
    }


}
