package com.psddev.cms.tool.page;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.servlet.ServletException;

import com.psddev.cms.db.ToolUser;
import com.psddev.cms.db.ToolUserAction;
import com.psddev.cms.db.ToolUserDevice;
import com.psddev.cms.tool.PageServlet;
import com.psddev.cms.tool.ToolPageContext;
import com.psddev.dari.db.Query;
import com.psddev.dari.db.State;
import com.psddev.dari.util.JspUtils;
import com.psddev.dari.util.ObjectUtils;
import com.psddev.dari.util.RoutingFilter;

@RoutingFilter.Path(application = "cms", value = "lookingGlass")
@SuppressWarnings("serial")
public class LookingGlass extends PageServlet {

    @Override
    protected String getPermissionId() {
        return null;
    }

    @Override
    protected void doService(ToolPageContext page) throws IOException, ServletException {
        UUID id = page.param(UUID.class, "id");

        if (id == null) {
            ToolUser user = page.getUser();
            ToolUserDevice device = user.findRecentDevice();

            if (device == null) {
                device = user.findCurrentDevice(page.getRequest());
            }

            id = device.getLookingGlassId();

            if (id == null) {
                id = UUID.randomUUID();

                device.setLookingGlassId(id);
                user.save();
            }

            page.redirect("", "id", id);
            return;
        }

        ToolUser user = Query.
                from(ToolUser.class).
                where("devices/lookingGlassId = ?", id).
                first();

        if (user == null) {
            throw new IllegalArgumentException(String.format(
                    "No looking glass at [%s]!", id));
        }

        Date lastUpdate = Query.
                from(ToolUser.class).
                where("_id = ?", user.getId()).
                noCache().
                lastUpdate();

        ToolUserDevice device = null;
        
        for (ToolUserDevice d : user.getDevices()) {
            if (id.equals(d.getLookingGlassId())) {
                device = d;
                break;
            }
        }

        ToolUserAction lastAction = null;

        if (device != null) {
            lastAction = device.findLastAction();

            if (lastAction != null &&
                    "ping".equals(page.param(String.class, "action"))) {
                long end = System.currentTimeMillis() + 30000;

                while (System.currentTimeMillis() < end &&
                        lastAction.getTime() == page.param(long.class, "time")) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException error) {
                        break;
                    }

                    Date newLastUpdate = Query.
                            from(ToolUser.class).
                            where("_id = ?", user.getId()).
                            noCache().
                            lastUpdate();

                    if (lastUpdate.before(newLastUpdate)) {
                        lastUpdate = newLastUpdate;
                        user = Query.from(ToolUser.class).where("_id = ?", user.getId()).noCache().first();
                        device = user.findRecentDevice();
                        lastAction = device.findLastAction();

                        if (lastAction == null) {
                            break;
                        }
                    }
                }

                Map<String, Object> response = new HashMap<String, Object>();

                response.put("changed", lastAction == null || lastAction.getTime() != page.param(long.class, "time"));
                page.getResponse().setContentType("application/json");
                page.writeRaw(ObjectUtils.toJson(response));
                return;
            }
        }

        page.writeHeader();
            page.writeStart("div", "class", "message message-info");
                page.writeHtml("Mirroring ");
                page.writeObjectLabel(user);

                if (device != null) {
                    page.writeHtml(" in ");
                    page.writeHtml(device.getUserAgentDisplay());

                    if (lastAction != null) {
                        Object lastActionContent = lastAction.getContent();

                        if (lastActionContent != null) {
                            page.writeHtml(" - ");
                            page.writeStart("a",
                                    "target", "_blank",
                                    "href", page.objectUrl("/content/edit.jsp", lastActionContent));
                                page.writeTypeObjectLabel(lastActionContent);
                            page.writeEnd();
                        }
                    }
                }
            page.writeEnd();

            State preview = State.getInstance(Query.
                    from(Object.class).
                    where("_id = ?", user.getCurrentPreviewId()).
                    first());

            if (preview != null) {
                page.writeStart("div", "style", page.cssString("margin", "0 -20px"));
                    page.writeStart("iframe",
                            "src", JspUtils.getAbsolutePath(page.getRequest(), "/_preview", "_cms.db.previewId", preview.getId()),
                            "style", page.cssString(
                                    "border-style", "none",
                                    "height", "10000px",
                                    "width", "100%"));
                    page.writeEnd();
                page.writeEnd();
            }

            page.writeStart("script", "type", "text/javascript");
                page.writeRaw("(function($, win) {");
                    page.writeRaw("var ping = function() {");
                        page.writeRaw("$.ajax({");
                            page.writeRaw("'dataType': 'json',");
                            page.writeRaw("'cache': false,");

                            page.writeRaw("'url': '");
                            page.writeRaw(page.cmsUrl("/lookingGlass",
                                    "id", id,
                                    "action", "ping",
                                    "time", lastAction != null ? lastAction.getTime() : null));
                            page.writeRaw("',");

                            page.writeRaw("'success': function(response) {");
                                page.writeRaw("if (response.changed) {");
                                    page.writeRaw("win.location = win.location;");
                                page.writeRaw("} else {");
                                    page.writeRaw("ping();");
                                page.writeRaw("}");
                            page.writeRaw("},");

                            page.writeRaw("'error': function() {");
                                page.writeRaw("setTimeout(ping, 5000);");
                            page.writeRaw("}");
                        page.writeRaw("});");
                    page.writeRaw("};");

                    page.writeRaw("$(win).bind('load', function() {");
                        page.writeRaw("ping();");
                    page.writeRaw("});");
                page.writeRaw("})(jQuery, window);");
            page.writeEnd();
        page.writeFooter();
    }
}
