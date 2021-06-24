package com.justinblank.util;

import org.junit.Test;
import org.quicktheories.core.Gen;
import org.quicktheories.generators.IntegersDSL;
import org.quicktheories.generators.ListsDSL;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;
import static org.quicktheories.QuickTheory.qt;

public class SparseSetTest {

    @Test
    public void testSparseSetAddAndClear() {
        SparseSet sparseSet = new SparseSet(10);
        assertEquals(10, sparseSet.capacity());

        assertEquals(-1, sparseSet.indexOf(0));
        sparseSet.add(0);
        assertEquals(0, sparseSet.indexOf(0));
        assertTrue(sparseSet.contains(0));
        sparseSet.add(10);
        assertEquals(1, sparseSet.indexOf(10));
        sparseSet.add(0);
        assertEquals(0, sparseSet.indexOf(0));

        sparseSet.clear();
        assertEquals(-1, sparseSet.indexOf(0));
        assertFalse(sparseSet.contains(0));
        sparseSet.add(10);
        assertEquals(0, sparseSet.indexOf(10));
        assertTrue(sparseSet.contains(10));
        sparseSet.remove(10);
        assertFalse(sparseSet.contains(10));
    }

    @Test
    public void testIndexOfHandlesGreaterThanMaxArguments() {
        SparseSet sparseSet = new SparseSet(10);
        assertEquals(-1, sparseSet.indexOf(11));
        assertEquals(-1, sparseSet.indexOf(100));
    }

    @Test
    public void testOperationSequence() {
        SparseSet set = new SparseSet(10);
        set.add(1);
        set.add(2);
        set.add(3);
        set.remove(2);
        assertEquals(2, set.size());
        assertTrue(set.contains(1));
        assertTrue(set.contains(3));
        assertFalse(set.contains(2));
    }

    @Test
    public void testAdd() {
        SparseSet sparseSet = new SparseSet(100);
        qt().forAll(new IntegersDSL().between(0, 100)).check((i) -> {
            sparseSet.add(i);
            return sparseSet.contains(i);
        });
    }

    @Test
    public void testAddAndClear() {
        SparseSet sparseSet = new SparseSet(100);
        qt().forAll(new IntegersDSL().between(0, 100)).check((i) -> {
            sparseSet.add(i);
            if (!sparseSet.contains(i)) {
                return false;
            }
            sparseSet.clear();
            return !sparseSet.contains(i);
        });
    }

    @Test
    public void testAddAndRemove() {
        SparseSet sparseSet = new SparseSet(100);
        qt().forAll(new IntegersDSL().between(0, 100)).check((i) -> {
            sparseSet.add(i);
            if (!sparseSet.contains(i)) {
                return false;
            }
            boolean removed = sparseSet.remove(i);
            return removed && !sparseSet.contains(i);
        });
    }

    @Test
    public void testGeneratedSequences() {
        Gen<List<Integer>> gen = new ListsDSL().of(new IntegersDSL().between(0, 301)).ofSizeBetween(0, 100);
        qt().forAll(gen).check((integers) -> {
            Set<Integer> referenceSet = new HashSet<>();
            SparseSet sparseSet = new SparseSet(100);
            for (Integer i : integers) {
                if (i < 100) {
                    if (referenceSet.add(i) != sparseSet.add(i)) {
                        return false;
                    }
                }
                else if (i < 200) {
                   if (referenceSet.remove(i - 100) != sparseSet.remove(i - 100)) {
                       return false;
                   }
                }
                else if (i < 300) {
                    boolean refContained = referenceSet.contains(i - 200);
                    boolean sparseContained = sparseSet.contains(i  - 200);
                    if (refContained != sparseContained) {
                        return false;
                    }
                }
                else if (i == 301) {
                    referenceSet.clear();
                    sparseSet.clear();
                }
                if (referenceSet.size() != sparseSet.size()) {
                    return false;
                }
                for (Integer contained : referenceSet) {
                    if (!sparseSet.contains(contained)) {
                        return false;
                    }
                }
                for (int j = 0; j < sparseSet.size(); j++) {
                    int n = sparseSet.getByIndex(j);
                    if (!referenceSet.contains(n)) {
                        return false;
                    }
                    if (sparseSet.indexOf(n) != j) {
                        return false;
                    }
                }
            }
            return true;
        });
    }

    @Test
    public void canAddMax() {
        SparseSet set = new SparseSet(10);
        set.add(10);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void checkOutOfBoundsAdd() {
        SparseSet set = new SparseSet(10);
        set.add(11);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void checkOutOfBoundsGetByIndex() {
        SparseSet sparseSet = new SparseSet(10);
        sparseSet.getByIndex(1);
    }

    @Test
    public void testToString() {
        SparseSet set = new SparseSet(10);
        assertEquals("[]", set.toString());
        set.add(10);
        set.add(8);
        assertEquals("[10, 8]", set.toString());
        set.add(7);
        assertEquals("[10, 8, 7]", set.toString());
        set.clear();
        assertEquals("[]", set.toString());
    }

    @Test
    public void testToStringSequences() {
        Gen<List<Integer>> gen = new ListsDSL().of(new IntegersDSL().between(0, 301)).ofSizeBetween(0, 100);
        qt().forAll(gen).check((integers) -> {
            Set<Integer> referenceSet = new HashSet<>();
            SparseSet sparseSet = new SparseSet(100);
            for (Integer i : integers) {
                if (i < 100) {
                    boolean refSetAdd = referenceSet.add(i);
                    boolean sparseSetAdd = sparseSet.add(i);
                    if (refSetAdd != sparseSetAdd) {
                        return false;
                    }
                } else if (i < 200) {
                    referenceSet.remove(i - 100);
                    sparseSet.remove(i - 100);
                } else if (i < 300) {
                    boolean refContained = referenceSet.contains(i - 200);
                    boolean sparseContained = sparseSet.contains(i - 200);
                    if (refContained != sparseContained) {
                        return false;
                    }
                } else if (i == 301) {
                    referenceSet.clear();
                    sparseSet.clear();
                }
                for (Integer contained : referenceSet) {
                    if (!sparseSet.toString().contains(contained.toString())) {
                        return false;
                    }
                }
            }
            return true;
        });
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNegativeCapacityNotAllowed() {
        SparseSet sparseSet = new SparseSet(-1);
    }

    @Test
    public void testZeroArgumentsAreAllowed() {
        SparseSet sparseSet = new SparseSet(0);
        sparseSet.add(0);
    }
}
