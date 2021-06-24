package com.justinblank.strings;

public class RegexSyntaxException extends RuntimeException {

    RegexSyntaxException(String s) {
        super(s);
    }

    RegexSyntaxException(String s, Exception e) {
        super(s, e);
    }
}
