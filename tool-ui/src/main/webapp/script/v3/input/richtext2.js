/* jshint undef: true, unused: true, browser: true, jquery: true, devel: true */
/* global define */

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
                    
                    var href, html, rel, target, title, cmsId;
                    
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
            { action:'enhancement', text: 'Enhancement', className: 'rte-toolbar-enhancement'  },
            { action:'marker', text: 'Marker', className: 'rte-toolbar-marker'  },
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
            self.enhancementInit();
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

            // Override the rich text editor to tell it how enhancements should be imported from HTML
            self.rte.enhancementFromHTML = function($content, line) {
            
                self.enhancementFromHTML($content, line);
            };

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

                case 'enhancement':
                    
                    // Stop the event from propagating, otherwise it will close the enhancement popup
                    event.stopPropagation();
                    event.preventDefault();
                    
                    self.enhancementCreate();
                    break;
                    
                case 'marker':
                    
                    // Stop the event from propagating, otherwise it will close the enhancement popup
                    event.stopPropagation();
                    event.preventDefault();
                    
                    self.enhancementCreate();
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

            // Update the toolbar so it makes the buttons active or inactive
            // based on the cursor position or selection
            self.toolbarUpdate();

            // Focus back on the editor
            self.focus();
        },

        
        /**
         * Update the active status of the toolbar icons based on the current editor position.
         */
        toolbarUpdate: function() {

            var $links, rte, self, styles;

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
                
                var config, $link, makeActive;

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
         * This is called only once when the editor is initialized.
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


        /**
         * Used by the link dialog, this function gets the values from the dialog
         * then resolves the deferred object so we can complete editing the link.
         */
        linkSave: function() {

            var attributes, $linkDialog, self;

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


        /**
         * Used by the link dialog, this function resolves the deferred object
         * so we can complete editing the link (and remove the link).
         */
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


        /*==================================================
         * Enhancements
         * Enhancements are bits of external content that sit within the editor content.
         * Users can do the following to the enhancement:
         * Create (and select an enhancement object in a popup)
         * Remove (mark for removal)
         * Remove completely (if already marked for removal)
         * Change (select the enhancement object)
         * Edit (modify the enhancement object in a popup)
         * Move up / down
         * Float left / right / full line
         * Set image size
         *==================================================*/
        
        /**
         */
        enhancementInit: function() {

            var self;
            
            self = this;

            // Counter to generate unique link targets for enhancement toolbar links
            self.enhancementGetTargetCounter = 0;

            // Okay, this is a hack so prepare yourself.
            // Set up a global click event to detect when user clicks on an enhancement in the popup.
            // However, since there can be multiple enhancements in the editor,
            // (and multiple rich text editors on the page) we must determine where the popup originated.
            // If the popup did not originate in our rich text editor, we will ignore the event and let
            // somebody else deal with it.
            $(document.body).on('click', '[data-enhancement]', function(event) {
                
                var data, $edit, editUrl, $enhancement, $popupTrigger, $select, $target;
                
                // The enhancement link that the user clicked
                $target = $(this);

                // Get the link that triggered the popup to appear.
                // This link will be inside the enhancement that is being changed.
                $popupTrigger = $target.popup('source');

                // Determine if that link is inside our rich text editor
                if (!self.$container.find($popupTrigger).length) {
                    // Not in our editor - must be from some other editor so we will ignore
                    return;
                }
                
                // Get the enhancement that is being changed.
                $enhancement = self.enhancementGetWrapper($popupTrigger);

                // Get the data for the selected enhancement
                // Note the .data() function will automatically convert from JSON string to a javacript object.
                // For example, the link might look like this:
                // <a data-enhancement='{"label":"Test Raw HTML","record":{"_ref":"0000014d-018f-da9a-a5cf-4fef59b30000",
                // "_type":"0000014b-75ea-d559-a95f-fdffd32f005f"},"_id":"0000014d-590f-d32d-abed-fdef3ad50001",
                // "_type":"0000014b-75ea-d559-a95f-fdffd3300055"}' href="#">Test Raw HTML</a>
                data = $target.data('enhancement');

                // Save the data on the enhancement so it can be used later
                $enhancement.data('rte-enhancement', data);

                // Modify the Select button in the toolbar
                $select = $enhancement.find('.rte-enhancement-toolbar-change');
                $select.text('Change');

                // Modify the "Edit" button in the toolbar so it will pop up the edit dialog for the enhancement
                $edit = $enhancement.find('.rte-enhancement-toolbar-edit');
                editUrl = $edit.attr('href') || '';
                editUrl = $.addQueryParameters(editUrl, 'id', data.record._ref);
                $edit.attr('href', editUrl);

                event.preventDefault();
                event.stopImmediatePropagation();
                $target.popup('close');
                return false;
            });

            
            // Set up a global close event to determine when the enhancement popup is closed
            // so we can remove the enhancement if nothing was selected.
            $(document).on('close', '.popup[name ^= "contentEnhancement-"]', function() {

                var $enhancement, $popupTrigger, $popup;

                // The popup that was closed
                $popup = $(this);

                // Get the link that triggered the popup to appear.
                // This link will be inside the enhancement that is being changed.
                $popupTrigger = $popup.popup('source');

                // Determine if that link is inside our rich text editor
                if (!self.$container.find($popupTrigger).length) {
                    // Not in our editor - must be from some other editor so we will ignore
                    return;
                }

                // Get the enhancement that is being changed.
                $enhancement = self.enhancementGetWrapper($popupTrigger);

                // Update the enhancement to show a preview of the content.
                // This will also remove the enhancement if it is empty.
                self.enhancementUpdate($enhancement);

            });
        },

        
        /**
         * Create a new enhancement.
         *
         * @param {Object} [config]
         * Optional data for the enhancement. This is normally used only when importing the enhancment from HTML.
         * @param {Object} [config.reference]
         * @param {String} [config.alignment]
         * The alignment for the enhancement: blank, "left", or "right"
         *
         * @param {Number} [line=current line]
         * Optional line number.
         */
        enhancementCreate: function(config, line) {

            var $enhancement, mark, self;

            self = this;
            
            // Create wrapper element for the enhancement and add the toolbar
            $enhancement = $('<div/>', {
                'class': 'rte-enhancement'
            }).append( self.enhancementToolbarCreate() );

            $('<div/>', {'class': 'rte-enhancement-label' }).appendTo($enhancement);

            // Add the enhancement to the editor
            mark = self.rte.enhancementAdd($enhancement[0], line, {
                block:true,
                // Set up a custom "toHTML" function so the editor can output the enhancement
                toHTML:function(){
                    return self.enhancementToHTML($enhancement);
                }
            });

            // Save the mark so we can use it to modify the enhancement later
            self.enhancementSetMark($enhancement, mark);

            // If the data for this enhancement was provided, save it as part of the enhancement
            if (config) {

                // config.id = $content.attr('data-id');
                // config.reference = $content.attr('data-reference');
                // config.alignment = $content.attr('data-alignment');
                // config.preview = $content.attr('data-preview');
                // config.text = $content.text();

                $enhancement.data('rte-enhancement', config.reference);

                if (config.alignment) {
                    self.enhancementSetPosition($enhancement, config.alignment);
                }
                
                self.enhancementUpdate($enhancement);
                
            } else {
                
                // No data was provided so this is a new enhancement.
                // Pop up the selection form.
                self.enhancementChange($enhancement);
            }
        },


        /**
         * Update the enhancement display, based on the enhancement data.
         */
        enhancementUpdate: function(el) {
            
            var $content, data, $enhancement, self;
            
            self = this;
            $enhancement = self.enhancementGetWrapper(el);
            $content = $enhancement.find('.rte-enhancement-label');
            data = $enhancement.data('rte-enhancement');

            if (!data) {
                self.enhancementRemoveCompletely($enhancement);
                return;
            }

            $content.empty();
            
            if (data.preview) {
                
                $('<figure/>', {
                    html: [
                        $('<img/>', {
                            src: data.preview,
                            title: data.label || ''
                        }),
                        $('<figcaption/>', {
                            text: data.label || ''
                        })
                    ]
                }).appendTo($content);
                
            } else {
                $content.text(data.label || 'Empty Enhancement');
            }
        },

        
        /**
         * @returns jQuery
         * Returns a jQuery object containing the toolbar.
         */
        enhancementToolbarCreate: function() {

            var self, $toolbar;

            self = this;
            
            $toolbar = $('<div/>', {
                'class': 'rte-enhancement-toolbar'
            });

            $('<a/>', {
                href: '#',
                text: 'Up',
                'class': 'rte-enhancement-toolbar-up'
            }).on('click', function(){
                self.enhancementMove(this, -1);
                return false;
            }).appendTo($toolbar);
    
            $('<a/>', {
                href: '#',
                text: 'Down',
                'class': 'rte-enhancement-toolbar-down'
            }).on('click', function(){
                self.enhancementMove(this, +1);
                return false;
            }).appendTo($toolbar);
    
            $('<a/>', {
                href: '#',
                text: 'Left',
                'class': 'rte-enhancement-toolbar-left'
            }).on('click', function(){
                self.enhancementSetPosition(this, 'left');
                return false;
            }).appendTo($toolbar);
    
            $('<a/>', {
                href: '#',
                text: 'Full',
                'class': 'rte-enhancement-toolbar-full'
            }).on('click', function(){
                self.enhancementSetPosition(this, 'full');
                return false;
            }).appendTo($toolbar);

            $('<a/>', {
                href: '#',
                text: 'Right',
                'class': 'rte-enhancement-toolbar-right'
            }).on('click', function(){
                self.enhancementSetPosition(this, 'right');
                return false;
            }).appendTo($toolbar);

            // TODO: image size selector
            
            $('<a/>', {
                href: CONTEXT_PATH + '/enhancementSelect',
                target: self.enhancementGetTarget(),
                text: 'Select', // Note this web be updated to "Change" once an enhancement is selected
                'class': 'rte-enhancement-toolbar-change'
            }).appendTo($toolbar);
            
            $('<a/>', {
                href: CONTEXT_PATH + '/content/enhancement.jsp', // Note this url will be modified to add the enhancement id
                target: self.enhancementGetTarget(),
                text: 'Edit',
                'class': 'rte-enhancement-toolbar-edit'
            }).on('click', function(){
            }).appendTo($toolbar);
            
            // CSS is used to hide this when the toBeRemoved class is set on the enhancement
            $('<a/>', {
                href: '#',
                text: 'Remove',
                'class': 'rte-enhancement-toolbar-remove'
            }).on('click', function(){
                self.enhancementRemove(this); // Mark to be removed
                return false;
            }).appendTo($toolbar);

            // CSS is used to hide this unless the toBeRemoved class is set on the enhancement
            $('<a/>', {
                href: '#',
                text: 'Restore',
                'class': 'rte-enhancement-toolbar-restore'
            }).on('click', function(){
                self.enhancementRestore(this, false);  // Erase the to be removed mark
                return false;
            }).appendTo($toolbar);
            
            // CSS is used to hide this unless the toBeRemoved class is set on the enhancement
            $('<a/>', {
                href: '#',
                text: 'Remove Completely',
                'class': 'rte-enhancement-toolbar-remove-completely'
            }).on('click', function(){
                self.enhancementRemoveCompletely(this);
                return false;
            }).appendTo($toolbar);

            return $toolbar;
        },


        /**
         * Pop up the enhancement selector form.
         */
        enhancementChange: function(el) {
            
            var $enhancement;
            var self = this;
            
            $enhancement = self.enhancementGetWrapper(el);

            // Okay this is a bit of a hack.
            // We will simulate a click on the "Select Enhancement" toolbar button,
            // because there is another click event on the page that will handle that
            // and pop up the appropriate form to select the enhancement.

            $enhancement.find('.rte-enhancement-toolbar-change').trigger('click');
        },

        
        enhancementRemove: function (el) {
            var $el, self;
            self = this;
            $el = self.enhancementGetWrapper(el);
            $el.addClass('toBeRemoved');
        },

        
        enhancementRemoveCompletely: function (el) {
            var mark, self;
            self = this;
            mark = self.enhancementGetMark(el);
            if (mark) {
                self.rte.enhancementRemove(mark);
            }
        },

        
        enhancementRestore: function (el) {
            var $el, self;
            self = this;
            $el = self.enhancementGetWrapper(el);
            $el.removeClass('toBeRemoved');
        },

        
        enhancementIsToBeRemoved: function(el) {
            var $el, self;
            self = this;
            $el = self.enhancementGetWrapper(el);
            return $el.hasClass('toBeRemoved');
        },

        
        enhancementMove: function(el, direction) {
            
            var mark, self;
            
            self = this;
            
            mark = self.enhancementGetMark(el);
            if (!mark) {
                return;
            }
            
            if (direction === 1 || direction === -1) {
                mark = self.rte.enhancementMove(mark, direction);
                self.enhancementSetMark(el, mark);
            }
        },

        
        /**
         * Sets the position for an enhancement.
         *
         * @param Element el
         * The enhancement element, or an element within the enhancement.
         *
         * @param String [type=full]
         * The positioning type: 'left', 'right'. If not specified defaults
         * to full positioning.
         */
        enhancementSetPosition: function(el, type) {
            
            var $el, mark, rte, self;
            
            self = this;
            rte = self.rte;
            $el = self.enhancementGetWrapper(el);
            mark = self.enhancementGetMark($el);

            $el.removeClass('rte-style-enhancement-right rte-style-enhancement-left rte-style-enhancement-full');

            switch (type) {

            case 'left':
                mark = rte.enhancementSetInline(mark);
                $el.addClass('rte-style-enhancement-left');
                break;
                
            case 'right':
                mark = rte.enhancementSetInline(mark);
                $el.addClass('rte-style-enhancement-right');
                break;
                
            default:
                mark = rte.enhancementSetBlock(mark);
                $el.addClass('rte-style-enhancement-full');
                break;
            }

            self.enhancementSetMark(el, mark);

            rte.refresh();
        },

        
        /**
         * Returns the position for an enhancement.
         *
         * @param Element el
         * The enhancement element, or an element within the enhancement.
         *
         * @returns String
         * Returns 'left' for float left, 'right' for float right, or empty string for full positioning.
         */
        enhancementGetPosition: function(el) {
            
            var $el, pos, self;
            
            self = this;
            $el = self.enhancementGetWrapper(el);

            if ($el.hasClass('rte-style-enhancement-left')) {
                pos = 'left';
            } else if ($el.hasClass('rte-style-enhancement-left')) {
                pos = 'right';
            }

            return pos || '';

        },

        
        /**
         * Given the element for the enhancement (or an element within that)
         * returns the wrapper element for the enhancement.
         *
         * @param Element el
         * The enhancement element, or an element within the enhancement.
         */
        enhancementGetWrapper: function(el) {
            var self;
            self = this;
            return $(el).closest('.rte-enhancement');
        },

        
        /**
         * Given the element for the enhancement (or an element within that)
         * returns the mark for that enhancement.
         *
         * @param Element el
         * The enhancement element, or an element within the enhancement.
         */
        enhancementGetMark: function(el) {
            var self;
            self = this;
            return self.enhancementGetWrapper(el).data('mark');
        },


        /**
         * Given the element for the enhancement (or an element within that)
         * sets the mark for that enhancement.
         *
         * @param Element el
         * The enhancement element, or an element within the enhancement.
         *
         * @paream Object mark
         * The mark object that was returned by the rte.enhancementCreate() function.
         */
        enhancementSetMark: function(el, mark) {
            var self;
            self = this;
            self.enhancementGetWrapper(el).data('mark', mark);
        },

        
        /**
         * Generate a unique link target for enhancement toolbar links.
         */
        enhancementGetTarget: function() {
            var self;
            self = this;
            return 'contentEnhancement-' + self.enhancementGetTargetCounter++;
        },


        /**
         * Convert an enhancement into HTML for output.
         *
         * @param Element el
         * The enhancement element, or an element within the enhancement.
         *
         * @returns {String}
         * The HTMl for the enhancement.
         */
        enhancementToHTML: function(el) {
            
            var alignment, data, $enhancement, html, $html, id, self;

            self = this;

            $enhancement = self.enhancementGetWrapper(el);

            // If the enhancement is marked to be removed?
            if (self.enhancementIsToBeRemoved($enhancement)) {
                return '';
            }
            
            // Get the enhancement data that was stored previously in a data attribute
            data = $enhancement.data('rte-enhancement') || {};
            if (data.record) {
                id = data.record._ref;
            }

            alignment = self.enhancementGetPosition(el);
            
            if (id) {
                
                $html = $('<button/>', {
                    'class': 'enhancement',
                    'data-id': id,
                    'data-reference': JSON.stringify(data),
                    text: data.label || ''
                });

                if (data.preview) {
                    $html.attr('data-preview', data.preview);
                }
                
                if (alignment) {
                    $html.attr('data-alignment', alignment);
                }
                
                html = $html[0].outerHTML;
            }
            
            return html || '';
        },

        /**
         * When importing from HTML, this function converts the enhancement HTML to an enhancement in the editor.
         *
         *
         * @param {jQuery} $content
         * The HTML for the enhancement, something like this:
         * <span class="enhancement" data-id="[id]" data-reference="[JSON]" data-alignment="[alignment]" data-preview="[preview]">Text</span>
         * Note that we output the enhancement as a button element, but in the textarea fields we received it as a span element.
         *
         * @param {Number} line
         * The line number for the enhancement.
         */
        enhancementFromHTML: function($content, line) {
            
            var self = this;
            var config = {};
            
            // Get enhancement options from the HTML, which looks like
            // <span data-id data-reference data-preview data-alignment/>
            
            try {
                config.reference = JSON.parse($content.attr('data-reference') || '') || {};
            } catch(e) {
                config.reference = {};
            }
             
            config.id = $content.attr('data-id');
            config.alignment = $content.attr('data-alignment');
            config.preview = $content.attr('data-preview');
            config.text = $content.text();

            self.enhancementCreate(config, line);
        },
        
        
        /*==================================================
         * Misc
         *==================================================*/
        
        fromHTML: function(html) {
            var self;
            self = this;
            return self.rte.fromHTML(html);
        },

        
        toHTML: function() {
            var html, self;
            self = this;
            html = self.rte.toHTML();
            return html;
        },

        focus: function() {
            var self;
            self = this;
            self.rte.focus();
            self.toolbarUpdate();
        }
    };

    
    // Expose as a jQuery plugin.
    $.plugin2('rte', {
        
        _defaultOptions: { },

        _create: function(input) {
            
            var options, rte;
            
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

// Set filename for debugging tools to allow breakpoints even when using a cachebuster
//# sourceURL=richtext2.js
