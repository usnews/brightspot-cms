package ${groupId};

import com.psddev.cms.db.Directory;
import com.psddev.cms.db.ToolUi;
import com.psddev.dari.db.Modification;
import com.psddev.dari.db.ObjectIndex;
import com.psddev.dari.db.Recordable;

/**
 * Classes implementing this interface MUST call
 * {@link Data#appendSequence(String) appendSequence(String)} from within their
 * implementation of
 * {@link Directory.Item#createPermalink(String) createPermalink(String)} to
 * ensure that the correct sequence ID is appended to the permalink on save.
 */
public interface SequentialDirectoryItem extends Directory.Item, Recordable {

    public static final class Data extends Modification<SequentialDirectoryItem> {

        @ToolUi.Hidden
        private Integer directoryItemIndex;

        @Override
        protected boolean onDuplicate(ObjectIndex index) {

            if (index != null) {
                String field = index.getField();

                if (Directory.PATHS_FIELD.equals(field)) {

                    if (directoryItemIndex == null) {
                        directoryItemIndex = 1;

                    } else {
                        directoryItemIndex++;
                    }

                    Directory.ObjectModification dirMod = as(Directory.ObjectModification.class);

                    dirMod.clearPaths();

                    for (Directory.Path path : dirMod.createPaths(null)) {
                        dirMod.addSitePath(path.getSite(), path.getPath(), path.getType());
                    }

                    return true;
                }
            }

            return false;
        }

        /**
         * Appends a sequence ID to the permalink if a duplicate permalink
         * was detected during a save.
         *
         * @param permalink The path to modify.
         * @return a new permalink with a sequence ID appended where appropriate.
         */
        public String appendSequence(String permalink) {

            if (directoryItemIndex != null && directoryItemIndex > 0) {
                permalink += "_" + String.valueOf(directoryItemIndex);
            }

            return permalink;
        }
    }
}
