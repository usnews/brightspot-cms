define([
    'leaflet',
    'leaflet.common',
    'leaflet.draw',
    'l.control.geosearch',
    'l.geosearch.provider.openstreetmap' ],

function() {
    L.Icon.Default.imagePath = CONTEXT_PATH + '/style/leaflet/images';

    return L;
});
