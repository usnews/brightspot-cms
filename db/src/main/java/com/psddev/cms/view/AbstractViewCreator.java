package com.psddev.cms.view;

public abstract class AbstractViewCreator<M> implements ViewCreator<M, Object> {

    protected M model;

    protected ViewContext context;

    @Override
    public Object createView(M model, ViewContext context) {
        this.model = model;
        this.context = context;
        return this;
    }
}
