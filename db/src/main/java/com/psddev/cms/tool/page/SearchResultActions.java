package com.psddev.cms.tool.page;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.servlet.ServletException;

import com.psddev.cms.db.ToolUser;
import com.psddev.cms.tool.PageServlet;
import com.psddev.cms.tool.Search;
import com.psddev.cms.tool.SearchResultAction;
import com.psddev.cms.tool.SearchResultSelection;
import com.psddev.cms.tool.ToolPageContext;
import com.psddev.dari.db.ObjectType;
import com.psddev.dari.db.Query;
import com.psddev.dari.util.ClassFinder;
import com.psddev.dari.util.ObjectUtils;
import com.psddev.dari.util.RoutingFilter;
import com.psddev.dari.util.TypeDefinition;

@RoutingFilter.Path(application = "cms", value = "/searchResultActions")
public class SearchResultActions extends PageServlet {

    private static final long serialVersionUID = 1L;

    public static final String SELECTION_ID_PARAMETER = "selectionId";

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

        if (user.getCurrentSearchResultSelection() == null) {
            user.resetCurrentSelection();
        }

        if ("item-add".equals(action)) {

            UUID itemId = page.param(UUID.class, "id");

            user.getCurrentSearchResultSelection().addItem(itemId);

        } else if ("item-remove".equals(action)) {

            UUID itemId = page.param(UUID.class, "id");

            user.getCurrentSearchResultSelection().removeItem(itemId);

        } else if ("clear".equals(action)) {

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
            } else {
                user.deactivateSelection(user.getCurrentSearchResultSelection());

                page.writeStart("div", "id", page.createId());
                page.writeEnd();

                page.writeStart("script", "type", "text/javascript");
                page.writeRaw("$('#" + page.getId() + "').closest('.search-result').find('.searchResultList :checkbox').prop('checked', false);");
                page.writeEnd();
            }
        }

        long count = user.getCurrentSearchResultSelection() == null ?
                    0 :
                    user.getCurrentSearchResultSelection().size();

        List<SearchResultSelection> own = SearchResultSelection.findOwnSelections(user);

        if (own != null && own.size() > 0) {

            page.writeStart("form", "method", "get", "action", page.cmsUrl("/searchResultActions"));
                page.writeTag("input", "type", "hidden", "name", "action", "value", "activate");
                page.writeTag("input", "type", "hidden", "name", "search", "value", ObjectUtils.toJson(new Search(page, (Set<UUID>) null).getState().getSimpleValues()));
                page.writeObjectSelect(
                        ObjectType.getInstance(ToolUser.class).getField("currentSearchResultSelection"),
                        user.getCurrentSearchResultSelection(),
                        user.getId(),
                        user.getState().getTypeId(),
                        "data-bsp-autosubmit", "",
                        "name", SELECTION_ID_PARAMETER);
            page.writeEnd();

        } else if (count > 0) {

            page.writeStart("h2");
            page.writeHtml("Selection");
            page.writeEnd();
        }

        if (count > 0) {

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
