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

	Object selected = wp.findOrReserve();
	State state = State.getInstance(selected);
	boolean summaryPage = true;

	if (selected != null) {
		Site site = wp.getSite();
		if (!(site == null || Site.Static.isObjectAccessible(site,
				selected))) {
			wp.redirect("/");
			return;
		}
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

	// What section guide are we displaying (if any)? 
	UUID sectionId = null;
	UUID selectedId = null;
	String sectionStr = wp.param("section");
	if (sectionStr != null && !sectionStr.equals("")) {
		sectionId = UUID.fromString(sectionStr);
	}
	
	// Are we displaying the Production guide for an object with a template, or a one-off page
	Page pg = null;
	if (selected != null && selected instanceof Page) {
		pg = (Page) selected;
	} else {
		pg = Query.findById(
		            Template.class, wp.uuidParam("templateId"));
		// Otherwise we use the default template
		if (pg == null) {
			if (selected != null) {
				pg = state.as(Template.ObjectModification.class)
					.getDefault();
			} 
		}
		if (pg == null) {
			// something has gone wrong
			// TODO: We should do something
		}
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

	// If we haven't selected one, default to the outermost section
	if (sectionId == null) {
		selectedId = pg.getLayout().getOutermostSection().getId();
	} else {
		selectedId = sectionId;
		summaryPage = false;
	}
	// This will get initialized when we iterate through the drop down list
	Section section = null;

	// --- Presentation ---
%>
<%
	wp.include("/WEB-INF/header.jsp");
%>
<style type="text/css">

.button {
    .background-image-vertical-gradient(@color-background, @color-note-lighter);
    border: 1px solid @color-note;
    .border-radius(5px);
    .box-shadow(~'inset 0 1px 0 rgba(255, 255, 255, 0.2), 0 1px 2px rgba(0, 0, 0, 0.05)');
    .box-sizing(border-box);
    color: @color-link;
    cursor: pointer;
    display: inline-block;
    font-weight: bold;
    height: 26px;
    line-height: @lineHeight-input;
    margin: 0;
    padding: 4px 15px;

    &:hover {
        background: @color-note-lighter;
        text-decoration: none;
    }
}

</style>

<div class="widget widget-content">
	<h1 class="icon-page_white_find">
		Production Guide:
		<%=pg.getName()%>

	</h1>
	<!--  Choose page variation -->
	<%
	   // Can use this instead if we put in a way to set target to this popup isntead of _top
	    if (state != null && !state.isNew()) {
		    wp.include("/WEB-INF/objectVariation.jsp", "object", selected);
	    }
	    // Display chosen variation
	   %> <div class="variation">
        Variation: <%= selectedVariation != null ? wp.objectLabel(selectedVariation) : "Default" %> 
       </div> 

	<div class="content-edit">
		<!-- Link back to content edit page -->
		<form href="" class="guideForm" action="<%=wp.url("", "section", section)%>">
			<%
				Section prev = null;
				Section nxt = null;
				Section cur = null;
				boolean foundSelected = false;
				int sectionCnt = 1;
				int curCnt = 1;
			%>

			<div class="guideForm-main">

				<%
					Content samplePage = Guide.Static.getSamplePage(pg);
					if (samplePage != null) {
						wp.write("<iframe src=\"", samplePage.getPermalink(),
								"\" width=\"400px\" height=\"800px\" align=\"left\"></iframe>");
					}
					wp.write("<div class=\"guideTop\">");
					if (!summaryPage) {
						wp.write(pg.getName(), ":  ");
						wp.write("<select name=\"section\" onchange=\"$(this).closest('form').submit();\">");
					}
					Iterator iter = sections.iterator();

					while (iter.hasNext()) {
						sectionCnt++;
						if (cur != null && !foundSelected) {
							prev = cur;
						}
						cur = (Section) iter.next();
						if (nxt == null && foundSelected) {
							nxt = cur;
						}
						if (cur.getId().equals(selectedId)) {
							foundSelected = true;
							section = cur;
							if (!summaryPage) {
								curCnt = sectionCnt;
								// If we are looking at a variation, we need to use that version of this section
								if (selectedVariation != null) {
									State sectionState = State.getInstance(section);
									Map<String, Object> variationData = (Map<String, Object>) sectionState
											.getValue("variations/"
													+ selectedVariation.getId());
									if (variationData != null) {
										sectionState.getValues().putAll(variationData);
									}
								}
							}
						}
						if (nxt == null && summaryPage) {
							// From the summary page, the 'next' is the first section id
							nxt = cur;
						}

						if (!summaryPage) {
							wp.write("<option value=\"" + cur.getId() + "\"");
							if (cur.getId().equals(selectedId)) {
								wp.write("selected=\"selected\"");
							}
							wp.write(">");
							wp.write(nameMap.get(cur.getId()));
							wp.write("</option>");
						} // end if we are displaying section list 		
					} // end while if
					if (!summaryPage) {
						wp.write("</select>");
					}
					if (selected != null) {
						wp.write("<input type=\"hidden\" name=\"id\" value=\"",
							((Content) selected).getId(), "\"/>");
					}

					wp.write("</div>");
					// Display the descriptive content
					if (summaryPage) {
						wp.write("<div class=\"guideTop\">");
						// Display the Summary Page
						request.setAttribute("pageProductionGuide",
								Guide.Static.getPageProductionGuide(pg));
						wp.write("Summary: ");
						wp.write(pg.getName());
						wp.write("</div>");
				        %><div class="guideDescription">
					        <cms:render value="${pageProductionGuide.description}" />
				       </div>
				       <%
					} else {
						if (section != null) {
							request.setAttribute("sectionProductionGuide",
									Guide.Static.getSectionProductionGuide(section));
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
									wp.write("<li><a href=\"", wp.objectUrl(
											"/content/guide.jsp", reference), "\">",
											reference.getName(), "</a></li>");
								}
								wp.write("</div>");
							}
							if (Guide.Static.getSectionTips(section) != null) {
				                %><div class="guideTips">
					             <cms:render value="${sectionProductionGuide.tips}" />
				                </div>
				               <%
					        }
						}
					}
					wp.write("</div>");
					wp.write("<div class=\"guidebuttons\" align=\"center\">");
					if (prev != null) {
						wp.write("<a href=\"", wp.url("", "section", prev.getId()),
								"\" class=\"button\">Previous</a>");
					} else {
						if (!summaryPage) {
							// we don't have a previous section, but we're not the first page, which 
							// means the previous page is the first (summary) page 
							wp.write("<a href=\"", wp.url("", "section", ""),
									"\" class=\"button\">Previous</a>");
						}
					}
					// Current page location
					if (!summaryPage) {
						wp.write(" ");
						wp.write(curCnt);
						wp.write(" of ");
						wp.write(sectionCnt);
						wp.write(" ");
					}
					if (nxt != null) {
						wp.write("<a href=\"", wp.url("", "section", nxt.getId()),
								"\" class=\"button\">", summaryPage ? "Continue"
										: "Next", "</a>");
					}
				%>

			</div>
			
			<div align="right" class="guidebuttons">
				<a href="" class="button">Print Production Guide</a>
			</div>
			<%
				if (summaryPage) {
					List<Template> relatedTemplates = Guide.Static
							.getRelatedTemplates(selected, pg);
					if (relatedTemplates != null && !relatedTemplates.isEmpty()) {
						//wp.write("<div class=\"widget widget-content\">");
						wp.write("<h3>Related Templates - Click on a Template Name to View Summary</h3>");

						// Display samples of related templates
						for (Page template : relatedTemplates) {
							Content sample = Guide.Static.getSamplePage(template);
							if (sample != null) {
								wp.write("<div class=\"relatedTemplate\">");
								wp.write("<a href=\"", wp.objectUrl(
										"/content/guide.jsp", sample,
										"variationId", wp.param("variationId"),
										"templateId", template.getId()),
										"\" class=\"Text\">", template.getName(),
										"</a>");
  								wp.write(
  										"<iframe width=\"300\" height=\"500\" src=\"",
  										sample.getPermalink(), "\"></iframe>");
								//wp.write("<iframe class=\"pageThumbnails_preview_frame\" src=\"/_preview?_cms.db.previewId=", sample.getId(), "\"></iframe>");
					
								wp.write("</div>");
							}
						}
						//wp.write("</div>");
					}
				}
			%>
			</form>
	   </div>
	</div>
	<%
		wp.include("/WEB-INF/footer.jsp");
	%>