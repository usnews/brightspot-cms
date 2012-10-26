package com.psddev.cms.tool;

import com.psddev.dari.db.Application;
import com.psddev.dari.db.Database;
import com.psddev.dari.db.Query;
import com.psddev.dari.db.State;
import com.psddev.dari.util.ObjectUtils;
import com.psddev.dari.util.PeriodicValue;
import com.psddev.dari.util.PullThroughCache;
import com.psddev.dari.util.TypeDefinition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Application.Abstract
public class Tool extends Application {

    private static final Logger LOGGER = LoggerFactory.getLogger(Tool.class);

    private static List<Plugin> getAllPlugins(Database database) {
        return Query.from(Plugin.class).sortAscending("displayName").using(database).selectAll();
    }

    /**
     * Returns a list of all the plugins with the given {@code pluginClass}.
     */
    public <T extends Plugin> List<T> findPlugins(Class<T> pluginClass) {
        List<T> plugins = new ArrayList<T>();
        for (Plugin plugin : getAllPlugins(getState().getDatabase())) {
            if (pluginClass.isInstance(plugin) && plugin.getTool() != null) {
                plugins.add((T) plugin);
            }
        }
        return plugins;
    }

    /** Returns a list of all the top-level areas. */
    public List<Area> findTopAreas() {
        List<Area> tops = new ArrayList<Area>();
        Area first = null;
        Area last = null;
        for (Area area : findPlugins(Area.class)) {
            if (area.getParent() == null) {
                if (area.getTool() instanceof CmsTool) {
                    String internalName = area.getInternalName();
                    if ("dashboard".equals(internalName)) {
                        first = area;
                    } else if ("admin".equals(internalName)) {
                        last = area;
                    } else {
                        tops.add(area);
                    }
                } else {
                    tops.add(area);
                }
            }
        }
        if (first != null) {
            tops.add(0, first);
        }
        if (last != null) {
            tops.add(last);
        }
        return tops;
    }

    /**
     * Returns a table of all the widgets with the given
     * {@code positionName}.
     */
    public List<List<Widget>> findWidgets(String positionName) {

        Map<Double, Map<Double, Widget>> widgetsMap = new TreeMap<Double, Map<Double, Widget>>();
        for (Widget widget : findPlugins(Widget.class)) {
            for (Widget.Position position : widget.getPositions()) {
                if (ObjectUtils.equals(position.getName(), positionName)) {
                    double column = position.getColumn();
                    Map<Double, Widget> widgets = widgetsMap.get(column);
                    if (widgets == null) {
                        widgets = new TreeMap<Double, Widget>();
                        widgetsMap.put(column, widgets);
                    }
                    widgets.put(position.getRow(), widget);
                    break;
                }
            }
        }

        List<List<Widget>> widgetsTable = new ArrayList<List<Widget>>();
        for (Map<Double, Widget> map : widgetsMap.values()) {
            List<Widget> widgets = new ArrayList<Widget>();
            widgets.addAll(map.values());
            widgetsTable.add(widgets);
        }

        return widgetsTable;
    }

    /**
     * Synchronizes the given {@code plugin} with the existing one in the
     * database if it can be found.
     */
    private void synchronizePlugin(Plugin plugin) {

        State pluginState = plugin.getState();
        Database database = getState().getDatabase();
        pluginState.setDatabase(database);

        UUID typeId = pluginState.getTypeId();
        Tool tool = plugin.getTool();
        String internalName = plugin.getInternalName();

        for (Plugin p : getAllPlugins(database)) {
            if (ObjectUtils.equals(typeId, p.getState().getTypeId())
                    && ObjectUtils.equals(tool, p.getTool())
                    && ObjectUtils.equals(internalName, p.getInternalName())) {
                pluginState.setId(p.getId());
                break;
            }
        }
    }

    /** Introduces the given {@code plugin}. */
    public void introducePlugin(Plugin plugin) {
        synchronizePlugin(plugin);
        plugin.save();
    }

    /** Discontinues the use of the given {@code plugin}. */
    public void discontinuePlugin(Plugin plugin) {
        synchronizePlugin(plugin);
        plugin.delete();
    }

    /** Creates an area with the given parameters. */
    public Area createArea(String displayName, String internalName, Area parent, String url) {
        Area area = new Area();
        area.setTool(this);
        area.setDisplayName(displayName);
        area.setInternalName(internalName);
        area.setParent(parent);
        area.setUrl(url);
        synchronizePlugin(area);
        return area;
    }

    /** Creates an widget with the given parameters. */
    public <T extends Widget> T createWidget(Class<T> widgetClass, String displayName, String internalName, String iconName) throws Exception {
        T widget = TypeDefinition.getInstance(widgetClass).newInstance();
        widget.setTool(this);
        widget.setDisplayName(displayName);
        widget.setInternalName(internalName);
        widget.setIconName(iconName);
        synchronizePlugin(widget);
        return widget;
    }
}
