<%@ page import="

com.psddev.cms.db.ToolSearch,
com.psddev.cms.db.ToolUi,
com.psddev.cms.tool.ToolPageContext,

com.psddev.dari.db.CompoundPredicate,
com.psddev.dari.db.Database,
com.psddev.dari.db.DatabaseEnvironment,
com.psddev.dari.db.FormInputProcessor,
com.psddev.dari.db.FormLabelRenderer,
com.psddev.dari.db.FormWriter,
com.psddev.dari.db.ObjectField,
com.psddev.dari.db.ObjectFieldComparator,
com.psddev.dari.db.ObjectIndex,
com.psddev.dari.db.ObjectType,
com.psddev.dari.db.Query,
com.psddev.dari.db.Predicate,
com.psddev.dari.db.PredicateParser,
com.psddev.dari.db.Recordable,
com.psddev.dari.db.State,
com.psddev.dari.util.HtmlWriter,
com.psddev.dari.util.JspUtils,
com.psddev.dari.util.ObjectUtils,
com.psddev.dari.util.PaginatedResult,

java.io.IOException,
java.io.StringWriter,
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

if (wp.requireUser()) {
    return;
}

SearchFormWriter html = new SearchFormWriter(wp);

State userState = State.getInstance(wp.getUser());
ToolSearch search = Query.from(ToolSearch.class).where("_id = ?", wp.uuidParam("id")).first();

if (search == null) {
    search = Query.from(ToolSearch.class).where("_id = ?", userState.get("cms.lastSearch")).first();

    if (search != null) {
        wp.redirect(null, "id", search.getId());
        return;
    } else {
        search = new ToolSearch();
    }
}

ObjectType queryType = Database.Static.getDefault().getEnvironment().getTypeById(wp.uuidParam("typeId"));
if (queryType == null) {
    queryType = search.getQueryType();
} else {
    search.setQueryType(queryType);
}

State queryState = null;
List<ObjectField> extraFields = new ArrayList<ObjectField>();

if (queryType != null) {
    queryState = State.getInstance(queryType.createObject(search.getId()));

    if (wp.isFormPost()) {
        search.setSortFieldName(wp.param("sort"));
        for (ObjectField field : search.getIndexedFields()) {
            html.update(queryState, wp.getRequest(), field.getInternalName());
        }
        for (ObjectField field : search.getIndexedFields()) {
            String name = field.getInternalName();
            queryState.put(ToolSearch.OPERATOR_PREFIX + name, wp.param(queryState.getId() + "/" + ToolSearch.OPERATOR_PREFIX + name));
        }

    } else {
        queryState.putAll(search.getState());
    }

    State searchState = search.getState();
    for (Iterator<Map.Entry<String, Object>> i = searchState.entrySet().iterator(); i.hasNext(); ) {
        Map.Entry<String, Object> entry = i.next();
        String key = entry.getKey();
        if (key != null &&
                (key.startsWith(ToolSearch.FIELD_PREFIX) ||
                key.startsWith(ToolSearch.OPERATOR_PREFIX))) {
            i.remove();
        }
    }

    searchState.putAll(queryState.getSimpleValues());
    searchState.setTypeId(ObjectType.getInstance(ToolSearch.class).getId());
    searchState.save();

    userState.put("cms.lastSearch", search.getId().toString());
    userState.save();

    if (wp.isFormPost()) {
        wp.redirect("");
    }
}

wp.include("/WEB-INF/header.jsp");

