package com.psddev.cms.db;

import com.psddev.cms.db.GuideSection;
import com.psddev.cms.db.ToolUi;
import com.psddev.dari.db.Record;
import com.psddev.dari.db.Query;
import com.psddev.dari.db.ReferentialText;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * A Production Guide is Editor-oriented Help documentation providing guidance on how to create and organize content for a site. It's content
 * is linked directly to the artifacts in the CMS so that helpful information is available contextually and can be easily kept up to date
 * as the site evolves.
 * 
 * A Guide object consists of some overall descriptive content about programming the Site, and then assocations to templates/pages that
 * make up the site. Those templates and their associated content fields also have editorial guidance associated with them via the modification classes
 * defined in this class.
 */
public class Guide extends Record {


	private static final Logger LOGGER = LoggerFactory.getLogger(Guide.class);

	@Required
	@Indexed(unique = true)
	@ToolUi.Note("Production Guide Title")
	private String title;

	@ToolUi.Note("Production Guide Overview Section")
	private ReferentialText overview;
	
	@ToolUi.Note("Select the templates (or one-off pages) to be included in this Production Guide in the order they should appear")
	private List<Page> templatesToIncludeInGuide;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public ReferentialText getOverview() {
		return overview;
	}

	public void setOverview(ReferentialText overview) {
		this.overview = overview;
	}

	public List<Page> getTemplatesToIncludeInGuide() {
		return templatesToIncludeInGuide;
	}

	public void setTemplatesToIncludeInGuide(
			List<Page> templatesToIncludeInGuide) {
		this.templatesToIncludeInGuide = templatesToIncludeInGuide;
	}


	/** Static utility methods. */
	public static final class Static {

		private Static() {
		}

		/*
		 * Get the main Production Guide
		 */
		public static GuidePage getPageProductionGuide(
				Page content) {
			if (content != null) {
				GuidePage guide = Query.from(GuidePage.class).where("pageType = ?", content.getId()).first();
				if (guide != null) {
					return guide;
				}
			}
			return null;

		}

		/*
		 * Get the Production Guide summary description
		 */
		public static ReferentialText getSummaryDescription(Page content) {
			if (content != null) {
				GuidePage guide = Static.getPageProductionGuide(content);
				if (guide != null) {
					return guide.getDescription();
				}
			}
			return null;

		}

		/*
		 * Get the Production Guide sample page
		 */
		public static Content getSamplePage(Page content) {
			if (content != null) {
				GuidePage guide = Static.getPageProductionGuide(content);
				if (guide != null) {
					return guide.getSamplePage();
				}
			}
			return null;

		}

		/*
		 * Return all pages/templates that the given section appears on (if it
		 * is shareable), excluding the provided (usually current) page.
		 */
		public static List<Page> getSectionReferences(Section section, Page page) {
			if (section != null && section.isShareable()) {
				List<Page> references = Query
						.from(Page.class)
						.where("* matches ? && not id = ?", section.getId(),
								page.getId()).selectAll();
				return references;
			}
			return null;

		}

		/*
		 * Get the Production Guide description for a template section
		 */
		public static GuideSection getSectionGuide(Page page, Section section) {
			if (section != null && page != null) {
				GuidePage pageGuide = getPageProductionGuide(page);
				if (pageGuide != null) {
					return pageGuide.findOrCreateSectionGuide(section);
				}
			}
			return null;
		}
		
		/*
		 * Get the Production Guide description for a template section
		 */
		public static ReferentialText getSectionDescription(Page page, Section section) {
			if (section != null && page != null) {
				GuidePage pageGuide = getPageProductionGuide(page);
				if (pageGuide != null) {
					return pageGuide.getSectionDescription(section);
				}
			}
			return null;
		}

		/*
		 * Get the Production Guide tips for a template section
		 */
		public static ReferentialText getSectionTips(Page page, Section section) {
			if (section != null && page != null) {
				GuidePage pageGuide = getPageProductionGuide(page);
				if (pageGuide != null) {
					return pageGuide.getSectionTips(section);
				}
			}
			return null;
		}


		/*
		 * Return the related templates (other than the one provided) that have
		 * a sample page defined
		 */
		public static List<Template> getRelatedTemplates(Object object,
				Page ignoreTemplate) {
			List<Template> relatedTemplates = new ArrayList<Template>();
			List<Template> usableTemplates = Template.Static.findUsable(object);
			for (Template template : usableTemplates) {
				if (!template.getId().equals(ignoreTemplate.getId())
						&& Guide.Static.getSamplePage(template) != null) {
					relatedTemplates.add(template);
				}
			}
			return relatedTemplates;
		}

		/*
		 * Return a map of section ids to names where names are a concatenation
		 * of the section name along with it's parental lineage in the layout
		 */
		public static HashMap<UUID, String> getSectionNameMap(
				Iterable<Section> sections) {
			HashMap<UUID, String> nameMap = new HashMap<UUID, String>();

			// This assumes that the sections are provided in an order such that
			// parents are
			// evaluated before children, which is how they get returned from
			// Section
			for (Section section : sections) {
				String sectionName = "";
				if (!nameMap.containsKey(section.getId())) {
					if (!section.getName().isEmpty()) {
						sectionName = section.getName();
					} else {
						// if the section wasn't given an explicit name, we use
						// the class name (e.g. VerticalContainerSection)
						sectionName = section.getClass().getSimpleName();
					}
					nameMap.put(section.getId(), sectionName);
				} else {
					sectionName = nameMap.get(section.getId());
				}

				if (section instanceof ContainerSection) {
					String childName = "";
					for (Section child : ((ContainerSection) section)
							.getChildren()) {
						if (!child.getName().isEmpty()) {
							childName = child.getName();
						} else {
							childName = child.getClass().getSimpleName();
						}

						if (!nameMap.containsKey(child.getId())) {
							nameMap.put(child.getId(), sectionName + " - "
									+ childName);
						}
					}
				}
			}
			return nameMap;
		}

	}
	


}