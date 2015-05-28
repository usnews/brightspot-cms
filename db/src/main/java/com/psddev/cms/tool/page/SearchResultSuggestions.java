package com.psddev.cms.tool.page;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.Map;

import javax.servlet.ServletException;

import com.psddev.cms.tool.PageServlet;
import com.psddev.cms.tool.Search;
import com.psddev.cms.tool.SearchResultSuggester;
import com.psddev.cms.tool.ToolPageContext;
import com.psddev.dari.util.ClassFinder;
import com.psddev.dari.util.ObjectUtils;
import com.psddev.dari.util.RoutingFilter;
import com.psddev.dari.util.TypeDefinition;

@RoutingFilter.Path(application = "cms", value = "/content/suggestions")
public class SearchResultSuggestions extends PageServlet {

    @Override
    protected String getPermissionId() {
        return null;
    }

    @Override
    protected void doService(ToolPageContext page) throws IOException, ServletException {

        if (page.requireUser()) {
            return;
        }

        Map<String, Object> searchData = (Map<String, Object>) ObjectUtils.fromJson(page.param(String.class, "search"));
        Search search = new Search(page);
        search.getState().setValues(searchData);

        SearchResultSuggester suggester = null;
        for (Class<? extends SearchResultSuggester> c : ClassFinder.Static.findClasses(SearchResultSuggester.class)) {
            if (c.isInterface() && Modifier.isAbstract(c.getModifiers())) {
                continue;
            }

            SearchResultSuggester candidateSuggester = TypeDefinition.getInstance(c).newInstance();

            if (candidateSuggester.isSupported(search)) {
                suggester = candidateSuggester;

                if (candidateSuggester.isPreferred(search)) {
                    break;
                }
            }
        }

        if (suggester != null) {
            suggester.writeHtml(search, page);
        }
    }
}
