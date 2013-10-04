package com.psddev.cms.tool.page;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.ServletException;

import com.psddev.cms.db.ToolUser;
import com.psddev.cms.db.ToolUserAction;
import com.psddev.cms.db.ToolUserDevice;
import com.psddev.cms.tool.PageServlet;
import com.psddev.cms.tool.ToolPageContext;
import com.psddev.dari.db.Query;
import com.psddev.dari.util.JspUtils;
import com.psddev.dari.util.RoutingFilter;

@RoutingFilter.Path(application = "cms", value = "toolUserHistory")
@SuppressWarnings("serial")
public class ToolUserHistory extends PageServlet {

    @Override
    protected String getPermissionId() {
        return null;
    }

    @Override
    protected void doService(ToolPageContext page) throws IOException, ServletException {
        ToolUser user = page.getUser();
        Map<String, List<ToolUserDevice>> devicesByUserAgent = new TreeMap<String, List<ToolUserDevice>>();

        for (ToolUserDevice device : Query.
                from(ToolUserDevice.class).
                where("user = ?", user).
                selectAll()) {
            String userAgent = device.getUserAgentDisplay();
            List<ToolUserDevice> devices = devicesByUserAgent.get(userAgent);

            if (devices == null) {
                devices = new ArrayList<ToolUserDevice>();
                devicesByUserAgent.put(userAgent, devices);
            }

            devices.add(device);
        }

        page.writeHeader();
            page.writeStart("div", "class", "widget", "style", "overflow: hidden;");
                page.writeStart("h1", "class", "icon icon-object-history");
                    page.writeHtml("History");
                page.writeEnd();

                page.writeStart("ul");
                    for (Map.Entry<String, List<ToolUserDevice>> entry : devicesByUserAgent.entrySet()) {
                        ToolUserDevice device = null;
                        List<ToolUserAction> actions = null;
                        long lastTime = 0;

                        for (ToolUserDevice d : entry.getValue()) {
                            List<ToolUserAction> a = Query.
                                    from(ToolUserAction.class).
                                    where("device = ?", d).
                                    sortDescending("time").
                                    selectAll();

                            if (!a.isEmpty()) {
                                long time = a.get(0).getTime();

                                if (lastTime < time) {
                                    lastTime = time;
                                    device = d;
                                    actions = a;
                                }
                            }
                        }

                        if (device == null) {
                            continue;
                        }

                        String lookingGlassUrl = page.cmsUrl("/lookingGlass", "id", device.getOrCreateLookingGlassId());

                        page.writeStart("li", "style", "clear: right;");
                            page.writeHtml(entry.getKey());

                            page.writeStart("div", "style", page.cssString(
                                    "float", "right",
                                    "text-align", "center"));
                                page.writeStart("a",
                                        "class", "icon icon-facetime-video",
                                        "target", "_blank",
                                        "href", lookingGlassUrl);
                                    page.writeHtml("Looking Glass");
                                page.writeEnd();

                                page.writeTag("br");

                                page.writeTag("img",
                                        "width", 150,
                                        "height", 150,
                                        "src", page.cmsUrl("qrCode",
                                                "data", JspUtils.getAbsoluteUrl(page.getRequest(), lookingGlassUrl),
                                                "size", 150));
                            page.writeEnd();

                            page.writeStart("ul",
                                    "class", "links",
                                    "style", page.cssString("margin-right", "150px"));
                                for (ToolUserAction action : actions) {
                                    Object actionContent = action.getContent();

                                    if (actionContent == null) {
                                        continue;
                                    }

                                    page.writeStart("li");
                                        page.writeStart("a",
                                                "target", "_top",
                                                "href", page.objectUrl("/content/edit.jsp", actionContent));
                                            page.writeTypeObjectLabel(actionContent);
                                        page.writeEnd();
                                    page.writeEnd();
                                }
                            page.writeEnd();
                        page.writeEnd();
                    }
                page.writeEnd();
            page.writeEnd();
        page.writeFooter();
    }
}
