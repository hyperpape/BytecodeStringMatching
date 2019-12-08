package com.justinblank.strings;

import com.justinblank.strings.RegexAST.*;

import java.util.*;

public class RegexParser {

    private int index = 0;
    private int parenDepth = 0;
    private String regex;
    private Stack<Node> nodes = new Stack<>();

    protected RegexParser(String regex) {
        this.regex = regex;
    }

    public static Node parse(String regex) {
        return new RegexParser(regex)._parse();
    }

    private Node _parse() {
        while (index < regex.length()) {
            char c = takeChar();

            switch (c) {
                case '(':
                    nodes.push(LParenNode.getInstance());
                    break;
                case '{':
                    if (nodes.isEmpty()) {
                        throw new RegexSyntaxException();
                    }
                    int left = consumeInt();
                    char next = takeChar();
                    if (next != ',') {
                        throw new RegexSyntaxException("Expected ',' at " + index + ", but found " + next);
                    }
                    int right = consumeInt();
                    nodes.push(new CountedRepetition(nodes.pop(), left, right));
                    next = takeChar();
                    if (next != '}') {
                        throw new RegexSyntaxException();
                    }
                    break;
                case '?':
                    if (nodes.isEmpty()) {
                        throw new RegexSyntaxException("");
                    }
                    nodes.push(new CountedRepetition(nodes.pop(), 0, 1));
                    break;
                case '[':
                    nodes.push(buildCharSet());
                    break;
                case '+':
                    if (nodes.isEmpty()) {
                        throw new RegexSyntaxException();
                    }
                    Node lastNode = nodes.pop();
                    nodes.push(new Concatenation(lastNode, new Repetition(lastNode)));
                    break;
                case '*':
                    nodes.push(new Repetition(nodes.pop()));
                    break;
                case '|':
                    assertNonEmpty("'|' cannot be the final character in a regex");
                    Node last = nodes.peek();
                    if (last instanceof CharRangeNode || last instanceof Concatenation || last instanceof LiteralNode) {
                        nodes.push(new Alternation(last, null));
                    }
                    break;
                case '}':
                    throw new RegexSyntaxException("Unbalanced '}' character");
                case ')':
                    collapseParenNodes();
                    break;
                case ']':
                    throw new RegexSyntaxException("Unbalanced ']' character");
                default:
                    if (nodes.isEmpty()) {
                        nodes.push(LiteralNode.fromChar(c));
                    }
                    else if (nodes.peek() instanceof LiteralNode) {
                        ((LiteralNode) nodes.peek()).append(c);
                    }
                    else {
                        nodes.push(LiteralNode.fromChar(c));
                    }
            }
        }
        if (nodes.isEmpty()) {
            return new LiteralNode("");
        }
        Node node = nodes.pop();
        if (node instanceof LParenNode) {
            throw new RegexSyntaxException("Unbalanced '('");
        }
        while (!nodes.isEmpty()) {
            Node next = nodes.pop();
            if (next instanceof Alternation && ((Alternation) next).right == null) {
                Alternation alt = (Alternation) next;
                assertNonEmpty("Alternation needed something to alternate");
                Node nextNext = nodes.pop();
                node = new Alternation(nextNext, node);
            }
            else if (next instanceof LiteralNode && node instanceof LiteralNode) {
                node = new LiteralNode(((LiteralNode) next).getLiteral() + ((LiteralNode) node).getLiteral());
            }
            else if (next instanceof LParenNode) {
                throw new RegexSyntaxException("Unbalanced ( found");
            }
            else {
                node = new Concatenation(next, node);
            }
        }
        return node;
    }

