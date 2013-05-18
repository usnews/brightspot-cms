package com.psddev.cms.tool.page;

import java.io.IOException;
import java.util.UUID;

import javax.servlet.ServletException;

import com.psddev.cms.db.Notification;
import com.psddev.cms.tool.PageServlet;
import com.psddev.cms.tool.ToolPageContext;
import com.psddev.dari.db.Database;
import com.psddev.dari.db.ObjectType;
import com.psddev.dari.db.Query;
import com.psddev.dari.util.RoutingFilter;

@RoutingFilter.Path(application = "cms", value = "/adminNotifications")
@SuppressWarnings("serial")
public class AdminNotifications extends PageServlet {

    @Override
    protected String getPermissionId() {
        return "area/admin/notifications";
    }

    @Override
    protected void doService(ToolPageContext page) throws IOException, ServletException {
        UUID notificationId = page.param(UUID.class, "id");
        Notification notification = Query.
                from(Notification.class).
                where("_id = ?", notificationId).
                first();

        ObjectType type;

        if (notification == null) {
            type = ObjectType.getInstance(page.param(UUID.class, "typeId"));

            if (type != null) {
                notification = (Notification) type.createObject(notificationId);
            }

        } else {
            type = notification.getState().getType();
        }

        if (notification != null &&
                page.tryStandardUpdate(notification)) {
            return;
        }

        page.writeHeader();
            page.writeStart("div", "class", "withLeftNav");
                page.writeStart("div", "class", "leftNav");
                    page.writeStart("div", "class", "widget");
                        page.writeStart("h1", "class", "icon icon-object-notification");
                            page.writeHtml("Notifications");
                        page.writeEnd();

                        page.writeStart("form",
                                "method", "get",
                                "action", page.url(null));
                            page.writeTypeSelect(
                                    Database.Static.getDefault().getEnvironment().getTypeByClass(Notification.class).findConcreteTypes(),
                                    type,
                                    null,
                                    "data-searchable", true,
                                    "name", "typeId",
                                    "style", "margin-bottom: 5px;");

                            page.writeStart("button", "class", "icon icon-action-create");
                                page.writeHtml("New");
                            page.writeEnd();
                        page.writeEnd();

                        page.writeStart("ul", "class", "links");
                            for (Notification n : Query.
                                    from(Notification.class).
                                    selectAll()) {
                                page.writeStart("li", "class", n.equals(notification) ? "selected" : null);
                                    page.writeStart("a", "href", page.objectUrl(null, n));
                                        page.writeTypeObjectLabel(n);
                                    page.writeEnd();
                                page.writeEnd();
                            }
                        page.writeEnd();
                    page.writeEnd();
                page.writeEnd();

                if (notification != null) {
                    page.writeStart("div", "class", "main");
                        page.writeStart("div", "class", "widget");
                            page.writeStandardForm(notification);
                        page.writeEnd();
                    page.writeEnd();
                }
            page.writeEnd();
        page.writeFooter();
    }
}
