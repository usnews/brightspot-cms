package com.psddev.cms.tool.page;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.servlet.ServletException;

import com.psddev.cms.db.GuideType;
import com.psddev.cms.tool.PageServlet;
import com.psddev.cms.tool.ToolPageContext;
import com.psddev.dari.db.ObjectField;
import com.psddev.dari.db.ObjectType;
import com.psddev.dari.db.ReferentialText;
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

        page.writeHeader();
            page.writeStart("h1", "class", "icon icon-object-guide");
                page.writeHtml(field.getLabel());
            page.writeEnd();

            // Constraints.
            List<String> constraints = new ArrayList<String>();

            if (field.isRequired()) {
                constraints.add("Required");
            }

            Object min = GuideType.Static.getFieldMinimumValue(field);

            if (min != null) {
                constraints.add("Minimum: " + min);
            }

            Object max = GuideType.Static.getFieldMaximumValue(field);

            if (max != null) {
                constraints.add("Maximum: " + max);
            }

            if (!constraints.isEmpty()) {
                page.writeStart("h2");
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

            // Editorial field description.
            GuideType guideType = GuideType.Static.getGuideType(type);
            ReferentialText fieldDescription = null;

            if (guideType != null) {
                fieldDescription = guideType.getFieldDescription(fieldName, null, false);
            }

            if (fieldDescription != null && !fieldDescription.isEmpty()) {
                page.writeStart("h2");
                    page.writeHtml("Description");
                page.writeEnd();

                StringBuilder cleaned = new StringBuilder();

                for (Object item : fieldDescription) {
                    if (item instanceof String) {
                        cleaned.append(item);
                    }
                }

                page.writeRaw(cleaned.toString().replaceAll("(?i)(\\s*<br[^>]*>\\s*)+$", ""));
            }
        page.writeFooter();
    }
}
