define(['jquery', 'v3/input/richtextCodeMirror'], function($, CodeMirrorRte) {
    
    /**
     * @class
     * Rich text editor.
     * Uses the CodeMirrorRte to provide editing interface, but
     * this object provides the following functionality:
     *  - defines which styles are supported
     *  - provides a toolbar
     *  - sets up keyboard shortcuts
     *  - provides an interface for enhancements
     *  - provides an interface for links
     *
     * @example
     * rte = Object.create(Rte);
     * rte.init('#mytextarea');
     */
    var Rte;

    Rte = {

        /**
         * Style definitions to pass to the CodeMirrorRte.
         */
        styles: {

            bold: {
                className: 'rte-style-bold',
                element: 'b'
            },
            italic: {
                className: 'rte-style-italic',
                element: 'i'
            },
            underline: {
                className: 'rte-style-underline',
                element: 'u'
            },
            quote: {
                className: 'rte-style-quote',
                element: 'q'
            },
            strikethrough: {
                className: 'rte-style-strikethrough',
                element: 'strike'
            },
            superscript: {
                className: 'rte-style-superscript',
                element: 'sup',
                clear: ['subscript']
            },
            subscript: {
                className: 'rte-style-subscript',
                element: 'sub',
                clear: ['superscript']
            },
            link: {
                className: 'rte-style-link',
                element: 'a',

                // Do not allow links to span multiple lines
                singleLine: true,
                
                // Function to read attributes from a link and save them on the mark
                fromHTML: function($el, mark) {
                    
                    mark.attributes = {
                        href: $el.attr('href'),
                        target: $el.attr('target'),
                        rel: $el.attr('rel'),
                        title: $el.attr('title'),
                        'data-cms-id': $el.attr('data-cms-id'),
                        'data-cms-href': $el.attr('data-cms-href')
                    };
                },
                
                // Function to return the opening HTML element for a link
                toHTML: function(mark) {
                    
                    var $el, href, html, rel, target, title, cmsId, cmsHref;
                    
                    // For a link set the attributes on the element that were set on the mark
                    html = '<a';

                    if (mark.attributes) {
                        href = mark.attributes.href || '';
                        title = mark.attributes.title || '';
                        target = mark.attributes.target || '';
                        rel = mark.attributes.rel || '';
                        cmsId = mark.attributes['data-cms-id'] || '';
                        cmsHref = mark.attributes['data-cms-href'] || '';
                    }

                    if (href) { html += ' href="' + href + '"'; }
                    if (title) { html += ' title="' + title + '"'; }
                    if (target) { html += ' target="' + target + '"'; }
                    if (rel) { html += ' rel="' + rel + '"'; }
                    if (cmsId) { html += ' data-cms-id="' + cmsId + '"'; }
                    if (cmsHref) { html += ' data-cms-href="' + cmsHref + '"'; }
                    
                    html += '>';
                    
                    return html;
                },

                onClick: function(mark) {

                    // TODO: replace this with an interface to prompt for link attributes
                    alert('temporary onClick for links');
                    
                    mark.attributes = {
                        href: 'http://cnn.com',
                        target: '_blank',
                        rel: 'nofollow'
                    };
                }
            },
            ol : {
                className: 'rte-style-ol',
                line: true,
                element: 'li',
                elementContainer: 'ol',
                clear: ['ul', 'alignLeft', 'alignCenter', 'alignRight']
            },
            ul: {
                className: 'rte-style-ul',
                line: true,
                element: 'li',
                elementContainer: 'ul',
                clear: ['ol', 'alignLeft', 'alignCenter', 'alignRight']
            },
            
            alignLeft: {
                className: 'rte-style-align-left',
                line: true,
                // Align left is the default so do not output any special HTML
                // element: 'div',
                // elementAttr: {
                //     style: 'text-align:left'
                // },
                clear: ['alignCenter', 'alignRight', 'ol', 'ul']
            },
            alignCenter: {
                className: 'rte-style-align-center',
                line: true,
                element: 'div',
                elementAttr: {
                    style: 'text-align:center'
                },
                clear: ['alignLeft', 'alignRight', 'ol', 'ul']
            },
            alignRight: {
                className: 'rte-style-align-right',
                line: true,
                element: 'div',
                elementAttr: {
                    style: 'text-align:right'
                },
                clear: ['alignLeft', 'alignCenter', 'ol', 'ul']
            },

            // Special styles used for enhancements
            enhancementContent: {
                className: 'rte-enhancement-content',
                element: 'div',
                elementAttr: {
                    'class': 'rte-enhancement-content'
                },
                internal: true
            }
        },

        
        /**
         * Which buttons are in the toolbar?
         * This is an array of toolbar config objects with the following properties:
         *
         * @property {String} style
         * The style that should be set when the toolbar link is clicked.
         * This must be a style key from the styles object.
         *
         * @property {String} text
         * The text that is displayed in the toolbar link.
         * Note the text might be hidden / replaced by an image using CSS.
         *
         * @property {String} className
         * A class to place on the toolbar link so it can be styled.
         *
         * @property {Boolean} separator
         * Set this and no other properties to add a separator between groups of toolbar icons.
         *
         * @property {String} action
         * The name of a supported toolbar action. The following are supported:
         *
         * @property {String} action='collapse'
         * the toolbar icon will collapse a style if the cursor is within that style.
         * This can be used to collapse quotes. You must also specify the collapseStyle property.
         *
         * @property {String} action='clear'
         * Clear all styles within the range.
         *
         * @property {String} action='trackChangesToggle'
         * Toggle the track changes function.
         *
         * @property {String} action='trackChangesAccept'
         * Accept any changes within the range.
         *
         * @property {String} action='trackChangesReject'
         * Accept any changes within the range.
         *
         * @property {String} action='trackChangesShowFinalToggle'
         * Toggle the track changes "show final" function.
         *
         * @example:
         * For a single icon provide the following information:
         * { style: 'bold', text: 'B', className: 'rte-toolbar-bold' },
         *
         * @example:
         * For a drop-down submenu:
         * {
         *   text: 'Headings',
         *   className: 'rte-toolbar-headings',
         *   submenu: [
         *     { style: 'h1', text: 'H1', className: 'rte-toolbar-h1' },
         *     { style: 'h2', text: 'H2', className: 'rte-toolbar-h2' },
         *   ]
         * }
         *
         * Note it is assumed that submenu items are mutually exclusive.
         */
        toolbarConfig: [
            { style: 'bold', text: 'B', className: 'rte-toolbar-bold' },
            { style: 'italic', text: 'I', className: 'rte-toolbar-italic' },
            { style: 'underline', text: 'U', className: 'rte-toolbar-underline' },
            { style: 'strikethrough', text: 'S', className: 'rte-toolbar-strikethrough' },
            { style: 'superscript', text: 'Super', className: 'rte-toolbar-superscript' },
            { style: 'subscript', text: 'Sub', className: 'rte-toolbar-subscript' },
            { separator:true },
            { style: 'quote', text: 'Quote', className: 'rte-toolbar-quote' },
            { action:'collapse', text: 'Collapse', className: 'rte-toolbar-quote-collapse', collapseStyle: 'quote' },
            { separator:true },
            { style: 'ul', text: '&bull;', className: 'rte-toolbar-ul' },
            { style: 'ol', text: '1.', className: 'rte-toolbar-ol' },
            { separator:true },
            { style: 'alignLeft', text: 'Left', className: 'rte-toolbar-align-left', activeIfUnset:['alignCenter', 'alignRight', 'ol', 'ul'] },
            { style: 'alignCenter', text: 'Center', className: 'rte-toolbar-align-center' },
            { style: 'alignRight', text: 'Right', className: 'rte-toolbar-align-right' },
            { separator:true },
            { action:'clear', text: 'Clear', className: 'rte-toolbar-clear'  },
            { separator:true },
            { action:'trackChangesToggle', text: 'Track Changes', className: 'rte-toolbar-track-changes' },
            { action:'trackChangesAccept', text: 'Accept', className: 'rte-toolbar-track-changes-accept' },
            { action:'trackChangesReject', text: 'Reject', className: 'rte-toolbar-track-changes-reject' },
            { action:'trackChangesShowFinalToggle', text: 'Show Final', className: 'rte-toolbar-track-changes-show-final' }
        ],

        
        /**
         * Location for the toolbar to be added.
         * If this is undefined then the toolbar will be created above the editor.
         * If you provide this then the content of that element will be replaced with the toolbar.
         *
         * @type {element|selector|jQuery object}
         *
         * @example
         * <div id="mytoolbar"></div>
         *
         * rte.toolbarLocation = '#mytoolbar';
         */
        toolbarLocation: undefined,

        
        /**
         * If the element is a textarea of an input, should the rte add an onsubmit
         * handler to the parent form, so the element will be updated will be updated
         * before the form is submitted?
         * 
         */
        doOnSubmit: true,

        
        /**
         * Initialize the rich text editor.
         *
         * @param {element|selector|jQuery object} element
         * The element for the rich text editor. This can be a textarea or input element, or a div.
         *
         * @param {Object} [options]
         * Optional options to set on the object. For example, to override the toolbarLocation parameter,
         * pass it in as an option: {toolbarLocation: mydiv}
         *
         * @example
         * rte.init('#mytextarea');
         *
         * @example
         * rte.init('#mytextarea', {toolbarLocation: '#mytoolbar'});
         */
        init: function(element, options) {

            var self;

            self = this;

            if (options) {
                $.extend(true, self, options);
            }

            self.$el = $(element);

            self.initRte();
            self.toolbarInit();
        },


        /**
         * Initialize the rich text editor.
         */
        initRte: function() {
            
            var content, self;
            
            self = this;

            // Get the value from the textarea
            content = self.$el.val() || '';

            // Create the codemirror rich text editor object
            self.rte = Object.create(CodeMirrorRte);

            // Add our styles to the styles that are already built into the rich text editor
            self.rte.styles = $.extend(true, self.rte.styles, self.styles);

            // Create a div under the text area to display the editor
            self.$container = $('<div/>', {
                'class': 'rte-wrapper'
            }).insertAfter(self.$el);

            // Hide the textarea
            self.$el.hide();

            // Set up a submit event on the form to copy the value back into the textarea
            if (self.doOnSubmit) {
                
                self.$el.closest('form').on('submit', function(){
                    self.$el.val(self.toHTML());
                });
            }
            
            // Initialize the editor
            self.rte.init(self.$container);

            // Set the content into the editor
            self.rte.fromHTML(content);
        },


        /*==================================================
         * TOOLBAR 
         *==================================================*/
        
        toolbarInit: function() {

            var self, $toolbar;

            self = this;

            // Set up the toolbar container
            $toolbar = $('<div/>', {'class': 'rte-toolbar'});
            if (self.toolbarLocation) {
                $toolbar.appendTo(self.toolbarLocation);
            } else {
                $toolbar.insertBefore(self.$el);
            }
            self.$toolbar = $toolbar;
            
            // Loop through the toolbar config to set up buttons
            $.each(self.toolbarConfig, function(i, item) {

                var $item;

                if (item.separator) {
                    
                    // Add a separator between items
                    $item = $('<span/>', {'class':'rte-toolbar-separator', text:' | '}).appendTo($toolbar);
                    
                } else if (item.submenu) {
                    
                    // This is a submenu
                    console.log('rte toolbar submenu not yet implemented');
                    
                } else {

                    // This is a toolbar button
                    $item = $('<a/>', {
                        href: '#',
                        'class': item.className || '',
                        html: item.text || '',
                        data: {
                            toolbarConfig:item
                        }
                    }).appendTo($toolbar);

                    $item.on('click', function(event) {
                        event.preventDefault();
                        self.toolbarHandleClick(item);
                    });
                }
            });

            // Whenever the cursor moves, update the toolbar to show which styles are selected
            self.$container.on("rteCursorActivity", function() {
                self.toolbarUpdate();
            });

            self.toolbarUpdate();
        },


        /**
         * When user clicks a toolbar item, do somehting.
         * In general this toggles the style associated with the item,
         * but it can also do more based on the "action" parameter
         * in the toolbar config.
         *
         * @param {Object} item
         * An entry from the toolbarConfig object.
         */
        toolbarHandleClick: function(item) {
            
            var rte, self, styleObj;

            self = this;

            rte = self.rte;
            
            styleObj = self.rte.styles[item.style] || {};

            if (item.action) {

                switch (item.action) {
                    
                case 'clear':
                    rte.removeStyles();
                    break;
                    
                case 'collapse':
                    if (item.collapseStyle) {
                        rte.inlineCollapse(item.collapseStyle);
                    }
                    break;

                case 'trackChangesToggle':
                    rte.trackToggle();
                    break;

                case 'trackChangesAccept':
                    rte.trackAcceptRange();
                    break;
                    
                case 'trackChangesReject':
                    rte.trackRejectRange();
                    break;
                    
                case 'trackChangesShowFinalToggle':
                    rte.trackDisplayToggle();
                    break;
                }

            } else if (item.style) {
                
                rte.toggleStyle(item.style);
            }
            
            self.toolbarUpdate();
            
            self.focus();
        },

        
        /**
         * Update the active status of the toolbar icons based on the current editor position.
         */
        toolbarUpdate: function() {

            var $links, rte, self, styles, gotAlignment, lastPos;

            self = this;
            rte = self.rte;
            
            // First make all the buttons inactive,
            // Then we'll decide which need to be active
            $links = self.$toolbar.find('a');
            $links.removeClass('active');

            // Get all the styles defined on the current range
            // Note ALL characters in the range must have the style or it won't be returned
            styles = $.extend({}, rte.inlineGetStyles(), rte.blockGetStyles());
            
            // Go through each link in the toolbar and see if the style is defined
            $links.each(function(){
                
                var config, $link, style;

                $link = $(this);

                // Get the toolbar config object (added to the link when the link was created in toolbarInit()
                config = $link.data('toolbarConfig');

                if (config.action) {

                    switch (config.action) {

                    case 'trackChangesToggle':
                        $link.toggleClass('active', rte.trackIsOn());
                        break;
                        
                    case 'trackChangesAccept':
                    case 'trackChangesReject':
                        if (styles.hasOwnProperty('trackInsert') || styles.hasOwnProperty('trackDelete')) {
                            $link.addClass('active');
                        }
                        break;
                        
                    case 'trackChangesShowFinalToggle':
                        $link.toggleClass('active', !rte.trackDisplayGet());
                        break;
                        
                    }
                    
                } else {
                    
                    // Check if the style for this toolbar item is defined for ALL characters in the range
                    if (config.style && styles[config.style] === true) {
                        $link.addClass('active');
                    }

                    // Special case if we have a toolbar icon that should be active when another set of
                    // styles are unset, then check for that here.
                    // For example, this can be used to make "Align Left" appear active unless some other styles
                    // such as "Align Center" or "Align Right" are set.
                    if (config.activeIfUnset) {
                        makeActive = true;
                        $.each(config.activeIfUnset, function(i, style) {
                            if (styles.hasOwnProperty(style)) {
                                makeActive = false;
                            }
                        });
                        if (makeActive) {
                            $link.addClass('active');
                        }
                        
                    }
                }
            });
            
            return;
        },

        
        toolbarToggle: function() {
            var self;
            self = this;
            self.$toolbar.toggle();
        },

        
        toolbarShow: function() {
            var self;
            self = this;
            self.$toolbar.show();
        },

        
        toolbarHide: function() {
            var self;
            self = this;
            self.$toolbar.hide();
        },

        
        fromHTML: function(html) {
            var self;
            self = this;
            return self.rte.fromHTML(html);
        },

        
        toHTML: function() {
            var self;
            self = this;
            return self.rte.toHTML();
        },

        focus: function() {
            var self;
            self = this;
            self.rte.focus();
            self.toolbarUpdate();
        }
    };


    // Expose as a jQuery plugin.
    var $inputs = $();

    $.plugin2('rte', {
        
        _defaultOptions: { },

        _create: function(input) {
            
            var $input, options;
            
            options = this.option();

            $.data(input, 'rte-options', $.extend(true, { }, options));

            rte = Object.create(Rte);
            rte.init(input, options);
            return;
        },

        enable: function() {
            return this;
        }
            
    });

    return Rte;

});