    private void collapseParenNodes() {
        assertNonEmpty("found unbalanced ')'");
        Node node = null;
        while (!(nodes.peek() instanceof LParenNode)) {
            Node next = nodes.pop();
            if (node == null) {
                node = next;
            }
            else if (next instanceof Alternation) {
                Alternation alt = (Alternation) next;
                assertNonEmpty("found '|' with no preceding content");
                Node nextNext = nodes.pop();
                if (nextNext instanceof LParenNode) {
                    throw new RegexSyntaxException("");
                }
                node = new Alternation(alt.left, node);
            }
            else {
                node = new Concatenation(next, node);
            }
            assertNonEmpty("found unbalanced ')'");
        }
        nodes.pop(); // remove the left paren
        if (node == null) {
            node = new LiteralNode("");
        }
        nodes.push(node);

    }

    private void assertNonEmpty(String s) {
        if (nodes.isEmpty()) {
            throw new RegexSyntaxException(s);
        }
    }

    private char takeChar() {
        return regex.charAt(index++);
    }

    private int consumeInt() {
        int initialIndex = index;
        try {
            while (index < regex.length()) {
                char next = regex.charAt(index);
                if (next < '0' || next > '9') {
                    String subString = regex.substring(initialIndex, index);
                    return Integer.parseInt(subString);
                }
                takeChar();
            }
        }
        catch (NumberFormatException e) {
            throw new RegexSyntaxException("Expected number, found " + regex.substring(initialIndex, index));
        }
        throw new RegexSyntaxException("Expected number, found " + regex.substring(initialIndex, index));
    }

    private Node buildCharSet() {
        Set<Character> characterSet = new HashSet<>();
        Set<CharRange> ranges = new HashSet<>();
        Character last = null;
        while (index < regex.length()) {
            char c = takeChar();
            if (c == ']') {
                if (last != null) {
                    characterSet.add(last);
                }
                return buildNode(characterSet, ranges);
            } else if (c == '-') {
                // TODO: find out actual semantics
                if (last == null || index == regex.length()) {
                    throw new RegexSyntaxException("Parsing failed");
                }
                char next = takeChar();
                ranges.add(new CharRange(last, next));
                last = null;
            } else if (c == '(' || c == ')') {
                throw new RegexSyntaxException("Parsing failed");
            } else if (c == '[') {
                throw new RegexSyntaxException("Unexpected '[' inside of character class");
            } else {
                if (last != null) {
                    characterSet.add(last);
                }
                last = c;
            }
        }
        throw new RegexSyntaxException("Parsing failed, unmatched [");
    }

    private Node buildNode(Set<Character> characterSet, Set<CharRange> ranges) {
        if (ranges.isEmpty() && characterSet.isEmpty()) {
            throw new RegexSyntaxException("Parsing failed: empty [] construction");
        } else if (characterSet.isEmpty() && ranges.size() == 1) {
            CharRange range = ranges.iterator().next();
            return new CharRangeNode(range);
        } else if (ranges.isEmpty() && characterSet.size() == 1) {
            Character character = characterSet.iterator().next();
            return new CharRangeNode(character, character);
        } else {
            return buildRanges(characterSet, ranges);
        }
    }

    private Node buildRanges(Set<Character> characterSet, Set<CharRange> ranges) {
        List<CharRange> sortedCharRanges = buildSortedCharRanges(characterSet, ranges);
        if (sortedCharRanges.size() == 1) {
            return new CharRangeNode(sortedCharRanges.get(0));
        } else {
            CharRangeNode first = new CharRangeNode(sortedCharRanges.get(0));
            CharRangeNode second = new CharRangeNode(sortedCharRanges.get(1));
            Node node = new Alternation(first, second);
            for (int i = 2; i < sortedCharRanges.size(); i++) {
                node = new Alternation(node, new CharRangeNode(sortedCharRanges.get(i)));
            }
            return node;
        }
    }

    private List<CharRange> buildSortedCharRanges(Set<Character> characterSet, Set<CharRange> ranges) {
        List<Character> characters = new ArrayList<>(characterSet);
        Collections.sort(characters);
        List<CharRange> charRanges = new ArrayList<>(ranges);
        characters.stream().map(c -> new CharRange(c, c)).forEach(charRanges::add);
        return CharRange.compact(charRanges);
    }
}
