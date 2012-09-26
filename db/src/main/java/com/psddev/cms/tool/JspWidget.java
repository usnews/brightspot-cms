package com.psddev.cms.tool;

import com.psddev.dari.util.JspUtils;
import com.psddev.dari.util.ObjectUtils;

import java.io.StringWriter;

/** Widget controlled by a JSP file. */
public class JspWidget extends Widget {

    public static final String REMOTE_DISPLAY_API = "/_jspWidget.display";
    public static final String REMOTE_UPDATE_API = "/_jspWidget.update";

    private static final String ATTRIBUTE_PREFIX = JspWidget.class.getName() + ".";
    private static final String IS_UPDATING_ATTRIBUTE = ATTRIBUTE_PREFIX + "isUpdating";
    private static final String OBJECT_ATTRIBUTE = ATTRIBUTE_PREFIX + "object";
    private static final String WIDGET_ATTRIBUTE = ATTRIBUTE_PREFIX + "widget";

    private String jsp;

    /**
     * Returns {@code true} if a widget is processing within the given
     * {@code page}.
     */
    public static boolean isUpdating(ToolPageContext page) {
        return Boolean.TRUE.equals(page.getRequest().getAttribute(IS_UPDATING_ATTRIBUTE));
    }

    /** Returns the object associated with the given {@code page}. */
    public static Object getObject(ToolPageContext page) {
        return page.getRequest().getAttribute(OBJECT_ATTRIBUTE);
    }

    /** Returns the widget associated with the given {@code page}. */
    public static JspWidget getWidget(ToolPageContext page) {
        return (JspWidget) page.getRequest().getAttribute(WIDGET_ATTRIBUTE);
    }

    /** Returns the JSP. */
    public String getJsp() {
        return jsp;
    }

    /** Sets the JSP. */
    public void setJsp(String jsp) {
        this.jsp = jsp;
    }

    // --- Widget support ---

    /** Calls the JSP to display or update with this widget. */
    private String callJsp(boolean isUpdating, ToolPageContext page, Object object) throws Exception {
        StringWriter writer = new StringWriter();
        JspUtils.includeEmbedded(
                page.getServletContext(), page.getRequest(), page.getResponse(), writer, getJsp(),
                IS_UPDATING_ATTRIBUTE, isUpdating,
                OBJECT_ATTRIBUTE, object,
                WIDGET_ATTRIBUTE, this);
        return writer.toString();
    }

    @Override
    public String display(ToolPageContext page, Object object) throws Exception {
        if (ObjectUtils.equals(getTool(), page.getTool())) {
            return callJsp(false, page, object);
        } else {
            return RemoteWidget.displayWidget(this, REMOTE_DISPLAY_API, page, object);
        }
    }

    @Override
    public void update(ToolPageContext page, Object object) throws Exception {
        if (ObjectUtils.equals(getTool(), page.getTool())) {
            callJsp(true, page, object);
        } else {
            RemoteWidget.updateWithWidget(this, REMOTE_UPDATE_API, page, object);
        }
    }
}
