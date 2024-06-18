package com.lca.analyzerlcaspring.repository;

import com.lca.analyzerlcaspring.entity.AssignmentStatus;
import com.lca.analyzerlcaspring.entity.StudentAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentAssignmentRepository extends CrudRepository<StudentAssignment, Integer> {
//    @Query(" from StudentAssignment where status = :status")
    List<StudentAssignment> findByStatus(@Param("status") AssignmentStatus status);


}
