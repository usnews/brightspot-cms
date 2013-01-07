<%@ page
	import="com.psddev.cms.tool.ToolPageContext,com.psddev.dari.db.ReferentialText,com.psddev.cms.db.Site,com.psddev.dari.db.Query,com.psddev.cms.db.Variation,java.util.Iterator,java.util.UUID,java.util.HashMap,java.util.List,java.util.Map,java.util.ArrayList,com.psddev.cms.db.Content,com.psddev.cms.db.ContainerSection,com.psddev.cms.db.PageFilter,com.psddev.cms.db.Page,com.psddev.cms.db.Section,com.psddev.cms.db.Guide,com.psddev.cms.db.Guide.*,com.psddev.cms.db.Template,com.psddev.dari.db.State,com.psddev.cms.tool.ToolPageContext"%>
<%@ taglib prefix="cms" uri="http://psddev.com/cms"%>
<jsp:useBean id="pageProductionGuide"
	class="com.psddev.cms.db.Guide$PageProductionGuideModification"
	scope="request" />
<jsp:useBean id="sectionProductionGuide"
	class="com.psddev.cms.db.Guide$SectionProductionGuideModification"
	scope="request" />


<%
	// --- Logic ---

	ToolPageContext wp = new ToolPageContext(pageContext);
	if (wp.requirePermission("area/dashboard")) {
		return;
	}

	Page pg = Query.findById(Page.class, wp.uuidParam("templateId"));
	if (pg == null) {
		// something has gone wrong
		// TODO: We should do something
	}

	// Was there a variation selected?
	String variationIdStr = wp.param("variationId");
	UUID variationId = null;
	Variation selectedVariation = null;
	if (variationIdStr != null) {
		variationId = UUID.fromString(variationIdStr);
		selectedVariation = Query.findById(Variation.class,
				wp.uuidParam("variationId"));
	}

	// If a variation was selected, we use that
	if (selectedVariation != null) {
		// get the state (if this is a page referencing a template, we need the state of the template object, not the edited object)
		State pageState = State.getInstance(pg);
		Map<String, Object> variationData = (Map<String, Object>) pageState
				.getValue("variations/" + selectedVariation.getId());
		if (variationData != null) {
			pageState.getValues().putAll(variationData);
		}
	}

	// Get the list of sections that are in the layout
	Iterable<Section> sections = pg.findSections();
	HashMap<UUID, String> nameMap = Guide.Static
			.getSectionNameMap(sections);
	int totalCnt = 1;
	if (nameMap.size() > 0) {
		totalCnt += nameMap.size();
	}

	// --- Presentation ---
%>

<script type="text/javascript">  
      window.onload = function(){window.print();}  
</script>
<style>
    @media print {
    body {
        margin: 0 !important;
    }
    body > * {
        display: none;
    }
    body > .popup[name=productionGuidePrintout] {
        display: block !important;
        position: static !important;        
    }
    body > .popup[name=productionGuidePrintout] .marker {
        display: none !important;
    }
     body > .popup[name=productionGuidePrintout] .closeButton {
        display: none !important;
    }
    body > .popup[name=productionGuidePrintout] .content {
        box-shadow: 0 0 0 !important;
        -webkit-box-shadow: 0 0 0 !important;
        -moz-box-shadow: 0 0 0 !important;;  
    }
    
  }
</style>
    

<%
	wp.include("/WEB-INF/header.jsp");
%>

<div class="widget">
	<h1 class="icon-page_white_find">
		Production Guide:
		<%=pg.getName()%>

	</h1>

	<div class="variation">
		Variation:
		<%=selectedVariation != null ? wp
					.objectLabel(selectedVariation) : "Default"%>
	</div>



	<%
		Section section = null;
		int curCnt = 1;

		// Summary Page
	%>
	<div class="content-edit">
		<form href="" class="guideForm">
			
			<div class="guideForm-page">
				<div class="guideForm-main">

					<%
						Content samplePage = Guide.Static.getSamplePage(pg);

						wp.write("<div class=\"guideTop\">");

						// Display the Summary Page
						request.setAttribute("pageProductionGuide",
								Guide.Static.getPageProductionGuide(pg));
						wp.write("Summary: ");
						wp.write(pg.getName());
						wp.write("</div>");
						// how best to format for printing?
						if (samplePage != null) {
							wp.write("<iframe name=\"pgSamplePage\" src=\"", samplePage.getPermalink(),
									"\" width=\"400px\" height=\"750px\" align=\"left\" scrolling=\"no\"></iframe>");
						}
					%><div class="guideDescription">
						<cms:render value="${pageProductionGuide.description}" />
					</div>
				</div>
				<%
					wp.write("<div class=\"guidebuttons\" align=\"center\">");
					wp.write(" ");
					wp.write(curCnt++);
					wp.write(" of ");
					wp.write(totalCnt);
					wp.write(" ");
					wp.write("</div>"); // end page number
				%>

			</div>



			<%
				// Sections
				Iterator iter = sections.iterator();

				while (iter.hasNext()) {
					wp.write("<div class=\"guideForm-page\">");
					wp.write("<div class=\"guideForm-main\">");
					wp.write("<div class=\"guideTop\">");
					section = (Section) iter.next();
					if (selectedVariation != null) {
						State sectionState = State.getInstance(section);
						Map<String, Object> variationData = (Map<String, Object>) sectionState
								.getValue("variations/" + selectedVariation.getId());
						if (variationData != null) {
							sectionState.getValues().putAll(variationData);
						}
					}
					request.setAttribute("sectionProductionGuide",
							Guide.Static.getSectionProductionGuide(section));
					wp.write("Section: ");
					wp.write(section.getName());
					wp.write("</div>"); // end guideTop
					// how best to format for printing?
					if (samplePage != null) {
						wp.write("<iframe name=\"pgSamplePage\" src=\"", samplePage.getPermalink(),
								"\" width=\"400px\" height=\"750px\" align=\"left\"></iframe>");
					}
			%><div class="guideDescription">
				<cms:render value="${sectionProductionGuide.description}" />
			</div>
			<%
				List<Page> references = Guide.Static.getSectionReferences(
							section, pg);

					if (references != null && references.size() > 0) {
						wp.write("<div class=\"moduleReferences\"");
						wp.write("<p>This module also appears on:</p>");
						for (Page reference : references) {
							wp.write("<li>", reference.getName(), "</li>");
						}
						wp.write("</div>");
					}
					if (Guide.Static.getSectionTips(section) != null) {
			%><div class="guideTips">
				<cms:render value="${sectionProductionGuide.tips}" />
			</div>
			<%
				}
				wp.write("</div>"); // end guideForm-main
				wp.write("</div>"); // end guideForm-page
				}
			%>
		</form>
	</div>
</div>
<script>

onload:{window.print();window.close();}
</script>

<%
	wp.include("/WEB-INF/footer.jsp");
%>