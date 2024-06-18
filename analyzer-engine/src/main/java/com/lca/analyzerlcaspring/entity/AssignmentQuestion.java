package com.lca.analyzerlcaspring.entity;

import javax.persistence.*;

@Entity
@Table(name = "assignment_question")
public class AssignmentQuestion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "generated_text")
    private String generatedText;

    @Column(name = "help_text")
    private String helpText;

    private String level;

    private String scope;

    @Column(name = "student_assignment_id")
    private int studentAssignmentId;

    public AssignmentQuestion() {

    }

    public AssignmentQuestion(int id, String generatedText, String helpText, String level, String scope, int studentAssignmentId) {
        this.id = id;
        this.generatedText = generatedText;
        this.helpText = helpText;
        this.level = level;
        this.scope = scope;
        this.studentAssignmentId = studentAssignmentId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getGeneratedText() {
        return generatedText;
    }

    public void setGeneratedText(String generatedText) {
        this.generatedText = generatedText;
    }

    public String getHelpText() {
        return helpText;
    }

    public void setHelpText(String helpText) {
        this.helpText = helpText;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public int getStudentAssignmentId() {
        return studentAssignmentId;
    }

    public void setStudentAssignmentId(int studentAssignmentId) {
        this.studentAssignmentId = studentAssignmentId;
    }
}
