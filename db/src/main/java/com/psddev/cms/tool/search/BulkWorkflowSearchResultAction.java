package com.psddev.cms.tool.search;

import com.psddev.cms.db.Workflow;
import com.psddev.cms.db.WorkflowState;
import com.psddev.cms.db.WorkflowTransition;
import com.psddev.cms.tool.Search;
import com.psddev.cms.tool.SearchResultAction;
import com.psddev.cms.tool.SearchResultSelection;
import com.psddev.cms.tool.SearchResultSelectionItem;
import com.psddev.cms.tool.ToolPageContext;
import com.psddev.dari.db.ObjectType;
import com.psddev.dari.db.Query;
import com.psddev.dari.db.State;
import com.psddev.dari.util.ObjectUtils;
import com.psddev.dari.util.UrlBuilder;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class BulkWorkflowSearchResultAction implements SearchResultAction {

    @Override
    public void writeHtml(ToolPageContext page, Search search, SearchResultSelection selection) throws IOException {

    }
}
