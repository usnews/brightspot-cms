package ${groupId};

import com.psddev.cms.db.*;
import com.psddev.dari.db.Application;
import com.psddev.dari.db.State;
import com.psddev.dari.util.HtmlElement;
import com.psddev.dari.util.HtmlNode;
import com.psddev.dari.util.HtmlWriter;
import com.psddev.dari.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * The default page stage updater that should be used for main content types.
 * Provides a default way to set up the page with seo information, css, javascript
 */
public class DefaultPageStage implements PageStage.SharedUpdatable {

    public void updateStageBefore(Object object, PageStage stage) {

        ${classPrefix}Application app = ${classPrefix}Application.getInstance();

        State state = State.getInstance(object);
        String title = state.as(Seo.ObjectModification.class).findTitle();
        String description = state.as(Seo.ObjectModification.class).findDescription();
        String permalink = null;

        if (object instanceof Content) {
            Content content = (Content) object;
            permalink = content.getPermalink();
        }

        stage.findOrCreateHeadElement("meta", "charset", "UTF-8");
        stage.setHttpEquiv("X-UA-Compatible", "IE=edge,chrome=1");

        // SEO Tags
        if (!StringUtils.isBlank(title)) {
            stage.setTitle(title);
        }

        if (!StringUtils.isBlank(description)) {
            stage.setMetaName("description", description);
        }

        if (!StringUtils.isBlank(permalink)){
            stage.findOrCreateHeadElement("link",
                    "rel", "canonical",
                    "href", permalink);
        }

        stage.setMetaProperty("og:site_name","${artifactId}");
        stage.setMetaProperty("og:url",permalink);

        if (app.isUseNonMinifiedCss()) {
            stage.addStyleSheet("/assets/style/${artifactId}.less"); 
            stage.addScript("/assets/script/less.js");
        } else {
            stage.addStyleSheet("/assets/style/${artifactId}.min.css");
        }

        String scriptDir = null;

        if (app.isUseNonMinifiedJs()) {
            scriptDir = "script";
        } else {
            scriptDir = "script.min";
        }

        stage.findOrCreateHeadElement("script",
                    "type", "text/javascript",
                    "data-main", "/assets/" + scriptDir + "/${artifactId}",
                    "src", ElFunctionUtils.resource("/assets/" + scriptDir + "/require.js")); 
    }

    public void updateStageAfter(Object object, PageStage stage) {

    }

    public void addConditionalJavascript(PageStage stage, String condition, String src) {
        Conditional conditional = new Conditional();
        conditional.setCondition(condition);

        List<HtmlNode> nodes = new ArrayList<HtmlNode>();

        HtmlElement script = new HtmlElement();
        script.setName("script");
        script.addAttributes("type", "text/javascript",
                "src", ElFunctionUtils.resource(src));

        nodes.add(script);

        conditional.setNodes(nodes);

        stage.getHeadNodes().add(conditional);
    }

    public static class Conditional extends HtmlNode {

        private String condition;
        private List<HtmlNode> nodes;

        public String getCondition() {
            return condition;
        }

        public void setCondition(String condition) {
            this.condition = condition;
        }

        public List<HtmlNode> getNodes() {
            if (nodes == null) {
                nodes = new ArrayList<HtmlNode>();
            }
            return nodes;
        }

        public void setNodes(List<HtmlNode> nodes) {
            this.nodes = nodes;
        }

        @Override
        public void writeHtml(HtmlWriter writer) throws IOException {
            if (getCondition() != null) {
                writer.write("<!--[if " + getCondition() + "]>");
                for (HtmlNode node : getNodes()) {
                    node.writeHtml(writer);
                }
                writer.write("<![endif]-->");
            }
        }
    }
}
