<%@ page
	import="com.psddev.cms.tool.ToolPageContext,com.psddev.dari.db.ReferentialText,com.psddev.cms.db.Site,com.psddev.dari.db.Query,com.psddev.cms.db.Variation,java.util.Iterator,java.util.UUID,java.util.HashMap,java.util.List,java.util.Map,java.util.ArrayList,com.psddev.cms.db.Content,com.psddev.cms.db.ContainerSection,com.psddev.cms.db.PageFilter,com.psddev.cms.db.Page,com.psddev.cms.db.Section,com.psddev.cms.db.Guide,com.psddev.cms.db.GuidePage,com.psddev.cms.db.GuideSection,com.psddev.cms.db.Template,com.psddev.dari.db.State,com.psddev.dari.util.JspUtils,com.psddev.cms.tool.ToolPageContext"%>
<%@ taglib prefix="cms" uri="http://psddev.com/cms"%>
<jsp:useBean id="productionGuide" class="com.psddev.cms.db.Guide"
	scope="request" />
<jsp:useBean id="pageProductionGuide"
	class="com.psddev.cms.db.GuidePage" scope="request" />
<jsp:useBean id="sectionProductionGuide"
	class="com.psddev.cms.db.GuideSection" scope="request" />
<%
	// --- Logic ---

	ToolPageContext wp = new ToolPageContext(pageContext);
	if (wp.requirePermission("area/dashboard")) {
		return;
	}
	Guide guide = Query.findById(Guide.class, wp.uuidParam("guideId"));
	String guideTitle = "Production Guide: ";

	if (guide != null) {
		guideTitle += guide.getTitle();
	}

	Object selected = wp.findOrReserve();
	State state = State.getInstance(selected);
	boolean summaryPage = true;
	boolean overviewPage = true;
	boolean foundSelected = false;
	Iterator iter = null;

	// What template guide are we displaying (if any)? 
	Page selectedTemplate = Query.findById(Page.class,
			wp.uuidParam("templateId"));
	if (selectedTemplate != null) {
		overviewPage = false;
	}

	List<Page> pages = null;
	String nextTemplate = "";
	if (guide != null) {
		pages = guide.getTemplatesToIncludeInGuide();
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
	String sectionStr = wp.param("sectionId");
	String sectionName = "";
	if (sectionStr != null && !sectionStr.equals("0")
			&& !sectionStr.equals("")) {
		sectionId = UUID.fromString(sectionStr);
	}

	// Are we displaying the Production guide for an object with a template, or a one-off page
	Page pg = null;
	if (selected != null && selected instanceof Page) {
		pg = (Page) selected;
	} else {
		pg = Query.findById(Template.class, wp.uuidParam("templateId"));
		// Otherwise we use the default template
		if (pg == null) {
			if (selected != null) {
				pg = state.as(Template.ObjectModification.class)
						.getDefault();
			}
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
	Iterable<Section> sections = null;
	HashMap<UUID, String> nameMap = null;
	if (pg != null) {
		if (guide != null) {
			guideTitle += " - ";
		}
		guideTitle += pg.getLabel();

		sections = pg.findSections();
		nameMap = Guide.Static.getSectionNameMap(sections);

		if (sectionId != null) {
			// if the indicated section isn't in this page/template, ignore it
			if (nameMap.get(sectionId) == null) {
				sectionId = null;
				selectedId = null;
			}
		}

		// If we haven't selected one, default to the outermost section
		if (sectionId == null) {
			selectedId = pg.getLayout().getOutermostSection().getId();
		} else {
			selectedId = sectionId;
			summaryPage = false;
		}
	}
	// This will get initialized when we iterate through the drop down list
	Section section = null;

	// --- Presentation ---
%>
<%
	wp.include("/WEB-INF/header.jsp");
%>
<style type="text/css">
.button { .background-image-vertical-gradient (@color-background,
	@color-note-lighter);
	border: 1px solid@color-note; . border-radius (5px); . box-shadow
	(~'inset 0 1px 0 rgba(255, 255, 255, 0.2), 0 1px 2px rgba(0, 0, 0,
	0.05)'); . box-sizing (border-box);
	color: @color-link;
	cursor: pointer;
	display: inline-block;
	font-weight: bold;
	height: 26px;
	line-height: @lineHeight-input;
	margin: 0;
	padding: 4px 15px; &: hover { background : @ color-note-lighter;
	text-decoration: none;
}
}
</style>

<div class="widget widget-content">
	<h1 class="icon-page_white_find">
		<%=guideTitle%>

	</h1>

	<div class="content-edit">
		<form href="" class="guideForm"
			action="<%=wp.url("", "sectionId", section)%>">
			<div class="guideForm-main">
				<%
					if (guide != null) {
						Page prevT = null;
						Page nxtT = null;
						Page curT = null;
						foundSelected = false;
						int templateCnt = 1;

						wp.write("Chapter: ");
						wp.write("<select name=\"templateId\" onchange=\"$(this).closest('form').submit();\">");

						iter = pages.iterator();

						wp.write("<option value=\"\"");
						if (overviewPage) {
							wp.write("selected=\"selected\"");
						}
						wp.write(">");
						wp.write("Overview");
						wp.write("</option>");

						while (iter.hasNext()) {
							templateCnt++;
							if (curT != null && !foundSelected) {
								prevT = curT;
							}
							curT = (Page) iter.next();
							if (nxtT == null && (foundSelected || overviewPage)) {
								nxtT = curT;
								nextTemplate = nxtT.getId().toString();
							}
							if (selectedTemplate != null
									&& curT.getId().equals(selectedTemplate.getId())) {
								foundSelected = true;
							}
							if (nxtT == null && overviewPage) {
								nxtT = curT;
							}

							wp.write("<option value=\"" + curT.getId() + "\"");
							if (!overviewPage
									&& curT.getId().equals(selectedTemplate.getId())) {
								wp.write("selected=\"selected\"");
							}
							wp.write(">");
							wp.write(curT.getName());
							wp.write("</option>");

						} // end while if

						wp.write("</select>");

						wp.write("<input type=\"hidden\" name=\"guideId\" value=\"",
								guide.getId(), "\"/>");
						if (selectedTemplate != null) {
							wp.write(
									"<input type=\"hidden\" name=\"templateId\" value=\"",
									selectedTemplate.getId(), "\"/>");
						}
				%>
				<div align="right" class="guideButtons">
					<a href="<%=wp.url("guidePrint.jsp", "guideId", guide.getId())%>"
						target="productionGuidePrintout" class="button">Print
						Production Guide</a>
				</div>
				<%
					} else {
				%>
				<div align="right" class="guideButtons">
					<a href="<%=wp.url("guidePrint.jsp", "templateId", pg.getId())%>"
						target="productionGuidePrintout" class="button">Print
						Production Guide</a>
				</div>
				<%
					}
				%>


				<%
					// Main Production Guide Overview
					if (overviewPage) {
						// Display the Overview
						request.setAttribute("productionGuide", guide);
				%><div class="guideOverview">
					<cms:render value="${productionGuide.overview}" />
				</div>
			</div>
			<%
				} else {
					// Production Guide for a given template/page (Can be queried directly without parent Guide Id)
					Section prev = null;
					Section nxt = null;
					Section cur = null;
					foundSelected = false;
					int sectionCnt = 1;
					int curCnt = 1;
			%>


			<%
				Content samplePage = Guide.Static.getSamplePage(pg);

					wp.write("<div class=\"guideTop\">");
					wp.write(pg.getName(), " Guide Section:  ");
					wp.write("<select name=\"sectionId\" onchange=\"$(this).closest('form').submit();\">");

					iter = sections.iterator();

					wp.write("<option value=\"0\"");
					if (summaryPage) {
						wp.write("selected=\"selected\"");
					}
					wp.write(">");
					wp.write("Summary");
					wp.write("</option>");

					while (iter.hasNext()) {
						Section tmp = cur;
						cur = (Section) iter.next();
						if (cur instanceof ContainerSection) {
							cur = tmp;
							continue;
						}
						if (tmp != null && !foundSelected) {
							prev = tmp;
						}
						sectionCnt++;
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
										sectionState.getValues().putAll(
												variationData);
									}
								}
							}
						}
						if (nxt == null && summaryPage) {
							// From the summary page, the 'next' is the first section id
							nxt = cur;
						}

						wp.write("<option value=\"" + cur.getId() + "\"");
						if (cur.getId().equals(selectedId) && !summaryPage) {
							wp.write("selected=\"selected\"");
						}
						wp.write(">");
						wp.write(nameMap.get(cur.getId()));
						wp.write("</option>");

					} // end while if

					wp.write("</select>");

					if (selected != null && selected instanceof Content) {
						wp.write("<input type=\"hidden\" name=\"id\" value=\"",
								((Content) selected).getId(), "\"/>");
					}
					if (pg != null) {
						wp.write(
								"<input type=\"hidden\" name=\"templateId\" value=\"",
								pg.getId(), "\"/>");
					}

					wp.write("</div>");
					// Display the descriptive content
					if (summaryPage) {
						// Display the Summary Page
						request.setAttribute("pageProductionGuide",
								Guide.Static.getPageProductionGuide(pg));
			%><div class="guideDescription">
				<cms:render value="${pageProductionGuide.description}" />
			</div>
			<%
				} else {
						if (section != null) {
							sectionName = section.getName();
							GuideSection sectionGuide = Guide.Static
									.getSectionGuide(pg, section);
							request.setAttribute("sectionProductionGuide",
									sectionGuide);
			%><div class="guideDescription">
				<cms:render value="${sectionProductionGuide.description}" />

				<%
					List<Page> references = Guide.Static
										.getSectionReferences(section, pg);

								if (references != null && references.size() > 0) {
									wp.write("<div class=\"guideModuleReferences\"");
									wp.write("<p>This module also appears on:</p>");
									for (Page reference : references) {
										wp.write("<li><a href=\"", wp.objectUrl(
												"/content/guide.jsp", reference),
												"\">", reference.getName(), "</a></li>");
									}
									wp.write("</div>");
								}
								if (sectionGuide != null
										&& sectionGuide.getTips() != null
										&& !sectionGuide.getTips().isEmpty()) {
				%><div class="guideTips">
					<cms:render value="${sectionProductionGuide.tips}" />
				</div>
				<%
					}
								wp.write("</div>"); // end guideDescription
							}

						}

						if (samplePage != null && samplePage.getPermalink() != null) {
							String pageUrl = samplePage.getPermalink();
							pageUrl += '?' + PageFilter.OVERLAY_PARAMETER + "=true";
				%>
				<div class="guidePreview">
					<iframe name="guidePreview" class="guidePreviewFrame"
						src="<%=pageUrl%>" onload="init_sample(window.guidePreview);"></iframe>
				</div>
				<%
					}
						wp.write("</div>"); // end guideForm-main
						wp.write("<div class=\"guideButtons\" align=\"center\">");
						if (prev != null && !summaryPage) {
							wp.write("<a href=\"", wp.url("", "sectionId", prev.getId()),
									"\" class=\"button\">Previous</a>");
						} else {
							if (!summaryPage) {
								// we don't have a previous section, but we're not the first page, which 
								// means the previous page is the first (summary) page 
								wp.write("<a href=\"", wp.url("", "sectionId", ""),
										"\" class=\"button\">Previous</a>");
							}
						}
						// Current page location
						wp.write(" ");
						wp.write(curCnt);
						wp.write(" of ");
						wp.write(sectionCnt);
						wp.write(" ");
						if (nxt != null) {
							wp.write("<a href=\"", wp.url("", "sectionId", nxt.getId()),
									"\" class=\"button\">Next</a>");
						}
						wp.write("</div>"); // end guidebuttons

						if (summaryPage) {
							List<Template> relatedTemplates = Guide.Static
									.getRelatedTemplates(selected, pg);
							if (relatedTemplates != null && !relatedTemplates.isEmpty()) {
								//wp.write("<div class=\"widget widget-content\">");
								wp.write("<h3>Related Templates - Click on a Template Name to View Summary</h3>");

								// Display samples of related templates
								for (Page template : relatedTemplates) {
									Content sample = Guide.Static
											.getSamplePage(template);
									if (sample != null) {
										wp.write("<div class=\"guideRelatedTemplate\">");
										wp.write("<a href=\"", wp.objectUrl(
												"/content/guide.jsp", sample,
												"variationId", wp.param("variationId"),
												"templateId", template.getId()),
												"\" class=\"Text\">", template
														.getName(), "</a>");
										wp.write(
												"<div class=\"guidePreview\"><iframe class=\"guidePreviewFrame\" src=\"",
												samplePage.getPermalink(),
												"\"></iframe></div>");

										wp.write("</div>");
									}
								}
							}
							wp.write("</div>");
						}
				%>


				<%
					}
					if (guide != null) {
						wp.write("<div class=\"guideButtons\" align=\"center\">");
						wp.write("<a href=\"", wp.url("", "templateId", nextTemplate),
								"\" class=\"button\">Next Chapter</a>");
						wp.write("</div>");
					}
				%>
			</div>
		</form>
	</div>