html.start("form", "action", wp.url(null, "id", search.getId()), "method", "post");
    html.start("div", "class", "withLeftNav");
        html.start("div", "class", "leftNav", "style", "width: 400px;");
            html.start("div", "class", "widget");
                html.start("h1").string("Advanced Search").end();

                html.start("div", "class", "inputContainer");
                    html.start("div", "class", "inputLabel");
                        html.start("label").string("Type").end();
                    html.end();

                    html.start("div", "class", "inputSmall");
                        html.start("select", "name", "typeId", "onchange", "$(this).closest('form').submit();", "style", "max-width: 100%;");
                            html.start("option").end();

                            List<ObjectType> types = new ArrayList<ObjectType>(Database.Static.getDefault().getEnvironment().getTypes());
                            Collections.sort(types, new ObjectFieldComparator("name", false));
                            for (ObjectType type : types) {
                                html.start("option",
                                        "selected", type.equals(queryType) ? "selected" : null,
                                        "value", type.getId());
                                    html.write(wp.objectLabel(type));
                                html.end();
                            }
                        html.end();
                    html.end();
                html.end();

                if (queryType != null) {
                    for (ObjectField field : search.getIndexedFields()) {
                        html.inputs(queryState, field.getInternalName());
                    }

                    html.start("div", "class", "buttons");
                        html.tag("input", "type", "submit", "value", "Search");
                    html.end();
                }
            html.end();
        html.end();

        if (queryType != null) {
            html.start("div", "class", "main", "style", "margin-left: 415px;");
                html.start("div", "class", "widget");
                    html.start("h1").string("Result").end();

                    PaginatedResult<?> result = search.toQuery().select(wp.longParam("offset", 0L), wp.intParam("limit", 50));
                    ObjectField sortField = search.getSortField();

                    extraFields.add(sortField);
                    for (String labelField : queryType.getLabelFields()) {
                        for (Iterator<ObjectField> i = extraFields.iterator(); i.hasNext(); ) {
                            if (i.next().getInternalName().equals(labelField)) {
                                i.remove();
                            }
                        }
                    }

                    html.string("Sort By: ");
                    html.start("select", "name", "sort", "onchange", "$(this).closest('form').submit();");
                        for (ObjectField field : search.getSortableFields()) {
                            html.start("option",
                                    "selected", field.equals(sortField) ? "selected" : null,
                                    "value", field.getInternalName());
                                html.string(field.getDisplayName());
                            html.end();
                        }
                    html.end();

                    html.start("ul", "class", "pagination");
                        if (result.hasPrevious()) {
                            html.start("li", "class", "previous");
                                html.start("a", "href", wp.url("", "offset", result.getPreviousOffset()));
                                    html.string("Previous ").string(result.getLimit());
                                html.end();
                            html.end();
                        }

                        html.start("li", "class", "label");
                            html.start("strong").string(result.getFirstItemIndex()).end();
                            html.string(" to ").start("strong").string(result.getLastItemIndex()).end();
                            html.string(" of ").start("strong").string(result.getCount()).end();
                        html.end();

                        if (result.hasNext()) {
                            html.start("li", "class", "next");
                                html.start("a", "href", wp.url("", "offset", result.getNextOffset()));
                                    html.string("Next ").string(result.getLimit());
                                html.end();
                            html.end();
                        }
                    html.end();

                    html.start("table");
                        html.start("thead");
                            html.start("th");
                                for (String labelField : queryType.getLabelFields()) {
                                    html.string(queryState.getField(labelField).getDisplayName());
                                }
                            html.end();

                            for (ObjectField field : extraFields) {
                                html.start("th").string(field.getDisplayName()).end();
                            }
                        html.end();

                        html.start("tbody");
                            for (Object item : result.getItems()) {
                                State itemState = State.getInstance(item);
                                html.start("tr");
                                    html.start("td");
                                        html.start("a", "href", wp.url("/content/edit.jsp",
                                                "id", itemState.getId(),
                                                "searchId", search.getId()));
                                            html.string(itemState.getLabel());
                                        html.end();
                                    html.end();

                                    for (ObjectField field : extraFields) {
                                        Object value = itemState.get(field.getInternalName());
                                        html.start("td").string(value instanceof Recordable ? ((Recordable) value).getState().getLabel() : value).end();
                                    }
                                html.end();
                            }
                        html.end();
                    html.end();
                html.end();
            html.end();
        }
    html.end();
html.end();

wp.include("/WEB-INF/footer.jsp");
%><%!

private static class SearchFormWriter extends FormWriter {

    private final ToolPageContext page;

    public SearchFormWriter(ToolPageContext page) throws IOException {
        super(page.getWriter());
        this.page = page;

        setLabelRenderer(new FormLabelRenderer.Default() {
            @Override
            protected void doDisplay(String inputId, String inputName, ObjectField field, HtmlWriter writer) throws IOException {
                writer.start("div", "class", "inputLabel");
                    super.doDisplay(inputId, inputName, field, writer);
                writer.end();
            }
        });

        Map<String, FormInputProcessor> processors = getInputProcessors();
        processors.put(ObjectField.RECORD_TYPE, RECORD);
        processors.put(ObjectField.REFERENTIAL_TEXT_TYPE, TEXT);
        processors.put(ObjectField.TEXT_TYPE, TEXT);
    }

    @Override
    protected void writeField(State state, ObjectField field, FormInputProcessor processor) throws IOException {
        if (processor == null) {
            processor = getInputProcessors().get(field.getInternalItemType());
        }

        String fieldName = field.getInternalName();
        String inputId = "i" + UUID.randomUUID().toString().replace("-", "");
        String inputName = state.getId() + "/" + fieldName;

        start("div", "class", "inputContainer");
            write(getLabelRenderer().display(inputId, inputName, field));
            if (processor == null) {
                start("div", "class", "inputSmall").string("N/A").end();
            } else {
                write(processor.display(inputId, inputName, field, state.get(ToolSearch.FIELD_PREFIX + fieldName)));
            }
        end();
    }

    @Override
    protected void updateField(State state, HttpServletRequest request, ObjectField field, FormInputProcessor processor) {
        if (processor == null) {
            processor = getInputProcessors().get(field.getInternalItemType());
        }

        String fieldName = field.getInternalName();
        String inputName = state.getId() + "/" + fieldName;

        if (processor != null) {
            state.put(ToolSearch.FIELD_PREFIX + fieldName, processor.update(inputName, field, request));
        }
    }

