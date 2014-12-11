package com.psddev.cms.tool;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.psddev.cms.db.ToolUi;
import com.psddev.dari.db.Modification;
import com.psddev.dari.db.ObjectIndex;
import com.psddev.dari.db.ObjectType;
import com.psddev.dari.db.Query;
import com.psddev.dari.db.Recordable;
import com.psddev.dari.util.ObjectUtils;
import com.psddev.dari.util.StringUtils;

public interface Dashboard extends Recordable {

    public List<? extends DashboardColumn<? extends DashboardWidget>> getColumns();

    public default List<? extends DashboardWidget> getWidgets() {
        List<DashboardWidget> widgets = new ArrayList<DashboardWidget>();
        List<?> columns = getColumns();
        if (columns != null) {
            for (Object columnObj : columns) {
                if (columnObj instanceof DashboardColumn) {
                    List<?> columnWidgets = ((DashboardColumn<?>) columnObj).getWidgets();
                    if (columnWidgets != null) {
                        for (Object widgetObj : columnWidgets) {
                            if (widgetObj instanceof DashboardWidget) {
                                widgets.add((DashboardWidget) widgetObj);
                            }
                        }
                    }
                }
            }
        }
        return widgets;
    }

    public default String getHierarchicalParent() {
        return "dashboard";
    }

    public default void writeHeaderHtml(ToolPageContext page) throws IOException {
        page.writeHeader();
    }

    public default void writeFooterHtml(ToolPageContext page) throws IOException {
        page.writeFooter();
    }

    @FieldInternalNamePrefix("cms.dashboard.")
    public static final class Data extends Modification<Dashboard> {

        public static final String INTERNAL_NAME_PREFIX = "cms.dashboard.";
        public static final String WIDGET_POSITION_PREFIX = INTERNAL_NAME_PREFIX + "widgets.";

        @ToolUi.Hidden
        @Indexed(unique = true)
        private String name;

        @Override
        public void beforeSave() {
            if (ObjectUtils.isBlank(getName()) && !ObjectUtils.isBlank(getOriginalObject().getState().getLabel())) {
                setName(StringUtils.toCamelCase(getLabel()).replaceAll("[^A-Za-z0-9]", ""));
            }

            List<? extends DashboardWidget> widgets = getOriginalObject().getWidgets();

            if (widgets != null) {
                for (Object widgetObj : widgets) {
                    if (widgetObj instanceof DashboardWidget && ObjectUtils.isBlank(((DashboardWidget) widgetObj).as(DashboardWidget.Data.class).getName()) && !ObjectUtils.isBlank(((DashboardWidget) widgetObj).getState().getLabel())) {
                        DashboardWidget widget = (DashboardWidget) widgetObj;
                        String widgetLabel = widget.getState().getLabel().replaceAll("[^A-Za-z0-9]", "");
                        String originalName = StringUtils.toCamelCase(!StringUtils.isBlank(widgetLabel) ? widgetLabel : widget.getState().getId().toString());
                        widget.as(DashboardWidget.Data.class).setName(originalName);
                        int i = 0;
                        for (Object otherWidgetObj : widgets) {
                            if (widget.equals(otherWidgetObj) || !(otherWidgetObj instanceof DashboardWidget)) {
                                continue;
                            }
                            DashboardWidget otherWidget = (DashboardWidget) otherWidgetObj;

                            if (widget.as(DashboardWidget.Data.class).getName().equals(otherWidget.as(DashboardWidget.Data.class).getName())) {
                                widget.as(DashboardWidget.Data.class).setName(originalName + (++i));
                                ObjectType widgetType = ObjectType.getInstance(widget.getClass());
                                if (widgetType.isConcrete() && !widgetType.isEmbedded()) {
                                    widget.getState().save();
                                }
                            }
                        }
                    }
                }
            }
        }

        @Override
        public boolean onDuplicate(ObjectIndex index) {
            boolean corrected = false;
            String originalName = getName();
            int i = 0;

            while (Query.from(Dashboard.class).master().noCache().where("_id != ?", getId()).and("cms.dashboard.name = ?", getName()).hasMoreThan(0)) {
                setName(originalName + "-" + (++i));
            }

            return corrected;
        }

        public String getName() {
            return !ObjectUtils.isBlank(name) ? name : getId().toString();
        }

        public void setName(String name) {
            this.name = name;
        }

        public DashboardWidget getWidgetByName(String widgetName) {
            if (widgetName != null) {
                for (Object widgetObj : getOriginalObject().getWidgets()) {
                    if (widgetObj instanceof DashboardWidget && widgetName.equals(((DashboardWidget) widgetObj).getState().as(DashboardWidget.Data.class).getName())) {
                        return (DashboardWidget) widgetObj;
                    }
                }
            }
            return null;
        }

        public String getWidgetPosition() {
            return WIDGET_POSITION_PREFIX + getName();
        }

        public String getPermissionId() {
            return "area/" + getOriginalObject().getHierarchicalParent() + "/" + getInternalName();
        }

        public String getInternalName() {
            return INTERNAL_NAME_PREFIX + getName();
        }
    }
}
