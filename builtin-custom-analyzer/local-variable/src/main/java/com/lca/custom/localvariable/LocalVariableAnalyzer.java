package com.lca.custom.localvariable;

import com.lca.core.CustomAnalyzer;
import com.lca.core.dto.LCAGenerated;
import com.lca.core.dto.LCATemplate;
import com.lca.core.entity.Level;
import com.lca.core.entity.Scope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.CtModel;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtLoop;
import spoon.reflect.code.CtSwitch;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.reference.CtLocalVariableReference;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.*;

public class LocalVariableAnalyzer implements CustomAnalyzer {
    Logger LOGGER = LoggerFactory.getLogger(LocalVariableAnalyzer.class);

    private final String LINE_REPLACEMENT = "{line}";
    private final String CLASS_REPLACEMENT = "{class}";
    private final String VARIABLE_REPLACEMENT = "{variable}";
    private final String FUNCTION_REPLACEMENT = "{function}";

    @Override
    public List<LCAGenerated> analyze(CtModel ctModel, int studentAssignmentId) {
        List<LCATemplate> templateList = this.getLCATemplateList();
        LOGGER.info("Got question template for global variable: " + templateList.size());

        PriorityQueue<CtMethod> ctMethodPriorityQueue = this.getAllMethodWithLocalVariable(ctModel);

        LOGGER.info("Got method with global variable list: " + ctMethodPriorityQueue.size());

        List<LCAGenerated> generatedList = new ArrayList<>();

        Random rand = new Random();

        while (!ctMethodPriorityQueue.isEmpty() && !templateList.isEmpty()) {
            CtMethod ctMethod = ctMethodPriorityQueue.poll();
            CtLocalVariable ctLocalVariable = this.getHighestUseLocalVariable(ctMethod);

            LCATemplate questionTemplateDTO = templateList.get(rand.nextInt(templateList.size()));
            templateList.remove(questionTemplateDTO);

            generatedList.add(this.parseLocalVariableQuestion(
                    ctLocalVariable, questionTemplateDTO, studentAssignmentId
            ));
        }

        return generatedList;
    }

    @Override
    public String getAnalyzerName() {
        return "LOCAL_VARIABLE";
    }

    @Override
    public List<LCATemplate> getLCATemplateList() {
        List<LCATemplate> LCATemplateList = new ArrayList<>();
        LCATemplateList.add(new LCATemplate(
                "Identify all assignment statements related to variable {variable} of function {function}.",
                Scope.TEXT,
                Level.ATOM));
        LCATemplateList.add(new LCATemplate(
                "Identify the scope of variable {variable} declared in function {function}.",
                Scope.TEXT,
                Level.RELATION));
        LCATemplateList.add(new LCATemplate(
                "Given the input data of your choice of function {function}, determine how the value of variable {variable} change or how variable {variable} affect the value of the returned data during execution.",
                Scope.EXECUTION,
                Level.ATOM));
        LCATemplateList.add(new LCATemplate(
                "Does variable {variable} hold any specific role (e.g. walker, most recent holder) in function {function}? Explain.",
                Scope.EXECUTION,
                Level.RELATION));
        LCATemplateList.add(new LCATemplate(
                "Explain how the computational flows of function {function} change according to the value of {variable}.",
                Scope.EXECUTION,
                Level.MACRO));
        LCATemplateList.add(new LCATemplate(
                "How variable {variable} help function {function} complete its goals?",
                Scope.FUNCTION,
                Level.ATOM));
        return LCATemplateList;
    }

    private LCAGenerated parseLocalVariableQuestion(
            CtLocalVariable ctLocalVariable,
            LCATemplate template,
            int studentAssignmentId
    ) {
        String text = template.getTemplate();
        text = replaceLine(text, String.valueOf(ctLocalVariable.getPosition().getLine()));
        text = replaceClass(text, ctLocalVariable.getParent(CtClass.class).getSimpleName());
        text = replaceFunction(text, ctLocalVariable.getParent(CtMethod.class).getSimpleName());
        text = replaceVariable(text, ctLocalVariable.getSimpleName());
            return new LCAGenerated(
                text,
                "",
                template.getLevel(),
                template.getScope(),
                studentAssignmentId);
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

    private String replaceFunction(String text, String replace) {
        if (text.contains(FUNCTION_REPLACEMENT)) {
            return text.replace(FUNCTION_REPLACEMENT, replace + "()");
        }
        return text;
    }

    private CtLocalVariable getHighestUseLocalVariable(CtMethod ctMethod) {
        HashMap<CtLocalVariable, Integer> localVariableReferenceFrequency = new HashMap<>();
        List<CtLocalVariable> ctLocalVariableList = ctMethod.getElements(new TypeFilter<>(CtLocalVariable.class));
        List<CtLocalVariableReference> ctLocalVariableReferenceList = ctMethod.getElements(new TypeFilter<>(CtLocalVariableReference.class));

        LOGGER.info("Getting highest use variable of method " + ctMethod.getSimpleName());

        ctLocalVariableReferenceList.forEach(ctLocalVariableReference -> {
            if (ctLocalVariableList.contains(ctLocalVariableReference.getDeclaration())) {
                CtLocalVariable ctLocalVariable = ctLocalVariableReference.getDeclaration();
                localVariableReferenceFrequency.put(ctLocalVariable, localVariableReferenceFrequency.getOrDefault(ctLocalVariable, 0) + 1);
            }
        });

        LOGGER.info("Highest use local variable map: " + localVariableReferenceFrequency);

        CtLocalVariable highestUseVariable = ctLocalVariableList.get(0);
        for (CtLocalVariable ctLocalVariable : localVariableReferenceFrequency.keySet()) {
            if (localVariableReferenceFrequency.get(ctLocalVariable) > localVariableReferenceFrequency.get(highestUseVariable)) {
                highestUseVariable = ctLocalVariable;
            }
        }

        LOGGER.info("Highest use variable of method " + ctMethod.getSimpleName() + " is " + highestUseVariable.getSimpleName());

        return highestUseVariable;
    }

    private PriorityQueue<CtMethod> getAllMethodWithLocalVariable(CtModel ctModel) {
        LOGGER.info("Getting all method that have variable declaration.");
        List<CtMethod> ctMethodList = ctModel.getElements(new TypeFilter<>(CtMethod.class));
        ctMethodList.removeIf(ctMethod -> ctMethod.getParent(CtClass.class) == null || ctMethod.getElements(new TypeFilter<>(CtLocalVariable.class)).isEmpty());
        LOGGER.info("Got all method with variable declaration: " + ctMethodList.size());
        PriorityQueue<CtMethod> ctMethodPriorityQueue = new PriorityQueue<>(new LocalVariableComparator());
        ctMethodPriorityQueue.addAll(ctMethodList);
        LOGGER.info("Got " + ctMethodPriorityQueue.size() + " methods.");
        return ctMethodPriorityQueue;
    }

    private class LocalVariableComparator implements Comparator<CtMethod> {
        @Override
        public int compare(CtMethod method1, CtMethod method2) {
            int method1Complexity = this.calculateMethodComplexity(method1);
            int method2Complexity = this.calculateMethodComplexity(method2);

            return Integer.compare(method2Complexity, method1Complexity);
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
