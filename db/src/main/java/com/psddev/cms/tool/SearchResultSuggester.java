package com.psddev.cms.tool;

import java.io.IOException;

public interface SearchResultSuggester {

    public static final double DEFAULT_PRIORITY_LEVEL = 0;

    /**
     * Returns {@code double} priority level for this
     * suggester for the given {@code search}. Highest
     * priority level will be used by {@code SearchResultSuggestions}
     * If not supported, a value less than 0 should be returned.
     *
     * @param search Can't be {@code null}
     */
    public double getPriority(Search search);

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
