---
layout: default
title: Caching
id: caching
---

## Caching

When using the CMS [Template Tool](brightspot-cms/template-tool.html) to build pages, every section created has the ability to be cached. The user interface (see screen grab below) contains a field for Cache Duration in Milliseconds. Adding a timespan here is essentially leveraging the `cms:cache` tag, which can wrap around sections within your jsp files to cache content.

`<cms:cache name="${}" duration="60000"> </cms:cache>`

![](http://docs.brightspot.s3.amazonaws.com/cache-duration.png)


### Dari Utils Caching

Dari provides a number of utility classes which may be used to cache any content (although typically from 3rd party providers) which generally is time-consuming to refresh and/or doesn't change frequently.

### PullThroughValue

**Used to cache a single value.**

Each time `cache.get()` is called, `isExpired()` is called to determine if the cache has expired. If `isExpired()` returns true a call is made to `produce()` to refresh the cache.


    public class PullThroughValueExample {
        
        private final static Logger logger =    
            LoggerFactory.getLogger(PullThroughValueExample.class);        
        
        private final static long CACHE_EXPIRY = 5 * 60 * 1000;   //  Refresh every 5 minutes

        private static PullThroughValue<String> cache = 
                new PullThroughValue<String>() {
                   
            @Override
            protected String produce() {                
                // Insert your code here to fetch and return new content for the cache
                
                return new String();
            }

            @Override
            protected boolean isExpired(Date lastProduceDate) {                
                // Check if the cache has expired, return true if it has, otherwise false
                
                long currentTime = System.currentTimeMillis();
                if ((currentTime - lastProduceDate.getTime()) > CACHE_EXPIRY) {
                    logger.debug("Cache expired -  refreshing cache");
                    return true;
                }
                return false;
            }
        };

        
        /**
         * Returns the latest value from the cache
         * @return 
         */
        public static String getLatestValue() {
            return cache.get();
        }
    }




### PullThroughCache

**Used to cache key, value pairs.**

Each time `cache.get(key)` is called, `isExpired()` is called to determine if the cache has expired for this key. If `isExpired()` returns true a call is made to `produce()` to refresh the cache for this key.


    public class PullThroughCacheExample {

        private final static Logger logger =
                LoggerFactory.getLogger(PullThroughCacheExample.class);

        private final static long CACHE_EXPIRY = 6 * 60 * 60 * 1000; // Refresh every 6 hours

        private static PullThroughCache<String, CachedObject> cache =
            new PullThroughCache<String, CachedObject>() {
                @Override
                public CachedObject produce(String key) throws Exception{
                    // Insert your code here to fetch and return
                    // new content for the specified key in the cache

                    return new CachedObject();
                }

                @Override
                protected boolean isExpired(String key, Date lastProduceDate) {
                    // Check if the cache has expired, return true if it has, otherwise false

                    long currentTime = System.currentTimeMillis();
                    if ((currentTime - lastProduceDate.getTime()) > CACHE_EXPIRY) {
                        logger.debug("Cache expired for [" + key + "] - refreshing cache");
                        return true;
                    }
                    return false;
                }
        };

        
        /**
         * Object to cache
         */   
        public static class CachedObject {
        }

        /**
         * Returns the CachedObject from the cache for the given key
         * 
         * @return 
         */
        public static CachedObject getValue(String key) {
            return cache.get(key);
        }
    }



### PeriodicCache

**Used to cache key value pairs and is updated at a specified interval.**

For periodic cache, `update()` is called at the specified interval to refresh the cache


    public class PeriodicCacheExample {

        private final static Logger logger =
                LoggerFactory.getLogger(PeriodicCacheExample.class);
        
       private static final double CACHE_REFRESH_INTERVAL = 3600.0;  // Refresh every hour

       private static PeriodicCache<Location, WeatherInformation> cache = 
            new PeriodicCache<Location, WeatherInformation>(CACHE_REFRESH_INTERVAL) {
            
            @Override
            protected Map<Location, WeatherInformation> update(){                
                // Insert your code here to fetch and return a new map which is
                // used to replace the cache
                
                return new HashMap<Location, WeatherInformation>();                
            }            
        };

       
        /**
         * Key to use when accessing cache
         */   
        public static class Location {
            String name;
        }
        
        /**
         * Object to cache
         */   
        public static class WeatherInformation {
            int temperature;
            String outlook;              
        }
        

        /**
         * Returns the CachedObject from the cache for the given key
         * 
         * @return 
         */
        public static WeatherInformation getValue(Location key) {
            return cache.get(key);
        }
    }