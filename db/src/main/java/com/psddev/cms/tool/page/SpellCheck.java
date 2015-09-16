package com.psddev.cms.tool.page;

import com.psddev.cms.tool.PageServlet;
import com.psddev.cms.nlp.SpellChecker;
import com.psddev.cms.tool.ToolPageContext;
import com.psddev.dari.util.CompactMap;
import com.psddev.dari.util.ObjectUtils;
import com.psddev.dari.util.RoutingFilter;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@RoutingFilter.Path(application = "cms", value = "spellCheck")
public class SpellCheck extends PageServlet {

    @Override
    protected String getPermissionId() {
        return null;
    }

    @Override
    protected void doService(ToolPageContext page) throws IOException, ServletException {
        Map<String, Object> response = new CompactMap<>();

        try {
            Locale locale = Locale.forLanguageTag(page.param(String.class, "locale"));
            SpellChecker spellChecker = SpellChecker.getInstance(locale);

            if (spellChecker == null) {
                response.put("status", "unsupported");

            } else {
                response.put("status", "supported");

                List<String> words = page.params(String.class, "word");

                if (!ObjectUtils.isBlank(words)) {
                    response.put("results", words
                            .stream()
                            .map(word -> spellChecker.suggest(locale, word))
                            .collect(Collectors.toList()));
                }
            }

        } catch (Exception error) {
            response.put("status", "error");
            response.put("message", error.getMessage());
        }

        page.getResponse().setContentType("application/javascript");
        page.write(ObjectUtils.toJson(response));
    }
}
