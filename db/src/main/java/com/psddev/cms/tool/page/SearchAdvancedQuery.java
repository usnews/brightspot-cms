package com.psddev.cms.tool.page;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.servlet.ServletException;

import com.psddev.cms.tool.PageServlet;
import com.psddev.cms.tool.ToolPageContext;
import com.psddev.dari.db.ComparisonPredicate;
import com.psddev.dari.db.CompoundPredicate;
import com.psddev.dari.db.Database;
import com.psddev.dari.db.DatabaseEnvironment;
import com.psddev.dari.db.ObjectField;
import com.psddev.dari.db.ObjectIndex;
import com.psddev.dari.db.ObjectStruct;
import com.psddev.dari.db.ObjectType;
import com.psddev.dari.db.Predicate;
import com.psddev.dari.db.Query;
import com.psddev.dari.util.ObjectUtils;
import com.psddev.dari.util.RoutingFilter;
import com.psddev.dari.util.StringUtils;

@RoutingFilter.Path(application = "cms", value = "searchAdvancedQuery")
public class SearchAdvancedQuery extends PageServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected String getPermissionId() {
        return null;
    }

    @Override
    protected void doService(ToolPageContext page) throws IOException, ServletException {
        List<String> paramNames = page.paramNamesList();
        int lastIndex = -1;
        Predicate globalPredicate = null;
        PredicateType globalPredicateType = page.param(PredicateType.class, "gpt");

        Collections.sort(paramNames);

        page.writeHeader();
            page.writeStart("div", "class", "widget widget-searchAdvancedQuery");
                page.writeStart("h1", "class", "icon icon-wrench");
                    page.writeHtml("Advanced Query Builder");
                page.writeEnd();

                page.writeStart("style", "type", "text/css");
                    page.writeCss(".form-submit",
                            "opacity", "0.5",
                            "pointer-events", "none");
                page.writeEnd();

                page.writeStart("form",
                        "method", "get",
                        "action", page.url(null),
                        "onsubmit", "$(this).addClass('form-submit');return true;");
                    page.writeStart("select",
                            "class", "autoSubmit",
                            "name", "gpt");
                        for (PredicateType pt : PredicateType.values()) {
                            if (pt.getCompoundOperator() != null) {
                                if (globalPredicateType == null) {
                                    globalPredicateType = pt;
                                }

                                page.writeStart("option",
                                        "selected", pt.equals(globalPredicateType) ? "selected" : null,
                                        "value", pt.name());
                                    page.writeHtml(pt.getLabel());
                                    page.writeHtml(":");
                                page.writeEnd();
                            }
                        }
                    page.writeEnd();

                    page.writeStart("div", "class", "fixedScrollable");
                        page.writeStart("ul");
                            for (String paramName : paramNames) {
                                if (paramName.startsWith("p")) {
                                    Integer index = ObjectUtils.to(Integer.class, paramName.substring(1));

                                    if (index != null) {
                                        if (lastIndex < index) {
                                            lastIndex = index;
                                        }

                                        page.writeStart("li");
                                            globalPredicate = CompoundPredicate.combine(
                                                    globalPredicateType.getCompoundOperator(),
                                                    globalPredicate,
                                                    writePredicateType(page, paramNames, paramName, String.valueOf(index)));
                                        page.writeEnd();
                                    }
                                }
                            }
                        page.writeEnd();
                    page.writeEnd();

                    page.writeStart("button",
                            "class", "icon icon-action-add link",
                            "name", "p" + (lastIndex + 1),
                            "value", 1);
                        page.writeHtml("Add Another ");
                        page.writeHtml(globalPredicateType.getLabel());
                    page.writeEnd();

                    page.writeStart("div", "class", "actions");
                        page.writeStart("button",
                                "class", "icon icon-action-search",
                                "name", "action-search",
                                "value", true);
                            page.writeHtml("Search");
                        page.writeEnd();
                    page.writeEnd();
                page.writeEnd();
            page.writeEnd();

            if (page.param(String.class, "action-search") != null) {
                String pageId = page.createId();

                page.writeStart("div", "id", pageId);
                page.writeEnd();

                page.writeStart("script", "type", "text/javascript");
                    page.writeRaw("var $page = $('#" + pageId + "'),");
                    page.writeRaw("$textarea = $page.popup('source').closest('.searchFilter-advancedQuery').find('textarea');");
                    page.writeRaw("$textarea.val('" + StringUtils.escapeJavaScript(globalPredicate != null ? globalPredicate.toString() : "") + "');");
                    page.writeRaw("$textarea.change();");
                    page.writeRaw("$page.popup('close');");
                page.writeEnd();
            }
        page.writeFooter();
    }

    private Predicate writePredicateType(
            ToolPageContext page,
            List<String> paramNames,
            String predicateParam,
            String paramPrefix)
            throws IOException {

        page.writeTag("input",
                "type", "hidden",
                "name", predicateParam,
                "value", 1);

        String predicateTypeParam = paramPrefix + ".pt";
        PredicateType predicateType = page.paramOrDefault(PredicateType.class, predicateTypeParam, PredicateType.C);

        page.writeStart("select",
                "class", "autoSubmit",
                "name", predicateTypeParam);
            for (PredicateType pt : PredicateType.values()) {
                page.writeStart("option",
                        "selected", pt.equals(predicateType) ? "selected" : null,
                        "value", pt.name());
                    page.writeHtml(pt.getLabel());
                page.writeEnd();
            }
        page.writeEnd();

        if (PredicateType.C.equals(predicateType)) {
            String comparisonTypeParam = paramPrefix + ".ct";
            String comparisonFieldNameParam = paramPrefix + ".cf";
            String comparisonOperatorParam = paramPrefix + ".co";
            String comparisonValueParam = paramPrefix + ".cv";
            DatabaseEnvironment environment = Database.Static.getDefault().getEnvironment();
            ObjectType comparisonType = ObjectType.getInstance(page.param(UUID.class, comparisonTypeParam));
            String comparisonFieldName = page.param(String.class, comparisonFieldNameParam);
            ObjectField comparisonField = environment.getField(comparisonFieldName);
            ComparisonOperator comparisonOperator = page.param(ComparisonOperator.class, comparisonOperatorParam);

            if (comparisonField == null && comparisonType != null) {
                comparisonField = comparisonType.getField(comparisonFieldName);
            }

            page.writeHtml(" ");
            page.writeTypeSelect(
                    null,
                    comparisonType,
                    "Any Types",
                    "class", "autoSubmit",
                    "name", comparisonTypeParam,
                    "data-searchable", true);

            page.writeHtml(" ");
            page.writeStart("select",
                    "class", "autoSubmit",
                    "name", comparisonFieldNameParam,
                    "data-searchable", true);
                page.writeStart("option", "value", "");
                    page.writeHtml("Any Fields");
                page.writeEnd();

                if (comparisonType != null) {
                    page.writeStart("optgroup", "label", comparisonType.getLabel());
                        for (ObjectField field : getIndexedFields(comparisonType)) {
                            String fieldName = field.getInternalName();

                            page.writeStart("option",
                                    "selected", field.equals(comparisonField) ? "selected" : null,
                                    "value", fieldName);
                                page.writeHtml(field.getDisplayName());
                            page.writeEnd();
                        }
                    page.writeEnd();
                }

                page.writeStart("optgroup", "label", "Global");
                    for (ObjectField field : getIndexedFields(environment)) {
                        String fieldName = field.getInternalName();

                        page.writeStart("option",
                                "selected", field.equals(comparisonField) ? "selected" : null,
                                "value", fieldName);
                            page.writeHtml(field.getDisplayName());
                        page.writeEnd();
                    }
                page.writeEnd();
            page.writeEnd();

            page.writeHtml(" ");
            page.writeStart("select",
                    "class", "autoSubmit",
                    "name", comparisonOperatorParam);
                for (ComparisonOperator op : ComparisonOperator.values()) {
                    if ((comparisonField == null &&
                            !op.equals(ComparisonOperator.M)) ||
                            (comparisonField != null &&
                            !op.isDisplayedFor(comparisonField))) {
                        if (op.equals(comparisonOperator)) {
                            comparisonOperator = null;
                        }

                        continue;
                    }

                    if (comparisonOperator == null) {
                        comparisonOperator = op;
                    }

                    page.writeStart("option",
                            "selected", op.equals(comparisonOperator) ? "selected" : null,
                            "value", op.name());
                        page.writeHtml(op.getLabel());
                        page.writeHtml(":");
                    page.writeEnd();
                }
            page.writeEnd();

            page.writeHtml(" ");
            comparisonOperator.writeValueInputs(page, comparisonValueParam, comparisonField);

            return CompoundPredicate.combine(
                    "AND",
                    comparisonType == null ? null : new ComparisonPredicate("=", false, "_type", Arrays.asList(comparisonType.getId())),
                    comparisonOperator.createPredicate(page, comparisonValueParam, comparisonField));

        } else if (predicateType.getCompoundOperator() != null) {
            Predicate predicate = null;
            String subPredicateIndexParam = paramPrefix + ".p";
            int lastSubPredicateIndex = -1;

            page.writeStart("ul");
                for (String paramName : paramNames) {
                    if (paramName.startsWith(subPredicateIndexParam)) {
                        Integer subPredicateIndex = ObjectUtils.to(Integer.class, paramName.substring(subPredicateIndexParam.length()));

                        if (subPredicateIndex != null) {
                            if (lastSubPredicateIndex < subPredicateIndex) {
                                lastSubPredicateIndex = subPredicateIndex;
                            }

                            page.writeStart("li");
                                predicate = CompoundPredicate.combine(
                                        predicateType.getCompoundOperator(),
                                        predicate,
                                        writePredicateType(page, paramNames, paramName, paramPrefix + "." + subPredicateIndex));
                            page.writeEnd();
                        }
                    }
                }
            page.writeEnd();

            page.writeStart("button",
                    "class", "icon icon-action-add link",
                    "name", paramPrefix + ".p" + (lastSubPredicateIndex + 1),
                    "value", 1);
                page.writeHtml("Add Another ");
                page.writeHtml(predicateType.getLabel());
            page.writeEnd();

            return predicate;

        } else {
            return null;
        }
    }

    private List<ObjectField> getIndexedFields(ObjectStruct struct) {
        List<ObjectField> fields = struct.getFields();
        Set<String> indexedFields = new HashSet<String>();

        for (ObjectIndex index : struct.getIndexes()) {
            indexedFields.addAll(index.getFields());
        }

        for (Iterator<ObjectField> i = fields.iterator(); i.hasNext(); ) {
            if (!indexedFields.contains(i.next().getInternalName())) {
                i.remove();
            }
        }

        Collections.sort(fields);

        return fields;
    }

    private enum PredicateType {

        A("Match All (AND)", "AND"),
        O("Match Any (OR)", "OR"),
        N("Match None (NOT)", "NOT"),
        C("Comparison", null);

        private final String label;
        private final String compoundOperator;

        private PredicateType(String label, String compoundOperator) {
            this.label = label;
            this.compoundOperator = compoundOperator;
        }

        public String getLabel() {
            return label;
        }

        public String getCompoundOperator() {
            return compoundOperator;
        }
    }

    private enum ComparisonOperator {

        M("Contains Text") {

            @Override
            public boolean isDisplayedFor(ObjectField field) {
                String t = field.getInternalItemType();

                return ObjectField.REFERENTIAL_TEXT_TYPE.equals(t) ||
                        ObjectField.TEXT_TYPE.equals(t);
            }

            @Override
            public void writeValueInputs(
                    ToolPageContext page,
                    String valueParam,
                    ObjectField field)
                    throws IOException {

                page.writeTag("input",
                        "type", "text",
                        "name", valueParam,
                        "value", page.param(String.class, valueParam));
            }

            @Override
            public Predicate createPredicate(
                    ToolPageContext page,
                    String valueParam,
                    ObjectField field) {

                return createPredicateWithOperatorAndValue(
                        field,
                        "matches",
                        page.param(String.class, valueParam));
            }
        },

        I("Is") {

            @Override
            public boolean isDisplayedFor(ObjectField field) {
                String t = field.getInternalItemType();

                return ObjectField.NUMBER_TYPE.equals(t) ||
                        ObjectField.RECORD_TYPE.equals(t) ||
                        ObjectField.TEXT_TYPE.equals(t);
            }

            @Override
            public void writeValueInputs(
                    ToolPageContext page,
                    String valueParam,
                    ObjectField field)
                    throws IOException {

                if (ObjectField.RECORD_TYPE.equals(field.getInternalItemType())) {
                    page.writeObjectSelect(
                            field,
                            Query.fromAll().where("_id = ?", page.param(UUID.class, valueParam)).first(),
                            "name", valueParam);

                } else {
                    page.writeTag("input",
                            "type", "text",
                            "name", valueParam,
                            "value", page.param(String.class, valueParam));
                }
            }

            @Override
            public Predicate createPredicate(
                    ToolPageContext page,
                    String valueParam,
                    ObjectField field) {

                return createPredicateWithOperatorAndValue(
                        field,
                        "=",
                        page.param(String.class, valueParam));
            }
        },

        N("Is Not") {

            @Override
            public boolean isDisplayedFor(ObjectField field) {
                return I.isDisplayedFor(field);
            }

            @Override
            public void writeValueInputs(
                    ToolPageContext page,
                    String valueParam,
                    ObjectField field)
                    throws IOException {
                I.writeValueInputs(page, valueParam, field);
            }

            @Override
            public Predicate createPredicate(
                    ToolPageContext page,
                    String valueParam,
                    ObjectField field) {

                return createPredicateWithOperatorAndValue(
                        field,
                        "!=",
                        page.param(String.class, valueParam));
            }
        },

        T("Is Set") {

            @Override
            public boolean isDisplayedFor(ObjectField field) {
                return ObjectField.BOOLEAN_TYPE.equals(field.getInternalType());
            }

            @Override
            public void writeValueInputs(
                    ToolPageContext page,
                    String valueParam,
                    ObjectField field)
                    throws IOException {
            }

            @Override
            public Predicate createPredicate(
                    ToolPageContext page,
                    String valueParam,
                    ObjectField field) {

                return createPredicateWithOperatorAndValue(
                        field,
                        "=",
                        Boolean.TRUE);
            }
        },

        F("Is Not Set") {

            @Override
            public boolean isDisplayedFor(ObjectField field) {
                return T.isDisplayedFor(field);
            }

            @Override
            public void writeValueInputs(
                    ToolPageContext page,
                    String valueParam,
                    ObjectField field)
                    throws IOException {
            }

            @Override
            public Predicate createPredicate(
                    ToolPageContext page,
                    String valueParam,
                    ObjectField field) {

                return createPredicateWithOperatorAndValue(
                        field,
                        "!=",
                        Boolean.TRUE);
            }
        },

        S("Is Missing") {

            @Override
            public boolean isDisplayedFor(ObjectField field) {
                return !ObjectField.BOOLEAN_TYPE.equals(field.getInternalType());
            }

            @Override
            public void writeValueInputs(
                    ToolPageContext page,
                    String valueParam,
                    ObjectField field)
                    throws IOException {
            }

            @Override
            public Predicate createPredicate(
                    ToolPageContext page,
                    String valueParam,
                    ObjectField field) {

                return createPredicateWithOperatorAndValue(
                        field,
                        "=",
                        Query.MISSING_VALUE);
            }
        },

        G("Is Not Missing") {

            @Override
            public boolean isDisplayedFor(ObjectField field) {
                return S.isDisplayedFor(field);
            }

            @Override
            public void writeValueInputs(
                    ToolPageContext page,
                    String valueParam,
                    ObjectField field)
                    throws IOException {
            }

            @Override
            public Predicate createPredicate(
                    ToolPageContext page,
                    String valueParam,
                    ObjectField field) {

                return createPredicateWithOperatorAndValue(
                        field,
                        "!=",
                        Query.MISSING_VALUE);
            }
        },

        B("Between") {

            @Override
            public boolean isDisplayedFor(ObjectField field) {
                String t = field.getInternalItemType();

                return ObjectField.DATE_TYPE.equals(t) ||
                        ObjectField.NUMBER_TYPE.equals(t);
            }

            @Override
            public void writeValueInputs(
                    ToolPageContext page,
                    String valueParam,
                    ObjectField field)
                    throws IOException {

                boolean date = ObjectField.DATE_TYPE.equals(field.getInternalItemType());
                String valueToParam = valueParam + "t";

                page.writeTag("input",
                        "type", "text",
                        "class", date ? "date" : null,
                        "name", valueParam,
                        "value", page.param(String.class, valueParam));

                page.writeHtml(" ");
                page.writeTag("input",
                        "type", "text",
                        "class", date ? "date" : null,
                        "name", valueToParam,
                        "value", page.param(String.class, valueToParam));
            }

            @Override
            public Predicate createPredicate(
                    ToolPageContext page,
                    String valueParam,
                    ObjectField field) {

                boolean date = ObjectField.DATE_TYPE.equals(field.getInternalItemType());
                Object from = page.param(date ? Date.class : Double.class, valueParam);
                Object to = page.param(date ? Date.class : Double.class, valueParam + "t");

                return CompoundPredicate.combine(
                        "AND",
                        from == null ? null : createPredicateWithOperatorAndValue(field, ">=", from),
                        to == null ? null : createPredicateWithOperatorAndValue(field, ">=", to));
            }
        };

        private final String label;

        private ComparisonOperator(String label) {
            this.label = label;
        }

        public abstract boolean isDisplayedFor(ObjectField field);

        public abstract void writeValueInputs(
                ToolPageContext page,
                String valueParam,
                ObjectField field)
                throws IOException;

        public abstract Predicate createPredicate(
                ToolPageContext page,
                String valueParam,
                ObjectField field);

        public String getLabel() {
            return label;
        }

        public Predicate createPredicateWithOperatorAndValue(
                ObjectField field,
                String operator,
                Object value) {

            if (value == null) {
                return null;

            } else {
                return new ComparisonPredicate(
                        operator,
                        false,
                        field != null ? field.getUniqueName() : "_any",
                        Arrays.asList(value));
            }
        }
    }
}
