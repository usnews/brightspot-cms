package com.psddev.cms.view;

import com.psddev.cms.db.PageStage;

import java.util.List;

public interface ViewContext {

    <V> V createView(Class<V> viewClass, Object model);

    default Class<?> getLayoutViewClass(Object model) {
        LayoutView annotation = model.getClass().getAnnotation(LayoutView.class);
        return annotation != null ? annotation.value() : null;
    }

    default Class<?> getMainViewClass(Object model) {
        MainView annotation = model.getClass().getAnnotation(MainView.class);
        return annotation != null ? annotation.value() : null;
    }

    PageStage getPageStage();

    String getParameter(String name);

    List<String> getParameters(String name);

    String getHeader(String name);

    List<String> getHeaders(String name);
}
