<%@ page import="

com.psddev.cms.db.Content,
com.psddev.cms.db.Template,
com.psddev.cms.db.ToolUi,
com.psddev.cms.db.WorkStream,
com.psddev.cms.tool.PageWriter,
com.psddev.cms.tool.Search,
com.psddev.cms.tool.ToolPageContext,

com.psddev.dari.db.Database,
com.psddev.dari.db.ObjectField,
com.psddev.dari.db.ObjectType,
com.psddev.dari.db.Query,
com.psddev.dari.db.State,
com.psddev.dari.util.ObjectUtils,

java.util.ArrayList,
java.util.Collections,
java.util.HashSet,
java.util.Iterator,
java.util.List,
java.util.Map,
java.util.Set,
java.util.UUID
" %><%

ToolPageContext wp = new ToolPageContext(pageContext);
PageWriter writer = wp.getWriter();
Search search = null;
String searchJson = wp.param(String.class, "search");

if (searchJson != null) {
    search = new Search();
    search.getState().setValues((Map<String, Object>) ObjectUtils.fromJson(searchJson));
}

String name = wp.param(String.class, Search.NAME_PARAMETER);

if (search == null) {
    UUID[] typeIds = (UUID[]) request.getAttribute("validTypeIds");

    if (ObjectUtils.isBlank(typeIds)) {
        Class<?> typeClass = (Class<?>) request.getAttribute("validTypeClass");

        if (typeClass != null) {
            typeIds = new UUID[] { ObjectType.getInstance(typeClass).getId() };
        }
    }

    if (typeIds != null) {
        search = new Search(wp, typeIds);

    } else {
        search = new Search(wp);
    }
}

search.setName(name);

Set<ObjectType> validTypes = search.findValidTypes();
ObjectType selectedType = search.getSelectedType();

if (validTypes.isEmpty()) {
    validTypes.addAll(ObjectType.getInstance(Content.class).findConcreteTypes());
}

String resultTarget = wp.createId();
String newJsp = (String) request.getAttribute("newJsp");
String newTarget = (String) request.getAttribute("newTarget");
boolean singleType = validTypes.size() == 1;

if (selectedType == null && singleType) {
    selectedType = validTypes.iterator().next();
}

List<ObjectType> globalFilters = new ArrayList<ObjectType>();

for (ObjectType t : Database.Static.getDefault().getEnvironment().getTypes()) {
    if (t.as(ToolUi.class).isGlobalFilter()) {
        globalFilters.add(t);
    }
}

globalFilters.remove(selectedType);
Collections.sort(globalFilters);

List<ObjectField> fieldFilters = new ArrayList<ObjectField>();

if (selectedType != null) {
    for (ObjectField field : selectedType.getIndexedFields()) {
        ToolUi fieldUi = field.as(ToolUi.class);

        if (fieldUi.isEffectivelyFilterable()) {
            fieldFilters.add(field);
        }
    }
}

if (wp.isFormPost()) {
    String workStreamName = wp.param(String.class, "workStreamName");

    if (!ObjectUtils.isBlank(workStreamName)) {
        WorkStream workStream = new WorkStream();
        workStream.setName(workStreamName);
        workStream.setQuery(search.toQuery());
        workStream.save();

        wp.writeStart("script", "type", "text/javascript");
            wp.write("window.location = window.location;");
        wp.writeEnd();

        return;
    }
}

