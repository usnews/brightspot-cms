package com.psddev.cms.tool.page;

import com.psddev.cms.tool.PageServlet;
import com.psddev.cms.tool.ToolPageContext;
import com.psddev.cms.tool.widget.SearchCarouselWidget;
import com.psddev.dari.util.RoutingFilter;

import javax.servlet.ServletException;
import java.io.IOException;

@RoutingFilter.Path(application = "cms", value = "/searchCarousel")
public class SearchCarousel extends PageServlet {

    @Override
    protected String getPermissionId() {
        return null;
    }

    @Override
    protected void doService(ToolPageContext page) throws IOException, ServletException {
        new SearchCarouselWidget().writeHtml(page, null);
    }
}
