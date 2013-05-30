package com.psddev.cms.db;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.psddev.dari.db.Database;
import com.psddev.dari.db.Query;
import com.psddev.dari.db.Recordable;
import com.psddev.dari.util.PeriodicValue;
import com.psddev.dari.util.PullThroughCache;

public interface Taxon extends Recordable {

    public boolean isRoot();

    public Collection<? extends Taxon> getChildren();

    /** {@link Taxon} utility methods. */
    public static final class Static {

        private static final PullThroughCache<Class<? extends Taxon>, PeriodicValue<List<? extends Taxon>>>
                TAXA_BY_CLASS = new PullThroughCache<Class<? extends Taxon>, PeriodicValue<List<? extends Taxon>>>() {

            @Override
            protected PeriodicValue<List<? extends Taxon>> produce(final Class<? extends Taxon> taxonClass) {
                return new PeriodicValue<List<? extends Taxon>>() {

                    @Override
                    protected List<? extends Taxon> update() {
                        Query<? extends Taxon> query = Query.from(taxonClass).using(Database.Static.getDefaultOriginal());
                        Date cacheUpdate = getUpdateDate();
                        Date databaseUpdate = query.lastUpdate();

                        if (databaseUpdate == null || (cacheUpdate != null && !databaseUpdate.after(cacheUpdate))) {
                            List<? extends Taxon> taxa = get();

                            return taxa != null ? taxa : Collections.<Taxon>emptyList();
                        }

                        return query.selectAll();
                    }
                };
            }
        };

        @SuppressWarnings("unchecked")
        public static <T extends Taxon> List<T> getRoots(Class<T> taxonClass) {
            List<T> roots = new ArrayList<T>();
            
            for (Taxon t : TAXA_BY_CLASS.get(taxonClass).get()) {
                if (t.isRoot()) {
                    roots.add((T) t);
                }
            }

            return roots;
        }
    }
}
