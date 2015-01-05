package com.psddev.cms.tool;

import java.io.IOException;

public interface SearchResultAction {

    public void writeHtml(
            ToolPageContext page,
            Search search,
            SearchResultSelection selection)
            throws IOException;
}
