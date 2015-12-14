# Split Page Template

With a generic wrapper template (`page.hbs`):
    
    <!doctype html>
    <html>
    <body>
        {{body}}
    </body>
    </html>

Where `body` can contain one column (`one-column.hbs`):

    <div class="full">
        {{full}}
    </div>
    
Or two (`two-column.hbs`):
    
    <div class="left">
        {{left}}
    </div>
    <div class="right">
        {{#each right}}
            {{this}}
        {{/each}}
    </div>
    
The classes can be annotated:

    @ViewMapping(value = ObjectToPageView.class, types = { PageFilter.PAGE_VIEW_TYPE })
    public class Article implements HasRight
    
    @ViewMapping(value = ObjectToPageView.class, types = { PageFilter.PAGE_VIEW_TYPE })
    public class Gallery
    
    public interface HasRight {
    
        List<?> getRightModules();
    }
    
And the view creator written:
    
    public class ObjectToPageView extends AbstractViewCreator<Object> implements PageView {
    
        public Object getBody() {
            if (model instanceof HasRight) {
                return new TwoColumnView.Builder(request).
                        left(request.createView("main", model)).
                        right(((HasRight) model)
                                .getRightModules()
                                .map(m -> request.createView("module", m))).
                        build();
                
            } else {
                return new OneColumnView.Builder(request).
                        full(request.createView("main", model)).
                        build();
            }
        }
    }
    
Articles will display in two columns: content on the left and modules on the
right. Gallery will display in one column.
