package ${groupId};

import com.psddev.cms.db.*;
import com.psddev.dari.db.ReferentialText;
import com.psddev.dari.util.StringUtils;

import java.lang.String;
import java.util.Set;

/**
 * Basic Article object
 */
@PageStage.UpdateClass(DefaultPageStage.class)
@Renderer.LayoutPath("/WEB-INF/common/page-container.jsp")
@Renderer.Paths ({
        @Renderer.Path(value="/WEB-INF/renderer/mainContent/article.jsp")
})
public class Article extends Content implements Directory.Item, PageStage.Updatable, SequentialDirectoryItem {

    @Required
    private String headline;

    private Author author;

    private String subheadline;

    @FieldDisplayName("Main Image")
    @Required
    private Image image;

    @Required
    private ReferentialText body;

    private Source source;

    //This is a hidden field for ingested content
    @Indexed(unique = true)
    @ToolUi.Hidden
    private String uniqueIdentifier;

    public String getHeadline() {
        return headline;
    }

    public void setHeadline(String headline) {
        this.headline = headline;
    }

    public Author getAuthor() {
        return author;
    }

    public void setAuthor(Author author) {
        this.author = author;
    }

    public String getSubheadline() {
        return subheadline;
    }

    public void setSubheadline(String subheadline) {
        this.subheadline = subheadline;
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public ReferentialText getBody() {
        return body;
    }

    public void setBody(ReferentialText body) {
        this.body = body;
    }

    public Source getSource() {
        return source;
    }

    public void setSource(Source source) {
        this.source = source;
    }

    public String getUniqueIdentifier() {
        return uniqueIdentifier;
    }

    public void setUniqueIdentifier(String uniqueIdentifier) {
        this.uniqueIdentifier = uniqueIdentifier;
    }

    /* Directory.Item interface */
    public String createPermalink(Site site) {
        String permalink = "/articles/";
        if (!StringUtils.isBlank(headline)) {
            permalink += StringUtils.toNormalized(headline);
        }
        return as(SequentialDirectoryItem.Data.class).appendSequence(permalink);
    }

    /* PageStage interface */
    public void updateStage(PageStage stage) {
        // Page-specific CSS, Javascript, Open-Graph elements...
    }

    /**
     * Source for an Article Object
     */
    public static class Source extends Content {

        @Indexed(unique = true)
        @Required
        public String title;
        public Link link;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public Link getLink() {
            return link;
        }

        public void setLink(Link link) {
            this.link = link;
        }
    }
}
