requirejs.config({
    shim: {
        'leaflet.common': [ 'leaflet' ],
        'leaflet.draw': [ 'leaflet' ],
        'l.control.geosearch': [ 'leaflet' ],
        'l.geosearch.provider.openstreetmap': [ 'l.control.geosearch' ]
    }
});

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
