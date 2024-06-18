package com.lca.analyzerlcaspring.entity;

import javax.persistence.*;

@Entity
@Table(name= "executed_test")
public class ExecutedTest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String executedTest;

    private boolean isFailing;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_assignment_id")
    private StudentAssignment studentAssignment;

    public ExecutedTest() {}

    public ExecutedTest(Long id, String executedTest, boolean isFailing, StudentAssignment studentAssignment) {
        this.id = id;
        this.executedTest = executedTest;
        this.isFailing = isFailing;
        this.studentAssignment = studentAssignment;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getExecutedTest() {
        return executedTest;
    }

    public void setExecutedTest(String executedTest) {
        this.executedTest = executedTest;
    }

    public boolean isFailing() {
        return isFailing;
    }

    public void setFailing(boolean failing) {
        isFailing = failing;
    }

    public StudentAssignment getStudentAssignment() {
        return studentAssignment;
    }

    public void setStudentAssignment(StudentAssignment studentAssignment) {
        this.studentAssignment = studentAssignment;
    }
}
