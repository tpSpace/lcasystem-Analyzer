package com.lca.analyzerlcaspring.entity;

import io.hypersistence.utils.hibernate.type.basic.PostgreSQLEnumType;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "student_assignment")
@TypeDef(name = "enum_postgressql", typeClass = PostgreSQLEnumType.class)
public class StudentAssignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String url;


    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "assignmentstatus")
    @Type(type = "enum_postgressql")
    private AssignmentStatus status;

    @OneToMany(mappedBy = "studentAssignment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<LocalizationReport> localizationReport;

    @OneToMany(mappedBy = "studentAssignment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<ExecutedTest> executedTests;


    public Set<ExecutedTest> getExecutedTests() {
        return executedTests;
    }

    public void setExecutedTests(Set<ExecutedTest> executedTests) {
        this.executedTests = executedTests;
    }

    public Set<LocalizationReport> getLocalizationReport() {
        return localizationReport;
    }

    public void setLocalizationReport(Set<LocalizationReport> localizationReport) {
        this.localizationReport = localizationReport;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public AssignmentStatus getStatus() {
        return status;
    }

    public void setStatus(AssignmentStatus status) {
        this.status = status;
    }

}
