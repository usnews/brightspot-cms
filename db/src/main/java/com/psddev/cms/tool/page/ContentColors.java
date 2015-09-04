package com.psddev.cms.tool.page;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import javax.servlet.ServletException;

import com.psddev.cms.tool.PageServlet;
import com.psddev.cms.tool.ToolPageContext;
import com.psddev.dari.db.ColorDistribution;
import com.psddev.dari.db.Query;
import com.psddev.dari.db.State;
import com.psddev.dari.util.HuslColorSpace;
import com.psddev.dari.util.ObjectUtils;
import com.psddev.dari.util.RoutingFilter;

@RoutingFilter.Path(application = "cms", value = "contentColors")
@SuppressWarnings("serial")
public class ContentColors extends PageServlet {

    @Override
    protected String getPermissionId() {
        return null;
    }

    @Override
    protected void doService(final ToolPageContext page) throws IOException, ServletException {
        State state = State.getInstance(Query.from(Object.class).where("_id = ?", page.param(UUID.class, "id")).first());
        ColorDistribution distribution = state.as(ColorDistribution.Data.class).getDistribution();

        if (distribution == null) {
            return;
        }

        page.writeHeader();
            page.writeStart("div", "class", "widget");
                page.writeStart("h1", "class", "icon icon-tint");
                    page.writeHtml(page.localize(null, "contentColors.title"));
                page.writeEnd();

                page.writeStart("h2");
                    page.writeHtml(page.localize(null, "contentColors.dominant"));
                page.writeEnd();

                for (Map.Entry<String, Object> entry : distribution.getState().entrySet()) {
                    String fieldName = entry.getKey();
                    Object percentage = entry.getValue();

                    if (fieldName.startsWith("o") && percentage != null) {
                        int percentageInt = (int) (((Double) percentage) * 100);
                        String[] parts = fieldName.split("_");
                        int lightness = ObjectUtils.to(int.class, parts[3]);
                        int[] rgbNumbers = HuslColorSpace.Static.fromHUSLtoRGB(
                                ObjectUtils.to(int.class, parts[1]),
                                ObjectUtils.to(int.class, parts[2]),
                                lightness);
                        String rgb = String.format(
                                "#%02X%02X%02X",
                                rgbNumbers[0],
                                rgbNumbers[1],
                                rgbNumbers[2]);

                        page.writeStart("div", "style", page.cssString(
                                "background-color", rgb,
                                "color", lightness < 50 ? "white" : null,
                                "height", (percentageInt * 3) + "px",
                                "line-height", (percentageInt * 3) + "px",
                                "text-align", "center"));
                            page.writeHtml(rgb);
                            page.writeHtml(" (");
                            page.writeHtml(percentageInt);
                            page.writeHtml("%)");
                        page.writeEnd();
                    }
                }
            page.writeEnd();
        page.writeFooter();
    }
}
