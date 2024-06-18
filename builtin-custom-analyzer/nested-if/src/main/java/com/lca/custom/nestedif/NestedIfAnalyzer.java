package com.lca.custom.nestedif;

import com.lca.core.CustomAnalyzer;
import com.lca.core.dto.LCAGenerated;
import com.lca.core.dto.LCATemplate;
import com.lca.core.entity.Level;
import com.lca.core.entity.Scope;
import spoon.reflect.CtModel;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtLoop;
import spoon.reflect.code.CtSwitch;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.*;

public class NestedIfAnalyzer implements CustomAnalyzer {
    private final String LINE_REPLACEMENT = "{line}";
    private final String CLASS_REPLACEMENT = "{class}";
    private final String FUNCTION_REPLACEMENT = "{function}";

    @Override
    public List<LCAGenerated> analyze(CtModel ctModel, int studentAssignmentId) {
        List<LCATemplate> templateList = this.getLCATemplateList();

        PriorityQueue<CtIf> ctIfPriorityQueue = this.getAllNestedIf(ctModel);

        List<LCAGenerated> generatedList = new ArrayList<>();

        Random rand = new Random();

        while (!ctIfPriorityQueue.isEmpty() && !templateList.isEmpty()) {
            CtIf ctIf = ctIfPriorityQueue.poll();

            LCATemplate questionTemplateDTO = templateList.get(rand.nextInt(templateList.size()));
            templateList.remove(questionTemplateDTO);

            generatedList.add(this.parseNestedIfQuestion(
                    ctIf, questionTemplateDTO, studentAssignmentId
            ));
        }

        return generatedList;
    }

    @Override
    public String getAnalyzerName() {
        return "NESTED_IF";
    }

    @Override
    public List<LCATemplate> getLCATemplateList() {
        List<LCATemplate> LCATemplateList = new ArrayList<>();
        LCATemplateList.add(new LCATemplate(
                "Identify all possible blocks of the conditional statement in line {line} of function {function}.",
                Scope.TEXT,
                Level.BLOCK));
        LCATemplateList.add(new LCATemplate(
                "Describe the structure of the conditional statement in line {line} of function {function}.",
                Scope.TEXT,
                Level.MACRO));
        LCATemplateList.add(new LCATemplate(
                "Given the input data of your choice, explain the computational flows of the conditional statement in line {line} of function {function}.",
                Scope.EXECUTION,
                Level.ATOM));
        LCATemplateList.add(new LCATemplate(
                "Given a variable of your choice, explain how the conditional statement in line {line} of function {function} change its value.",
                Scope.EXECUTION,
                Level.ATOM));
        LCATemplateList.add(new LCATemplate(
                "Identify a set of inputs that will check all possible computation flows of function {function}.",
                Scope.EXECUTION,
                Level.MACRO));
        LCATemplateList.add(new LCATemplate(
                "How the conditional statement in line {line} help function {function} complete its goals?",
                Scope.FUNCTION,
                Level.BLOCK));
        return LCATemplateList;
    }

    public LCAGenerated parseNestedIfQuestion(
            CtIf ctIf,
            LCATemplate template,
            int studentAssignmentId) {
        String text = template.getTemplate();
        text = replaceLine(text, String.valueOf(ctIf.getPosition().getLine()));
        text = replaceClass(text, ctIf.getParent(CtClass.class).getQualifiedName());
        text = replaceFunction(text, ctIf.getParent(CtMethod.class).getSimpleName());
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

    private String replaceFunction(String text, String replace) {
        if (text.contains(FUNCTION_REPLACEMENT)) {
            return text.replace(FUNCTION_REPLACEMENT, replace + "()");
        }
        return text;
    }

    private PriorityQueue<CtIf> getAllNestedIf(CtModel ctModel) {
        List<CtIf> ctIfList = ctModel.getElements(new TypeFilter<>(CtIf.class));
        ctIfList.removeIf(ctIf -> {
            List<CtIf> ctIfChildren = ctIf.getElements(new TypeFilter<>(CtIf.class));
            ctIfChildren.remove(ctIf);
            return ctIfChildren.isEmpty();
        });
        PriorityQueue<CtIf> ctIfPriorityQueue = new PriorityQueue<>(new NestedIfComparator());
        ctIfPriorityQueue.addAll(ctIfList);
        return ctIfPriorityQueue;
    }

    private class NestedIfComparator implements Comparator<CtIf> {
        @Override
        public int compare(CtIf if1, CtIf if2) {
            int if1Complexity = this.calculateIfComplexity(if1);
            int if2Complexity = this.calculateIfComplexity(if2);

            return Integer.compare(if2Complexity, if1Complexity);
        }

        private int calculateIfComplexity(CtIf ctIf) {
            int ifComplexity = 1;
            ifComplexity += ctIf.getElements(new TypeFilter<>(CtIf.class)).size();
            ifComplexity += ctIf.getElements(new TypeFilter<>(CtLoop.class)).size();

            for (CtSwitch ctSwitch : ctIf.getElements(new TypeFilter<>(CtSwitch.class))) {
                ifComplexity += ctSwitch.getCases().size();
            }

            return ifComplexity;
        }
    }
}
