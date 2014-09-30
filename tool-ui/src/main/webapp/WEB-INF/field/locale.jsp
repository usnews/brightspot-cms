<%@ page session="false" import="

com.psddev.cms.db.ToolUi,
com.psddev.cms.tool.ToolPageContext,

com.psddev.dari.db.ObjectField,
com.psddev.dari.db.State,
com.psddev.dari.util.ObjectUtils,
com.psddev.dari.util.StringUtils,

java.util.Arrays,
java.util.Collections,
java.util.List,
java.util.Locale,
java.util.Comparator
" %><%

ToolPageContext wp = new ToolPageContext(pageContext);

State state = State.getInstance(request.getAttribute("object"));

ObjectField field = (ObjectField) request.getAttribute("field");
ToolUi ui = field.as(ToolUi.class);
String fieldName = field.getInternalName();

String inputName = (String) request.getAttribute("inputName");
Locale fieldValue = (Locale) state.get(fieldName);

String placeholder = ui.getPlaceholder();
if (field.isRequired()) {
    if (ObjectUtils.isBlank(placeholder)) {
        placeholder = "(Required)";
    } else {
        placeholder += " (Required)";
    }
}

if (Boolean.TRUE.equals(request.getAttribute("isFormPost"))) {
    fieldValue = wp.param(Locale.class, inputName);
    state.put(fieldName, fieldValue);
    return;
}

wp.writeStart("div", "class", "inputSmall");

    wp.writeStart("select",
            "id", wp.getId(),
            "name", inputName,
            "data-searchable", true,
            "placeholder", placeholder);

        wp.writeStart("option", "value", "");
        wp.writeEnd();

        List<Locale> availableLocales = Arrays.<Locale>asList(Locale.getAvailableLocales());

        Collections.<Locale>sort(availableLocales, new Comparator<Locale>() {
            @Override
            public int compare(Locale o1, Locale o2) {
                String dn1 = o1.getDisplayName(o1);
                String dn2 = o2.getDisplayName(o2);

                if (dn1.length() > 0) {
                    dn1 = dn1.substring(0, 1).toUpperCase(o1) + dn1.substring(1);
                }

                if (dn2.length() > 0) {
                    dn2 = dn2.substring(0, 1).toUpperCase(o2) + dn2.substring(1);
                }

                return ObjectUtils.compare(dn1, dn2, true);
            }
        });

        for (Locale availableLocale : availableLocales) {

            String displayName = availableLocale.getDisplayName(availableLocale);

            if (!StringUtils.isBlank(displayName)) {
                displayName = displayName.substring(0, 1).toUpperCase(availableLocale) + displayName.substring(1);

                wp.writeStart("option",
                        "selected", availableLocale.equals(fieldValue) ? "selected" : null,
                        "value", availableLocale.toLanguageTag());
                    wp.writeHtml(displayName);
                wp.writeEnd();
            }
        }
    wp.writeEnd();

wp.writeEnd();

%>
