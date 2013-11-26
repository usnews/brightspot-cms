<%@ page import="

com.psddev.cms.db.Content,
com.psddev.cms.db.Renderer,
com.psddev.cms.db.ToolUi,
com.psddev.cms.tool.PageWriter,
com.psddev.cms.tool.ToolPageContext,

com.psddev.dari.db.Database,
com.psddev.dari.db.Query,
com.psddev.dari.db.ObjectField,
com.psddev.dari.db.ObjectFieldComparator,
com.psddev.dari.db.ObjectType,
com.psddev.dari.db.State,

com.psddev.dari.util.CssUnit,
com.psddev.dari.util.HtmlGrid,
com.psddev.dari.util.HtmlObject,
com.psddev.dari.util.HtmlWriter,
com.psddev.dari.util.ObjectUtils,
com.psddev.dari.util.StringUtils,
com.psddev.dari.util.StorageItem,

java.io.IOException,
java.util.ArrayList,
java.util.Collections,
java.util.Date,
java.util.HashMap,
java.util.HashSet,
java.util.List,
java.util.Map,
java.util.Set,
java.util.UUID
" %><%

// --- Logic ---

final ToolPageContext wp = new ToolPageContext(pageContext);

State state = State.getInstance(request.getAttribute("object"));

final ObjectField field = (ObjectField) request.getAttribute("field");
String fieldName = field.getInternalName();
List<Object> fieldValue = (List<Object>) state.getValue(fieldName);
if (fieldValue == null) {
    fieldValue = new ArrayList<Object>();
}

List<ObjectType> validTypes = new ArrayList<ObjectType>(field.findConcreteTypes());
boolean isValueExternal = !field.isEmbedded();
if (isValueExternal && validTypes != null && validTypes.size() > 0) {
    isValueExternal = false;
    for (ObjectType type : validTypes) {
        if (!type.isEmbedded()) {
            isValueExternal = true;
            break;
        }
    }
}

Collections.sort(validTypes, new ObjectFieldComparator("_label", false));

final String inputName = (String) request.getAttribute("inputName");
String idName = inputName + ".id";
String typeIdName = inputName + ".typeId";
String publishDateName = inputName + ".publishDate";
String layoutsName = inputName + ".layouts";

if ((Boolean) request.getAttribute("isFormPost")) {
    if (!isValueExternal) {
        Map<UUID, Object> existing = new HashMap<UUID, Object>();

        for (Object item : fieldValue) {
            existing.put(State.getInstance(item).getId(), item);
        }

        fieldValue.clear();

        UUID[] ids = wp.uuidParams(idName);
        UUID[] typeIds = wp.uuidParams(typeIdName);
        Date[] publishDates = wp.dateParams(publishDateName);

        for (int i = 0, s = Math.min(Math.min(ids.length, typeIds.length), publishDates.length); i < s; ++ i) {
            Object item = existing.get(ids[i]);
            State itemState = State.getInstance(item);

            if (item != null) {
                itemState.setTypeId(typeIds[i]);

            } else {
                ObjectType type = ObjectType.getInstance(typeIds[i]);
                item = type.createObject(null);
                itemState = State.getInstance(item);
                itemState.setId(ids[i]);
            }

            wp.updateUsingParameters(item);
            itemState.putValue(Content.PUBLISH_DATE_FIELD, publishDates[i] != null ? publishDates[i] : new Date());
            itemState.putValue(Content.UPDATE_DATE_FIELD, new Date());
            fieldValue.add(item);

            if (field.isEmbedded() && !itemState.isNew()) {
                itemState.setId(null);
                itemState.setStatus(null);
            }
        }

    } else {
        fieldValue.clear();

        for (UUID id : wp.uuidParams(inputName)) {
            Object item = Query.findById(Object.class, id);
            if (item != null) {
                fieldValue.add(item);
            }
        }

        if (!ObjectUtils.isBlank(field.as(Renderer.FieldData.class).getListLayouts())) {
            state.as(Renderer.Data.class).getListLayouts().put(fieldName, wp.params(String.class, layoutsName));
        }
    }

    state.putValue(fieldName, fieldValue);
    return;
}

