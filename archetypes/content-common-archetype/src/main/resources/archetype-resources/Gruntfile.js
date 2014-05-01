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
            }
        }
    });
};
