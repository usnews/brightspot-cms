package com.psddev.cms.tool.widget;

import com.psddev.cms.tool.Dashboard;
import com.psddev.cms.tool.DashboardWidget;
import com.psddev.cms.tool.ToolPageContext;
import com.psddev.cms.tool.search.CarouselSearchResultView;
import com.psddev.dari.util.StringUtils;

import javax.servlet.ServletException;
import java.io.IOException;

public class CarouselSearchWidget extends DashboardWidget {

    @Override
    public void writeHtml(ToolPageContext page, Dashboard dashboard) throws IOException, ServletException {
        String searchUrl = page.param(String.class, "search");

        if (searchUrl == null) {
            return;
        }

        page.writeStart("div", "class", "widget-carousel");
            page.writeStart("div", "class", "frame");

                page.writeStart("a", "href", StringUtils.addQueryParameters(searchUrl, "view", CarouselSearchResultView.class.getName(), "id", page.param(String.class, "id")));
                page.writeEnd();

            page.writeEnd();
        page.writeEnd();
    }
}
