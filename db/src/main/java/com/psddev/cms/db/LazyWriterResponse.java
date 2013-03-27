package com.psddev.cms.db;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * {@link HttpServletResponse} that uses the {@link LazyWriter}.
 */
public class LazyWriterResponse extends HttpServletResponseWrapper {

    private final HttpServletRequest request;
    private LazyWriter lazyWriter;
    private PrintWriter writer;

    public LazyWriterResponse(HttpServletRequest request, HttpServletResponse response) {
        super(response);
        this.request = request;
    }

    public LazyWriter getLazyWriter() throws IOException {
        if (lazyWriter == null) {
            lazyWriter = new LazyWriter(request, super.getWriter());
        }
        return lazyWriter;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        if (writer == null) {
            writer = new PrintWriter(getLazyWriter());
        }
        return writer;
    }
}
