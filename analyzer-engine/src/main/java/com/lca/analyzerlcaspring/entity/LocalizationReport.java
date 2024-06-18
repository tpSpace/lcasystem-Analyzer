package com.lca.analyzerlcaspring.entity;

import javax.persistence.*;

@Entity
@Table(name= "localization_report")
public class LocalizationReport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String location;
    private int lineNumber;
    private Double score;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name= "student_assignment_id")
    private StudentAssignment studentAssignment;

    public LocalizationReport(){}

    public LocalizationReport(String location,int lineNumber, Double score, StudentAssignment studentAssignment) {
        this.location = location;
        this.lineNumber = lineNumber;
        this.score = score;
        this.studentAssignment = studentAssignment;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public Long getId() {
        return id;
    }

    public String getLocation() {
        return location;
    }

    public Double getScore() {
        return score;
    }

    public StudentAssignment getStudentAssignment() {
        return studentAssignment;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public void setStudentAssignment(StudentAssignment studentAssignment) {
        this.studentAssignment = studentAssignment;
    }
}
