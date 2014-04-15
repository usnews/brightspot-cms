package com.psddev.cms.tool;

import com.psddev.cms.db.Taxon;
import com.psddev.dari.db.Query;
import com.psddev.dari.util.ObjectUtils;
import com.psddev.dari.util.StringUtils;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TaxonSearchResultRenderer extends SearchResultRenderer {

    private static final String PARENT_ID_PARAMETER = "taxonParentId";
    private static final String TAXON_LEVEL_PARAMETER = "taxonLevel";
    private final Collection<? extends Taxon> taxonResults;
    private int level = 1;
    private int nextLevel = 2;
    private boolean displayTaxonView = true;

    public TaxonSearchResultRenderer(ToolPageContext page, Search search) throws IOException {

        super(page, search);

        if (ObjectUtils.isBlank(search.getQueryString()) &&
                search.getSelectedType() != null &&
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

        if(ObjectUtils.isBlank(taxonResults)){
            displayTaxonView = false;
        }
    }

    @Override
    public void render() throws IOException {

        if(displayTaxonView){
            level = page.paramOrDefault(int.class, TAXON_LEVEL_PARAMETER, 1);
            nextLevel = level+1;

            if(level ==1){
                page.writeStart("h2").writeHtml("Result").writeEnd();

                if (search.findSorts().size() > 1) {
                    page.writeStart("div", "class", "searchSorter");
                    renderSorter();
                    page.writeEnd();
                }

                page.writeStart("div", "class", "searchPagination");
                renderPagination();
                page.writeEnd();
            }

            page.writeStart("div", "class", "searchResultList");
            if(level == 1){
                page.writeStart("div", "class", "taxonomyContainer");
                page.writeStart("div", "class", "searchTaxonomy");
            }
            if (!ObjectUtils.isBlank(taxonResults)) {
                renderList(taxonResults);
            } else {
                renderEmpty();
            }
            page.writeEnd();

            if(level == 1){
                page.writeEnd();
                page.writeEnd();
            }
        } else {
            super.render();
        }
    }

    private void writeTaxon(Taxon taxon) throws IOException {
        page.writeStart("li");
        renderBeforeItem(taxon);
        writeTaxonLabel(taxon);
        renderAfterItem(taxon);

        Collection<? extends Taxon> children = taxon.getChildren();

        if (children != null && !children.isEmpty()) {
            page.writeElement("a",
                    "href", page.url("", PARENT_ID_PARAMETER, taxon.as(Taxon.Data.class).getId(), TAXON_LEVEL_PARAMETER, nextLevel),
                    "class", "taxonomyExpand",
                    "target", "d"+nextLevel);
        }
        page.writeEnd();
    }

    private void writeTaxonLabel(Taxon taxon) throws IOException {
        if (taxon == null) {
            page.writeHtml("N/A");
        }
        String altLabel = taxon.as(Taxon.Data.class).getAltLabel();
        if (ObjectUtils.isBlank(altLabel)) {
            page.writeObjectLabel(taxon);
        } else {
            String visibilityLabel = taxon.getState().getVisibilityLabel();
            if (!ObjectUtils.isBlank(visibilityLabel)) {
                page.writeStart("span", "class", "visibilityLabel");
                    page.writeHtml(visibilityLabel);
                page.writeEnd();

                page.writeHtml(" ");
            }
            page.writeHtml(altLabel);
        }
    }

    @Override
    public void renderList(Collection<?> listItems) throws IOException {
        if(displayTaxonView){
            page.writeStart("ul", "class", "taxonomy");
            for (Taxon root : (Collection<Taxon>)listItems) {
                writeTaxon(root);
            }
            page.writeEnd();
            page.writeStart("div",
                    "class", "frame taxonChildren",
                    "name", "d"+nextLevel);
            page.writeEnd();
        } else {
            super.renderList(listItems);
        }
    }

    public void renderPagination() throws IOException {
        if(!displayTaxonView){
            super.renderPagination();
        }
    }

    @Override
    public void renderSorter() throws IOException {
        if(!displayTaxonView){
            super.renderSorter();
        }
    }
}
