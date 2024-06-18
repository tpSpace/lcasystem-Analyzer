package com.lca.analyzerlcaspring;

import com.lca.analyzerlcaspring.service.AssignmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AnalyzerLcaSpringApplication implements CommandLineRunner {

    @Autowired
    private AssignmentService assignmentService;

    public static void main(String[] args) {
        SpringApplication.run(AnalyzerLcaSpringApplication.class, args);
    }

    @Override
    public void run(String... args) {
        assignmentService.analyzeAssignment();
    }
}
