package com.psddev.cms.view;

public interface ViewContext {

    <V> V createView(Class<V> viewClass, Object model);
}
