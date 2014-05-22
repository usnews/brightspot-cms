package com.psddev.cms.db;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.psddev.dari.db.Modification;
import com.psddev.dari.db.ObjectFieldComparator;
import com.psddev.dari.db.ObjectType;
import com.psddev.dari.db.Query;
import com.psddev.dari.db.Recordable;
import com.psddev.dari.util.ObjectUtils;

public interface Taxon extends Recordable {

    public boolean isRoot();

    public Collection<? extends Taxon> getChildren();

    @FieldInternalNamePrefix("cms.taxon.")
    public static final class Data extends Modification<Taxon> {

        @Indexed
        @ToolUi.Hidden
        private Boolean root;

        @ToolUi.Hidden
        private String altLabel;

        public Boolean isRoot() {
            return Boolean.TRUE.equals(root);
        }

        public void setRoot(Boolean root) {
            this.root = root ? Boolean.TRUE : null;
        }

        public String getAltLabel() {
            return altLabel;
        }

        public void setAltLabel(String altLabel) {
            this.altLabel = altLabel;
        }

        public void beforeSave() {
            this.setRoot(this.getOriginalObject().isRoot());
        }

    }

    /** {@link Taxon} utility methods. */
    public static final class Static {

        public static <T extends Taxon> List<T> getRoots(Class<T> taxonClass) {
            List<T> roots = Query.from(taxonClass).where("cms.taxon.root = true").selectAll();
            sort(roots);
            return roots;
        }

        public static <T extends Taxon> List<? extends Taxon> getChildren(T taxon) {
            List<Taxon> children = new ArrayList<Taxon>();
            if (taxon != null) {
                children.addAll(taxon.getChildren());
            }
            sort(children);
            return children;
        }

        public static <T extends Taxon> void sort(List<T> taxa) {
            if (taxa != null && !taxa.isEmpty()) {
                ObjectType taxonType = taxa.get(0).getState().getType();
                ToolUi ui = taxonType.as(ToolUi.class);
                if (!ObjectUtils.isBlank(ui.getDefaultSortField())) {
                    Collections.sort(taxa, new ObjectFieldComparator(ui.getDefaultSortField(), true));
                }
            }
        }

    }

}
