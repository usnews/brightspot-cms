package com.psddev.cms.tool.page;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;

import com.psddev.cms.db.ImageTag;
import com.psddev.cms.tool.PageServlet;
import com.psddev.cms.tool.Search;
import com.psddev.cms.tool.SearchResultRenderer;
import com.psddev.cms.tool.ToolPageContext;
import com.psddev.dari.db.State;
import com.psddev.dari.util.CompactMap;
import com.psddev.dari.util.ImageEditor;
import com.psddev.dari.util.ObjectUtils;
import com.psddev.dari.util.RoutingFilter;
import com.psddev.dari.util.StorageItem;

@RoutingFilter.Path(application = "cms", value = "enhancementSearchResult")
public class EnhancementSearchResult extends PageServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected String getPermissionId() {
        return null;
    }

    @Override
    protected void doService(ToolPageContext page) throws IOException, ServletException {
        Search search = new Search(page);
        SearchResultRenderer resultRenderer = new SearchResultRenderer(page, search) {

            @Override
            public void renderBeforeItem(Object item) throws IOException {
                Map<String, String> enhancement = new CompactMap<String, String>();
                State state = State.getInstance(item);

                enhancement.put("id", state.getId().toString());
                enhancement.put("label", state.getLabel());

                StorageItem preview = state.getPreview();

                if (preview != null) {
                    if (ImageEditor.Static.getDefault() != null) {
                        enhancement.put("preview", new ImageTag.Builder(preview).setHeight(100).toUrl());

                    } else {
                        enhancement.put("preview", preview.getPublicUrl());
                    }
                }

                page.writeStart("a",
                        "data-enhancement", ObjectUtils.toJson(enhancement),
                        "href", "#");
            }

            @Override
            public void renderAfterItem(Object item) throws IOException {
                page.writeEnd();
            }
        };
        
        resultRenderer.render();
    }
}
