package com.psddev.cms.tool.page;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.servlet.ServletException;

import com.psddev.cms.db.Content;
import com.psddev.cms.tool.CmsTool;
import com.psddev.cms.tool.PageServlet;
import com.psddev.cms.tool.SearchResultSelection;
import com.psddev.cms.tool.SearchResultSelectionItem;
import com.psddev.cms.tool.ToolPageContext;
import com.psddev.dari.db.ObjectField;
import com.psddev.dari.db.ObjectType;
import com.psddev.dari.db.Query;
import com.psddev.dari.db.State;
import com.psddev.dari.util.ObjectUtils;
import com.psddev.dari.util.RoutingFilter;

@RoutingFilter.Path(application = "cms", value = "/createDraft")
public class CreateDraft extends PageServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected String getPermissionId() {
        return null;
    }

    @Override
    protected void doService(ToolPageContext page) throws IOException, ServletException {
        String typeIdAndField = page.param(String.class, "typeIdAndField");
        int commaAt = typeIdAndField.indexOf(',');
        ObjectType type = ObjectType.getInstance(ObjectUtils.to(UUID.class, typeIdAndField.substring(0, commaAt)));
        ObjectField field = type.getField(typeIdAndField.substring(commaAt + 1));
        UUID selectionId = page.param(UUID.class, "selectionId");
        Set<UUID> itemIds = new HashSet<>();
        
        for (SearchResultSelectionItem item : Query.
                from(SearchResultSelectionItem.class).
                where("selectionId = ?", selectionId).
                selectAll()) {

            itemIds.add(item.getItemId());
        }

        List<Object> items = Query.
                fromAll().
                where("_id = ?", itemIds).
                referenceOnly().
                selectAll();

        State state = State.getInstance(type.createObject(null));

        state.as(Content.ObjectModification.class).setDraft(true);
        state.put(field.getInternalName(), items.size() == 1 ? items.get(0) : items);
        state.saveUnsafely();

        Query.
                from(SearchResultSelection.class).
                where("_id = ?", selectionId).
                deleteAll();

        page.getResponse().sendRedirect(
                page.toolUrl(CmsTool.class, "/content/edit.jsp",
                        "id", state.getId()));
    }
}
