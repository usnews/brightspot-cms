package com.psddev.cms.tool;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.psddev.cms.db.Draft;
import com.psddev.cms.db.ToolUi;
import com.psddev.dari.db.Database;
import com.psddev.dari.db.ObjectField;
import com.psddev.dari.db.ObjectType;
import com.psddev.dari.db.Query;
import com.psddev.dari.db.State;

public class CreateDraftSearchResultAction implements SearchResultAction {

    @Override
    public void writeHtml(
            ToolPageContext page,
            Search search,
            SearchResultSelection selection)
            throws IOException {

        if (selection == null) {
            return;
        }

        Query<SearchResultSelectionItem> itemsQuery = Query.
                from(SearchResultSelectionItem.class).
                where("selectionId = ?", selection.getId());

        if (!itemsQuery.hasMoreThan(100)) {
            Set<UUID> itemIds = new HashSet<>();

            for (SearchResultSelectionItem item : itemsQuery.selectAll()) {
                itemIds.add(item.getItemId());
            }

            Set<ObjectType> itemTypes = new HashSet<>();

            for (Object item : Query.
                    fromAll().
                    where("_id = ?", itemIds).
                    referenceOnly().
                    selectAll()) {

                itemTypes.add(State.getInstance(item).getType());
            }

            boolean multiple = itemIds.size() != 1;
            List<TypeAndField> creates = new ArrayList<>();

            for (ObjectType type : Database.Static.getDefault().getEnvironment().getTypes()) {
                if (!type.isConcrete() ||
                        !page.hasPermission("type/" + type.getId() + "/write") ||
                        (!page.getCmsTool().isDisplayTypesNotAssociatedWithJavaClasses() &&
                        type.getObjectClass() == null) ||
                        Draft.class.equals(type.getObjectClass()) ||
                        (type.isDeprecated() &&
                        !Query.fromType(type).hasMoreThan(0))) {

                    continue;
                }

                for (ObjectField field : type.getFields()) {
                    if (field.as(ToolUi.class).isHidden() ||
                            multiple ^ field.isInternalCollectionType()) {

                        continue;
                    }

                    Set<ObjectType> fieldTypes = new HashSet<>();

                    for (ObjectType fieldType : field.getTypes()) {
                        fieldTypes.addAll(fieldType.findConcreteTypes());
                    }

                    if (fieldTypes.containsAll(itemTypes)) {
                        creates.add(new TypeAndField(type, field));
                    }
                }
            }

            if (!creates.isEmpty()) {
                Collections.sort(creates);

                page.writeStart("div", "class", "searchResult-action-createContent");
                    page.writeStart("h2");
                        page.writeHtml("Create");
                    page.writeEnd();

                    page.writeStart("form",
                            "method", "post",
                            "target", "_top",
                            "action", page.toolUrl(CmsTool.class, "/createDraft"));

                        page.writeElement("input",
                                "type", "hidden",
                                "name", "selectionId",
                                "value", selection.getId());

                        page.writeStart("select", "name", "typeIdAndField");
                            for (TypeAndField create : creates) {
                                page.writeStart("option",
                                        "value", create.type.getId() + "," + create.field.getInternalName());

                                    page.writeObjectLabel(create.type);
                                    page.writeHtml(" (");
                                    page.writeObjectLabel(create.field);
                                    page.writeHtml(')');
                                page.writeEnd();
                            }
                        page.writeEnd();

                        page.writeStart("button");
                            page.writeHtml("New");
                        page.writeEnd();
                    page.writeEnd();
                page.writeEnd();
            }
        }
    }

    private static class TypeAndField implements Comparable<TypeAndField> {

        public final ObjectType type;
        public final ObjectField field;

        public TypeAndField(ObjectType type, ObjectField field) {
            this.type = type;
            this.field = field;
        }

        @Override
        public int compareTo(TypeAndField other) {
            int typeCompare = type.compareTo(other.type);

            return typeCompare != 0 ? typeCompare : field.compareTo(other.field);
        }
    }
}
