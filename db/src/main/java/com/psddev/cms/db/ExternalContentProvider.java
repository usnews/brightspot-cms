package com.psddev.cms.db;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.psddev.dari.util.HtmlWriter;

/**
 * {@link ExternalContent} from providers that doesn't support the oEmbed
 * specification.
 */
public interface ExternalContentProvider {

    /**
     * @return {@code null} if this provider doesn't support embedding
     * the given {@code content}.
     */
    public Map<String, Object> createResponse(ExternalContent content);

    /**
     * Skeletal implementation of {@link ExternalContentProvider} optimized
     * for returning {@code rich} oEmbed type.
     */
    public abstract static class RichExternalContentProvider implements ExternalContentProvider {

        private ExternalContent content;

        public ExternalContent getContent() {
            return content;
        }

        /**
         * Returns the regular expression pattern against the URL that's used
         * to determined whether this provider supports embedded the external
         * content.
         *
         * <p>The resulting matcher is passed to {@link #updateHtml}.</p>
         *
         * @return Never {@code null}.
         */
        protected abstract Pattern getUrlPattern();

        /**
         * Updates the HTML that's returned in the response.
         *
         * @param urlMatcher Never {@code null}.
         * @param html Never {@code null}.
         */
        protected abstract void updateHtml(Matcher urlMatcher, HtmlWriter html) throws IOException;

        /**
         * Updates the given {@code response}.
         *
         * @param response Never {@code null}.
         */
        protected void updateResponse(Map<String, Object> response) {
        }

        @Override
        public final Map<String, Object> createResponse(ExternalContent content) {
            this.content = content;
            Matcher urlMatcher = getUrlPattern().matcher(content.getUrl());

            if (!urlMatcher.matches()) {
                return null;
            }

            StringWriter string = new StringWriter();
            HtmlWriter html = new HtmlWriter(string);

            try {
                try {
                    updateHtml(urlMatcher, html);

                } finally {
                    html.close();
                }

            } catch (IOException error) {
                throw new IllegalStateException(error);
            }

            Map<String, Object> response = new HashMap<String, Object>();

            response.put("type", "rich");
            response.put("html", string.toString());
            updateResponse(response);

            return response;
        }
    }
}
