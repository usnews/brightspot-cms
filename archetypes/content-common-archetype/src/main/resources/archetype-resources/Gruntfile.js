module.exports = function(grunt) {
    require('bsp-grunt')(grunt, {
        bsp: {
            styles: {
                dir: "assets/style",
                less: "${artifactId}.less"
            },

            scripts: {
                dir: "assets/script",
                rjsModules: [
                    {
                        name: '${artifactId}'
                    }
                ]
            },

            bower: {
                less: [{src: 'dist/less-1.7.0.js', dest: 'less.js'}]
            }
        }
    });
};
