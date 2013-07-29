/*
 * L.Control.GeoSearch - search for an address and zoom to it's location
 * https://github.com/smeijer/leaflet.control.geosearch
 */

L.GeoSearch = {};
L.GeoSearch.Provider = {};

// MSIE needs cors support
jQuery.support.cors = true;

L.GeoSearch.Result = function (x, y, label, zoom) {
    this.X = x;
    this.Y = y;
    this.Label = label;
    this.Zoom = zoom;
};

L.Control.GeoSearch = L.Control.extend({
    options: {
        position: 'topcenter'
    },

    initialize: function (options) {
        this._config = {};
        this.setConfig(options);
    },

    setConfig: function (options) {
        this._config = {
            'country': options.country || '',
            'provider': options.provider,
            'success': options.success,
            
            'searchLabel': options.searchLabel || 'Enter Address',
            'notFoundMessage' : options.notFoundMessage || 'Sorry, that address could not be found.',
            'messageHideDelay': options.messageHideDelay || 3000,
            'zoomLevel': options.zoomLevel || 13,
            'markerAdded': null
        };
    },

    onAdd: function (map) {
        var $controlContainer = $(map._controlContainer);

        if ($controlContainer.children('.leaflet-top.leaflet-center').length == 0) {
            $controlContainer.append('<div class="leaflet-top leaflet-center"></div>');
            map._controlCorners.topcenter = $controlContainer.children('.leaflet-top.leaflet-center').first()[0];
        }

        this._map = map;
        this._container = L.DomUtil.create('div', 'leaflet-control-geosearch');

        var searchbox = document.createElement('input');
        searchbox.id = 'leaflet-control-geosearch-qry';
        searchbox.type = 'text';
        searchbox.placeholder = this._config.searchLabel;
        this._searchbox = searchbox;

        var msgbox = document.createElement('div');
        msgbox.id = 'leaflet-control-geosearch-msg';
        msgbox.className = 'leaflet-control-geosearch-msg';
        this._msgbox = msgbox;

        var resultslist = document.createElement('ul');
        resultslist.id = 'leaflet-control-geosearch-results';
        this._resultslist = resultslist;

        $(this._msgbox).append(this._resultslist);
        $(this._container).append(this._searchbox, this._button, this._msgbox);

        L.DomEvent.
            addListener(this._container, 'click', L.DomEvent.stop).
            addListener(this._searchbox, 'keypress', this._onKeyUp, this);

        L.DomEvent.disableClickPropagation(this._container);

        return this._container;
    },
    
    geosearch: function (qry) {
        try {
            var provider = this._config.provider;

            if(typeof provider.GetLocations == 'function') {
                var results = provider.GetLocations(qry, function(results) {
                    this._showLocation(results[0].X, results[0].Y, results[0].Zoom);
                }.bind(this));
            }
            else {
                var url = provider.GetServiceUrl(qry);

                $.getJSON(url, function (data) {
                    try {
                        var result = provider.ParseJSON(data);
                        if (result === null)
                            throw this._config.notFoundMessage;

                        this._showLocation(result.X, result.Y, result.Zoom);
                    } catch (error) {
                        this._printError(error);
                    }
                }.bind(this));
            }
        }
        catch (error) {
            this._printError(error);
        }
    },
    
    _showLocation: function (x, y, zoom) {
        if (typeof this._positionMarker === 'undefined')
            this._positionMarker = L.marker([y, x]).addTo(this._map);
        else
            this._positionMarker.setLatLng([y, x]);

        this._map.setView([y, x], zoom === 'undefined' ? this._config.zoomLevel : zoom, false);

        var success = this._config.success;
        if (success !== null) {
            success(this._positionMarker);
        }
    },

    _printError: function(message) {
        $(this._resultslist)
            .html('<li>'+message+'</li>')
            .fadeIn('slow').delay(this._config.messageHideDelay).fadeOut('slow',
                    function () { $(this).html(''); });
    },

    _onKeyUp: function (e) {
        var escapeKey = 27;
        var enterKey = 13;

        if (e.keyCode === escapeKey) {
            $(e.target).val('');
            $(this._map._container).focus();
        } else if (e.keyCode === enterKey) {
            this.geosearch($(e.target).val());
            e.preventDefault();
        }
    }
});
