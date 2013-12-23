package com.psddev.cms.db;

import java.io.Writer;

import javax.servlet.http.HttpServletRequest;


/**
 * For writing invisible {@code <span>} blocks without disrupting the natural
 * page layout by delaying the writes until the appropriate points in the HTML.
 *
 * @deprecated Use {@link com.psddev.dari.util.LazyWriter} instead.
 */
@Deprecated
public class LazyWriter extends com.psddev.dari.util.LazyWriter {

    public LazyWriter(HttpServletRequest request, Writer delegate) {
        super(request, delegate);
    }

    /** @deprecated Use {@link #LazyWriter(HttpServletRequest, Writer)} instead. */
    @Deprecated
    public LazyWriter(Writer delegate) {
        this(null, delegate);
    }
}
