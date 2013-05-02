package com.psddev.cms.db;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import com.psddev.dari.util.HtmlWriter;
import com.psddev.dari.util.IoUtils;
import com.psddev.dari.util.ObjectUtils;
import com.psddev.dari.util.TypeReference;

/**
 * @see <a href="http://oembed.com/">oEmbed Specification</a>
 */
@ToolUi.Referenceable
public class ExternalContent extends Content implements Renderer {

    @Required
    @ToolUi.NoteHtml("<a class=\"icon icon-action-preview\" target=\"contentExternalPreview\" onclick=\"this.href = CONTEXT_PATH + '/content/externalPreview?url=' + encodeURIComponent($(this).closest('.inputContainer').find('> .inputSmall > textarea').val() || ''); return true;\">Preview</a>")
    private String url;

    private Integer maximumWidth;
    private Integer maximumHeight;

    @ToolUi.Hidden
    private Map<String, Object> response;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Integer getMaximumWidth() {
        return maximumWidth;
    }

    public void setMaximumWidth(Integer maximumWidth) {
        this.maximumWidth = maximumWidth;
    }

    public Integer getMaximumHeight() {
        return maximumHeight;
    }

    public void setMaximumHeight(Integer maximumHeight) {
        this.maximumHeight = maximumHeight;
    }

    public Map<String, Object> getResponse() {
        String url = getUrl();
        Integer width = getMaximumWidth();
        Integer height = getMaximumHeight();

        if (!ObjectUtils.isBlank(url) &&
                (response == null ||
                !ObjectUtils.equals(url, response.get("_url")) ||
                !ObjectUtils.equals(width, response.get("_maximumWidth")) ||
                !ObjectUtils.equals(height, response.get("_maximumHeight")))) {
            try {
                    System.out.println(url);
                for (Element link : Jsoup.connect(url).get().select("link[rel=alternate][type=application/json+oembed]")) {
                    String oEmbedUrl = link.attr("href");

                    if (!ObjectUtils.isBlank(oEmbedUrl)) {
                        Map<String, Object> newResponse = ObjectUtils.to(
                                new TypeReference<Map<String, Object>>() { },
                                ObjectUtils.fromJson(IoUtils.toString(new URL(oEmbedUrl))));

                        newResponse.put("_url", url);
                        newResponse.put("_maximumWidth", width);
                        newResponse.put("_maximumHeight", height);
                        response = newResponse;

                        break;
                    }
                }

            } catch (IOException error) {
                error.printStackTrace();
            }
        }

        return response;
    }

    public void setResponse(Map<String, Object> response) {
        this.response = response;
    }

    @Override
    protected void beforeSave() {
        super.beforeSave();
        getResponse();
    }

    @Override
    public void renderObject(
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse,
            HtmlWriter writer)
            throws IOException {
        Map<String, Object> response = getResponse();
        Object type = response.get("type");

        if ("photo".equals(type)) {
            writer.writeTag("img",
                    "src", response.get("url"),
                    "width", response.get("width"),
                    "height", response.get("height"),
                    "alt", response.get("title"));

        } else if ("video".equals(type)) {
            writer.writeRaw(response.get("html"));

        } else if ("link".equals(type)) {
            writer.writeStart("a",
                    "href", response.get("_url"));
                writer.writeHtml(response.get("title"));
            writer.writeEnd();

        } else if ("rich".equals(type)) {
            writer.writeRaw(response.get("html"));
        }
    }
}
