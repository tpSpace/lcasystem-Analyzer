package com.lca.custom.recursionfunction;

import com.lca.core.CustomAnalyzer;
import com.lca.core.dto.LCAGenerated;
import com.lca.core.dto.LCATemplate;
import com.lca.core.entity.Level;
import com.lca.core.entity.Scope;
import spoon.reflect.CtModel;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLoop;
import spoon.reflect.code.CtSwitch;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.*;

public class RecursionFunctionAnalyzer implements CustomAnalyzer {
    private final String LINE_REPLACEMENT = "{line}";
    private final String CLASS_REPLACEMENT = "{class}";

    @Override
    public List<LCAGenerated> analyze(CtModel ctModel, int studentAssignmentId) {
        List<LCATemplate> templateList = this.getLCATemplateList();

        PriorityQueue<CtMethod> ctMethodPriorityQueue = this.getAllRecursionFunction(ctModel);

        List<LCAGenerated> generatedList = new ArrayList<>();

        Random rand = new Random();

        while (!ctMethodPriorityQueue.isEmpty() && !templateList.isEmpty()) {
            CtMethod ctMethod = ctMethodPriorityQueue.poll();

            LCATemplate questionTemplateDTO = templateList.get(rand.nextInt(templateList.size()));
            templateList.remove(questionTemplateDTO);

            generatedList.add(this.parseRecursionFunctionQuestion(
                    ctMethod, questionTemplateDTO, studentAssignmentId
            ));
        }

        return generatedList;
    }

    @Override
    public String getAnalyzerName() {
        return "RECURSION_FUNCTION";
    }

    @Override
    public List<LCATemplate> getLCATemplateList() {
        List<LCATemplate> LCATemplateList = new ArrayList<>();
        LCATemplateList.add(new LCATemplate(
                "Given the inputs of your choice, what is the program output for function {function} of class {class}?",
                Scope.EXECUTION,
                Level.ATOM));
        LCATemplateList.add(new LCATemplate(
                "How the call stacks grow when executing function {function} of class {class}, given inputs of your choice?",
                Scope.EXECUTION,
                Level.RELATION));
        LCATemplateList.add(new LCATemplate(
                "Create meaningful test cases for the allowed inputs and expected outputs of function {function} in class {class}.",
                Scope.FUNCTION,
                Level.RELATION));
        LCATemplateList.add(new LCATemplate(
                "What is the purpose of function {function} in class {class}?",
                Scope.FUNCTION,
                Level.RELATION));
        return LCATemplateList;
    }

    public LCAGenerated parseRecursionFunctionQuestion(
            CtMethod ctMethod,
            LCATemplate template,
            int studentAssignmentId) {
        String text = template.getTemplate();
        text = replaceLine(text, String.valueOf(ctMethod.getPosition().getLine()));
        text = replaceClass(text, ctMethod.getParent(CtClass.class).getQualifiedName());
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

    public PriorityQueue<CtMethod> getAllRecursionFunction(CtModel ctModel) {
        List<CtMethod> ctMethodList = ctModel.getElements(new TypeFilter<>(CtMethod.class));
        ctMethodList.removeIf(ctMethod -> {
            List<CtInvocation> ctInvocationList = ctMethod.getElements(new TypeFilter<>(CtInvocation.class));
            ctInvocationList.removeIf(ctInvocation ->
                    ctInvocation.getExecutable().getDeclaration() == null
                            || !ctInvocation.getExecutable().getDeclaration().equals(ctMethod));
            return ctInvocationList.isEmpty();
        });

        PriorityQueue<CtMethod> ctMethodPriorityQueue = new PriorityQueue<>(new RecursionFunctionComparator());
        ctMethodPriorityQueue.addAll(ctMethodList);
        return ctMethodPriorityQueue;
    }

    private class RecursionFunctionComparator implements Comparator<CtMethod> {
        @Override
        public int compare(CtMethod method1, CtMethod method2) {
            int method1Complexity = this.calculateMethodComplexity(method1);
            int method2Complexity = this.calculateMethodComplexity(method2);

            return Integer.compare(method2Complexity, method1Complexity);
        }

        private int calculateMethodComplexity(CtMethod ctMethod) {
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