// --- Presentation ---

if (!isValueExternal) {
    Set<ObjectType> previewTypes = new HashSet<ObjectType>();
    Set<ObjectType> previewUploadTypes = new HashSet<ObjectType>();

    PREVIEWABLE: for (ObjectType t : validTypes) {
        for (ObjectField f : t.getFields()) {
            if (ObjectField.RECORD_TYPE.equals(f.getInternalType())) {
                for (ObjectType ft : f.getTypes()) {
                    if (ft.getPreviewField() != null) {
                        previewTypes.add(t);
                        previewUploadTypes.add(ft);
                    }
                }
            }
        }
    }

    if (previewTypes.size() != validTypes.size()) {
        previewUploadTypes.clear();
    }

    wp.writeStart("div", "class", "inputLarge repeatableForm" + (!previewUploadTypes.isEmpty() ? " repeatableForm-previewable" : ""));
        wp.writeStart("ol");
            for (Object item : fieldValue) {
                State itemState = State.getInstance(item);
                ObjectType itemType = itemState.getType();
                Date itemPublishDate = itemState.as(Content.ObjectModification.class).getPublishDate();

                wp.writeStart("li",
                        "data-type", wp.getObjectLabel(itemType),
                        "data-label", wp.getObjectLabel(item));
                    wp.writeTag("input",
                            "type", "hidden",
                            "name", idName,
                            "value", itemState.getId());

                    wp.writeTag("input",
                            "type", "hidden",
                            "name", typeIdName,
                            "value", itemType.getId());

                    wp.writeTag("input",
                            "type", "hidden",
                            "name", publishDateName,
                            "value", itemPublishDate != null ? itemPublishDate.getTime() : null);

                    wp.writeFormFields(item);
                wp.writeEnd();
            }

            for (ObjectType type : validTypes) {
                wp.writeStart("li",
                        "class", "template" + (!previewUploadTypes.isEmpty() ? " collapsed" : ""),
                        "data-type", wp.getObjectLabel(type));
                    wp.writeStart("a",
                            "href", wp.cmsUrl("/content/repeatableObject.jsp",
                                    "inputName", inputName,
                                    "typeId", type.getId()));
                    wp.writeEnd();
                wp.writeEnd();
            }
        wp.writeEnd();

        if (!previewUploadTypes.isEmpty()) {
            StringBuilder typeIdsQuery = new StringBuilder();

            for (ObjectType type : previewUploadTypes) {
                typeIdsQuery.append("typeId=").append(type.getId()).append("&");
            }

            typeIdsQuery.setLength(typeIdsQuery.length() - 1);

            wp.writeStart("a",
                    "class", "action-upload",
                    "href", wp.url("/content/uploadFiles?" + typeIdsQuery),
                    "target", "uploadFiles");
                wp.writeHtml("Upload Files");
            wp.writeEnd();
        }
    wp.writeEnd();

} else {
    Set<ObjectType> types = field.getTypes();
    final StringBuilder typeIdsCsv = new StringBuilder();
    StringBuilder typeIdsQuery = new StringBuilder();
    boolean previewable = false;

    if (types != null && !types.isEmpty()) {
        for (ObjectType type : types) {
            typeIdsCsv.append(type.getId()).append(",");
            typeIdsQuery.append("typeId=").append(type.getId()).append("&");

            if (!ObjectUtils.isBlank(type.getPreviewField())) {
                previewable = true;
            }
        }

        typeIdsCsv.setLength(typeIdsCsv.length() - 1);
        typeIdsQuery.setLength(typeIdsQuery.length() - 1);
    }

    PageWriter writer = wp.getWriter();
    Map<String, List<String>> layouts = field.as(Renderer.FieldData.class).getListLayouts();

    if (layouts != null && !layouts.isEmpty()) {
        String containerId = wp.createId();

        Map<String, HtmlGrid> grids = new HashMap<String, HtmlGrid>();

        writer.writeStart("style", "type", "text/css");
            writer.writeCommonGridCss();

            for (String layoutName : layouts.keySet()) {
                HtmlGrid grid = HtmlGrid.Static.find(application, layoutName);

                if (grid == null) {
                    throw new IllegalArgumentException(String.format(
                            "[%s] isn't a valid layout! Check your CSS and makes sure it's defined properly.",
                            layoutName));
                }

                List<CssUnit> frColumns = new ArrayList<CssUnit>();
                List<List<String>> frTemplate = new ArrayList<List<String>>();

                for (CssUnit column : grid.getColumns()) {
                    String unit = column.getUnit();

                    if ("px".equals(unit)) {
                        column = new CssUnit(column.getNumber() / 200, "fr");

                    } else if ("em".equals(unit)) {
                        column = new CssUnit(column.getNumber() / 20, "fr");
                    }

                    frColumns.add(column);
                }

                for (List<String> t : grid.getTemplate()) {
                    frTemplate.add(new ArrayList<String>(t));
                }

                grid = new HtmlGrid(frColumns, grid.getRows(), frTemplate);

                grids.put(layoutName, grid);
                writer.writeGridCss("." + layoutName, grid);
            }
        writer.writeEnd();

        writer.start("div",
                "class", "inputLarge repeatableLayout",
                "id", containerId);
            writer.start("ol");

                List<String> fieldLayoutNames = state.as(Renderer.Data.class).getListLayouts().get(fieldName);

                if (fieldLayoutNames != null) {
                    int itemIndex = 0;

                    for (String fieldLayoutName : fieldLayoutNames) {
                        writer.start("li");

                            List<String> layoutNames = new ArrayList<String>(layouts.keySet());
                            Collections.sort(layoutNames);

                            writer.start("div", "class", "repeatableLabel");
                                writer.start("select",
                                        "class", "toggleable",
                                        "data-root", "li",
                                        "name", layoutsName);
                                    for (String layoutName : layoutNames) {
                                        writer.start("option",
                                                "data-hide", ".layout",
                                                "data-show", "." + layoutName,
                                                "selected", layoutName.equals(fieldLayoutName) ? "selected" : null,
                                                "value", layoutName);
                                            writer.html(StringUtils.toLabel(layoutName));
                                        writer.end();
                                    }
                                writer.end();
                            writer.end();

                            writer.start("div", "class", "layouts");
                                for (String layoutName : layoutNames) {
                                    HtmlGrid grid = grids.get(layoutName);
                                    List<HtmlObject> values = new ArrayList<HtmlObject>();

                                    for (int i = 0, size = grid.getAreas().size(); i < size; ++ i) {
                                        String itemClass = i < layouts.get(layoutName).size() ? layouts.get(layoutName).get(i) : null;
                                        final StringBuilder itemTypeIdsCsv = new StringBuilder();
                                        Set<ObjectType> itemTypes = itemClass != null ? Database.Static.getDefault().getEnvironment().getTypesByGroup(itemClass) : null;

                                        if (itemTypes == null || itemTypes.isEmpty()) {
                                            itemTypeIdsCsv.append(typeIdsCsv);

                                        } else {
                                            for (ObjectType type : itemTypes) {
                                                itemTypeIdsCsv.append(type.getId()).append(",");
                                            }
                                            itemTypeIdsCsv.setLength(itemTypeIdsCsv.length() - 1);
                                        }

                                        final State itemState;

                                        if (!layoutName.equals(fieldLayoutName)) {
                                            itemState = null;

                                        } else {
                                            itemState = itemIndex < fieldValue.size() ? State.getInstance(fieldValue.get(itemIndex)) : null;
                                            ++ itemIndex;
                                        }

                                        values.add(new HtmlObject() {
                                            public void format(HtmlWriter writer) throws IOException {
                                                StorageItem preview = itemState != null ? itemState.getPreview() : null;

                                                writer.start("div", "class", "inputContainer-listLayoutItemContainer");
                                                    writer.start("div", "class", "inputContainer-listLayoutItem");
                                                        writer.tag("input",
                                                                "type", "text",
                                                                "class", "objectId",
                                                                "data-searcher-path", field.as(ToolUi.class).getInputSearcherPath(),
                                                                "data-label", itemState != null ? itemState.getLabel() : null,
                                                                "data-typeIds", itemTypeIdsCsv,
                                                                "data-pathed", ToolUi.isOnlyPathed(field),
                                                                "data-additional-query", field.getPredicate(),
                                                                "data-preview", preview != null ? preview.getUrl() : null,
                                                                "name", inputName,
                                                                "value", itemState != null ? itemState.getId() : null);
                                                    writer.end();
                                                writer.end();
                                            }
                                        });
                                    }

                                    writer.start("div", "class", "layout " + layoutName);
                                        writer.grid(values, grid, true);
                                    writer.end();
                                }
                            writer.end();

                        writer.end();
                    }
                }

                writer.start("li", "class", "template");

                    List<String> layoutNames = new ArrayList<String>(layouts.keySet());
                    Collections.sort(layoutNames);

                    writer.start("div", "class", "repeatableLabel");
                        writer.start("select",
                                "class", "toggleable",
                                "data-root", "li",
                                "name", layoutsName);
                            for (String layoutName : layoutNames) {
                                writer.start("option",
                                        "data-hide", ".layout",
                                        "data-show", "." + layoutName,
                                        "value", layoutName);
                                    writer.html(StringUtils.toLabel(layoutName));
                                writer.end();
                            }
                        writer.end();
                    writer.end();

                    writer.start("div", "class", "layouts");
                        for (String layoutName : layoutNames) {
                            HtmlGrid grid = grids.get(layoutName);
                            List<HtmlObject> values = new ArrayList<HtmlObject>();

                            for (int i = 0, size = grid.getAreas().size(); i < size; ++ i) {
                                String itemClass = i < layouts.get(layoutName).size() ? layouts.get(layoutName).get(i) : null;
                                final StringBuilder itemTypeIdsCsv = new StringBuilder();
                                Set<ObjectType> itemTypes = itemClass != null ? Database.Static.getDefault().getEnvironment().getTypesByGroup(itemClass) : null;

                                if (itemTypes == null || itemTypes.isEmpty()) {
                                    itemTypeIdsCsv.append(typeIdsCsv);

                                } else {
                                    for (ObjectType type : itemTypes) {
                                        itemTypeIdsCsv.append(type.getId()).append(",");
                                    }
                                    itemTypeIdsCsv.setLength(itemTypeIdsCsv.length() - 1);
                                }

                                values.add(new HtmlObject() {
                                    public void format(HtmlWriter writer) throws IOException {
                                        writer.start("div", "class", "inputContainer-listLayoutItemContainer");
                                            writer.start("div", "class", "inputContainer-listLayoutItem");
                                                writer.tag("input",
                                                        "type", "text",
                                                        "class", "objectId",
                                                        "data-searcher-path", field.as(ToolUi.class).getInputSearcherPath(),
                                                        "data-typeIds", itemTypeIdsCsv,
                                                        "data-pathed", ToolUi.isOnlyPathed(field),
                                                        "data-additional-query", field.getPredicate(),
                                                        "name", inputName);
                                            writer.end();
                                        writer.end();
                                    }
                                });
                            }

                            writer.start("div", "class", "layout " + layoutName);
                                writer.grid(values, grid, true);
                            writer.end();
                        }
                    writer.end();

                writer.end();
            writer.end();
        writer.end();

        return;
    }

    writer.start("div", "class", "inputSmall repeatableObjectId" + (previewable ? " repeatableObjectId-previewable" : ""));
        writer.start("ol");
            if (fieldValue != null) {
                for (Object item : fieldValue) {
                    writer.start("li");
                        wp.writeObjectSelect(field, item, "name", inputName);
                    writer.end();
                }
            }
            writer.start("li", "class", "template");
                wp.writeObjectSelect(field, null, "name", inputName);
            writer.end();
        writer.end();

        if (previewable) {
            writer.start("a",
                    "class", "action-upload",
                    "href", wp.url("/content/uploadFiles?" + typeIdsQuery),
                    "target", "uploadFiles");
                writer.html("Upload Files");
            writer.end();
        }
    writer.end();
}
%>
