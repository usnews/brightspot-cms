package com.psddev.cms.tool.page;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.Comparator;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.servlet.ServletException;

import com.psddev.cms.db.ToolUser;
import com.psddev.cms.tool.PageServlet;
import com.psddev.cms.tool.Search;
import com.psddev.cms.tool.SearchResultAction;
import com.psddev.cms.tool.SearchResultSelection;
import com.psddev.cms.tool.SearchResultSelectionItem;
import com.psddev.cms.tool.ToolPageContext;
import com.psddev.dari.db.Query;
import com.psddev.dari.util.ClassFinder;
import com.psddev.dari.util.ObjectUtils;
import com.psddev.dari.util.RoutingFilter;
import com.psddev.dari.util.TypeDefinition;

@RoutingFilter.Path(application = "cms", value = "/searchResultActions")
public class SearchResultActions extends PageServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected String getPermissionId() {
        return null;
    }

    @Override
    protected void doService(ToolPageContext page) throws IOException, ServletException {
        Search search = new Search();
        @SuppressWarnings("unchecked")
        Map<String, Object> searchValues = (Map<String, Object>) ObjectUtils.fromJson(page.param(String.class, "search"));

        search.getState().setValues(searchValues);

        String action = page.param(String.class, "action");
        ToolUser user = page.getUser();

        if ("item-add".equals(action)) {

            if (user.getCurrentSearchResultSelection() == null) {
                throw new IllegalStateException();
            }

            UUID itemId = page.param(UUID.class, "id");

            user.getCurrentSearchResultSelection().addItem(itemId);

        } else if ("item-remove".equals(action)) {

            if (user.getCurrentSearchResultSelection() == null) {
                throw new IllegalStateException();
            }

            UUID itemId = page.param(UUID.class, "id");

            user.getCurrentSearchResultSelection().removeItem(itemId);

        } else if ("clear".equals(action)) {

            if (user.getCurrentSearchResultSelection() == null) {
                throw new IllegalStateException();
            }

            user.deactivateSelection(user.getCurrentSearchResultSelection());

            page.writeStart("div", "id", page.createId());
            page.writeEnd();

            page.writeStart("script", "type", "text/javascript");
                page.writeRaw("$('#" + page.getId() + "').closest('.search-result').find('.searchResultList :checkbox').prop('checked', false);");
            page.writeEnd();
        } else if ("activate".equals(action)) {

            UUID selectionId = page.param(UUID.class, "selectionId");

            if (selectionId != null) {

                user.activateSelection(Query.from(SearchResultSelection.class).where("_id = ?", selectionId).first());
            }
        }

        long count = user.getCurrentSearchResultSelection() == null
                    ? 0
                    : user.getCurrentSearchResultSelection().size();

        if (count > 0) {
            page.writeStart("h2");
                page.writeHtml("Selection");
            page.writeEnd();

            page.writeStart("a",
                    "class", "action action-cancel",
                    "href", page.url("", "action", "clear", "selectionId", null));

                page.writeHtml("Clear");
            page.writeEnd();

            page.writeStart("p");
                page.writeHtml(count);
                page.writeHtml(" items selected.");
            page.writeEnd();

        } else {
            page.writeStart("h2");
                page.writeHtml("All");
            page.writeEnd();
        }

        // Sort SearchResultActions first by getClass().getSimpleName(), then by getClass().getName() for tie-breaking.
        for (Class<? extends SearchResultAction> actionClass : ClassFinder.Static.findClasses(SearchResultAction.class).
            stream().
            filter(c -> !c.isInterface() && !Modifier.isAbstract(c.getModifiers())).
            sorted(Comparator.
                <Class<? extends SearchResultAction>, String>comparing(Class::getSimpleName).
                thenComparing(Class::getName)).
            collect(Collectors.toList())) {

            TypeDefinition.getInstance(actionClass).newInstance().writeHtml(page, search, count > 0 ? user.getCurrentSearchResultSelection() : null);
        }
    }
}
