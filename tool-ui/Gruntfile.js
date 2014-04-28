module.exports = function(grunt) {
    require('bsp-grunt')(grunt, {
        bsp: {
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
