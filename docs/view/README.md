# View Annotations

Given a simple article class:

    public class Article extends Content {
    
        private String headline;
        
        public String getHeadline() {
            return headline;
        }
           
        public List<Module> getRightModules() {
            return Query.from(Module.class).selectAll();
        }
    }
   
Old renderer annotations:

    @Renderer.LayoutPath("/two-column-page.jsp")
    @Renderer.Path("/article.jsp")
    public class Article
    
And the new view annotations:

    @ViewMapping(value = ObjectToTwoColumnPageView.class, types = { PageFilter.PAGE_VIEW_TYPE })
    @ViewMapping(value = ArticleToArticleView.class, types = { "main" })
    public class Article

Simple JSP used with the old renderer annotations (`two-column-page.jsp`):

    <!doctype html>
    <html>
    <body>
        <div class="left">
            <cms:render value="${mainContent}" />
        </div>
        <div class="right">
            <c:forEach items="${mainContent.rightModules}" var="module">
                <cms:render context="module" value="${module}" />
            </c:forEach>
        </div>
    </body>
    </html>
   
And the Handlebars template equivalent (`two-column-page.hbs`):

    <!doctype html>
    <html>
    <body>
        <div class="left">
            {{left}}
        </div>
        <div class="right">
            {{#each right}}
                {{this}}
            {{/each}}
        </div>
    </body>
    </html>

New classes necessary to translate the model into the different views:

    public class ObjectToTwoColumnPageView extends AbstractViewCreator<Object> implements TwoColumnPageView {
    
        public Object getLeft() {
            return request.createView("main", model);
        }
        
        public List<?> getRight() {
            if (model instanceof Article) {
                return ((Article) model)
                        .getRightModules()
                        .map(m -> request.createView("module", m));
                
            } else {
                return null;
            }
        }
    }
    
    public class ArticleToArticleView extends AbstractViewCreator<Article> implements ArticleView {
    
        public Object getHeadline() {
            return model.getHeadline();
        }
    }

## Frequently Asked Questions

- [What if the contents of `two-column-page.hbs` is split across multiple files?](split.md)