package com.justinblank.strings.Search;

import com.justinblank.strings.MatchResult;
import com.justinblank.strings.Matcher;

import java.util.Objects;

public class SearchMethodMatcher implements Matcher {

    private final SearchMethod method;
    private final String s;

    public SearchMethodMatcher(SearchMethod method, String s) {
        Objects.requireNonNull(method, s);
        this.method = method;
        this.s = s;
    }

    @Override
    public boolean matches() {
        return method.matches(s);
    }

    @Override
    public boolean containedIn() {
        return method.containedIn(s);
    }

    @Override
    public MatchResult find() {
        return method.find(s);
    }

    @Override
    public MatchResult find(int start, int end) {
        return method.find(s, start, end);
    }
}
