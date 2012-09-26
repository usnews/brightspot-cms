package com.psddev.cms.tool;

import com.psddev.dari.db.Record;
import com.psddev.dari.util.ObjectUtils;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

/** Small */
public class Widget extends Plugin {

    private String iconName;
    private Set<Position> positions;
    private Set<Widget> updateDependencies;

    /** Returns the icon name. */
    public String getIconName() {
        return iconName;
    }

    /** Sets the icon name. */
    public void setIconName(String iconName) {
        this.iconName = iconName;
    }

    /** Returns the positions. */
    public Set<Position> getPositions() {
        if (positions == null) {
            positions = new LinkedHashSet<Position>();
        }
        return positions;
    }

    /** Sets the positions. */
    public void setPositions(Set<Position> positions) {
        this.positions = positions;
    }

    /** Returns the update dependencies. */
    public Set<Widget> getUpdateDependencies() {
        if (updateDependencies == null) {
            updateDependencies = new LinkedHashSet<Widget>();
        }
        return updateDependencies;
    }

    /** Sets the update dependencies. */
    public void setUpdateDependencies(Set<Widget> dependencies) {
        this.updateDependencies = dependencies;
    }

    /**
     * Returns the unique ID that represents this widget for use in
     * permissions.
     */
    public String getPermissionId() {
        return "widget/" + getInternalName();
    }

    /**
     * Adds a position with the given {@code name}, {@code column},
     * and {@code row}.
     */
    public void addPosition(String name, double column, double row) {
        Position position = new Position();
        position.setName(name);
        position.setColumn(column);
        position.setRow(row);
        getPositions().add(position);
    }

    /** Displays the given {@code object}. */
    public String display(ToolPageContext page, Object object) throws Exception {
        throw new UnsupportedOperationException();
    }

    /** Updates the given {@code object}. */
    public void update(ToolPageContext page, Object object) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Embedded
    public static class Position extends Record {

        private @Required String name;
        private @Required double column;
        private @Required double row;

        /** Returns the name. */
        public String getName() {
            return name;
        }

        /** Sets the name. */
        public void setName(String name) {
            this.name = name;
        }

        /** Returns the column. */
        public double getColumn() {
            return column;
        }

        /** Sets the column. */
        public void setColumn(double column) {
            this.column = column;
        }

        /** Returns the row. */
        public double getRow() {
            return row;
        }

        /** Sets the row. */
        public void setRow(double row) {
            this.row = row;
        }
    }
}
