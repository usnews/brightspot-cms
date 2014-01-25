package com.psddev.cms.db;

import java.io.IOException;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.psddev.dari.db.Database;
import com.psddev.dari.util.AbstractFilter;

public class RichTextFilter extends AbstractFilter implements AbstractFilter.Auto {

    @Override
    public void updateDependencies(
            Class<? extends AbstractFilter> filterClass,
            List<Class<? extends Filter>> dependencies) {
        if (PageFilter.class.isAssignableFrom(filterClass)) {
            dependencies.add(getClass());
        }
    }

    @Override
    protected void doRequest(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain)
            throws IOException, ServletException {

        Object mainObject = PageFilter.Static.getMainObject(request);

        if (mainObject == null) {
            chain.doFilter(request, response);

        } else {
            RichTextDatabase rt = new RichTextDatabase();

            rt.setDelegate(Database.Static.getDefault());

            try {
                Database.Static.overrideDefault(rt);
                rt.clean(mainObject);
                chain.doFilter(request, response);

            } finally {
                Database.Static.restoreDefault();
            }
        }
    }
}
