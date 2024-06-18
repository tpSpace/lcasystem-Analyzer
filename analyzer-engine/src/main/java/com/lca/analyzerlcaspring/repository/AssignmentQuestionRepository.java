package com.lca.analyzerlcaspring.repository;

import com.lca.analyzerlcaspring.entity.AssignmentQuestion;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AssignmentQuestionRepository extends CrudRepository<AssignmentQuestion, Integer> {
}
