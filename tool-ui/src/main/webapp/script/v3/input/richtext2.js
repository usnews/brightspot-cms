define(['jquery', 'v3/input/richtextCodeMirror', 'v3/plugin/popup'], function($, CodeMirrorRte) {
    
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
    var CONTEXT_PATH, Rte;

    // Global variable set by the CMS, typically "/cms/"
    CONTEXT_PATH = window.CONTEXT_PATH || '';
    
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
                        cmsId: $el.attr('data-cms-id'),
                        cmsHref: $el.attr('data-cms-href')
                    };
                },
                
                // Function to return the opening HTML element for a link
                toHTML: function(mark) {
                    
                    var $el, href, html, rel, target, title, cmsId;
                    
                    // For a link set the attributes on the element that were set on the mark
                    html = '<a';

                    if (mark.attributes) {
                        href = mark.attributes.href || '';
                        title = mark.attributes.title || '';
                        target = mark.attributes.target || '';
                        rel = mark.attributes.rel || '';
                        cmsId = mark.attributes.cmsId || '';
                    }

                    if (href) { html += ' href="' + href + '"'; }
                    if (title) { html += ' title="' + title + '"'; }
                    if (target) { html += ' target="' + target + '"'; }
                    if (rel) { html += ' rel="' + rel + '"'; }
                    if (cmsId) {
                        html += ' data-cms-id="' + cmsId + '"';
                        html += ' data-cms-href="' + href + '"';
                    }
                    
                    html += '>';
                    
                    return html;
                },

                onClick: function(event, mark) {

                    var self;

                    // Note this onClick function is called in such a way that "this"
                    // refers to the Rte object, so we can access other functions in the object.
                    self = this;

                    // Stop the click from propagating up to the window
                    // because if it did, it would close the popup we will be opening.
                    if (event) {
                        event.stopPropagation();
                    }
                    
                    // Let the user edit the link, and when that is done update the mark
                    self.linkEdit(mark.attributes).done(function(attributes){
                        
                        if (attributes.remove || attributes.href === '' || attributes.href === 'http://') {
                            // Remove the link
                            mark.clear();
                        } else {
                            // Update the link attributes
                            mark.attributes = mark.attributes || {};
                            $.extend(mark.attributes, attributes);
                        }
                    }).fail(function(){

                        // If the popup was closed without saving and there is no href already the link,
                        // then remove the link.
                        if (!mark.attributes) {
                            mark.clear();
                        }
                        
                    });
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
            { style: 'link', text: 'Link', className: 'rte-toolbar-link' },
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

            self.initStyles();
            self.initRte();
            self.toolbarInit();
            self.linkInit();
        },


        /**
         * Determine which styles to support.
         * Modify certain callback functions so the Rte object will be available via the "this" keyword.
         */
        initStyles: function() {

            var self;

            self = this;

            // TODO: determine which styles should be for inline editor only
            
            // TODO: add customized styles from the CMS
            
            $.each(self.styles, function(i,styleObj) {

                // Modify the onClick function so it is called in the context of our object,
                // to allow the onclick function access to other functions
                if (styleObj.onClick) {
                    styleObj.onClick = $.proxy(styleObj.onClick, self);
                }
            });
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
                        self.toolbarHandleClick(item, event);
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
         *
         * @param {Object} [event]
         * The click event that from the toolbar button.
         * In case you need to stop the click from propagating.
         */
        toolbarHandleClick: function(item, event) {
            
            var mark, rte, self, styleObj;

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

                if (styleObj.onClick) {
                    
                    mark = rte.inlineGetMark(item.style) || rte.setStyle(item.style);
                    if (mark) {
                        styleObj.onClick(event, mark);
                    }
                    
                } else {
                    mark = rte.toggleStyle(item.style);
                }
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

        
        /*==================================================
         * LINKS
         *==================================================*/

        /**
         * Sets up the pop-up form that will be used to edit links.
         */
        linkInit: function() {

            var self;

            self = this;

            // The pop-up dialog used to prompt for links
            self.$linkDialog = $(
                '<div>' +
                    '<h2>Link</h2>' +
                    '<div class="rte-dialogLine">' +
                        '<input type="text" class="rte-dialogLinkHref">' +
                        '<input type="hidden" class="rte-dialogLinkId">' +
                        '<a class="rte-dialogLinkContent" target="linkById" href="' + CONTEXT_PATH + '/content/linkById.jsp?p=true">Content</a>' +
                    '</div>' +
                    '<div class="rte-dialogLine">' +
                        '<select class="rte-dialogLinkTarget">' +
                            '<option value="">Same Window</option>' +
                            '<option value="_blank">New Window</option>' +
                        '</select>' +
                        '<select class="rte-dialogLinkRel">' +
                            '<option value="">Relation</option>' +
                            '<option value="nofollow">nofollow</option>' +
                        '</select>' +
                    '</div>' +
                    '<a class="rte-dialogLinkSave">Save</a>' +
                    '<a class="rte-dialogLinkOpen" target="_blank">Open</a>' +
                    '<a class="rte-dialogLinkUnlink">Unlink</a>' +
                '</div>'
            ).on('click', '.rte-dialogLinkSave', function() {
                // User clicked "Save" button to save the link
                self.linkSave();
                self.$linkDialog.popup('close');
                return false;
            }).on('click', '.rte-dialogLinkUnlink', function() {
                // User clicked "Unlink" button to remove the link
                self.linkUnlink();
                self.$linkDialog.popup('close');
                return false;
            }).on('input', '.rte-dialogLinkHref', function(event) {
                // User changed the link href, so update the href in the "Open" link
                self.$linkDialog.find('.rte-dialogLinkOpen').attr('href', $(event.target).val() );
            }).on('keydown', '.rte-dialogLinkHref', function(event) {
                // If user presses enter key save the dialog
                if (event.which === 13) {
                    self.linkSave();
                    self.$linkDialog.popup('close');
                    return false;
                }
            }).appendTo(document.body)
                .popup() // turn it into a popup
                .popup('close') // but initially close the popup
                .popup('container').on('close', function() {
                    // If the popup is canceled with Esc or otherwise,
                    // do some cleanup such as removing the link if no link was
                    // previously selected
                    self.linkClose();
                });
            
        },

        
        /**
         * Displays a pop-up form to let users choose or edit a link.
         *
         * @param {Object} [attributes]
         * An object with key/value pairs for the link data.
         * @param {String} [attributes.href]
         * @param {String} [attributes.target]
         * @param {String} [attributes.rel]
         * @param {String} [attributes.title]
         * @param {String} [attributes.cmsId]
         * @param {String} [attributes.cmsHref]
         *
         * @returns {Promise}
         * Returns a promise that will be resolved with the link data
         * {Object} promise(attributes)
         * @param {String} [attributes.href]
         * @param {String} [attributes.target]
         * @param {String} [attributes.rel]
         * @param {String} [attributes.title]
         * @param {String} [attributes.cmsId]
         * @param {String} [attributes.cmsHref]
         * @param {Boolean} [attributes.remove]
         * If this is true, then remove the link.
         */
        linkEdit: function(attributes) {
            
            var deferred, $linkDialog, $href, self;

            self = this;

            attributes = attributes || {};

            $linkDialog = self.$linkDialog;
            
            deferred = $.Deferred();

            // Open the popup
            $linkDialog.popup('open');
            
            // Add existing attributes to the popup form
            $href = $linkDialog.find('.rte-dialogLinkHref');
            $href.val(attributes.href || 'http://');
            $linkDialog.find('.rte-dialogLinkId').val(attributes.cmsId || '');
            $linkDialog.find('.rte-dialogLinkTarget').val(attributes.target || '');
            $linkDialog.find('.rte-dialogLinkRel').val(attributes.rel || '');
            $linkDialog.find('.rte-dialogLinkOpen').attr('href', $href.val());
            $href.focus();

            // Save the deferred object so we can resolve it later
            self.linkDeferred = deferred;

            return deferred.promise();
        },


        linkSave: function() {

            var attributes, self;

            self = this;

            $linkDialog = self.$linkDialog;

            attributes = {
                href: $linkDialog.find('.rte-dialogLinkHref').val() || '',
                target: $linkDialog.find('.rte-dialogLinkTarget').val() || '',
                rel: $linkDialog.find('.rte-dialogLinkRel').val() || '',
                cmsId: $linkDialog.find('.rte-dialogLinkId').val() || ''
            };
            
            // Resolve the deferred object with the new attributes,
            // so whoever called linkEdit will be notified with the final results.
            self.linkDeferred.resolve(attributes);
        },


        linkUnlink: function() {
            var self;
            self = this;
            self.linkDeferred.resolve({remove:true});
        },

        
        /**
         * This function is called when the edit popup closes
         * whether from user input clicking outside the popup.
         */
        linkClose: function() {
            
            var self;

            self = this;

            // Reject the deferred object (if it hasn't already been resolved)
            if (self.linkDeferred) {
                self.linkDeferred.reject();
            }
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
