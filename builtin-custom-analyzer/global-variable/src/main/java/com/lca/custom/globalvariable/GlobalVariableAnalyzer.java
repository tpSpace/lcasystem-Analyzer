package com.lca.custom.globalvariable;

import com.lca.core.CustomAnalyzer;
import com.lca.core.dto.LCAGenerated;
import com.lca.core.dto.LCATemplate;
import com.lca.core.entity.Level;
import com.lca.core.entity.Scope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.CtModel;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtLoop;
import spoon.reflect.code.CtSwitch;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtVariable;
import spoon.reflect.reference.CtVariableReference;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.*;

public class GlobalVariableAnalyzer implements CustomAnalyzer {
    Logger LOGGER = LoggerFactory.getLogger(GlobalVariableAnalyzer.class);

    private final String LINE_REPLACEMENT = "{line}";
    private final String CLASS_REPLACEMENT = "{class}";
    private final String VARIABLE_REPLACEMENT = "{variable}";

    @Override
    public List<LCAGenerated> analyze(CtModel ctModel, int studentAssignmentId) {
        List<LCATemplate> templateList = this.getLCATemplateList();
        LOGGER.info("Got question template for global variable: " + templateList.size());

        PriorityQueue<CtClass> ctClassPriorityQueue = this.getAllClassWithVariable(ctModel);

        LOGGER.info("Got method with global variable list: " + ctClassPriorityQueue.size());

        List<LCAGenerated> generatedList = new ArrayList<>();

        Random rand = new Random();

        while (!ctClassPriorityQueue.isEmpty() && !templateList.isEmpty()) {
            CtClass ctClass = ctClassPriorityQueue.poll();
            CtVariable ctVariable = this.getHighestUseVariable(ctClass);

            LCATemplate questionTemplateDTO = templateList.get(rand.nextInt(templateList.size()));
            templateList.remove(questionTemplateDTO);

            generatedList.add(this.parseGlobalVariableQuestion(
                    ctVariable, questionTemplateDTO, studentAssignmentId
            ));
        }

        return generatedList;
    }

    @Override
    public String getAnalyzerName() {
        return "GLOBAL_VARIABLE";
    }

    @Override
    public List<LCATemplate> getLCATemplateList() {
        List<LCATemplate> templateList = new ArrayList<>();
        templateList.add(new LCATemplate(
                "How many assignment statements are related to variable {variable} of class {class}? What are those?",
                Scope.TEXT,
                Level.ATOM
        ));
        templateList.add(new LCATemplate(
                "Identify the scope of variable {variable} declared in class {class}.",
                Scope.TEXT,
                Level.RELATION
        ));
        templateList.add(new LCATemplate(
                "Given the function of class {class} of your choice, explain how the value of variable {variable} change during execution.",
                Scope.EXECUTION,
                Level.ATOM
        ));
        templateList.add(new LCATemplate(
                "Explain the purpose of variable {variable} in class {class}. How {variable} help {class} fulfill its goals?",
                Scope.FUNCTION,
                Level.ATOM
        ));
        templateList.add(new LCATemplate(
                "Why variable {variable} named that way? How this name reflect its responsibilities in class {class}?",
                Scope.FUNCTION,
                Level.RELATION
        ));
        return templateList;
    }

    public LCAGenerated parseGlobalVariableQuestion(CtVariable ctVariable, LCATemplate template, int studentAssignmentId) {
        String text = template.getTemplate();
        text = replaceLine(text, String.valueOf(ctVariable.getPosition().getLine()));
        text = replaceClass(text, ctVariable.getParent(CtClass.class).getQualifiedName());
        text = replaceVariable(text, ctVariable.getSimpleName());
        String helpText = generateHelpText(template.getTemplate(), ctVariable);
        return new LCAGenerated(
                text,
                "",
                template.getLevel(),
                template.getScope(),
                studentAssignmentId
        );
    }

    private String replaceLine(String text, String replace) {
        if (text.contains(LINE_REPLACEMENT)) {
            return text.replace(LINE_REPLACEMENT, String.valueOf(replace));
        }
        return text;
    }

