package com.psddev.cms.view;

/**
 * The result of {@link com.psddev.cms.view.ViewRenderer#render(ViewMap)
 * rendering} a view.
 */
public interface ViewResult {

    /**
     * Fetches the rendered view's output.
     *
     * @return the text output from a rendered view.
     */
    String get();
}
