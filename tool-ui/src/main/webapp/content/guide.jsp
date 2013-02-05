<%@ page
	import="com.psddev.cms.tool.ToolPageContext,
	java.util.Iterator,
	java.util.UUID,
	java.util.HashMap,
	java.util.List,
	java.util.Map,
	java.util.ArrayList,
	com.psddev.cms.db.Content,
	com.psddev.cms.db.ContainerSection,
    com.psddev.cms.db.Guide,
	com.psddev.cms.db.GuidePage,
	com.psddev.cms.db.GuideSection,
	com.psddev.cms.db.Page,
    com.psddev.cms.db.PageFilter,
	com.psddev.cms.db.Section,
	com.psddev.cms.db.Template,
	com.psddev.cms.db.Site,
	com.psddev.cms.db.Variation,
	com.psddev.dari.db.State,
	com.psddev.dari.util.JspUtils,
	com.psddev.dari.db.ReferentialText,
	com.psddev.dari.db.Query,
	com.psddev.cms.tool.ToolPageContext"%>
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
	String guideTitle = "";

	if (guide != null) {
		guideTitle += guide.getTitle();
	}

	Object selected = wp.findOrReserve();
	State state = State.getInstance(selected);
	boolean summaryPage = true;
	boolean overviewPage = true;
	boolean foundSelected = false;
	Iterator iter = null;
	String pageUrl = "";

	// What template guide are we displaying (if any)? 
	Page selectedTemplate = Query.findById(Page.class,
			wp.uuidParam("templateId"));
	if (selectedTemplate != null) {
		overviewPage = false;
	}

	List<Page> pages = null;
	String nextTemplateId = "";
	String nextTemplateName = "";
	if (guide != null) {
		pages = guide.getTemplatesToIncludeInGuide();
	}

	boolean isPopup = false;
	String isPopupStr = wp.param("popup");
	if (isPopupStr != null && isPopupStr.equals("true")) {
		isPopup = true;
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
			guideTitle += " - " + pg.getLabel();
		} else {
			guideTitle += pg.getLabel() + " Production Guide";
		}
		

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
			<div id="guideForm" class="guideForm-main">

				<%
					if (guide != null) {
						Page prevT = null;
						Page nxtT = null;
						Page curT = null;
						foundSelected = false;
						int templateCnt = 1;

						wp.write("View Chapter: ");
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
								nextTemplateId = nxtT.getId().toString();
								nextTemplateName = nxtT.getName();
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
				<ul class="guideControls">
					<li><a
						href="<%=wp.url("guidePrint.jsp", "guideId", guide.getId())%>"
						class="action-print" target="productionGuidePrintout">Print</a></li>
				</ul>
				<%
					} else {
						wp.write("<input type=\"hidden\" name=\"popup\" value=\"",
								isPopupStr, "\"/>");
				%>
				<ul class="guideControls">
					<%
						if (isPopup) {
					%>
					<li><a
						href="<%=wp.url("", "_isFrame", false, "templateId",
							pg.getId(), "popup", false)%>"
						target="_blank">View in Full</a></li>
					<%
						}
					%>
					<li><a
						href="<%=wp.url("guidePrint.jsp", "templateId", pg.getId())%>"
						class="action-print" target="productionGuidePrintout">Print</a></li>
				</ul>
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
					wp.write("View Section: ");
					wp.write("<select id=\"sectionChoice\" name=\"sectionId\" onchange=\"$(this).closest('form').submit();\">");

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
						// No production guides containers or sections with no name
						if (cur instanceof ContainerSection
								|| cur.getName() == null || cur.getName().isEmpty()) {
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

					} // end while next section

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
						    pageUrl = samplePage.getPermalink();
							pageUrl += '?' + PageFilter.OVERLAY_PARAMETER + "=true";
				%>
				<div class="guidePreview">
				    Sample <%=pg.getLabel()%>
					<iframe id="samplePagePreview" name="samplePagePreview"
						class="guidePreviewFrame" src="<%=samplePage.getPermalink()%>" scrolling="no"
						onload="init_sample(window.samplePagePreview);"></iframe>
				</div>
				<%
					}
						wp.write("</div>"); // end guideForm-main
						wp.write("<div class=\"guideButtons\" align=\"center\">");
						if (prev != null && !summaryPage) {
							wp.write("<a href=\"",
									wp.url("", "sectionId", prev.getId()),
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
						wp.write("<div style=\"display: inline; padding-left: 20px; padding-right: 20px;\">");
						wp.write(curCnt);
						wp.write(" of ");
						wp.write(sectionCnt);
						wp.write("</div>");
						if (nxt != null) {
							wp.write("<a href=\"",
									wp.url("", "sectionId", nxt.getId()),
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
									if (sample != null && sample.getPermalink() != null) {
										wp.write("<div class=\"guideRelatedTemplate\">");
										wp.write("<a href=\"", wp.objectUrl(
												"/content/guide.jsp", sample,
												"variationId", wp.param("variationId"),
												"templateId", template.getId()),
												"\" class=\"Text\">", template
														.getName(), "</a>");
										wp.write(
												"<div class=\"guidePreview\"><iframe class=\"guidePreviewFrame\" scrolling=\"no\" src=\"",
												sample.getPermalink(),
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
					if (guide != null && !pages.isEmpty()) {
						if (!nextTemplateId.isEmpty()) {
						    wp.write("<div class=\"guideButtons\" align=\"center\">");
					    	wp.write("<a href=\"", wp.url("", "templateId", nextTemplateId),
								"\">Next Chapter: ", nextTemplateName ,"</a>");
						    wp.write("</div>");
						} else {
							wp.write("<div class=\"guideButtons\" align=\"center\">");
					    	wp.write("<a href=\"", wp.url("", "templateId", ""),
								"\">Go to Overview</a>");
						    wp.write("</div>");
						}
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
if (typeof jQuery !== 'undefined') (function($, win, undef) {         
    var sectionId,
    doc = win.document,
    $doc = $(doc),
    sectionName,
    scrollAmount = 0,
    sectionTop = 0,
    sectionLeft = 0,
    sectionHeight = 0,
    sectionWidth = 0,        
    $frame,
    $guideForm,
    frameOffset,
    frameTransformPct,
    sectionChooserX,
    sectionChooserY,
    $source,
    $target,
    $paths = null,
    targetOffset,
    pathsCanvas,
    pathSourceX, pathSourceY, pathSourceDirection,
    pathTargetX, pathTargetY, pathTargetDirection,
    sourceOffset,
    targetOffset,
    isBackReference = false,
    pathSourceControlX,
    pathSourceControlY,
    pathTargetControlX,
    pathTargetControlY;
        
    sectionId = "<%=sectionId%>";
    sectionName = "<%=sectionName%>";
    $paths = $("#pgCanvas");
    if ($paths.length > 0) {
        $paths.remove();
    }
    
    createDupWithMarkup = (function($body)  { 
     // Create a duplicate of the page with overlay flag on to get section placements
       // without potentially messing up the page
        var $duplicate = $('<iframe/>', {
            'src': '<%=pageUrl%>',
            'css': {
                'background-color': 'white',
                'border': 'none',
                'height': 1500,
                'left': -10000,
                'overflow': 'auto',
                'position': 'absolute',
                'top': 0,
                'width': 1220
                }
        });

        $duplicate.load(function() {
            var $duplicateBody = $duplicate.contents().find('body'),
                    mainObjectData = $.parseJSON($duplicateBody.find('.cms-mainObject').text());
   			markSection($duplicateBody,$body);
    	});
    	
        $body.append($duplicate);
  
    }); // end createDupWithMarkup 

    markSection = (function($duplicateBody,$samplePageBody)  { 

    $duplicateBody.find('span.cms-overlayBegin').each(function() {
        displayMark = false;
        var $begin = $(this),
            data = $.parseJSON($begin.attr('data-object')),
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
                        
                     sectionLeft = minX;
                     sectionTop = minY;
                     sectionHeight = maxY - minY;
                     sectionWidth = maxX - minX;
                         
                     // if this is the section we want, make a border
                     $section = $('<a/>', {
                       'class': 'guidePreviewSection',
                       'id' : 'sectionBlock',
                       'css': {
                       'border': '10px solid black',
                       'height': sectionHeight,
                       'left': sectionLeft,
                       'position': 'absolute',
                       'top': sectionTop,
                       'width': sectionWidth
                     }
                     });

                     displayMark = true;
		             scrollAmount = 0;
		             // If the page is longer than the display, we need to scroll it to the section
		             if (minY > 600) {
		                scrollAmount = minY;
		             }
                 }                                   
             }); // end find overlayEnd
             // Sometimes there are more than one cmsOverlay-end processing to a section, waiting until here ensures we just display the
             // entire single block
             if (displayMark) {
                 $samplePageBody.append($section);
                 $samplePageBody.scrollTop(scrollAmount);
                 var newTop = $samplePageBody.scrollTop();
                 $target = $section;
                 targetOffset = $target.offset();
                 targetOffset.left = sectionLeft;
                 targetOffset.top = sectionTop - newTop;
             }
          });  // end find overlayBegin
                
          if ($target === undefined) {
             return;
          }
                    
          //iframe coordinates
          $frame = $("#samplePagePreview");
          $guideForm = $("#guideForm");
          frameOffset = $frame.offset();
          $source = $("#sectionChoice"); 
          // Percentage that the page was reduced for display in the frame
          frameTransformPct = .30
                              
          //section selector element coordinates
          sectionChooserX = $source.offset().left;
          sectionChooserY = $source.offset().top;
                               
          sourceOffset = $source.offset();
          // Adjust the target offset for the location and transform pct of the iframe
          targetOffset.left = targetOffset.left * frameTransformPct;
          targetOffset.top = targetOffset.top * frameTransformPct;
          targetOffset.left += frameOffset.left;
          targetOffset.top += frameOffset.top;
            
          // remove any previous lines
          var $body;
         // look first for the popup
          $body = $('.popup[name*="productionGuide"]');
          if ($body.length == 0) {
              //if no popup, PG is displayed as page
	           $body = $('body');
          } else {
          // if we put canvas in a popup, need to adjust offsets accordingly
		      sourceOffset.left -= $body.offset().left;
              sourceOffset.top -= $body.offset().top;
              targetOffset.left -= $body.offset().left;
              targetOffset.top -= $body.offset().top;
          }
                
   
          // Create the canvas area where line can be drawn
          $paths = $('<canvas/>', {
                      'class': 'fieldPreview-paths',
                      'id' : 'pgCanvas',
                      'css': {
                             'left': 0,
                             'pointer-events': 'none',
                             'position': 'absolute',
                             'top': 0
               }
           });
                
          $paths.attr({
              'width': $doc.width(),
              'height': $doc.height()
          });
          // insert the canvas
          $body.append($paths);               
          pathsCanvas = $paths[0].getContext('2d');
          // clear the canvas
          pathsCanvas.clearRect(0, 0, $paths.width, $paths.height);
              
          // draw a line from the dropdown box to the marked section 

          // for purposes of determining the center of the block, need to only consider visible content
          var targetHeight = $target.height() * frameTransformPct;
          var maxHeight = $('.guidePreview').height();
          if ((targetOffset.top + targetHeight) > maxHeight) {
            	 targetHeight = maxHeight - targetOffset.top;
          }   
   
          if (sourceOffset.left > targetOffset.left) {
                var targetWidth = $target.outerWidth();
                pathTargetX = targetOffset.left + targetWidth;
                pathTargetY = targetOffset.top + targetHeight / 2;
                isBackReference = true;

                if (targetOffset.left + targetWidth > sourceOffset.left) {
                       pathSourceX = sourceOffset.left + $source.width();
                       pathSourceY = sourceOffset.top + $source.height() / 2;
                       pathSourceDirection = 1;
                       pathTargetDirection = 1;

                } else {
                       pathSourceX = sourceOffset.left;
                       pathSourceY = sourceOffset.top + $source.height() / 2;
                       pathSourceDirection = -1;
                       pathTargetDirection = 1;
                }

          } else {
              pathSourceX = sourceOffset.left + $source.width();
              pathSourceY = sourceOffset.top + $source.height() / 2;
              pathTargetX = targetOffset.left;
              pathTargetY = targetOffset.top + targetHeight / 2;
              pathSourceDirection = 1;
              pathTargetDirection = -1;
          }

          pathSourceControlX = pathSourceX + pathSourceDirection * 100;
          pathSourceControlY = pathSourceY;
          pathTargetControlX = pathTargetX + pathTargetDirection * 100;
          pathTargetControlY = pathTargetY;

          pathsCanvas.strokeStyle = "black";
          pathsCanvas.fillStyle = "black";

          // Reference curve.
          // pathsCanvas.lineWidth = isBackReference ? 0.4 : 1.0;
          pathsCanvas.lineWidth = 1.0;
          pathsCanvas.beginPath();
          pathsCanvas.moveTo(pathSourceX, pathSourceY);
          pathsCanvas.bezierCurveTo(pathSourceControlX, pathSourceControlY, pathTargetControlX, pathTargetControlY, pathTargetX, pathTargetY);
          pathsCanvas.stroke();

          // Arrow head.
          var arrowSize = pathTargetX > pathTargetControlX ? 5 : -5;
          if (isBackReference) {
               arrowSize *= 0.8;
          }
          pathsCanvas.beginPath();
          pathsCanvas.moveTo(pathTargetX, pathTargetY);
          pathsCanvas.lineTo(pathTargetX - 2 * arrowSize, pathTargetY - arrowSize);
          pathsCanvas.lineTo(pathTargetX - 2 * arrowSize, pathTargetY + arrowSize);
          pathsCanvas.closePath();
          pathsCanvas.fill();                  
     }); // end markSection        
})(jQuery, window);
</script>

<script type="text/javascript">
function init_sample(obj) {
     body = $(obj.document.getElementsByTagName("body")[0]);
     createDupWithMarkup(body);
      
}
</script>

