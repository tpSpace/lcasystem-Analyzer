package com.lca.analyzerlcaspring.service;

import com.lca.analyzerlcaspring.config.PathConfig;
import com.lca.analyzerlcaspring.entity.ExecutedTest;
import com.lca.analyzerlcaspring.entity.LocalizationReport;
import com.lca.analyzerlcaspring.entity.StudentAssignment;
import com.lca.analyzerlcaspring.repository.ExecutedTestRepository;
import com.lca.analyzerlcaspring.repository.LocalizationReportRepository;
import com.lca.analyzerlcaspring.repository.StudentAssignmentRepository;
import fr.spoonlabs.flacoco.api.Flacoco;
import fr.spoonlabs.flacoco.api.result.FlacocoResult;
import fr.spoonlabs.flacoco.api.result.Location;
import fr.spoonlabs.flacoco.api.result.Suspiciousness;
import fr.spoonlabs.flacoco.core.config.FlacocoConfig;
import fr.spoonlabs.flacoco.core.test.method.TestMethod;
import org.apache.maven.shared.invoker.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class FaultLocalizationServiceImpl implements FaultLocalizationService {
    Logger LOGGER = LoggerFactory.getLogger(SpoonServiceImpl.class);
    @Autowired
    private LocalizationReportRepository localizationReportRepository;
    @Autowired
    private StudentAssignmentRepository studentAssignmentRepository;
    @Autowired
    private ExecutedTestRepository executedTestRepository;

    @Transactional
    @Override
    public void runFaultLocalizationService(int studentAssignmentID) {
        try {
            //checking for studentAssignmentID
            Optional<StudentAssignment> studentAssignmentOptional = studentAssignmentRepository.findById(studentAssignmentID);
            if (studentAssignmentOptional.isEmpty()) {
                LOGGER.error("Assignment ID not found: " + studentAssignmentID);
                return;
            }
            StudentAssignment studentAssignment = studentAssignmentOptional.get();
            String path = PathConfig.getAssignmentPath(studentAssignmentID);
            LOGGER.info("Starting Fault Localization at path:" + path);

            String projectPath = compileProject(path); //test

            FlacocoConfig config = new FlacocoConfig();
            config.setProjectPath(projectPath);

            Flacoco flacoco = new Flacoco(config);
            FlacocoResult result = flacoco.run();

            Set<TestMethod> executedTests = result.getExecutedTests();
            Set<TestMethod> failingTests = result.getFailingTests();

            Set<String> failingTestsName = failingTests.stream()
                    .map(TestMethod::getFullyQualifiedMethodName)
                    .collect(Collectors.toSet());
            executedTests.forEach(test -> {
                ExecutedTest executedTest = new ExecutedTest();
                executedTest.setExecutedTest(test.getFullyQualifiedMethodName());
                executedTest.setFailing(failingTestsName.contains(test.getFullyQualifiedMethodName()));
                LOGGER.info("Test method string: " + test.toString());
                executedTest.setStudentAssignment(studentAssignment);
                executedTestRepository.save(executedTest);
            });


            Map<Location, Suspiciousness> resultMap = result.getDefaultSuspiciousnessMap();
            resultMap.forEach((location, suspiciousness) -> {
                LocalizationReport localizationReport = new LocalizationReport(); // test
                localizationReport.setLocation(location.getClassName());
                localizationReport.setScore(suspiciousness.getScore());
                localizationReport.setLineNumber(location.getLineNumber());
                localizationReport.setStudentAssignment(studentAssignment);
                localizationReportRepository.save(localizationReport);

            });
            LOGGER.info("Wrapping up fault localization process..."); // end debug mode right here to ensure no data on assignment_question is saved

        } catch (Exception e) {
            LOGGER.error("Fault localization error: {}\nPlease check for tests file in the analyzed Java project in case of insufficient test coverage!", e.getMessage());

        }
    }

    public String compileMavenProject(Path projectDir) {
        try {
            Path pomPath = findPomFile(projectDir);
            if (pomPath != null) {
                InvocationRequest request = new DefaultInvocationRequest();
                request.setPomFile(pomPath.toFile());
                request.addArgs(Collections.singletonList("test-compile"));
                request.setBatchMode(true); // non-input request for user

                Invoker invoker = new DefaultInvoker();
                invoker.setMavenHome(new File(System.getenv("MAVEN_HOME")));
                InvocationResult result = invoker.execute(request);

                if (result.getExitCode() != 0) {
                    throw new IllegalStateException("Build failed!");
                }
                LOGGER.info("Maven compilation successful with POM.xml file at: " + pomPath);
                return pomPath.getParent().toString();
            } else {
                LOGGER.info("No pom.xml file found in project directory or its subdirectories");
                return null;
            }
        } catch (Exception e) {
            LOGGER.error("An unexpected error has occurred while compiling Maven project for project path: ", e);
            return null;
        }

    }

    private String compileAntProject(Path projectDir) {
        LOGGER.error("Feature incomplete. ");
        return null;
    }

    private Path findPomFile(Path startPath) throws IOException {
        try (Stream<Path> pathStream = Files.walk(startPath)) {
            return pathStream
                    .filter(p -> p.getFileName().toString().equals("pom.xml"))
                    .findFirst()
                    .orElse(null);
        }
    }

    private ProjectType determineProjectType(Path projectDir) {
        try {
            Path pomPath = findPomFile(projectDir);
            if (pomPath != null) {
                return ProjectType.MAVEN;
            } else if (Files.exists(projectDir.resolve("build.xml"))) {
                return ProjectType.ANT;
            } else {
                return ProjectType.UNKNOWN;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public String compileProject(String projectPath) {
        Path projectDir = Paths.get(projectPath);
        ProjectType type = determineProjectType(projectDir);

        return switch (type) {
            case MAVEN -> compileMavenProject(projectDir);
            case ANT -> compileAntProject(projectDir);
            default -> {
                LOGGER.error("Unsupported project type at path: {}", projectPath);
                yield null;
            }
        };
    }

    public enum ProjectType {
        MAVEN, ANT, GRADLE, UNKNOWN

    }

}
