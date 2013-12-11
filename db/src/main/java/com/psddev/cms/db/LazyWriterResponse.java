package com.psddev.cms.db;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * {@link HttpServletResponse} that uses the {@link LazyWriter}.
 *
 * @deprecated Use {@link com.psddev.dari.util.LazyWriterResponse} instead.
 */
public class LazyWriterResponse extends com.psddev.dari.util.LazyWriterResponse {

    public LazyWriterResponse(HttpServletRequest request, HttpServletResponse response) {
        super(request, response);
    }
}
