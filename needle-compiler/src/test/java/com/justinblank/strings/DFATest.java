package com.justinblank.strings;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DFATest {

    @Test
    public void testCalculateOffsetRepetition() {
        var dfa = DFA.createDFA("b*c");
        assertThat(dfa.calculateOffset()).isEmpty();
    }

    @Test
    public void testCalculateOffsetLiteral() {
        var dfa = DFA.createDFA("abc");
        var offset = dfa.calculateOffset();
        check(offset, 2, 'c', 'c');
    }

    @Test
    public void testCalculateOffsetLiteralFollowedByRange() {
        var dfa = DFA.createDFA("ab[c-d]");
        var optOffset = dfa.calculateOffset();
        check(optOffset, 2, 'c', 'd');
    }

    @Test
    public void testCalculateOffsetWithInternalCharRange() {
        var dfa = DFA.createDFA("a[0-9]c");
        var optOffset = dfa.calculateOffset();
        check(optOffset, 2, 'c', 'c');
    }

    @Test
    public void testCalculateOffsetWithInternalUnionWithSameChild() {
        var dfa = DFA.createDFA("a([0-9]|b)c");
        var optOffset = dfa.calculateOffset();
        check(optOffset, 2, 'c', 'c');
    }

    @Test
    public void testCalculateOffsetWithInternalUnionWithDifferentChild() {
        var dfa = DFA.createDFA("a(([0-9]c)|([a-z]d))");
        var optOffset = dfa.calculateOffset();
        assertThat(optOffset).isEmpty();
    }

    @Test
    public void testLiteralPrefixGivesOffset() {
        var dfa = DFA.createDFA("abc*");
        var optOffset = dfa.calculateOffset();
        check(optOffset, 1, 'b', 'b');
    }

    @Test
    public void testOffsetForCountedRepetitionWithNoOffset() {
        var dfa = DFA.createDFA("A{0,4}");
        var optOffset = dfa.calculateOffset();
        assertThat(optOffset).isEmpty();
    }

    @Test
    public void testOffsetForCountedRepetitionWithNonZeroOffset() {
        var dfa = DFA.createDFA("A{3,4}");
        var optOffset = dfa.calculateOffset();
        check(optOffset, 2, 'A', 'A');
    }

    @Test
    public void testChainForCountedRepetition() {
        var dfa = DFA.createDFA("A{3,4}");
        var chain = dfa.chain();
        assertEquals(2, chain.size());
    }

    @Test
    public void testChainForAlternation() {
        var dfa = DFA.createDFA("(A|B)");
        var chain = dfa.chain();
        assertEquals(0, chain.size());
    }

    @Test
    public void testChainLiteralFollowedByAlternation() {
        var dfa = DFA.createDFA("AB(A|B)");
        var chain = dfa.chain();
        assertEquals(2, chain.size());
    }

    @Test
    public void testChainLiteralFollowedByRepetition() {
        var dfa = DFA.createDFA("ABa*");
        var chain = dfa.chain();
        assertEquals(1, chain.size());
    }

    @Test
    public void testChainLiteralFollowedByRepetitionOfAlternation() {
        var dfa = DFA.createDFA("AB(a|c)*");
        var chain = dfa.chain();
        assertEquals(1, chain.size());
    }

    private void check(Optional<Offset> optOffset, int i, char start, char end) {
        assertTrue(optOffset.map(o -> {
            assertEquals(i, o.length);
            assertEquals(new CharRange(start, end), o.charRange);
            return true;
        }).orElse(false));
    }

    @Test
    public void testOffsetsDiamond() {
        var dfa = DFA.createDFA("ab(cd|ef)ghi");
        var offsets = dfa.calculateOffsets();
        // offsets before 'a', 'c', 'e'
        assertEquals(3, offsets.size());
    }

    @Test
    public void testOffsetsFakeFork() {
        var dfa = DFA.createDFA("(a|c)efg");
        var offsets = dfa.calculateOffsets();
        assertEquals(1, offsets.size());
    }

    @Test
    public void testOffsetsFork() {
        var dfa = DFA.createDFA("(ab|cd)efg");
        var offsets = dfa.calculateOffsets();
        assertEquals(2, offsets.size());
    }
}
