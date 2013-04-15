package com.psddev.cms.db;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

import com.psddev.dari.db.Database;
import com.psddev.dari.db.Modification;
import com.psddev.dari.db.ObjectType;
import com.psddev.dari.db.Query;
import com.psddev.dari.db.Record;
import com.psddev.dari.db.State;
import com.psddev.dari.util.ErrorUtils;
import com.psddev.dari.util.ObjectUtils;

/** Represents a generic content. */
@Content.Searchable
public abstract class Content extends Record {

    private static final String PREFIX = "cms.content.";

    public static final String PUBLISH_DATE_FIELD = PREFIX + "publishDate";
    public static final String PUBLISH_USER_FIELD = PREFIX + "publishUser";
    public static final String UPDATE_DATE_FIELD = PREFIX + "updateDate";
    public static final String UPDATE_USER_FIELD = PREFIX  + "updateUser";

    public static final String SEARCHABLE_GROUP = PREFIX + "searchable";

    /** Returns the best permalink for this {@code object}. */
    public String getPermalink() {
        return as(Directory.ObjectModification.class).getPermalink();
    }

    /** Returns the date when this object was published. */
    public Date getPublishDate() {
        return as(ObjectModification.class).getPublishDate();
    }

    /** Returns the tool user that published this object. */
    public ToolUser getPublishUser() {
        return as(ObjectModification.class).getPublishUser();
    }

    /** Returns the date when this object was last updated. */
    public Date getUpdateDate() {
        return as(ObjectModification.class).getUpdateDate();
    }

    /** Returns the tool user that last updated this object. */
    public ToolUser getUpdateUser() {
        return as(ObjectModification.class).getUpdateUser();
    }

    /** Modification that adds CMS content information. */
    public static final class ObjectModification extends Modification<Object> {

        private ObjectModification() {
        }

        @Indexed(visibility = true)
        @InternalName("cms.content.status")
        private String status;

        private @Indexed @InternalName(PUBLISH_DATE_FIELD) Date publishDate;
        private @Indexed @InternalName(PUBLISH_USER_FIELD) ToolUser publishUser;
        private @Indexed @InternalName(UPDATE_DATE_FIELD) Date updateDate;
        private @Indexed @InternalName(UPDATE_USER_FIELD) ToolUser updateUser;

        /**
         * Returns the current content status.
         *
         * @return {@code null} if published and available publically.
         */
        public String getStatus() {
            if (status != null) {
                ContentStatus contentStatus = ObjectUtils.to(ContentStatus.class, status);

                if (contentStatus != null) {
                    return contentStatus.toString();
                }

                DraftStatus draftStatus = Query.from(DraftStatus.class).where("_id = ?", status).first();

                if (draftStatus != null) {
                    return draftStatus.getLabel();
                }
            }

            return null;
        }

        /**
         * Returns the unique ID associated with the current content status.
         *
         * @return {@code null} if published and available publically.
         */
        public String getStatusId() {
            if (status != null) {
                ContentStatus contentStatus = ObjectUtils.to(ContentStatus.class, status);

                if (contentStatus != null) {
                    return contentStatus.name();
                }

                DraftStatus draftStatus = Query.from(DraftStatus.class).where("_id = ?", status).first();

                if (draftStatus != null) {
                    return draftStatus.getId().toString();
                }
            }

            return null;
        }

        /**
         * Sets the status to published so that the content is available
         * publically.
         */
        public void setStatusPublished() {
            this.status = null;
        }

        /**
         * Sets the status to one of the standard content statuses.
         *
         * @param status Can't be {@code null}.
         */
        public void setStatus(ContentStatus status) {
            ErrorUtils.errorIfNull(status, "status");
            this.status = status.name();
        }

        /**
         * Sets the status to one of the draft statuses.
         *
         * @param status Can't be {@code null}.
         */
        public void setStatus(DraftStatus status) {
            ErrorUtils.errorIfNull(status, "status");
            this.status = status.getId().toString();
        }

        /** Returns the date when the given {@code object} was published. */
        public Date getPublishDate() {
            return publishDate;
        }

        /** Sets the date when the given {@code object} was published. */
        public void setPublishDate(Date publishDate) {
            this.publishDate = publishDate;
        }

        /** Returns the tool user that published the given {@code object}. */
        public ToolUser getPublishUser() {
            return publishUser;
        }

        /** Sets the tool user that published the given {@code object}. */
        public void setPublishUser(ToolUser publishUser) {
            this.publishUser = publishUser;
        }

        /** Returns the date when the given {@code object} was last updated. */
        public Date getUpdateDate() {
            return updateDate;
        }

        /** Sets the date when the given {@code object} was last updated. */
        public void setUpdateDate(Date updateDate) {
            this.updateDate = updateDate;
        }

        /** Returns the tool user that last updated the given {@code object}. */
        public ToolUser getUpdateUser() {
            return updateUser;
        }

        /** Sets the tool user that last updated the given {@code object}. */
        public void setUpdateUser(ToolUser updateUser) {
            this.updateUser = updateUser;
        }
    }

    /** Static utility methods. */
    public static final class Static {

        private Static() {
        }

        /**
         * Deletes the given {@code object}, and returns a trash object
         * that can be used later to restore it.
         */
        public static Trash deleteSoftly(Object object, Site site, ToolUser user) {
            State objectState = State.getInstance(object);
            Site.ObjectModification objectSiteMod = objectState.as(Site.ObjectModification.class);

            if (site == null || ObjectUtils.equals(objectSiteMod.getOwner(), site)) {
                Trash trash = new Trash(user, object);
                try {
                    trash.beginWrites();
                    objectState.delete();
                    trash.save();
                    trash.commitWrites();
                    return trash;
                } finally {
                    trash.endWrites();
                }

            } else {
                objectSiteMod.getConsumers().remove(site);
                if (objectSiteMod.isGlobal()) {
                    objectSiteMod.getBlacklist().add(site);
                }
                objectState.save();
                return null;
            }
        }

