package com.psddev.cms.view;

import com.psddev.dari.util.TypeDefinition;

/**
 * A base implementation of a ViewCreator that facilitates sub-classes to act
 * as both ViewCreator and view interface implementation. Ex.
 *
 * <blockquote><pre>
 * {@literal @}ViewMapping(ArticleMapped.ToArticleView.class)
 * public class Article extends Content {
 * &nbsp;   private String headline;
 *
 * &nbsp;   public String getHeadline() {
 * &nbsp;       return headline;
 * &nbsp;   }
 * }
 *
 * public interface ArticleView {
 * &nbsp;   String getTitle();
 * }
 *
 * public class ArticleViewRequest {
 * &nbsp;   {@literal @}HttpParameter
 * &nbsp;   private int page;
 *
 * &nbsp;   public int getPage() {
 * &nbsp;       return page < 1 ? 1 : page;
 * &nbsp;   }
 * }
 *
 * public final class ArticleMapped {
 *
 * &nbsp;   public static class ToArticleView extends AbstractParameterizedView&lt;Article, ArticleViewRequest&gt; implements ArticleView {
 * &nbsp;       public String getTitle() {
 * &nbsp;           return model.getHeadline() + " [Page " + request.getPage() + "]";
 * &nbsp;       }
 * &nbsp;   }
 * }
 * </pre></blockquote>

 * Sub-classes are required to implement an interface of the view for which
 * they want to create.
 *
 * Sub-classes will have direct instance variable access to the model and view
 * request to implement the methods defined by the view interface.
 *
 * @param <M> the model type from which the view creator can create views.
 * @param <VR> the view request type.
 */
public abstract class AbstractParameterizedView<M, VR> implements ViewCreator<M, Object, VR> {

    protected M model;

    protected VR request;

    @Override
    public Object createView(M model, VR request) {
        this.model = model;
        this.request = request;
        return this;
    }

    /**
     * Creates a view of type {@code viewClass} that is
     * {@link ViewMapping mapped} to the given {@code model}.
     *
     * @param viewClass the view type to create.
     * @param model the model to create the view from.
     * @param <V> the view type to be created.
     * @return the newly created view.
     */
    public <T, V> V createView(Class<V> viewClass, T model) {

        Class<? extends ViewCreator<? super T, V, ? super VR>> viewCreatorClass = ViewCreator.findCreatorClass(model, viewClass, null, request);
        if (viewCreatorClass != null) {

            ViewCreator<? super T, ? extends V, ? super VR> viewCreator = TypeDefinition.getInstance(viewCreatorClass).newInstance();
            if (viewCreator != null) {

                return viewCreator.createView(model, request);
            }
        }

        return null;
    }

    /**
     * Creates a view that is {@link ViewMapping mapped} to
     * the specified {@code viewType} with {@link ViewMapping#types()} for the
     * given {@code model}.
     *
     * @param viewType the type of view to create
     * @param model the model to create the view from.
     * @return the newly created view.
     */
    public <T> Object createView(String viewType, T model) {

        Class<? extends ViewCreator<? super T, Object, ? super VR>> viewCreatorClass = ViewCreator.findCreatorClass(model, null, viewType, request);
        if (viewCreatorClass != null) {

            ViewCreator<? super T, ?, ? super VR> viewCreator = TypeDefinition.getInstance(viewCreatorClass).newInstance();
            if (viewCreator != null) {

                return viewCreator.createView(model, request);
            }
        }

        return null;
    }
}
