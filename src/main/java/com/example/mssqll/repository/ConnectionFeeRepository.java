package com.example.mssqll.repository;

import com.example.mssqll.models.ConnectionFee;
import com.example.mssqll.models.Status;
import com.example.mssqll.models.ExtractionTask;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


@Repository
public interface ConnectionFeeRepository extends JpaRepository<ConnectionFee, Long>, JpaSpecificationExecutor<ConnectionFee> {
    @Transactional
    void deleteByExtractionTaskId(Long extractionTaskId);

    @Modifying
    @Transactional
    @Query("update ConnectionFee cf set cf.status = :status where cf.extractionTask = :extractionTask")
    void updateStatusByExtractionTask(Status status, ExtractionTask extractionTask);

    @Query("SELECT SUM(cf.totalAmount) FROM ConnectionFee cf WHERE cf.parent = :parentId and cf.status <> 'REMINDER'and cf.status <> 'SOFT_DELETED'")
    Double sumTotalAmountByParentId(ConnectionFee parentId);

    @Query("select count(cf.id) from ConnectionFee  cf where cf.parent.id = :parentId")
    Integer childNumberByParentId(Long parentId);

    @Query(value = """
            WITH Descendants AS (
                SELECT * FROM connection_fees 
                WHERE parent_id = :parentId AND status != 'SOFT_DELETED'
                UNION ALL
                SELECT cf.* FROM connection_fees cf
                INNER JOIN Descendants d ON cf.parent_id = d.id 
                WHERE cf.status != 'SOFT_DELETED'
            )
            SELECT * FROM Descendants;
            """, nativeQuery = true)
    List<ConnectionFee> findAllDescendants(Long parentId);

    @Modifying
    @Transactional
    @Query("DELETE FROM ConnectionFee cf WHERE cf.parent.id = :parentId AND cf.status = 'REMINDER'")
    void deleteResidualEntriesByParentId(@Param("parentId") Long parentId);

    @Query("SELECT cf FROM ConnectionFee cf WHERE cf.parent.id = :parentId AND cf.status = 'REMINDER'")
    Optional<ConnectionFee> findReminderChildByParentId(@Param("parentId") Long parentId);

    @EntityGraph(attributePaths = {"parent"})
    List<ConnectionFee> findAll(Specification<ConnectionFee> spec);

}