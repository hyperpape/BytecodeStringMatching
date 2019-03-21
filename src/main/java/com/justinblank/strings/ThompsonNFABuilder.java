package com.justinblank.strings;

import com.justinblank.strings.RegexAST.*;

import java.util.Collections;
import java.util.List;

// TODO: This construction is like the Thompson construction. Verify and name appropriately
class ThompsonNFABuilder {

    public static NFA createNFA(Node ast) {
        NFA nfa = createPartial(ast);
        NFA finalState = new NFA(true);
        for (NFA terminal : nfa.terminalStates()) {
            terminal.addEpsilonTransition(finalState);
        }
        return nfa;
    }

    protected static NFA createPartial(Node ast) {
        NFA nfa;
        if (ast instanceof Concatenation) {
            Concatenation c = (Concatenation) ast;
            NFA head = createPartial(c.head);
            NFA tail = createPartial(c.tail);
            for (NFA terminal : head.terminalStates()) {
                terminal.addTransitions(CharRange.emptyRange(), Collections.singletonList((tail)));
            }
            nfa = head;
        }
        else if (ast instanceof Repetition) {
            Repetition r = (Repetition) ast;
            NFA child = createPartial(r.node);
            nfa = child;
            NFA end = new NFA(false);
            for (NFA terminal : nfa.terminalStates()) {
                terminal.addEpsilonTransition(nfa);
                terminal.addEpsilonTransition(end);
            }
            nfa.addEpsilonTransition(end);
        }
        // TODO: revisit this solution. It will perform badly, and the bytecode
        //  compiler would do better with higher level info about repetitions
        else if (ast instanceof CountedRepetition) {
            CountedRepetition countedRepetition = (CountedRepetition) ast;
            NFA child = new NFA(false);
            // NFA child = createPartial(countedRepetition.node);
            nfa = child;
            NFA end = new NFA(false);
            int repetition = 0;
            for (; repetition < countedRepetition.min; repetition++) {
                NFA child2 = createPartial(countedRepetition.node);
                for (NFA terminal : child.terminalStates()) {
                    terminal.addEpsilonTransition(child2);
                }
                child = child2;
            }
            for (; repetition < countedRepetition.max; repetition++) {
                NFA child2 = createPartial(countedRepetition.node);
                for (NFA terminal : child.terminalStates()) {
                    terminal.addEpsilonTransition(child2);
                    terminal.addEpsilonTransition(end);
                }
                child = child2;
            }
        }
        else if (ast instanceof Alternation) {
            Alternation a = (Alternation) ast;
            nfa = new NFA(false);
            NFA left = createPartial(a.left);
            NFA right = createPartial(a.right);
            nfa.addTransitions(CharRange.emptyRange(), List.of(left, right));
            NFA end = new NFA(false);
            left.terminalStates().forEach(n -> n.addEpsilonTransition(end));
            right.terminalStates().forEach(n -> n.addEpsilonTransition(end));
        }
        else if (ast instanceof CharRangeNode) {
            CharRangeNode range = (CharRangeNode) ast;
            nfa = new NFA(false);
            NFA end = new NFA(false);
            nfa.addTransitions(range.range(), Collections.singletonList(end));
        }
        else {
            throw new IllegalStateException("");
        }
        return nfa;
    }

}