/** Map plugin for Location fields. */
(function($, win, undef) {

var doc = win.document,
        $win = $(win);

$.plugin2('locationMap', {
    '_create': function(locationMap) {
        var plugin = this;

        var map = L.map(locationMap);
        new L.TileLayer.MapQuestOpenOSM().addTo(map);

        var latInput = $(locationMap).find(".locationMapLatitude");
        var longInput = $(locationMap).find(".locationMapLongitude");
        var zoomInput = $(locationMap).find(".locationMapZoom");
        var geoJsonInput = $(locationMap).find(".locationMapGeoJson");

        var lat = $(latInput).val();
        var lng = $(longInput).val();
        var zoom = $(zoomInput).val();
        var geojson = $.parseJSON($(geoJsonInput).val());
        var showMarker = true;

        if ((!lat || lat === '' || lat.length === 0) ||
            (!lng || lng === '' || lng.length === 0)) {
            lat = 39.8282;
            lng = -98.5795;
            zoom = 4;
            showMarker = false;
        }

        map.setView([lat, lng], zoom);

        map.on('zoomend', function(e) {
            zoomInput.val(map.getZoom());
        });

        if (geojson) {
            var myStyle = {
                "color": "#3a87ad",
                "opacity": 1,
                "weight": 4,
                "fillColor": "#3a87ad",
                "fillOpacity": 0.5
            };

            var drawnItems = new L.FeatureGroup();
            map.addLayer(drawnItems);
            
            var savedItems = L.GeoJSON.geometryToLayer(geojson, myStyle);
            for (x in savedItems.getLayers()) {
                var layer = savedItems.getLayers()[x];

                if (layer instanceof L.MultiPolygon) {
                    // Convert to individual polygons.
                    var latlngs = layer.getLatLngs();
                    for (x in latlngs) {
                        var polygon = new L.Polygon(latlngs[x], { shapeOptions: myStyle });
                        drawnItems.addLayer(polygon);
                    }
                } else {
                    layer.setStyle(myStyle);
                    drawnItems.addLayer(layer);
                }
            }
            drawnItems.setStyle(myStyle);

            var drawControl = new L.Control.Draw({
                draw: {
                    polyline:  false,
                    rectangle: false,
                    marker:    false,
                    polygon:   { allowIntersection: false, shapeOptions: myStyle },
                    circle:    { shapeOptions: myStyle }
                },
                edit: {
                    featureGroup: drawnItems,
                    remove: true
                }
            });
            map.addControl(drawControl);

            map.on('draw:created', function (e) {
                var type = e.layerType,
                    layer = e.layer;

                drawnItems.addLayer(layer);

                // Extract the MuliPolyon feature.
                var geojson = drawnItems.toGeoJSON();
                geoJsonInput.val(JSON.stringify(geojson));
            });

            map.on('draw:edited', function (e) {
                // Extract the MuliPolyon feature.
                var geojson = drawnItems.toGeoJSON();
                geoJsonInput.val(JSON.stringify(geojson));
            });

            map.on('draw:deleted', function (e) {
                // Extract the MuliPolyon feature.
                var geojson = drawnItems.toGeoJSON();
                geoJsonInput.val(JSON.stringify(geojson));
            });
        } else {
            var marker = null;
            if (showMarker) {
                marker = L.marker([lat, lng], { draggable: true }).addTo(map);
                marker.on('dragend', function(e) {
                    plugin.updateLatLng(e.target, map, latInput, longInput, zoomInput);
                });
            }

            new L.Control.GeoSearch({
                zoomLevel: 16,
                provider: new L.GeoSearch.Provider.OpenStreetMap(),
                success: function(m) {
                    if (marker !== null && m !== marker) {
                        map.removeLayer(marker);
                    }

                    marker = m;
                    marker.dragging.enable();

                    marker.on('dragend', function(e) {
                        plugin.updateLatLng(e.target, map, latInput, longInput, zoomInput);
                    });

                    plugin.updateLatLng(marker, map, latInput, longInput, zoomInput);
                }
            }).addTo(map);
        }
    },

    'updateLatLng' : function(marker, map, latInput, longInput, zoomInput) {
        var latlong = marker.getLatLng();
        latInput.val(latlong.lat);
        longInput.val(latlong.lng);
        zoomInput.val(map.getZoom());
    }
});

}(jQuery, window));

