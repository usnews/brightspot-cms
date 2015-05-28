package com.psddev.cms.tool;

import java.io.IOException;

public interface SearchResultSuggester {

    /**
     * Returns {@code true} if this suggester supports the given
     * {@code search}.
     *
     * @param search Can't be {@code null}.
     */
    public boolean isSupported(Search search);

    /**
     * Returns {@code true} if this suggester is the preferred default for
     * displaying the given {@code search}.
     *
     * @param search Can't be {@code null}.
     */
    public boolean isPreferred(Search search);

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
