/**
 * L.Control.GeoSearch - search for an address and zoom to it's location
 * L.GeoSearch.Provider.OpenStreetMap uses openstreetmap geocoding service
 * https://github.com/smeijer/leaflet.control.geosearch
 */

L.GeoSearch.Provider.OpenStreetMap = L.Class.extend({
    options: {

    },

    initialize: function(options) {
        options = L.Util.setOptions(this, options);
    },

    GetServiceUrl: function (qry) {
        var parameters = L.Util.extend({
            q: qry,
            format: 'json'
        }, this.options);

        return 'http://nominatim.openstreetmap.org/search'
            + L.Util.getParamString(parameters);
    },

    ParseJSON: function (data) {
        if (data.length == 0) {
            return [];
        }

        // - If single result, use it.
        // - If multiple results find "city", otherwise pick first.
        var klass = data[0]['class'];
        var isCity = 
            data[0].type === 'city' ||
            data[0].type === 'suburb' ||
            klass === 'city';

        var result = new L.GeoSearch.Result(
                data[0].lon, 
                data[0].lat, 
                data[0].display_name,
                isCity ? 10 : 16
        );

        for (i in data) {
            var klass = data[i]['class'];
            var isCity = 
                data[i].type === 'city' ||
                data[i].type === 'suburb' ||
                klass === 'city';

            if (!isCity) {
                continue;
            }

            result = new L.GeoSearch.Result(
                data[i].lon, 
                data[i].lat, 
                data[i].display_name,
                10
            );
        }
        
        return result;
    }
});
