package com.psddev.cms.view;

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
 * public final class ArticleMapped {
 *
 * &nbsp;   public static class ToArticleView extends AbstractView&lt;Article&gt; implements ArticleView {
 * &nbsp;       public String getTitle() {
 * &nbsp;           return model.getHeadline();
 * &nbsp;       }
 * &nbsp;   }
 * }
 * </pre></blockquote>

 * Sub-classes are required to implement an interface of the view for which
 * they want to create.
 *
 * Sub-classes will have direct instance variable access to the model to
 * implement the methods defined by the view interface.
 *
 * @param <M> the model type from which the view creator can create views.
 */
public abstract class AbstractView<M> extends AbstractParameterizedView<M, Object> {
}