        /**
         * Returns {@code true} if the instances of the given {@code type}
         * is searchable.
         */
        public static boolean isSearchableType(ObjectType type) {
            return type != null &&
                    type.getGroups().contains(SEARCHABLE_GROUP);
        }

        /** Returns {@code true} if the given {@code object} is searchable. */
        public static boolean isSearchable(Object object) {
            return object != null &&
                    isSearchableType(State.getInstance(object).getType());
        }

        /**
         * Publishes the given {@code object} in the given {@code site}
         * as the given {@code user}, and returns a history object that
         * can be used later to revert all changes.
         */
        public static History publish(Object object, Site site, ToolUser user) {
            State objectState = State.getInstance(object);
            ObjectModification objectContentMod = objectState.as(ObjectModification.class);
            Site.ObjectModification objectSiteMod = objectState.as(Site.ObjectModification.class);

            if (object instanceof Site) {
                site = (Site) object;
                objectSiteMod.setOwner(site);

            } else if (objectState.isNew() &&
                    objectSiteMod.getOwner() == null) {
                objectSiteMod.setOwner(site);
            }

            Date now = new Date();
            objectContentMod.setUpdateDate(now);
            Date publishDate = objectContentMod.getPublishDate();
            if (publishDate == null) {
                objectContentMod.setPublishDate(now);
            }

            objectContentMod.setUpdateUser(user);
            ToolUser publishUser = objectContentMod.getPublishUser();
            if (publishUser == null) {
                objectContentMod.setPublishUser(user);
            }

            History history = new History(user, object);
            try {
                history.beginWrites();
                objectState.save();
                history.save();
                history.commitWrites();
                return history;
            } finally {
                history.endWrites();
            }
        }

        /**
         * Purges the given {@code object} completely, including all of
         * its drafts, histories, and trashes.
         */
        public static void purge(Object object, Site site, ToolUser user) {
            State objectState = State.getInstance(object);
            Site.ObjectModification objectSiteMod = objectState.as(Site.ObjectModification.class);

            if (!ObjectUtils.equals(objectSiteMod.getOwner(), site)) {
                deleteSoftly(object, site, user);
                return;
            }

            Database database = objectState.getDatabase();
            UUID objectId = objectState.getId();

            try {
                database.beginWrites();
                database.deleteByQuery(Query.from(Draft.class).where("objectId = ?", objectId));
                database.deleteByQuery(Query.from(History.class).where("objectId = ?", objectId));
                database.deleteByQuery(Query.from(Trash.class).where("objectId = ?", objectId));
                objectState.delete();
                database.commitWrites();
            } finally {
                database.endWrites();
            }
        }
    }

    /**
     * Specifies whether the instances of the target type should be
     * searchable.
     */
    @Documented
    @Inherited
    @ObjectType.AnnotationProcessorClass(SearchableProcessor.class)
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface Searchable {
        boolean value() default true;
    }

    private static class SearchableProcessor implements ObjectType.AnnotationProcessor<Searchable> {
        @Override
        public void process(ObjectType type, Searchable annotation) {
            Set<String> groups = type.getGroups();
            if (annotation.value()) {
                groups.add(SEARCHABLE_GROUP);
            } else {
                groups.remove(SEARCHABLE_GROUP);
            }
        }
    }

    /** @deprecated Use {@link ObjectModification} or {@link Static} instead. */
    @Deprecated
    public static abstract class Global extends Record {

        /** @deprecated Use {@link ObjectModification#getPublishDate} instead. */
        @Deprecated
        public static Date getPublishDate(Object object) {
            return State.getInstance(object).as(ObjectModification.class).getPublishDate();
        }

        /** @deprecated Use {@link ObjectModification#getPublishUser} instead. */
        @Deprecated
        public static ToolUser getPublishUser(Object object) {
            return State.getInstance(object).as(ObjectModification.class).getPublishUser();
        }

        /** @deprecated Use {@link ObjectModification#getUpdateDate} instead. */
        @Deprecated
        public static Date getUpdateDate(Object object) {
            return State.getInstance(object).as(ObjectModification.class).getUpdateDate();
        }

        /** @deprecated Use {@link ObjectModification#getUpdateUser} instead. */
        @Deprecated
        public static ToolUser getUpdateUser(Object object) {
            return State.getInstance(object).as(ObjectModification.class).getUpdateUser();
        }

        /** @deprecated Use {@link Static#publish} instead. */
        @Deprecated
        public static History publish(ToolUser user, Object object) {
            return Static.publish(object, null, user);
        }

        /** @deprecated Use {@link Static#deleteSoftly} instead. */
        @Deprecated
        public static Trash deleteSoftly(ToolUser user, Object object) {
            return Static.deleteSoftly(object, null, user);
        }

        /** @deprecated Use {@link Static#purge} instead. */
        @Deprecated
        public static void purge(ToolUser user, Object object) {
            Static.purge(object, null, user);
        }

        /** @deprecated Use {@link Static#isSearchableType} instead. */
        @Deprecated
        public static boolean isSearchableType(ObjectType type) {
            return Static.isSearchableType(type);
        }

        /** @deprecated Use {@link Static#isSearchable} instead. */
        @Deprecated
        public static boolean isSearchable(Object object) {
            return Static.isSearchable(object);
        }
    }
}
