package com.psddev.cms.tool;

import com.psddev.cms.db.Taxon;
import com.psddev.dari.db.Query;
import com.psddev.dari.util.ObjectUtils;

import java.io.IOException;
import java.util.Collection;
import java.util.UUID;

public class TaxonSearchResultRenderer extends SearchResultRenderer {

    private static final String PARENT_ID_PARAMETER = "taxonParentId";
    private static final String TAXON_LEVEL_PARAMETER = "taxonLevel";
    private final Collection<? extends Taxon> taxonResults;
    private int level = 1;
    private int nextLevel = 2;

    public TaxonSearchResultRenderer(ToolPageContext page, com.psddev.cms.tool.Search search) throws IOException {

        super(page, search);

        if (search.getSelectedType() != null &&
                search.getSelectedType().getGroups().contains(Taxon.class.getName()) &&
                search.getVisibilities().isEmpty()) {

            UUID taxonParentUuid = page.paramOrDefault(UUID.class, PARENT_ID_PARAMETER, null);
            if (!ObjectUtils.isBlank(taxonParentUuid)) {
                Taxon parent = Query.findById(Taxon.class, taxonParentUuid);
                taxonResults = parent.getChildren();
            } else {
                taxonResults = Taxon.Static.getRoots((Class<Taxon>) search.getSelectedType().getObjectClass());
            }
        } else {
            taxonResults = null;
        }
    }

    private void writeTaxon(Taxon taxon) throws IOException {
        page.writeStart("li");
        renderBeforeItem(taxon);
        page.writeObjectLabel(taxon);
        renderAfterItem(taxon);

        Collection<? extends Taxon> children = taxon.getChildren();

        if (children != null && !children.isEmpty()) {
            page.writeElement("a",
                    "href", page.url("", PARENT_ID_PARAMETER, taxon.as(Taxon.TaxonModification.class).getId(), TAXON_LEVEL_PARAMETER, nextLevel),
                    "class", "taxonomyExpand",
                    "target", "d"+nextLevel);
        }
        page.writeEnd();
    }

    @Override
    public void renderList(Collection<?> listItems) throws IOException {
        level = page.paramOrDefault(int.class, TAXON_LEVEL_PARAMETER, 1);
        nextLevel = level+1;
        if (!ObjectUtils.isBlank(taxonResults)) {
            if(level == 1){
                page.writeStart("div", "class", "taxonomyContainer");
                page.writeStart("div", "class", "searchTaxonomy");
            }

                page.writeStart("ul", "class", "taxonomy");
                for (Taxon root : taxonResults) {
                    writeTaxon(root);
                }
                page.writeEnd();
                page.writeStart("div",
                        "class", "frame taxonChildren",
                        "name", "d"+nextLevel);
                page.writeEnd();
            if(level == 1){
                page.writeEnd();
                page.writeEnd();
            }
        } else {
            renderEmpty();
        }
    }

    public void renderPagination() throws IOException {
        page.writeStart("ul", "class", "pagination");

            page.writeStart("li");
                page.writeHtml("All Results");
            page.writeEnd();

        page.writeEnd();
    }
}