</div>
</div>
<%
	wp.include("/WEB-INF/footer.jsp");
%>
<script type="text/javascript">
function init_sample(obj) {
   var samplePageBody = obj.document.getElementsByTagName("body")[0];
   
    if (samplePageBody) {
      $(samplePageBody).find('span.cms-overlayBegin').each(function() {
                    var $begin = $(this),
                            data = $.parseJSON($begin.text()),
                            found = false,
                            minX = Number.MAX_VALUE,
                            maxX = 0,
                            minY = Number.MAX_VALUE,
                            maxY = 0,
                            $section,
                            $edit;
       });
    }
   
}
</script>
<script type="text/javascript">
function init_sample(obj) {
    body = $(obj.document.getElementsByTagName("body")[0]);
    markSection(body);
}

(function($, win, undef) {
                     
        var $samplePageWin = $(window.frames[ "guidePreview" ]);
        sectionId = "<%=sectionId%>";
        sectionName = "<%=sectionName%>";
        
        markSection = (function($samplePageBody)  { 

                 $samplePageBody.find('span.cms-overlayBegin').each(function() {
                    var $begin = $(this),
                            data = $.parseJSON($begin.text()),
                            found = false,
                            minX = Number.MAX_VALUE,
                            maxX = 0,
                            minY = Number.MAX_VALUE,
                            maxY = 0,
                            $section,
                            $edit;
                           

                    // Calculate the section size using the marker SPANs.
                    $begin.nextUntil('span.cms-overlayEnd').filter(':visible').each(function() {
                        var $item = $(this),
                                itemOffset = $item.offset(),
                                itemMinX = itemOffset.left,
                                itemMaxX = itemMinX + $item.outerWidth(),
                                itemMinY = itemOffset.top,
                                itemMaxY = itemMinY + $item.outerHeight();

                        found = true;

                        if (minX > itemMinX) {
                            minX = itemMinX;
                        }
                        if (maxX < itemMaxX) {
                            maxX = itemMaxX;
                        }
                        if (minY > itemMinY) {
                            minY = itemMinY;
                        }
                        if (maxY < itemMaxY) {
                            maxY = itemMaxY;
                        }
                        
                        if (found && data.sectionId == sectionId) {
                        
                            // if this is the section we want, make a border
                            $section = $('<a/>', {
                              'class': 'guidePreviewSection',
                              'css': {
                                'border': '10px solid',
                                'height': maxY - minY,
                                'left': minX,
                                'position': 'absolute',
                                'top': minY,
                                'width': maxX - minX
                              }
                            });

                           
                            $samplePageBody.append($section);
                        }
                                            
                    });
                });
                    
             });         
                    
        

})(jQuery, window);
</script>