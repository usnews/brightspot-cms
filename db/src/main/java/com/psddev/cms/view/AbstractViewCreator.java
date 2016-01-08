package com.psddev.cms.view;

/**
 * @deprecated Use {@link com.psddev.cms.view.AbstractView} instead.
 */
@Deprecated
public abstract class AbstractViewCreator<M> extends AbstractParameterizedView<M, ViewRequest> {

    protected M model;

    protected ViewRequest request;

    @Override
    public Object createView(M model, ViewRequest request) {
        this.model = model;
        this.request = request;
        return this;
    }
}
