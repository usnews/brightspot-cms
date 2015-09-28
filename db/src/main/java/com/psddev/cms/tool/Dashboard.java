package com.psddev.cms.tool;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

import com.psddev.dari.db.Record;
import com.psddev.dari.util.ClassFinder;
import com.psddev.dari.util.TypeDefinition;

@Dashboard.Embedded
public class Dashboard extends Record {

    private List<DashboardColumn> columns;

    /**
     * Creates a default dashboard containing instances of all classes that
     * implement {@link DefaultDashboardWidget}.
     *
     * @return Never {@code null}.
     */
    public static Dashboard createDefaultDashboard() {
        Dashboard dashboard = new Dashboard();
        List<DashboardColumn> columns = dashboard.getColumns();

        ClassFinder.findConcreteClasses(DefaultDashboardWidget.class).forEach(c -> {
            DefaultDashboardWidget widget = TypeDefinition.getInstance(c).newInstance();
            int columnIndex = widget.getColumnIndex();

            IntStream.range(0, columnIndex - columns.size() + 1)
                    .forEach(i -> columns.add(new DashboardColumn()));

            columns.get(columnIndex)
                    .getWidgets()
                    .add(widget);
        });

        double width = 1.0;

        for (DashboardColumn column : columns) {
            width /= 1.61803398875;

            column.setWidth(width);

            Collections.sort(
                    column.getWidgets(),
                    Comparator.comparingInt(w -> ((DefaultDashboardWidget) w).getWidgetIndex())
                            .thenComparing(w -> w.getClass().getName()));
        }

        return dashboard;
    }

    /**
     * @deprecated Use {@link #createDefaultDashboard()} instead.
     */
    @Deprecated
    public static Dashboard getDefaultDashboard() {
        return createDefaultDashboard();
    }

    public List<DashboardColumn> getColumns() {
        if (columns == null) {
            columns = new ArrayList<>();
        }
        return columns;
    }

    public void setColumns(List<DashboardColumn> columns) {
        this.columns = columns;
    }
}
