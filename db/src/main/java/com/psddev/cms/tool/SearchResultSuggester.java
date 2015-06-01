package com.psddev.cms.tool;

import java.io.IOException;

public interface SearchResultSuggester {

    public static final int DEFAULT_PRIORITY_LEVEL = 3;

    /**
     * Returns {@code double} priority level for this
     * suggester for the given {@code search}.
     * If not supported, {@code -1} should be returned.
     *
     * @param search Can't be {@code null}
     */
    public int getPriority(Search search);

    /**
     * @param search     Can't be {@code null}.
     * @param page       Can't be {@code null}.
     * @throws IOException if unable to write to the given {@code page}.
     */
    public void writeHtml(
            Search search,
            ToolPageContext page)
            throws IOException;
}
