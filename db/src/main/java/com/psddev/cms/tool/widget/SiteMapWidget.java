package com.psddev.cms.tool.widget;

import com.psddev.cms.db.Content;
import com.psddev.cms.tool.Dashboard;
import com.psddev.cms.tool.ToolPageContext;
import com.psddev.cms.tool.page.SiteMap;

import javax.servlet.ServletException;
import java.io.IOException;

public class SiteMapWidget extends Content implements CmsWidget {

    @Override
    public void writeHtml(ToolPageContext page, Dashboard dashboard) throws IOException, ServletException {
        SiteMap.reallyDoService(page);
    }
}
