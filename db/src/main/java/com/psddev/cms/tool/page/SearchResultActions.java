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
import com.psddev.cms.tool.SearchResultSelectionItem;
import com.psddev.cms.tool.ToolPageContext;
import com.psddev.dari.db.Query;
import com.psddev.dari.util.ClassFinder;
import com.psddev.dari.util.ObjectUtils;
import com.psddev.dari.util.RoutingFilter;
import com.psddev.dari.util.TypeDefinition;
import com.psddev.dari.util.UrlBuilder;

@RoutingFilter.Path(application = "cms", value = "/searchResultActions")
public class SearchResultActions extends PageServlet {

    private static final long serialVersionUID = 1L;

    public static final String SELECTION_ID_PARAMETER = "selectionId";
    public static final String ITEM_ID_PARAMETER = "id";

    public static final String ACTION_PARAMETER = "action";

    public static final String ACTION_ADD = "item-add";
    public static final String ACTION_REMOVE = "item-remove";
    public static final String ACTION_CLEAR = "clear";
    public static final String ACTION_ACTIVATE = "activate";

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

        String action = page.param(String.class, ACTION_PARAMETER);
        ToolUser user = page.getUser();

        if (user.getCurrentSearchResultSelection() == null) {
            user.resetCurrentSelection();
        }

        SearchResultSelection selection = user.getCurrentSearchResultSelection();


        boolean isSaved = user.isSavedSearchResultSelection(selection);

        UUID selectionId = page.param(UUID.class, SELECTION_ID_PARAMETER);

        if (ACTION_ADD.equals(action)) {

            // add an item to the current selection
            selection.addItem(page.param(UUID.class, ITEM_ID_PARAMETER));

        } else if (ACTION_REMOVE.equals(action)) {

            // remove an item from the current selection
            selection.removeItem(page.param(UUID.class, ITEM_ID_PARAMETER));

        } else if (ACTION_CLEAR.equals(action) && isSaved) {

            // delete the saved selection
            SearchResultSelection selectionToDelete = user.getCurrentSearchResultSelection();
            user.resetCurrentSelection();

            Query.from(SearchResultSelectionItem.class).where("selectionId = ?", selectionToDelete.getId()).deleteAll();
            selectionToDelete.delete();

        } else if (ACTION_CLEAR.equals(action) ||
                (ACTION_ACTIVATE.equals(action) && selectionId == null)) {

            // deactivate the current selection

            user.deactivateSelection(user.getCurrentSearchResultSelection());

            page.writeStart("div", "id", page.createId());
            page.writeEnd();

            page.writeStart("script", "type", "text/javascript");
                page.writeRaw("$('#" + page.getId() + "').closest('.search-result').find('.searchResultList :checkbox').prop('checked', false);");
            page.writeEnd();

        } else if (ACTION_ACTIVATE.equals(action)) {

            // activate the specified selection

            user.activateSelection(Query.from(SearchResultSelection.class).where("_id = ?", selectionId).first());
        }

        long count = user.getCurrentSearchResultSelection() == null ?
                    0 :
                    user.getCurrentSearchResultSelection().size();

        List<SearchResultSelection> own = SearchResultSelection.findOwnSelections(user);

        if (own != null && own.contains(user.getCurrentSearchResultSelection()) && !user.isSavedSearchResultSelection(user.getCurrentSearchResultSelection())) {
            own.remove(user.getCurrentSearchResultSelection());
        }

        if (own != null && own.size() > 0) {

            page.writeStart("form", "method", "get", "action", page.cmsUrl("/searchResultActions"));
                page.writeTag("input", "type", "hidden", "name", ACTION_PARAMETER, "value", ACTION_ACTIVATE);
                page.writeTag("input", "type", "hidden", "name", "search", "value", ObjectUtils.toJson(new Search(page, (Set<UUID>) null).getState().getSimpleValues()));

                page.writeStart("select",
                        "data-searchable", "true",
                        "data-bsp-autosubmit", "",
                        "name", SELECTION_ID_PARAMETER);

                page.writeStart("option", "value", "");
                    page.writeHtml("New Selection");
                page.writeEnd();

                for (SearchResultSelection ownSelection : own) {
                    page.writeStart("option",
                            "selected", ownSelection.equals(user.getCurrentSearchResultSelection()) ? "selected" : null,
                            "value", ownSelection.getId());
                    page.writeObjectLabel(ownSelection);
                    page.writeEnd();
                }

                page.writeEnd();

            page.writeEnd();

            if (count > 0) {
                page.writeStart("div", "class", "searchResult-action-simple");
                writeDeleteAction(page);
                page.writeEnd();
            }

        } else if (count > 0) {

            page.writeStart("h2");
            page.writeHtml("Selection");
            writeDeleteAction(page);
            page.writeEnd();
        }

        page.writeStart("a",
                "class", "reload",
                "href", new UrlBuilder(page.getRequest()).
                        currentScheme().
                        currentHost().
                        currentPath().
                        parameter("search", ObjectUtils.toJson(new Search(page, (Set<UUID>) null).getState().getSimpleValues()))).writeEnd();

        if (count > 0) {

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

    private void writeDeleteAction(ToolPageContext page) throws IOException {
        if (page.getUser().isSavedSearchResultSelection(page.getUser().getCurrentSearchResultSelection())) {
            page.writeStart("a",
                    "class", "action action-delete",
                    "href", page.url("", ACTION_PARAMETER, ACTION_CLEAR, SELECTION_ID_PARAMETER, null));
            page.writeHtml("Delete");
            page.writeEnd();

        } else {
            page.writeStart("a",
                    "class", "action action-cancel",
                    "href", page.url("", ACTION_PARAMETER, ACTION_CLEAR, SELECTION_ID_PARAMETER, null));
            page.writeHtml("Clear");
            page.writeEnd();
        }
    }
}
