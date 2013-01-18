package com.psddev.cms.db;


import com.psddev.dari.db.Database;
import com.psddev.dari.db.ObjectField;
import com.psddev.dari.db.ObjectType;
import com.psddev.dari.db.Record;
import com.psddev.dari.db.Query;
import com.psddev.dari.db.ReferentialText;
import com.psddev.dari.db.State;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * Production Guide class to hold information about Content types and their associated fields
 * 
 * ObjectType and ObjectField don't currently lend themselves well to Modification classes or else this 
 * (and the other Guide<xxx> classes) would have been implemented as a modification class
 */
@Record.LabelFields({"documentedType/name"})
public class GuideType extends Record {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(GuideType.class);

	@ToolUi.Note("Content type for Production Guide information")
	@Required
	@Indexed
	ObjectType documentedType;

	@ToolUi.Note("Production Guide information about this content type")
	@ToolUi.Hidden
	// No plan for this yet - may not be needed
	ReferentialText description;

	@ToolUi.Note("Production Guide field descriptions for this content type (Note that any fields within an embedded type should be defined separately in a Guide for the embedded type)")
	private List<GuideField> fieldDescriptions;

	public ObjectType getDocumentedType() {
		return documentedType;
	}

	public void setDocumentedType(ObjectType documentedType) {
		this.documentedType = documentedType;
	}

	public ReferentialText getDescription() {
		return description;
	}

	public void setDescription(ReferentialText description) {
		this.description = description;
	}

	public List<GuideField> getFieldDescriptions() {
		return fieldDescriptions;
	}

	/*
	 * Add a field description entry. Assumes that entry doesn't already exist.
	 */
	public void addFieldDescription(GuideField fieldDescription) {
		if (fieldDescriptions == null) {
			fieldDescriptions = new ArrayList<GuideField>();
		}
		fieldDescriptions.add(fieldDescription);
	}

	public void setFieldDescriptions(List<GuideField> fieldDescriptions) {
		this.fieldDescriptions = fieldDescriptions;
	}

	public ReferentialText getFieldDescription(String fieldName,
			boolean createIfMissing) {
		ReferentialText desc = null;
		if (fieldDescriptions != null) {
			for (GuideField gf : fieldDescriptions) {
				if (gf.getFieldName().equals(fieldName)) {
					return gf.getDescription();
				}
			}
		}
		if (createIfMissing == true) {
			GuideField gf = new GuideField();
			gf.fieldName = fieldName;
			// TODO - pull description from annotation?
			addFieldDescription(gf);
		}
		return desc;
	}
	
	public GuideField getGuideField(String fieldName) {
		if (fieldDescriptions != null) {
			for (GuideField gf : fieldDescriptions) {
				if (gf.getFieldName().equals(fieldName)) {
					return gf;
				}
			}
		}		
		return null;
	}

	public void setFieldDescription(String fieldName,
			ReferentialText description, boolean annotation) {
		ReferentialText desc = null;
		if (fieldDescriptions != null) {
			for (GuideField gf : fieldDescriptions) {
				if (gf.getFieldName().equals(fieldName)) {
					gf.setDescription(description);
					gf.setFromAnnotation(annotation);
					return;
				}
			}
		}
		// if didn't already exist
		GuideField gf = new GuideField();
		gf.fieldName = fieldName;
		gf.description = description;
		gf.setFromAnnotation(annotation);
		addFieldDescription(gf);

	}

	public void generateFieldDescriptionList() {
		// Create an entry for each field
		ObjectType type = getDocumentedType();
		if (type != null) {
			List<ObjectField> fields = type.getFields();
			for (ObjectField field : fields) {
				getFieldDescription(field.getInternalName(), true);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.psddev.dari.db.Record#beforeSave()
	 */
	public void beforeSave() {
		generateFieldDescriptionList();
	}

	@Record.Embedded
	@Record.LabelFields({ "fieldName", "description" })
	public static class GuideField extends Record {

		@Required
		@Indexed
		@ToolUi.Note("Internal fieldname in this object type")
		String fieldName;

		@ToolUi.Note("Production Guide information about this field")
		ReferentialText description;
		
		// True if the description was populated from an annotation (if false, annotations are ignored)
		@ToolUi.Hidden
		boolean fromAnnotation;

		public String getFieldName() {
			return fieldName;
		}

		public void setFieldName(String fieldName) {
			this.fieldName = fieldName;
		}

		public ReferentialText getDescription() {
			return description;
		}

		public void setDescription(ReferentialText description) {
			this.description = description;
		}

		public boolean isFromAnnotation() {
			return fromAnnotation;
		}

		public void setFromAnnotation(boolean fromAnnotation) {
			this.fromAnnotation = fromAnnotation;
		}		

	}

	public static final class Static {

		public static ReferentialText getFieldDescription(State state,
				String fieldName) {
			if (state != null) {
				ObjectType typeDefinition = state.getType();
				GuideType guide = getGuideType(typeDefinition);
				if (guide != null) {
					return guide.getFieldDescription(fieldName, false);
				}
			}
			return null;
		}

		public static boolean hasFieldGuideInfo(State state, String fieldName) {
			ObjectField field = state.getField(fieldName);
			if (field.isRequired())
				return true;
			if (field.getMaximum() != null)
				return true;
			if (field.getMinimum() != null)
				return true;
			if (field.getDefaultValue() != null)
				return true;
			ReferentialText desc = getFieldDescription(state, fieldName);
			if (desc != null && !desc.isEmpty())
				return true;

			return false;
		}

		public static GuideType getGuideType(ObjectType objectType) {
			return Query.from(GuideType.class)
					.where("documentedType = ?", objectType).first();
		}
		
		public static synchronized GuideType findOrCreateGuide(ObjectField field) {
			GuideType guide = Query.from(GuideType.class)
					.where("documentedType = ?", field.getParentType()).first();
			if (guide == null) {
				Database db = Database.Static.getDefault();
				guide = new GuideType();
				guide.setDocumentedType(field.getParentType());
				guide.save();
				db.commitWrites();
			}	
			return guide;
		}

		public static synchronized void setDescription(ObjectField field, ReferentialText descText) {
			Database db = Database.Static.getDefault();
			GuideType guide = Static.findOrCreateGuide(field) ;
			guide.setFieldDescription(field.getInternalName(), descText, true);
			guide.save();
			db.commitWrites();
		}
	}

	/* Annotation processors */

	/** Specifies Production description content for field */
	@Documented
	@Inherited
	@ObjectField.AnnotationProcessorClass(DescriptionProcessor.class)
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.FIELD })
	public @interface Description {
		String value() default "";
	}

	private static class DescriptionProcessor implements
			ObjectField.AnnotationProcessor<Description> {

		@Override
		public void process(ObjectType type, ObjectField field,
				Description annotation) {
			// field.as(ToolUi.class).setHidden(annotation.value());
			// find/create a GuideType object for the fields type
			// add the description
			GuideType guide = Static.findOrCreateGuide(field);
			GuideField gf = guide.getGuideField(field.getInternalName());
			ReferentialText currentDesc = gf.getDescription();
			if (currentDesc == null || currentDesc.isEmpty() || gf.isFromAnnotation() == true ) {
				ReferentialText descText = new ReferentialText();
				descText.add(annotation.value());
				if (!descText.equals(currentDesc)) {
					Static.setDescription(field, descText);
				}
			}

		}

	}
	
	

}