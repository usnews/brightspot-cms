package com.psddev.cms.db;

import java.io.IOException;
import java.io.Writer;
import java.util.Locale;

/**
 * Provides ability to write non-layout-related inserts (e.g. non-visible span blacks) without disrupting the page
 * by delaying writes to the appropriate points in html
 */
public  class LazyWriter extends Writer {

    private final Writer delegate;
    private final StringBuilder lazy = new StringBuilder();
    private final StringBuilder pending = new StringBuilder();

    private boolean inString;
    private boolean stringEscaping;
    private char stringStartLetter;

    private boolean commentStarting;
    private boolean inSingleComment;
    private boolean inMultiComment;
    private boolean multiCommentEnding;

    private boolean inTag;
    private final StringBuilder tagName = new StringBuilder();
    private boolean tagNameFound;
    private boolean inScriptOrStyle;
    private boolean inBody;


    public LazyWriter(Writer delegate) {
        this.delegate = delegate;
    }

    @Override
    public void write(char[] buffer, int offset, int length) throws IOException {
        for (length += offset; offset < length; ++ offset) {
            char letter = buffer[offset];

            pending.append(letter);

            if (inString) {
                if (stringEscaping) {
                    stringEscaping = false;

                } else if (letter == '\\') {
                    stringEscaping = true;

                } else if (letter == stringStartLetter) {
                    inString = false;
                }

            } else if (commentStarting) {
                commentStarting = false;

                if (letter == '/') {
                    inSingleComment = true;

                } else if (letter == '*') {
                    inMultiComment = true;
                }

            } else if (inSingleComment) {
                if (letter == '\r' || letter == '\n') {
                    inSingleComment = false;
                }

            } else if (inMultiComment) {
                if (letter == '*') {
                    multiCommentEnding = true;

                } else if (multiCommentEnding && letter == '/') {
                    inMultiComment = false;
                    multiCommentEnding = false;

                } else {
                    multiCommentEnding = false;
                }

            } else if (letter == '<') {
                inTag = true;
                tagName.setLength(0);
                tagNameFound = false;
                inScriptOrStyle = false;

            } else if (inTag) {
                boolean endTag = letter == '>';

                if (endTag || Character.isWhitespace(letter)) {
                    tagNameFound = true;

                } else if (!tagNameFound) {
                    tagName.append(letter);
                }

                if (endTag) {
                    String tagNameLc = tagName.toString().toLowerCase(Locale.ENGLISH);

                    if ("script".equals(tagNameLc) || "style".equals(tagNameLc)) {
                        inScriptOrStyle = true;

                    } else if ("/script".equals(tagNameLc) || "/style".equals(tagNameLc)) {
                        inScriptOrStyle = false;

                    } else if ("body".equals(tagNameLc)) {
                        inBody = true;
                    }

                    writePending();

                    if (inBody && lazy.length() > 0) {
                        delegate.append(lazy);
                        lazy.setLength(0);
                    }

                    inTag = false;

                } else if (letter == '\'' || letter == '"') {
                    inString = true;
                    stringStartLetter = letter;
                }

            } else if (inScriptOrStyle) {
                if (letter == '\'' || letter == '"') {
                    inString = true;
                    stringStartLetter = letter;

                } else if (letter == '/') {
                    commentStarting = true;
                }
            }
        }
    }

    public void writeLazily(String string) throws IOException {
        if (!inBody || inTag || inScriptOrStyle) {
            lazy.append(string);

        } else {
            delegate.write(string);
        }
    }

    public void writePending() throws IOException {
        if (pending.length() == 0) {
            return;
        }

        delegate.append(pending);
        pending.setLength(0);
    }

    @Override
    public void flush() throws IOException {
        delegate.flush();
    }

    @Override
    public void close() throws IOException {
        writePending();
        delegate.close();
    }
}
