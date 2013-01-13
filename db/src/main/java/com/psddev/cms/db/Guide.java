package com.psddev.cms.db;

import com.psddev.dari.db.Database;
import com.psddev.dari.db.Modification;
import com.psddev.dari.db.ObjectField;
import com.psddev.dari.db.ObjectType;
import com.psddev.dari.db.Predicate;
import com.psddev.dari.db.PredicateParser;
import com.psddev.dari.db.Record;
import com.psddev.dari.db.Query;
import com.psddev.dari.db.ReferentialText;
import com.psddev.dari.db.State;
import com.psddev.dari.db.ValidationException;
import com.psddev.dari.util.ObjectUtils;
import com.psddev.dari.util.PeriodicCache;
import com.psddev.dari.util.PullThroughCache;
import com.psddev.dari.util.PullThroughValue;
import com.psddev.dari.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;

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

	public static final String FIELD_PREFIX = "cms.guide.";
	public static final String NAME_FIELD = FIELD_PREFIX + "name";
	public static final String DESC_FIELD = FIELD_PREFIX + "description";
	public static final String SAMPLE_FIELD = FIELD_PREFIX + "samplePage";
	public static final String TIPS_FIELD = FIELD_PREFIX + "tips";
	public static final String SHOW_FIELD = FIELD_PREFIX + "showGuides";
	// public static final String FIELD_DESCRIPTIONS = FIELD_PREFIX +
	// "fieldDescriptions";

	private static final Logger LOGGER = LoggerFactory.getLogger(Guide.class);

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

	/** Modification that adds guide information for a page. */
	@Modification.Classes({ Page.class })
	public static final class PageProductionGuideModification extends
			Modification<Object> {

		@InternalName(NAME_FIELD)
		@ToolUi.Note("Production Guide title for this page/template type")
		private String name;

		@ToolUi.Note("Production Guide text for this page/template")
		@InternalName(DESC_FIELD)
		private ReferentialText description;

		@ToolUi.Note("Sample (Published) Page as documentation example for this page/template")
		@InternalName(SAMPLE_FIELD)
		private Content samplePage;

		public PageProductionGuideModification() {
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		/** Returns the Production Guide description for this object. */
		public ReferentialText getDescription() {
			return description;
		}

		/** Sets the Production Guide description for this object. */
		public void setDescription(ReferentialText description) {
			this.description = description;
		}

		public Content getSamplePage() {
			return samplePage;
		}

		public void setSamplePage(Content samplePage) {
			this.samplePage = samplePage;
		}

	}

	/** Modification that adds guide information for a page. */
	@Modification.Classes({ Section.class })
	public static class SectionProductionGuideModification extends
			Modification<Object> {

		@ToolUi.Note("Production Guide description for this section")
		@InternalName(DESC_FIELD)
		private ReferentialText description;

		@ToolUi.Note("Production Guide Tips for this section")
		@InternalName(TIPS_FIELD)
		private ReferentialText tips;

		public SectionProductionGuideModification() {
		}

		/** Returns the Production Guide description for this object. */
		public ReferentialText getDescription() {
			return description;
		}

		/** Sets the Production Guide description for this object. */
		public void setDescription(ReferentialText description) {
			this.description = description;
		}

		public ReferentialText getTips() {
			return tips;
		}

		public void setTips(ReferentialText tips) {
			this.tips = tips;
		}

	}


	/** Modification that records a user's production guide settings. */
	@Modification.Classes({ ToolUser.class })
	@ToolUi.Hidden
	public static final class ProductionGuideSettingsModification extends
			Modification<Object> {

		@ToolUi.Note("If true, default to display of Production Guide when editing content")
		@InternalName(SHOW_FIELD)
		// For now, we are not using this, so hide it from the CMS UI
		@ToolUi.Hidden
		private boolean showGuides = true;

		public boolean isShowGuides() {
			return showGuides;
		}

		public void setShowGuides(boolean showGuides) {
			this.showGuides = showGuides;
		}

	}

	/** Static utility methods. */
	public static final class Static {

		private Static() {
		}

		/*
		 * Get the main Production Guide
		 */
		public static Guide.PageProductionGuideModification getPageProductionGuide(
				Page content) {
			if (content != null) {
				Guide.PageProductionGuideModification guide = content
						.as(Guide.PageProductionGuideModification.class);
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
				Guide.PageProductionGuideModification guide = content
						.as(Guide.PageProductionGuideModification.class);
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
				Guide.PageProductionGuideModification guide = content
						.as(Guide.PageProductionGuideModification.class);
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
		 * Get the Production Guide for a template section
		 */
		public static Guide.SectionProductionGuideModification getSectionProductionGuide(
				Section section) {
			if (section != null) {
				Guide.SectionProductionGuideModification guide = section
						.as(Guide.SectionProductionGuideModification.class);
				if (guide != null) {
					return guide;
				}
			}
			return null;

		}

		/*
		 * Get the Production Guide description for a template section
		 */
		public static ReferentialText getSectionDescription(Section section) {
			if (section != null) {
				Guide.SectionProductionGuideModification guide = section
						.as(Guide.SectionProductionGuideModification.class);
				if (guide != null) {
					return guide.getDescription();
				}
			}
			return null;

		}

		/*
		 * Get the Production Guide tips for a template section
		 */
		public static ReferentialText getSectionTips(Section section) {
			if (section != null) {
				Guide.SectionProductionGuideModification guide = section
						.as(Guide.SectionProductionGuideModification.class);
				if (guide != null) {
					return guide.getTips();
				}
			}
			return null;

		}


		/*
		 * Get the flag per user which dictates whether to automatically show
		 * production guides
		 */
		public static boolean showByDefault(ToolUser user) {

			ProductionGuideSettingsModification userMod = user
					.as(ProductionGuideSettingsModification.class);
			if (userMod != null) {
				return userMod.isShowGuides();
			} else {
				return false;
			}

		}

		/*
		 * Set the flag per user which dictates whether to automatically show
		 * production guides
		 */
		public static void setShowByDefault(ToolUser user, boolean toShow) {

			ProductionGuideSettingsModification userMod = user
					.as(ProductionGuideSettingsModification.class);
			if (userMod != null) {
				userMod.setShowGuides(toShow);
			}
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