    private String replaceClass(String text, String replace) {
        if (text.contains(CLASS_REPLACEMENT)) {
            return text.replace(CLASS_REPLACEMENT, replace);
        }
        return text;
    }

    private String replaceVariable(String text, String replace) {
        if (text.contains(VARIABLE_REPLACEMENT)) {
            return text.replace(VARIABLE_REPLACEMENT, replace);
        }
        return text;
    }

    private String generateHelpText(String template, CtVariable ctVariable) {
        if (template.contains(CLASS_REPLACEMENT)) {
            LOGGER.info(ctVariable.getParent(CtClass.class).toString());
            return ctVariable.getParent(CtClass.class).toString();
        }
        return null;
    }

    private PriorityQueue<CtClass> getAllClassWithVariable(CtModel ctModel) {
        LOGGER.info("Getting all class that have variable declaration.");
        List<CtClass> ctClassList = ctModel.getElements(new TypeFilter<>(CtClass.class));
        ctClassList.removeIf(ctClass -> ctClass.getElements(new TypeFilter<>(CtVariable.class)).isEmpty());
        LOGGER.info("Got all class with variable declaration: " + ctClassList.size());
        PriorityQueue<CtClass> ctClassPriorityQueue = new PriorityQueue<>(new GlobalVariableComparator());
        ctClassPriorityQueue.addAll(ctClassList);
        return ctClassPriorityQueue;
    }

    private CtVariable getHighestUseVariable(CtClass ctClass) {
        HashMap<CtVariable, Integer> globalVariableReferenceFrequency = new HashMap<>();
        List<CtVariable> ctVariableList = ctClass.getElements(new TypeFilter<>(CtVariable.class));
        List<CtVariableReference> ctVariableReferenceList = ctClass.getElements(new TypeFilter<>(CtVariableReference.class));

        LOGGER.info("Getting highest use variable of method " + ctClass.getQualifiedName());

        ctVariableReferenceList.forEach(ctVariableReference -> {
            if (ctVariableList.contains(ctVariableReference.getDeclaration())) {
                CtVariable ctVariable = ctVariableReference.getDeclaration();
                globalVariableReferenceFrequency.put(ctVariable, globalVariableReferenceFrequency.getOrDefault(ctVariable, 0) + 1);
            }
        });

        LOGGER.info("Highest use local variable map: " + globalVariableReferenceFrequency);

        CtVariable highestUseVariable = ctVariableList.get(0);
        for (CtVariable ctVariable : globalVariableReferenceFrequency.keySet()) {
            if (globalVariableReferenceFrequency.get(ctVariable) > globalVariableReferenceFrequency.get(highestUseVariable)) {
                highestUseVariable = ctVariable;
            }
        }

        LOGGER.info("Highest use variable of method " + ctClass.getQualifiedName() + " is " + highestUseVariable.getSimpleName());

        return highestUseVariable;
    }

    private class GlobalVariableComparator implements Comparator<CtClass> {
        @Override
        public int compare(CtClass class1, CtClass class2) {
            double class1Complexity = this.calculateClassComplexity(class1);
            double class2Complexity = this.calculateClassComplexity(class2);

            return Double.compare(class2Complexity, class1Complexity);
        }

        public double calculateClassComplexity(CtClass ctClass) {
            List<CtMethod> ctMethodList = ctClass.getElements(new TypeFilter<>(CtMethod.class));
            double totalMethodComplexity = 0;
            for (CtMethod ctMethod : ctMethodList) {
                totalMethodComplexity += calculateMethodComplexity(ctMethod);
            }
            return totalMethodComplexity / ctMethodList.size();
        }

        public int calculateMethodComplexity(CtMethod ctMethod) {
            int methodComplexity = 1;
            methodComplexity += ctMethod.getElements(new TypeFilter<>(CtLoop.class)).size();
            methodComplexity += ctMethod.getElements(new TypeFilter<>(CtIf.class)).size();

            for (CtSwitch ctSwitch : ctMethod.getElements(new TypeFilter<>(CtSwitch.class))) {
                methodComplexity += ctSwitch.getCases().size();
            }

            return methodComplexity;
        }
    }

}
