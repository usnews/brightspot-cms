module.exports = function(grunt) {
    require('bsp-grunt')(grunt, {
        bsp: {
            bower: {
                'codemirror': [
                    {
                        dest: 'codemirror',
                        expand: true,
                        src: 'lib/**'
                    },

                    {
                        dest: 'codemirror',
                        expand: true,
                        src: [
                            'mode/clike/clike.js',
                            'mode/css/css.js',
                            'mode/htmlembedded/htmlembedded.js',
                            'mode/htmlmixed/htmlmixed.js',
                            'mode/javascript/javascript.js',
                            'mode/xml/xml.js'
                        ]
                    }
                ],

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
