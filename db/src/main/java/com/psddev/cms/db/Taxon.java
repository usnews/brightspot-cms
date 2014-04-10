package com.psddev.cms.db;

import java.util.Collection;
import java.util.List;

import com.psddev.cms.tool.TaxonSearchResultRenderer;
import com.psddev.dari.db.Modification;
import com.psddev.dari.db.Query;
import com.psddev.dari.db.Recordable;
import com.psddev.dari.util.PaginatedResult;

public interface Taxon extends Recordable {

    public boolean isRoot();

    public Collection<? extends Taxon> getChildren();

    @ToolUi.SearchResultRendererClass(TaxonSearchResultRenderer.class)
    @FieldInternalNamePrefix("taxon.")
    public static final class Data extends Modification<Taxon> {

        private Data(){
        }

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

        public void beforeSave(){
            this.setRoot(this.getOriginalObject().isRoot());
        }
    }

    /** {@link Taxon} utility methods. */
    public static final class Static {

        public static <T extends Taxon> List<T> getRoots(Class<T> taxonClass) {
            return Query.from(taxonClass).where("taxon.root = true").selectAll();
        }

        public static <T extends Taxon> PaginatedResult<T> getPaginatedRoots(Class<T> taxonClass, long offset, int limit) {
            return Query.from(taxonClass).where("taxon.root = true").select(offset, limit);
        }
    }
}
