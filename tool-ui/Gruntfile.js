module.exports = function(grunt) {
    require('bsp-grunt')(grunt, {
        bsp: {
            bower: {
                'jsdiff': [
                    'diff.js'
                ],

                'leaflet-dist': [
                    {
                        dest: 'leaflet',
                        expand: true,
                        src: [
                            '*.css',
                            'images/**'
                        ]
                    },

                    {
                        dest: 'leaflet.js',
                        src: 'leaflet-src.js'
                    }
                ],

                'leaflet.draw': [
                    {
                        cwd: 'dist',
                        dest: 'leaflet',
                        expand: true,
                        src: [
                            '*.css',
                            'images/**'
                        ]
                    },

                    {
                        dest: 'leaflet.draw.js',
                        src: 'dist/leaflet.draw-src.js'
                    }
                ],

                'L.GeoSearch': [
                    {
                        cwd: 'src/css',
                        dest: 'leaflet',
                        expand: true,
                        src: '*.css'
                    },

                    {
                        cwd: 'src/js',
                        expand: true,
                        src: '**/*.js'
                    }
                ]
            },

            styles: {
                dir: 'style',
                less: 'cms.less'
            },

            scripts: {
                dir: 'script',
                rjsModules: [
                    {
                        name: 'cms'
                    }
                ]
            }
        }
    });
};