writer.start("div", "class", "searchForm");
    writer.start("div", "class", "searchControls");

        writer.start("div", "class", "searchFilters");
            if ((!singleType && !validTypes.isEmpty()) ||
                    !globalFilters.isEmpty() ||
                    !fieldFilters.isEmpty()) {
                writer.start("h2").html("Filters").end();
            }

            writer.start("form",
                    "method", "get",
                    "action", wp.url(null));

                writer.tag("input", "type", "hidden", "name", "reset", "value", "true");
                writer.tag("input", "type", "hidden", "name", Search.NAME_PARAMETER, "value", search.getName());

                for (ObjectType type : search.getTypes()) {
                    writer.tag("input", "type", "hidden", "name", Search.TYPES_PARAMETER, "value", type.getId());
                }

                writer.tag("input", "type", "hidden", "name", Search.IS_ONLY_PATHED, "value", search.isOnlyPathed());
                writer.tag("input", "type", "hidden", "name", Search.ADDITIONAL_QUERY_PARAMETER, "value", search.getAdditionalPredicate());
                writer.tag("input", "type", "hidden", "name", Search.PARENT_PARAMETER, "value", search.getParentId());
                writer.tag("input", "type", "hidden", "name", Search.SUGGESTIONS_PARAMETER, "value", search.isSuggestions());

                writer.tag("input",
                        "type", "hidden",
                        "name", Search.QUERY_STRING_PARAMETER,
                        "value", search.getQueryString());

                if (!singleType && !validTypes.isEmpty()) {
                    wp.writeTypeSelect(
                            validTypes,
                            selectedType,
                            "Any Types",
                            "class", "autoSubmit",
                            "name", Search.SELECTED_TYPE_PARAMETER,
                            "data-searchable", true);
                }

            writer.end();

            writer.start("form",
                    "class", "autoSubmit",
                    "method", "get",
                    "action", wp.url(request.getAttribute("resultJsp")),
                    "target", resultTarget);

                writer.tag("input", "type", "hidden", "name", Search.NAME_PARAMETER, "value", search.getName());
                writer.tag("input", "type", "hidden", "name", Search.SORT_PARAMETER, "value", search.getSort());

                for (ObjectType type : search.getTypes()) {
                    writer.tag("input", "type", "hidden", "name", Search.TYPES_PARAMETER, "value", type.getId());
                }

                writer.tag("input", "type", "hidden", "name", Search.IS_ONLY_PATHED, "value", search.isOnlyPathed());
                writer.tag("input", "type", "hidden", "name", Search.ADDITIONAL_QUERY_PARAMETER, "value", search.getAdditionalPredicate());
                writer.tag("input", "type", "hidden", "name", Search.PARENT_PARAMETER, "value", search.getParentId());
                writer.tag("input", "type", "hidden", "name", Search.SUGGESTIONS_PARAMETER, "value", search.isSuggestions());
                writer.tag("input", "type", "hidden", "name", Search.OFFSET_PARAMETER, "value", search.getOffset());
                writer.tag("input", "type", "hidden", "name", Search.LIMIT_PARAMETER, "value", search.getLimit());
                writer.tag("input", "type", "hidden", "name", Search.SELECTED_TYPE_PARAMETER, "value", selectedType != null ? selectedType.getId() : null);

                writer.start("div", "class", "searchInput");
                    writer.start("label", "for", wp.createId()).html("Search").end();
                    writer.tag("input",
                            "type", "text",
                            "class", "autoFocus",
                            "id", wp.getId(),
                            "name", Search.QUERY_STRING_PARAMETER,
                            "value", search.getQueryString());
                    writer.start("button").html("Go").end();
                writer.end();

                if (selectedType == null) {
                    writer.start("div", "class", "searchFiltersGlobal");
                        for (ObjectType filter : globalFilters) {
                            String filterId = filter.getId().toString();
                            State filterState = State.getInstance(Query.from(Object.class).where("_id = ?", search.getGlobalFilters().get(filterId)).first());

                            writer.start("div", "class", "searchFilter");
                                writer.tag("input",
                                        "type", "text",
                                        "class", "objectId",
                                        "name", "gf." + filterId,
                                        "placeholder", "Filter: " + filter.getDisplayName(),
                                        "data-editable", false,
                                        "data-label", filterState != null ? filterState.getLabel() : null,
                                        "data-restorable", false,
                                        "data-typeIds", filterId,
                                        "value", filterState != null ? filterState.getId() : null);
                            writer.end();
                        }
                    writer.end();

                } else {
                    writer.start("div", "class", "searchFiltersLocal");
                        if (!fieldFilters.isEmpty()) {
                            writer.start("div", "class", "searchMissing");
                                writer.html("Missing?");
                            writer.end();
                        }

                        for (ObjectField field : fieldFilters) {
                            ToolUi fieldUi = field.as(ToolUi.class);
                            Set<ObjectType> fieldTypes = field.getTypes();
                            String fieldName = field.getInternalName();
                            StringBuilder fieldTypeIds = new StringBuilder();

                            if (!ObjectUtils.isBlank(fieldTypes)) {
                                for (ObjectType fieldType : fieldTypes) {
                                    fieldTypeIds.append(fieldType.getId()).append(",");
                                }

                                fieldTypeIds.setLength(fieldTypeIds.length() - 1);
                            }

                            String inputName = "f." + fieldName;
                            String displayName = field.getDisplayName();
                            String displayPrefix = (displayName.endsWith("?") ? displayName : displayName + ":") + " ";
                            Map<String, String> filterValue = search.getFieldFilters().get(fieldName);
                            String fieldValue = filterValue != null ? filterValue.get("") : null;
                            String fieldInternalItemType = field.getInternalItemType();

                            writer.start("div", "class", "searchFilter");
                                if (ObjectField.BOOLEAN_TYPE.equals(fieldInternalItemType)) {
                                    writer.writeStart("select", "name", inputName);
                                        writer.writeStart("option", "value", "").writeHtml(displayName).writeEnd();
                                        writer.writeStart("option",
                                                "selected", "true".equals(fieldValue) ? "selected" : null,
                                                "value", "true");
                                            writer.writeHtml(displayPrefix);
                                            writer.writeHtml("Yes");
                                        writer.writeEnd();

                                        writer.writeStart("option",
                                                "selected", "false".equals(fieldValue) ? "selected" : null,
                                                "value", "false");
                                            writer.writeHtml(displayPrefix);
                                            writer.writeHtml("No");
                                        writer.writeEnd();
                                    writer.writeEnd();

                                } else if (ObjectField.TEXT_TYPE.equals(fieldInternalItemType)) {
                                    if (field.getValues() == null || field.getValues().isEmpty()) {
                                        writer.writeTag("input",
                                                "type", "text",
                                                "name", inputName,
                                                "placeholder", displayName,
                                                "value", fieldValue);

                                        writer.writeTag("input",
                                                "type", "hidden",
                                                "name", inputName + ".s",
                                                "value", true);

                                    } else {
                                        writer.writeStart("select", "name", inputName);
                                            writer.writeStart("option", "value", "").writeHtml(displayName).writeEnd();

                                            for (ObjectField.Value v : field.getValues()) {
                                                writer.writeStart("option",
                                                        "selected", v.getValue().equals(fieldValue) ? "selected" : null,
                                                        "value", v.getValue());
                                                    writer.writeHtml(v.getLabel());
                                                writer.writeEnd();
                                            }
                                        writer.end();
                                    }

                                    writer.writeTag("input",
                                            "type", "checkbox",
                                            "name", inputName + ".m",
                                            "value", true,
                                            "checked", filterValue != null && ObjectUtils.to(boolean.class, filterValue.get("m")) ? "checked" : null);

                                } else {
                                    State fieldState = State.getInstance(Query.from(Object.class).where("_id = ?", fieldValue).first());

                                    writer.writeTag("input",
                                            "type", "text",
                                            "class", "objectId",
                                            "name", inputName,
                                            "placeholder", displayName,
                                            "data-additional-query", field.getPredicate(),
                                            "data-editable", false,
                                            "data-label", fieldState != null ? fieldState.getLabel() : null,
                                            "data-pathed", ToolUi.isOnlyPathed(field),
                                            "data-restorable", false,
                                            "data-searcher-path", fieldUi.getInputSearcherPath(),
                                            "data-typeIds", fieldTypeIds,
                                            "value", fieldValue);
                                    writer.writeTag("input",
                                            "type", "checkbox",
                                            "name", inputName + ".m",
                                            "value", true,
                                            "checked", filterValue != null && ObjectUtils.to(boolean.class, filterValue.get("m")) ? "checked" : null);
                                }
                            writer.end();
                        }
                    writer.end();
                }

            writer.end();

            writer.start("a",
                    "class", "action action-cancel",
                    "href", wp.url(null,
                            Search.NAME_PARAMETER, search.getName(),
                            "reset", true));
                writer.html("Reset");
            writer.end();

        writer.end();

        if (!ObjectUtils.isBlank(newJsp)) {
            writer.start("div", "class", "searchCreate");
                writer.start("h2").html("Create").end();

                writer.start("form",
                        "method", "get",
                        "action", wp.url(newJsp),
                        "target", ObjectUtils.isBlank(newTarget) ? null : newTarget);

                    if (singleType) {
                        writer.tag("input", "type", "hidden", "name", "typeId", "value", selectedType.getId());
                        writer.writeStart("button", "class", "action action-create", "style", "width: auto;");
                            writer.writeHtml("New " + wp.getObjectLabel(selectedType));
                        writer.writeEnd();

                    } else {
                        wp.writeTypeSelect(
                                validTypes,
                                selectedType,
                                null,
                                "name", "typeId",
                                "data-searchable", true);
                        writer.writeStart("button", "class", "action action-create");
                            writer.writeHtml("New");
                        writer.writeEnd();
                    }

                writer.end();
            writer.end();
        }

    writer.end();

    writer.start("div", "class", "searchResult frame", "name", resultTarget);
    writer.end();

writer.end();
%>
