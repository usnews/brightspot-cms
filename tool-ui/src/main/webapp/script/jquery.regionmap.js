/** Map plugin for Location fields. */
(function($, win, undef) {

var doc = win.document,
        $win = $(win);

$.plugin2('regionMap', {
    '_create': function(locationMap) {
        var plugin = this;

        var map = L.map(locationMap);
        new L.TileLayer.MapQuestOpenOSM().addTo(map);

        var zoomInput = $(locationMap).find(".regionMapZoom");
        var geoJsonInput = $(locationMap).find(".regionMapGeoJson");

        var zoom = $(zoomInput).val();
        var geojson = $.parseJSON($(geoJsonInput).val());

        var myStyle = {
            "color": "#3a87ad",
            "opacity": 1,
            "weight": 4,
            "fillColor": "#3a87ad",
            "fillOpacity": 0.5
        };

        var lat = 39.8282;
        var lng = -98.5795;

        map.setView([lat, lng], zoom);

        var regionLayer = new L.FeatureGroup();
        map.addLayer(regionLayer);
        
        var savedItems = L.GeoJSON.geometryToLayer(geojson, myStyle);
        for (x in savedItems.getLayers()) {
            var layer = savedItems.getLayers()[x];

            if (layer instanceof L.MultiPolygon) {
                // Convert to individual polygons because leaflet.draw does
                // not understand MultiPolygon.
                var latlngs = layer.getLatLngs();
                for (x in latlngs) {
                    var polygon = new L.Polygon(latlngs[x], { shapeOptions: myStyle });
                    regionLayer.addLayer(polygon);
                }
            } else {
                layer.setStyle(myStyle);
                regionLayer.addLayer(layer);
            }
        }
        regionLayer.setStyle(myStyle);

        var drawControl = new L.Control.Draw({
            draw: {
                polyline:  false,
                rectangle: false,
                marker:    false,
                polygon:   { allowIntersection: false, shapeOptions: myStyle },
                circle:    { shapeOptions: myStyle }
            },
            edit: {
                featureGroup: regionLayer,
                remove: true
            }
        });
        map.addControl(drawControl);

        // Bind map events.
        
        var updateFunc = function (e) {
            var geojson = regionLayer.toGeoJSON();
            geoJsonInput.val(JSON.stringify(geojson));
        };

        map.on('zoomend', function(e) {
            zoomInput.val(map.getZoom());
        });

        map.on('draw:created', function (e) {
            regionLayer.addLayer(e.layer);
            updateFunc(e);
        });

        map.on('draw:edited', updateFunc);
        map.on('draw:deleted', updateFunc);
    }
});

}(jQuery, window));