    private static final FormInputProcessor RECORD = new FormInputProcessor.Abstract() {

        @Override
        protected void doDisplay(String inputId, String inputName, ObjectField field, Object value, HtmlWriter writer) throws IOException {
            Set<ObjectType> validTypes = field.getTypes();
            String validTypeIds;

            if (validTypes == null || validTypes.isEmpty()) {
                validTypeIds = "";

            } else {
                StringBuilder idsBuilder = new StringBuilder();
                for (ObjectType type : validTypes) {
                    idsBuilder.append(type.getId()).append(",");
                }
                idsBuilder.setLength(idsBuilder.length() - 1);
                validTypeIds = idsBuilder.toString();
            }

            writer.start("div", "class", "inputSmall repeatableObjectId");
                writer.start("ul");

                    if (value != null) {
                        for (Object item : (List<?>) value) {
                            UUID id = ObjectUtils.to(UUID.class, item);
                            State state = State.getInstance(Query.from(Object.class).where("_id = ?", id).first());

                            if (state == null) {
                                continue;
                            }

                            String typeLabel = "";
                            if (validTypes == null || validTypes.size() != 1) {
                                ObjectType type = state.getType();
                                if (type != null) {
                                    typeLabel = type.getLabel();
                                }
                            }

                            writer.start("li");
                                writer.tag("input",
                                    "type", "text",
                                    "class", "objectId",
                                    "name", inputName,
                                    "data-label", typeLabel + state.getLabel(),
                                    "data-pathed", ToolUi.isOnlyPathed(field),
                                    "data-preview", state.getPreview(),
                                    "data-typeIds", validTypeIds,
                                    "value", id);
                            writer.end();
                        }
                    }

                    writer.start("li", "class", "template");
                        writer.tag("input",
                            "type", "text",
                            "class", "objectId",
                            "name", inputName,
                            "data-pathed", ToolUi.isOnlyPathed(field),
                            "data-typeIds", validTypeIds);
                    writer.end();

                writer.end();
            writer.end();
        }

        public Object update(String inputName, ObjectField field, HttpServletRequest request) {
            List<String> values = new ArrayList<String>();
            String[] inputs = request.getParameterValues(inputName);

            if (inputs != null) {
                for (String input : inputs) {
                    if (!ObjectUtils.isBlank(input)) {
                        values.add(input);
                    }
                }
            }

            return values;
        }
    };

    private static final FormInputProcessor TEXT = new FormInputProcessor.Abstract() {

        private String createToggleName(String inputName) {
            return inputName + ".toggle";
        }

        private String createTextName(String inputName) {
            return inputName + ".text";
        }

        @Override
        protected void doDisplay(String inputId, String inputName, ObjectField field, Object value, HtmlWriter writer) throws IOException {
            Set<ObjectField.Value> validValues = field.getValues();
            String textName = createTextName(inputName);

            writer.start("div", "class", "inputSmall repeatableText");
                writer.start("ul");

                    if (validValues == null || validValues.isEmpty()) {
                        String toggleName = createToggleName(inputName);

                        if (value != null) {
                            for (Object item : (List<?>) value) {
                                writer.start("li");
                                    writer.tag("input", "type", "checkbox", "name", toggleName, "value", "true");
                                    writer.tag("input", "type", "text", "class", "expandable", "name", textName, "value", item);
                                writer.end();
                            }
                        }

                        writer.start("li", "class", "template");
                            writer.tag("input", "type", "checkbox", "name", toggleName, "value", "true");
                            writer.tag("input", "type", "text", "class", "expandable", "name", textName);
                        writer.end();

                    } else {
                        writer.start("select", "multiple", "multiple", "name", textName);
                            for (ObjectField.Value validValue : validValues) {
                                writer.start("option", "value", validValue.getValue());
                                    writer.string(validValue.getLabel());
                                writer.end();
                            }
                        writer.end();
                    }

                writer.end();
            writer.end();
        }

        public Object update(String inputName, ObjectField field, HttpServletRequest request) {
            List<String> values = new ArrayList<String>();
            String[] texts = request.getParameterValues(createTextName(inputName));

            if (texts != null) {
                String[] toggles = request.getParameterValues(createToggleName(inputName));

                if (toggles != null) {
                    for (int i = 0, size = Math.min(texts.length, toggles.length); i < size; ++ i) {
                        if (Boolean.parseBoolean(toggles[i])) {
                            String text = texts[i];
                            if (!ObjectUtils.isBlank(text)) {
                                values.add(text);
                            }
                        }
                    }

                } else {
                    for (String text : texts) {
                        if (!ObjectUtils.isBlank(text)) {
                            values.add(text);
                        }
                    }
                }
            }

            return values;
        }
    };
}
%>
