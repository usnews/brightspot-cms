package com.psddev.cms.db;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.psddev.dari.util.AbstractFilter;

/**
 * Filter to ensure that all response objects are wrapped in
 * {@link PageResponse}. Typically, this filter doesn't need to be included
 * manually, because {@link PageFilter} will set it up.
 */
public class PageResponseFilter extends AbstractFilter {

    protected static final String ATTRIBUTE_PREFIX = PageResponseFilter.class + ".";
    protected static final String PAGE_RESPONSE_ATTRIBUTE = ATTRIBUTE_PREFIX + "pageResponse";

    @Override
    protected void doDispatch(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain)
            throws IOException, ServletException {

        PageResponse pageResponse = new PageResponse(response, (PageResponse) request.getAttribute(PAGE_RESPONSE_ATTRIBUTE));
        Object oldResponse = request.getAttribute("response");

        try {
            request.setAttribute(PAGE_RESPONSE_ATTRIBUTE, pageResponse);
            request.setAttribute("response", pageResponse);
            chain.doFilter(request, pageResponse);

        } finally {
            request.setAttribute("response", oldResponse);
        }
    }
}
