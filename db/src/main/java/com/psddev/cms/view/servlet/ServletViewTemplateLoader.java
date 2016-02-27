package com.psddev.cms.view.servlet;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.ServletContext;

import com.psddev.cms.view.AbstractViewTemplateLoader;
import com.psddev.dari.util.CodeUtils;

/**
 * Loads templates in the servlet context.
 */
public class ServletViewTemplateLoader extends AbstractViewTemplateLoader {

    private ServletContext servletContext;

    /**
     * @param servletContext the servlet HTTP servlet context .
     */
    public ServletViewTemplateLoader(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    @Override
    public InputStream getTemplateAsStream(String path) {
        return CodeUtils.getResourceAsStream(servletContext, path);
    }

    @Override
    protected URL getTemplateAsURL(String path) throws MalformedURLException {
        return CodeUtils.getResource(servletContext, path);
    }
}
