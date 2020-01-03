package com.justinblank.strings;

import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

public class NFAToDFACompiler {

    private Map<Set<NFA>, DFA> stateSets = new HashMap<>();
    private int state = 1; // root will always be zero
    private DFA root;
    private final NFA nfa;

    private NFAToDFACompiler(NFA nfa) {
        this.nfa = nfa;
    }

    public static DFA compile(NFA nfa) {
        DFA dfa = new NFAToDFACompiler(nfa)._compile(nfa);
        return MinimizeDFA.minimizeDFA(dfa);
    }

    private DFA _compile(NFA nfa) {
        Set<NFA> nfas = nfa.epsilonClosure();
        root = DFA.root(NFA.hasAcceptingState(nfas));
        addNFAStatesToDFA(nfas, root);
        return root;
    }

    private void addNFAStatesToDFA(Set<NFA> nfas, DFA dfa) {
        Stack<Set<NFA>> pending = new Stack<>();
        pending.add(nfas);
        while (!pending.isEmpty()) {
            nfas = pending.pop();
            DFA foundDFA = stateSets.get(nfas);
            if (foundDFA != null) {
                dfa = foundDFA;
            }
            Set<NFA> epsilonClosure = NFA.epsilonClosure(nfas);
            List<CharRange> ranges = CharRange.minimalCovering(findCharRanges(epsilonClosure));
            for (CharRange range : ranges) {
                // any element of the range is equally good here, getStart()/getEnd() doesn't matter
                Set<NFA> moves = NFA.epsilonClosure(transition(epsilonClosure, range.getStart()));
                DFA targetDfa = stateSets.get(moves);
                if (targetDfa == null) {
                    pending.add(moves);
                    boolean accepting = NFA.hasAcceptingState(moves);
                    targetDfa = new DFA(root, accepting, state++);
                    stateSets.put(moves, targetDfa);
                }
                dfa.addTransition(range, targetDfa);
            }
        }
    }

    protected static List<CharRange> findCharRanges(Collection<NFA> nfas) {
        List<CharRange> ranges = new ArrayList<>();
        for (NFA nfa : nfas) {
            List<Pair<CharRange, List<NFA>>> transitionList = nfa.getTransitions();
            for (Pair<CharRange, List<NFA>> pair : transitionList) {
                if (!pair.getLeft().isEmpty()) {
                    ranges.add(pair.getLeft());
                }
            }
        }
        return ranges;
    }

    protected Set<NFA> transition(Collection<NFA> nfaStates, char c) {
        Set<NFA> transitionStates = new HashSet<>();
        for (NFA source : nfaStates) {
            BitSet bs = source.transition(c);
            int set = -1;
            for (int i = bs.nextSetBit(0); i >= 0; i = bs.nextSetBit(i + 1)) {
                transitionStates.add(nfa.getState(i));
            }
        }
        return transitionStates;
    }


}
