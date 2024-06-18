package com.lca.custom.nestedloop;

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

public class NestedLoopAnalyzer implements CustomAnalyzer {
    private final String LINE_REPLACEMENT = "{line}";
    private final String CLASS_REPLACEMENT = "{class}";
    private final String FUNCTION_REPLACEMENT = "{function}";

    @Override
    public List<LCAGenerated> analyze(CtModel ctModel, int studentAssignmentId) {
        List<LCATemplate> templateList = this.getLCATemplateList();

        PriorityQueue<CtLoop> ctLoopPriorityQueue = this.getNestedLoopList(ctModel);

        List<LCAGenerated> generatedList = new ArrayList<>();

        Random rand = new Random();

        while (!ctLoopPriorityQueue.isEmpty() && !templateList.isEmpty()) {
            CtLoop ctLoop = ctLoopPriorityQueue.poll();

            LCATemplate questionTemplateDTO = templateList.get(rand.nextInt(templateList.size()));
            templateList.remove(questionTemplateDTO);

            generatedList.add(this.parseNestedLoopQuestion(
                    ctLoop, questionTemplateDTO, studentAssignmentId
            ));
        }

        return generatedList;
    }

    @Override
    public String getAnalyzerName() {
        return "NESTED_LOOP";
    }

    @Override
    public List<LCATemplate> getLCATemplateList() {
        List<LCATemplate> LCATemplateList = new ArrayList<>();
        LCATemplateList.add(new LCATemplate(
                "Describe the structure of the loop statement in line {line} of function {function}.",
                Scope.TEXT,
                Level.MACRO));
        LCATemplateList.add(new LCATemplate(
                "Given a variable of your choice, explain how the loop statement in line {line} of function {function} change its value.",
                Scope.EXECUTION,
                Level.ATOM));
        LCATemplateList.add(new LCATemplate(
                "Given an initial state of your choice, determine the number of iterations of the loop statement defined in line {line} of function {function}.",
                Scope.EXECUTION,
                Level.BLOCK));
        LCATemplateList.add(new LCATemplate(
                "Could any initial state of the loop statement in line {line} of function {function} results in an infinite loop? Explain why, and if yes, is there anyway to avoid?",
                Scope.EXECUTION,
                Level.RELATION));
        LCATemplateList.add(new LCATemplate(
                "Estimate the time complexity of function {function}. Explain.",
                Scope.EXECUTION,
                Level.MACRO));
        return LCATemplateList;
    }

    private PriorityQueue<CtLoop> getNestedLoopList(CtModel ctModel) {
        List<CtLoop> ctLoopList = ctModel.getElements(new TypeFilter<>(CtLoop.class));
        ctLoopList.removeIf(ctLoop -> {
            List<CtLoop> ctLoopChildren = ctLoop.getElements(new TypeFilter<>(CtLoop.class));
            ctLoopChildren.remove(ctLoop);
            return ctLoopChildren.isEmpty();
        });
        PriorityQueue<CtLoop> ctLoopPriorityQueue = new PriorityQueue<>(new NestedLoopComparator());
        ctLoopPriorityQueue.addAll(ctLoopList);
        return ctLoopPriorityQueue;
    }

    public LCAGenerated parseNestedLoopQuestion(
            CtLoop ctLoop,
            LCATemplate template,
            int studentAssignmentId) {
        String text = template.getTemplate();
        text = replaceLine(text, String.valueOf(ctLoop.getPosition().getLine()));
        text = replaceClass(text, ctLoop.getParent(CtClass.class).getQualifiedName());
        text = replaceFunction(text, ctLoop.getParent(CtMethod.class).getSimpleName());
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

    private class NestedLoopComparator implements Comparator<CtLoop> {
        @Override
        public int compare(CtLoop loop1, CtLoop loop2) {
            int loop1Complexity = this.calculateLoopComplexity(loop1);
            int loop2Complexity = this.calculateLoopComplexity(loop2);

            return Integer.compare(loop2Complexity, loop1Complexity);
        }

        private int calculateLoopComplexity(CtLoop ctLoop) {
            int loopComplexity = 1;
            loopComplexity += ctLoop.getElements(new TypeFilter<>(CtIf.class)).size();
            loopComplexity += ctLoop.getElements(new TypeFilter<>(CtLoop.class)).size();

            for (CtSwitch ctSwitch : ctLoop.getElements(new TypeFilter<>(CtSwitch.class))) {
                loopComplexity += ctSwitch.getCases().size();
            }

            return loopComplexity;
        }
    }
}
