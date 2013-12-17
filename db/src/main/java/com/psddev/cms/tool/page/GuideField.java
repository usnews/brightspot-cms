package com.psddev.cms.tool.page;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.servlet.ServletException;

import com.psddev.cms.db.GuideType;
import com.psddev.cms.db.ToolUi;
import com.psddev.cms.tool.PageServlet;
import com.psddev.cms.tool.ToolPageContext;
import com.psddev.dari.db.ObjectField;
import com.psddev.dari.db.ObjectType;
import com.psddev.dari.db.ReferentialText;
import com.psddev.dari.util.ObjectUtils;
import com.psddev.dari.util.RoutingFilter;

@RoutingFilter.Path(application = "cms", value = "guideField")
public class GuideField extends PageServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected String getPermissionId() {
        return null;
    }

    @Override
    protected void doService(ToolPageContext page) throws IOException, ServletException {
        ObjectType type = ObjectType.getInstance(page.param(UUID.class, "typeId"));

        if (type == null) {
            throw new IllegalArgumentException();
        }

        String fieldName = page.param(String.class, "field");
        ObjectField field = type.getField(fieldName);

        if (field == null) {
            field = type.getEnvironment().getField(fieldName);
        }

        page.writeHeader();
            page.writeStart("div", "class", "widget");
                page.writeStart("h1", "class", "icon icon-object-guide");
                    page.writeHtml(field.getLabel());
                page.writeEnd();

                page.writeStart("div", "class", "tabbed");

                    // Editorial field description.
                    page.writeStart("div", "data-tab", "For Editors");
                        GuideType guideType = GuideType.Static.getGuideType(type);
                        ReferentialText fieldDescription = null;

                        if (guideType != null) {
                            fieldDescription = guideType.getFieldDescription(fieldName, null, false);
                        }

                        if (fieldDescription != null && !fieldDescription.isEmpty()) {
                            StringBuilder cleaned = new StringBuilder();

                            for (Object item : fieldDescription) {
                                if (item instanceof String) {
                                    cleaned.append(item);
                                }
                            }

                            page.writeRaw(cleaned.toString().replaceAll("(?i)(\\s*<br[^>]*>\\s*)+$", ""));
                        }

                        // Constraints.
                        List<String> constraints = new ArrayList<String>();

                        if (field.isRequired()) {
                            constraints.add("Required");
                        }

                        Object absMin = GuideType.Static.getFieldMinimumValue(field);
                        Object absMax = GuideType.Static.getFieldMaximumValue(field);

                        if (absMin != null) {
                            constraints.add("Absolute Minimum: " + absMin);
                        }

                        if (absMax != null) {
                            constraints.add("Absolute Maximum: " + absMax);
                        }

                        ToolUi fieldUi = field.as(ToolUi.class);
                        Object sugMin = fieldUi.getSuggestedMinimum();
                        Object sugMax = fieldUi.getSuggestedMaximum();

                        if (sugMin != null) {
                            constraints.add("Suggested Minimum: " + sugMin);
                        }

                        if (sugMax != null) {
                            constraints.add("Suggested Maximum: " + sugMax);
                        }

                        if (!constraints.isEmpty()) {
                            page.writeStart("h2", "style", "margin-top:15px;");
                                page.writeHtml("Constraints");
                            page.writeEnd();

                            page.writeStart("ul");
                                for (String constraint : constraints) {
                                    page.writeStart("li");
                                        page.writeHtml(constraint);
                                    page.writeEnd();
                                }
                            page.writeEnd();
                        }

                        if ((fieldDescription == null ||
                                fieldDescription.isEmpty()) &&
                                constraints.isEmpty()) {
                            page.writeStart("div", "class", "message message-info");
                                page.writeHtml("No editorial production guide for this field.");
                            page.writeEnd();
                        }
                    page.writeEnd();

                    // Development help.
                    page.writeStart("div", "data-tab", "For Developers");
                        Class<?> fieldDeclaringClass = ObjectUtils.getClassByName(field.getJavaDeclaringClassName());

                        if (fieldDeclaringClass != null) {
                            Field javaField = field.getJavaField(fieldDeclaringClass);

                            page.writeStart("ul");
                                page.writeStart("li");
                                    page.writeHtml("Declared In: ");
                                    page.writeJavaClassLink(fieldDeclaringClass);
                                page.writeEnd();

                                page.writeStart("li");
                                    page.writeHtml("Field Type: ");

                                    page.writeStart("code");
                                        page.writeHtml(javaField.getGenericType());
                                    page.writeEnd();
                                page.writeEnd();

                                page.writeStart("li");
                                    page.writeHtml("Field Name: ");

                                    page.writeStart("code");
                                        page.writeHtml(field.getJavaFieldName());
                                    page.writeEnd();
                                page.writeEnd();
                            page.writeEnd();

                            ContentTools.Static.writeJavaAnnotationDescriptions(page, javaField);
                        }
                    page.writeEnd();
                page.writeEnd();
            page.writeEnd();
        page.writeFooter();
    }
}
