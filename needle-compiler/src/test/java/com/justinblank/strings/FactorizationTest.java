package com.justinblank.strings;

import org.junit.Test;

import java.util.*;

import static com.justinblank.strings.Factorization.best;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class FactorizationTest {

    @Test
    public void testBestDifferentLengths() {
        var set1 = Set.of("abc", "def", "ghi");
        var set2 = Set.of("abc", "def");
        assertEquals(set2, best(set1, set2));
    }

    @Test
    public void testBestNull() {
        Set<String> set1 = null;
        Set<String> set2 = null;
        assertEquals(null, best(set1, set2));
    }

    @Test
    public void testBestEmpty() {
        Set<String> set1 = Collections.emptySet();
        Set<String> set2 = Collections.emptySet();
        assertEquals(set2, best(set1, set2));
    }

    @Test
    public void testBest1Empty() {
        Set<String> set1 = Collections.emptySet();
        Set<String> set2 = Set.of("A");
        assertEquals(set2, best(set1, set2));
    }

    @Test
    public void testBestDifferentDominated() {
        var set1 = Set.of("abc12", "def12", "ghi12");
        var set2 = Set.of("abc", "def");
        assertEquals(set1, best(set1, set2));
    }

    @Test
    public void testBestSameLength() {
        var set1 = Set.of("abc", "def", "ghi");
        var set2 = Set.of("abc", "def", "ghi12");
        assertEquals(set2, best(set1, set2));
    }

    @Test
    public void testFactorizationSimpleConcatentation() {
        var node = RegexParser.parse("AB");
        assertEquals(Set.of("AB"), node.bestFactors().getFactors());
    }

    @Test
    public void testFactorizationRepetitionAndConcatenation() {
        var node = RegexParser.parse("A*B");
        Factorization factorization = node.bestFactors();
        assertTrue(factorization.getAll() == null || factorization.getAll().isEmpty());
        assertEquals(Set.of("B"), factorization.getSuffixes());
        assertTrue(factorization.getPrefixes() == null || factorization.getPrefixes().isEmpty());
        assertEquals(Set.of("B"), factorization.getFactors());
    }

    @Test
    public void testFactorization() {
        var node = RegexParser.parse("((GA|AAA)*)(TA|AG)");
        assertEquals(Set.of("TA", "AG"), node.bestFactors().getFactors());
    }

    @Test
    public void testFactorizationCharRangeUnion() {
        var node = RegexParser.parse("[AB][CD]");
        var factorization = node.bestFactors();
        var factorSet = Set.of("BC", "AC", "BD", "AD");
        assertEquals(factorSet, factorization.getPrefixes());
        assertEquals(factorSet, factorization.getSuffixes());
        assertEquals(factorSet, factorization.getFactors());
        assertEquals(factorSet, factorization.getAll());
    }

    @Test
    public void testFactorizationDNAExample() {
        var node = RegexParser.parse("(ABC(GA|AAA)*)(TA|AG)");
        Factorization factorization = node.bestFactors();
        assertEquals(Set.of("ABC"), factorization.getFactors());
    }

    @Test
    public void testFactorizationUnionOfLiterals() {
        var node = RegexParser.parse("((ABC)|(DEF))");
        var factorization = node.bestFactors();
        assertEquals(Set.of("ABC", "DEF"), factorization.getFactors());
        assertEquals(Set.of("ABC", "DEF"), factorization.getPrefixes());
        assertEquals(Set.of("ABC", "DEF"), factorization.getSuffixes());
        assertEquals(Set.of("ABC", "DEF"), factorization.getAll());
    }

    @Test
    public void testOneOrMoreRepetition() {
        var node = RegexParser.parse("A?");
        var factorization = node.bestFactors();
        var factorSet = Set.of("", "A");
        assertEquals(factorSet, factorization.getPrefixes());
        assertEquals(factorSet, factorization.getSuffixes());
        assertEquals(factorSet, factorization.getFactors());
        assertEquals(factorSet, factorization.getAll());
    }

    @Test
    public void testOneOrMoreRepetitionCharRange() {
        var node = RegexParser.parse("[A-Z]*");
        var factorization = node.bestFactors();
        assertNull(factorization.getPrefixes());
        assertNull(factorization.getSuffixes());
        assertNull(factorization.getFactors());
        assertNull(factorization.getAll());
    }

    @Test
    public void testPotentiallyEmptyCountedRepetition() {
        var node = RegexParser.parse("(AB){0,2}");
        var factorization = node.bestFactors();
        var expectedFactors = Set.of("", "AB", "ABAB");
        assertEquals(expectedFactors, factorization.getFactors());
        assertEquals(expectedFactors, factorization.getPrefixes());
        assertEquals(expectedFactors, factorization.getSuffixes());
        assertEquals(expectedFactors, factorization.getAll());

        assertEquals(Optional.empty(), factorization.getSharedPrefix());
    }

    @Test
    public void testPotentiallyEmptyCountedRepetitionOfRange() {
        var node = RegexParser.parse("[B-i]{0,2}");
        var factorization = node.bestFactors();
        var expectedFactors = Set.of("");
        assertEquals(expectedFactors, factorization.getFactors());
        assertEquals(expectedFactors, factorization.getPrefixes());
        assertEquals(expectedFactors, factorization.getSuffixes());
        assertNull(factorization.getAll());

        assertEquals(Optional.empty(), factorization.getSharedPrefix());
    }

    @Test
    public void testCountedRepetition() {
        var node = RegexParser.parse("(AB){1,2}");
        var factorization = node.bestFactors();
        Set<String> expectedFactors = Set.of("AB", "ABAB");
        assertEquals(expectedFactors, factorization.getFactors());
        assertEquals(expectedFactors, factorization.getPrefixes());
        assertEquals(expectedFactors, factorization.getSuffixes());
        assertEquals(expectedFactors, factorization.getAll());
    }

    @Test
    public void testPotentiallyEmptyCountedRepetitionWithLargeRange() {
        var node = RegexParser.parse("[A-Z]{0,2}");
        var factorization = node.bestFactors();
        assertEquals(Set.of(""), factorization.getFactors());
        assertEquals(Set.of(""), factorization.getPrefixes());
        assertEquals(Set.of(""), factorization.getSuffixes());
        assertNull(factorization.getAll());
    }

    @Test
    public void testSherlock() {
        var node = RegexParser.parse("([Ss]herlock)");
        var factors = node.bestFactors();
        assertEquals(Optional.of(List.of('S', 's')), factors.getInitialChars());
    }
}
