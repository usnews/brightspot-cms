package ${groupId};

import com.psddev.cms.db.Content;
import com.psddev.cms.db.ToolUi;
import com.psddev.dari.util.StringUtils;

/**
 * Abstract class that represents hyperlinks on a page
 */
@Content.Embedded
@Content.LabelFields("text")
public abstract class Link extends Content {

    @DisplayName("Link Text")
    private String text;

    @ToolUi.Note("Optional")
    private Target target = Target._top;


    public String getText() {
        if(StringUtils.isBlank(text)){
            if (this instanceof InternalLink) {
                return ((InternalLink) this).getContent().getLabel();
            } else {
                return this.getUrl();
            }
        }
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getTarget() {
        return target != null ? target.name() : null;
    }

    public void setTarget(Target target) {
        this.target = target;
    }

    abstract public String getUrl();


    public enum Target {
        _blank, _top;
    }

    /**
     * A link to an internal piece of content
     */
    @Embedded
    public static class InternalLink extends Link {

        @Required
        @ToolUi.OnlyPathed
        private Content content;

        public Content getContent() {
            return content;
        }

        public void setContent(Content content) {
            this.content = content;
        }

        public String getUrl() {
            return content.getPermalink();
        }
    }

    /**
     * A hyperlink to an external website.
     */
    @Embedded
    public static class ExternalLink extends Link {

        @Required
        private String url;

        @Override
        public String getUrl() {
            if (StringUtils.isBlank(url)) {
                return "";
            } else if (!url.startsWith("http://")) {
                return "http://" + url;
            }
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        @Override
        public void beforeSave(){
            //having trouble with editors saving extra spaces around the url
            if(!StringUtils.isBlank(url)){
                url = url.trim();
            }
        }
    }
}
