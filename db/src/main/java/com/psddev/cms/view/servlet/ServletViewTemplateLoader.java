package com.psddev.cms.view.servlet;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;

import com.psddev.cms.view.AbstractViewTemplateLoader;
import com.psddev.dari.util.CodeUtils;

/**
 * Loads templates in the servlet context.
 */
public class ServletViewTemplateLoader extends AbstractViewTemplateLoader {

    private HttpServletRequest request;

    /**
     * @param request the current HTTP request.
     */
    public ServletViewTemplateLoader(HttpServletRequest request) {
        this.request = request;
    }

    @Override
    public InputStream getTemplateAsStream(String path) {
        return CodeUtils.getResourceAsStream(request.getServletContext(), path);
    }

    @Override
    protected URL getTemplateAsURL(String path) throws MalformedURLException {
        return CodeUtils.getResource(request.getServletContext(), path);
    }
}
