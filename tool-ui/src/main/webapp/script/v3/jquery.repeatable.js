/***
Repeatable Inputs
-----------------

There are three types of repeatables:

* repeatableForm = multiple inputs can be repeated

* repeatableForm-previewable = a slideshow, which will have grid view and gallery view

* single input = A single input is used instead of an add more button.
  This is used only when the addButtonText option is set to an empty string
  and there is a single template and a single input
  If user presses enter on the input, then a new input is created so the user can add more keywords.

The types have the following things in common:

* They can have a template (or multiple templates) to add new items.
  An "add item" button is included for each template.

* A remove button is added next to each item item (or in the case of single input,
  the remove button might be styled to overlay the input).


How removing / restoring works
------------------------------

When user clicks the "remove" button, the item is not actually removed from the page,
instead a "toBeRemoved" class is added so the item can be styled to indicate it will be removed,
the inputs are all set to be disabled so they will not be sent to the backend, and the remove
button is changed to a restore button.

When user clicks the "restore" button, the "toBeRemoved" class is removed, the inputs
are no longer disabled, and the restore button is changed to a remove button.

HTML for repeatables
--------------------

The HTML within the repeatable element must conform to these standards:

* It must contain a list (UL or OL) where each item within the list is
  a repeatable input

* Within the list there should be a template. The template can be one of the list items
  with classname li.template or a script element with type="text/template".

==================================================
***/


(function($, win, undef) {

    var $win = $(win);

    // Counter to use to make sure the browser doesn't cache ajax requests
    var cacheNonce = 0;

    // Defaults to use for repeatables
    var repeatableDefaults = {
        addButtonText: 'Add',
        removeButtonText: 'Remove',
        restoreButtonText: 'Restore',
        sortableOptions: {
            delay: 300
        }
    };
    
    require(['v3/input/carousel'], function(carouselUtility) {

        // Create the jquery plugin .repeatable() (using plugin2)
        $.plugin2('repeatable', {

            /**
             * Default options used by plugin2
             */
            _defaultOptions: repeatableDefaults,

            
            /**
             * Function called by plugin2 when a new element is added to the page.
             *
             * ??? - it appears that a second parameter "options" is supported here,
             * but we don't seem to be using that
             *
             * @param {Element} container
             * The repeatable element that was added to the page.
             */
            _create: function(container) {
                var plugin = this;
                var $container = $(container);
                var options = plugin.option();
                var repeatable;
                
                // Create a copy of the repeatableUtility object
                // This uses prototypal inheritance so we're not actually copying the entire object
                // It allows each repeatable instance to have its own object that saves state
                repeatable = Object.create(repeatableUtility);

                // Initialize the repeatable utility to get things started
                repeatable.init($container, options);
            },


            /**
             * Function to add a new item through the jquery plugin.
             *
             * ??? - when does this get called, I can't seem to find any reference to it
             *
             * @param {Function} callback
             * Function to call after the item has been added.
             * Refer to repeatableUtility.addItem
             */
            'add': function(callback) {

                // TODO: instead of triggering a click we should directly call the function
                // that adds the item.
                
                // Trigger the click on the add button,
                // and pass it a callback function to run after the item is added.
                // ???: I'm not really sure if this works or how closest(.addButton)
                // could find the add button if $caller is actually the containing element?

                var plugin = this;
                var $caller = plugin.$caller;
                
                $caller.closest('.addButton').trigger('click', [ callback ]);

                return $caller;
            }
        });

        
        /**
         * Object to initialize and control repeatable inputs.
         *
         * This object should not be used directly, instead create a new object for each
         * repeatable using Object.create and prototypal inheritance.
         * After creating the new object, call the .init() function.
         *
         * @example
         * myRepeatable = Object.create(repeatableUtility);
         * myRepeatable.init(element, options);
         */
        var repeatableUtility = {

            /**
             * Default options
             */
            defaults: repeatableDefaults,

                
            /**
             * Initialize the object and set up the user interface.
             *
             * @param {Element|jQuery object|jQuery selector} element
             * The element that contains the repeatable HTML.
             *
             * @param {Object} [options]
             */
            init: function(element, options) {
                
                var self = this;

                // Save the element for later use
                self.$element = $(element).first();

                // Set up a place to save other dom elements for later use
                self.dom = {};
                
                // Override the default options
                self.options = $.extend(true, {}, self.defaults, options);
                
                // Get the various things we will need from the DOM
                self.initDOM();

                // Set up the event-input-disable functionality
                self.initInputDisable();
                
                // If the list needs to be sortable make it so
                self.initSortable();

                // Determine which type of repeatable we are dealing with
                self.initMode();

                // Intialize a carousel if we need it (for mode=preview)
                self.modePreviewInit();

                // For each item initialize it
                self.dom.$list.find('> li').each(function(){
                    self.initItem(this);
                });

                // After we're done initilizing all the items,
                // update the carousel if ncessary
                self.modePreviewUpdateCarousel();

                // Create the "Add Item" button(s)
                self.initAddButton();
            },

            
            /**
             * Get various things we will need from the DOM and save for later.
             */
            initDOM: function() {

                var self = this;
                var $list;
                var $templates = $();

                // Find the list of inputs (must be in a UL or OL)
                $list = self.$element.find('> ul, > ol').first();
                self.dom.$list = $list; // save for later use
                
                // Templates can be in an LI or a SCRIPT element
                $list.find('> li.template, > script[type="text/template"]').each(function() {

                    var $template = $(this);

                    if ($template.is('li.template')) {
                        $templates = $templates.add($template);
                    } else {
                        $templates = $templates.add($($template.text()));
                    }

                    // Remove the template from the page
                    $template.remove();
                });

                // Save templates for later use
                self.dom.$templates = $templates;
                
            },

            
            /**
             * Set up the event-input-disable functionality.
             * ??? need more info on how this works
             */
            initInputDisable: function() {

                var self = this;
                
                // Mark the input as something that can be locked or disabled
                self.$element.addClass('event-input-disable');

                // If the "input-disable" event is triggered, then add or remove a class
                self.$element.bind('input-disable', function(event, disable) {
                    $(event.target).closest('.inputContainer').toggleClass('state-disabled', disable);
                });
            },


            /**
             * Make the list of items sortable.
             * Only applies if the list is an ordered list (OL element).
             */
            initSortable: function() {
                
                var self = this;
                
                // Check if the list is an OL and if so make it sortable
                if (self.dom.$list.is('ol')) {
                    
                    self.dom.$list.sortable(self.options.sortableOptions);
                    
                    // Note: there is a 'sortable.end' event triggered after an item is dragged and dropped,
                    // which will be used later to adjust the carousel
                   
                }
            },

            
            /**
             * Determine which type of repeatable we are dealing with:
             * Sets the "mode" property to one of the following:
             * form, single, preview
             *
             * Note that initDOM() must be called before this is called.
             *
             * Also see the following functions:
             * modeIsPreview(), modeIsSingle(), ...
             */
            initMode: function() {
                
                var self = this;
                var $templateInputs;
                
                self.mode = 'form';

                // Set to single input mode if there is no add button text,
                // and if there is a single template with a single input
                $templateInputs = self.dom.$templates.find(':input').not(':input[name$=".toggle"]');
                if (!self.options.addButtonText &&
                    self.dom.$templates.length === 1 &&
                    $templateInputs.length === 1) {
                    
                    self.mode = 'single';
                    return;
                }

                // Set to preview mode
                if (self.$element.hasClass('repeatableForm-previewable')) {
                    self.mode = 'preview';
                    return;
                }
            },


            /**
             * Initialize the "Add Item" controls.
             *
             * There is a single container for the add item controls,
             * and within it can be mutiple add buttons (one for each template).
             *
             * The text of the add button is defined by a data attribute on the template
             * and the addButtonText options.
             */
            initAddButton: function() {

                var self = this;
                var $addButtonContainer;
                
                // Create the Add Item container
                //   -- Initialize single input mode if necessary
                //   -- Create an add button for each template
                //   -- Set up click event on the add button

                // Create a container where all the "Add Item" buttons will be displayed
                // First check to see if there is already a div to place them,
                // if not create a new div at the bottom.
                $addButtonContainer = self.$element.find('.addButtonContainer');
                if (!$addButtonContainer.length) {
                    $addButtonContainer = $('<div/>', { 'class': 'addButtonContainer' }).appendTo(self.$element);
                }

                // Save the add button container for later use
                // (in single mode we will add more to it)
                self.dom.$addButtonContainer = $addButtonContainer;
                
                // If we are in single input mode then we'll do some extra stuff
                self.modeSingleInitAddButton();

                // Create an "Add Item" link for each template we found
                var idIndex = 0;
                self.dom.$templates.each(function() {
                    
                    var $template = $(this);
                    var itemType = $template.attr('data-type') || 'Item';
                    var addButtonText;

                    // Determine which text to use for the add button
                    addButtonText = itemType;
                    if (self.options.addButtonText) {
                        addButtonText = self.options.addButtonText + ' ' + addButtonText;
                    }
                    
                    // Add an element for the "Add Button" control
                    $('<span/>', {
                        
                        'class': 'addButton',
                        text: addButtonText,
                        
                        // Save the template on the add button control so when user
                        // clicks it we will know which template to add
                        // 'data-addButtonTemplate': $template
                        
                    }).on('click', function(event, customCallback) {

                        // The click event for the add button supports an
                        // optional callback function that will be called after
                        // the new item is added.

                        // Add the new item based on this template
                        self.addItem($template, customCallback);

                        return false;
                        
                    }).appendTo($addButtonContainer);
                });
            },

            
            /**
             * Initialize an item. This should be called for each list item on the page,
             * plus it should be called for any new item added to the page.
             *
             * @param {Element|jQuery object|jQuery selector} element
             * The LI element that contains the item HTML.
             */
            initItem: function(element) {
                
                var self = this;
                var $item = $(element);

                // Create an image for the item if necessary,
                // and set up other stuff for mode=preview
                self.modePreviewInitItem($item);
                
                // Create a the label for the item if necessary.
                // The label acts as a toggle to show and hide the item,
                // plus it can load the content of the item from a URL
                self.initItemLabel($item);

                // Collapse the item unless it has an error message within it
                if ($item.find('.message-error').length === 0) {
                    self.itemCollapse($item);
                }

                // An input with ".toggle" in the name will be checked
                // to tell the backend that it should be saved, but the checkbox will not actually
                // be shown to the user, so we hid it here
                $item.find(':input[name$=".toggle"]').hide();

                // Add the remove control to the item
                self.initItemRemove($item);
            },


            /**
             * Initialize the label for an item.
             *
             * @param {Element|jQuery object} element
             * The item (LI element).
             */
            initItemLabel: function(element) {
                
                var self = this;
                var $item = $(element);
                var type = $item.attr('data-type');
                var label = $item.attr('data-label');
                var $label;
                var $existingLabel;
                var labelText;

                // Do not add a label if  there is no data-type attribute
                if (!type) {
                    return;
                }

                // Do not add a label if this is a mode=preview
                if (self.modeIsPreview()) {
                    return;
                }
                
                // The text for the label will be the data type such as "Slideshow Slide"
                // And if a data-label attribute was provided append it after a colon such as "Slideshow Slide: My Slide"
                labelText = type;
                if (label) {
                    labelText += ': ' + label;
                }

                $label = $('<div/>', {
                    'class': 'repeatableLabel',
                    text: labelText,
                    
                    // TODO: describe purpose of these attributes 
                    'data-object-id': $item.find('> input[type="hidden"][name$=".id"]').val(),
                    'data-dynamic-text': '${content.state.getType().label}: ${content.label}'
                    
                }).on('click', function() {
                    self.itemToggle($item);
                });

                // If there was already a label on the page remove the repeatableLabel class
                // so it doesn't conflict with the new label we are creating,
                // then add this existing label to the new label we are creating.
                $existingLabel = $item.find(" > .repeatableLabel");
                if ($existingLabel.length) {
                    
                    $existingLabel.removeClass('repeatableLabel').appendTo($label);

                    // Just in case the label has any inputs, make sure clicks on the input do not bubble up
                    // to the label and make it toggle the item
                    $label.find(':input').click(function(e) {
                        e.stopPropagation();
                    });
                }

                // Add our label to item at the top
                $item.prepend($label);
            },


            /**
             * Initialize the remove button for an individiual item.
             *
             * @param {Element|jQuery object} item
             * The item (LI element).
             */
            initItemRemove: function(item) {
                
                var self = this;
                var $item = $(item);
                
                // Add the remove button to the item
                $('<span/>', {
                    'class': 'removeButton',
                    'text': self.options.removeButtonText
                }).on('click', function(){
                    self.removeItemToggle( $item );
                }).appendTo($item);

            },

            
            /**
             * Add a new item to the list, based from a template.
             *
             * @param {Element|jQuery object} template
             * The template for the new item.
             *
             * @param {Function} customCallback
             * A function to call after the new item has been added.
             */
            addItem: function(template, customCallback) {
                
                var self = this;
                var $template = $(template);
                var $addedItem;
                var promise;
                var templateUrl;
                var $templateInputs;
                
                // For single input mode, don't add another item if the input is empty
                if (self.modeIsSingle() && !self.dom.$singleInput.val()) {
                    return false;
                }

                // ???
                self.$element.find(".objectId-placeholder").hide();

                // Create a copy of the template to use as the new item
                $addedItem = $template.clone();
                $addedItem.removeClass('template');

                // If the template contains inputs that end with ".toggle" then
                // start by making them checked to tell the backend it should save this item
                $addedItem.find(':input[name$=".toggle"]').attr('checked', 'checked');

                // Determine if we need to AJAX the template onto the page
                // (if the template has no input but has a link)
                templateUrl = $addedItem.find('a').attr('href');
                $templateInputs = $addedItem.find(':input')
                if (templateUrl && $templateInputs.length === 0) {

                    // Not sure why, but sending some kind of cache breaker in the data?
                    ++ cacheNonce;

                    promise = $.ajax({
                        'cache': false,
                        'url': templateUrl,
                        'data': { '_nonce': cacheNonce }
                    }).always(function(response) {
                        
                        // The response will either be the data (on success), or a jqXHR object (on error)
                        var content = typeof(response) === 'string' ? response : response.responseText;

                        // Add the loaded content into the new item
                        $addedItem.html(content);
                    });
                    
                } else {
                    // We're not loading any data, so we'll create an already
                    // resolved promise to the next step will complete immediately
                    promise = $.Deferred().resolve().promise();
                }
                
                // Run more code after the item has been added to the page
                promise.always(function() {

                    // Add the item to the page
                    self.dom.$list.append($addedItem);
                    
                    // Initialize stuff on the new item
                    // Note this will collapse the item as well so we will uncollapse it below
                    self.initItem($addedItem);
                    
                    // Make sure it's not collapsed since it's a new item
                    self.itemUncollapse($addedItem);

                    // For single input mode, when user enters a value into the input,
                    // we copy that value into the added item, then clear the value
                    // from the original input so the user can enter another value
                    if (self.modeIsSingle()) {
                        $addedItem.find(':input:not([name$=".toggle"])').val(self.dom.$singleInput.val());
                        self.dom.$singleInput.val('');
                    }

                    // Adjust the element ids so they don't conflict
                    $addedItem.find('*[id]').attr('id', function(index, attr) {

                        var newAttr;
                        
                        self.idIndex += 1;
                        
                        newAttr = attr + 'r' + self.idIndex;

                        // Also adjust the LABEL elements that are linked to other inputs
                        $addedItem.find('*[for=' + attr + ']').attr('for', newAttr);

                        // Also adjust the auto-show elements so they show the correct elements
                        $addedItem.find('*[data-show=#' + attr + ']').attr('data-show', '#'+newAttr);
                        
                        return newAttr;
                    });

                    // Trigger a change event
                    // So other code can act on this if necessary
                    $addedItem.change();

                    // Trigger a custom "create" event for the item
                    // So other code can act on this if necessary
                    $addedItem.trigger('create');

                    // Trigger a resize event for the window
                    // Since we have added new content
                    $win.resize();

                    if (customCallback) {
                        customCallback.call($addedItem[0]);
                    }
                }); // END promise.always()

                // Return the promise just in case someone wants to do something after the item is added
                // (but doesn't want to use the callback function)
                return promise;
            },


            /**
             * Shortcut function to tell if the mode is 'form'
             * @returns {Boolean}
             */
            modeIsForm: function() {
                var self = this;
                return self.mode === 'form';
            },

            
            /**
             * Shortcut function to tell if the mode is 'single'
             * @returns {Boolean}
             */
            modeIsSingle: function() {
                var self = this;
                return self.mode === 'single';
            },

            
            /**
             * Shortcut function to tell if the mode is 'preview'
             * @returns {Boolean}
             */
            modeIsPreview: function() {
                var self = this;
                return self.mode === 'preview';
            },


            /**
             * Toggle the display of an item (and load the item if necessary).
             * If the item contains a hidden input with data-form-fields-url attribute,
             * then that means it has not been loaded yet.
             * In that case dynamically load the item the first time the item is toggled.
             *
             * @param {Element|jQuery object} item
             * The list item that should be toggled.
             *
             * @param {Boolean} [collapseFlag]
             * Set to true to force the item to collapse.
             * Set to false to force the item to uncollapse.
             * If undefined then the item will toggle between collapsed and uncollapsed.
             */
            itemToggle: function(item, collapseFlag) {
                
                var self = this;
                var $item = $(item);
                var deferred;

                // Don't do anything if mode=preview
                if (self.modeIsPreview()) {
                    return;
                }
                
                // Collapse or uncollapse the item
                $item.toggleClass('collapsed', collapseFlag);

                // Load the item if necessary,
                // or if it's already loaded do some stuff immediately
                if (!self.itemIsCollapsed($item)) {
                    
                    self.itemLoad($item).always(function(){
                    
                        // Trigger the resize event since we changed the item size
                        $item.resize();

                        // Trigger a change event on the first input within the item
                        // so other code can do something if necessary
                        $item.find(':input:first').change();
                    });
                }
            },


            /**
             * Collapse an item.
             *
             * @param {Element|jQuery object} item
             * The list item that should be collapsed.
             */
            itemCollapse: function(item) {
                var self = this;
                self.itemToggle(item, true);
            },

            
            /**
             * Uncollapse an item (and load the item if necessary).
             *
             * @param {Element|jQuery object} item
             * The list item that should be uncollapsed.
             */
            itemUncollapse: function(item) {
                var self = this;
                self.itemToggle(item, false);
            },

            
            /**
             * Determine if an item is currently collapsed.
             *
             * @param {Element|jQuery object} item
             * The list item to test.
             */
            itemIsCollapsed: function(item) {
                var self = this;
                var $item = $(item);
                return $item.hasClass('collapsed');
            },

            
            /**
             * Checks an item to determine if it needs to load some dynamic content,
             * and loads it if necessary.
             *
             * @param {Element|jQuery object} item
             * The item to load.
             *
             * @param {Element|jQuery object} [location]
             * Optional location where to append the loaded content.
             * If not specified will append the loaded content to the item.
             *
             * @returns {Promise}
             * Returns a promise that tells you when the item is done loading.
             * You can use this promise even if the item is already on the page
             * and doesn't need to load anything.
             *
             * @example
             * myRepeatable.itemLoad(element).always(function(){ alert('Item is done loading'); });
             */
            itemLoad: function(item, location) {
                
                var self = this;
                var $item = $(item);
                var $location = location ? $(location) : $item;
                var $input;
                var url;
                var data;

                // In case we do not need to load anything, we'll create a deferred
                // promise that is already resolved
                var promise = $.Deferred().resolve().promise();
                
                // Look for a hidden input that has a data-form-fields-url attribute
                // which indicates we need to load the form fields fields for this item.
                // Note after we load the content we will remove data-form-fields-url
                // so it will only be found and loaded once.
                $input = $item.find('> input[data-form-fields-url]');

                if ($input.length > 0) {
                    
                    // Get the URL to fetch
                    url = $input.attr('data-form-fields-url');

                    // Get the data to pass to the URL
                    data = $input.val();
                    
                    // Remove the attribute and data so we don't fetch again
                    $input.removeAttr('data-form-fields-url');
                    $input.val('');

                    // Fetch the content
                    // Override the promise we created by that returned by the ajax call,
                    // so we can pass that back and you can take action when the ajax completes
                    promise = $.ajax({
                        'type': 'POST',
                        'cache': false,
                        'url': url,
                        'data': { 'data': data },
                    }).always(function(response) {

                        // The response will either be the data (on success), or a jqXHR object (on error)
                        var content = typeof(response) === 'string' ? response : response.responseText;

                        // When ajax completes add the content to the page
                        $(content).appendTo($location);

                        // If the item has already been removed, mark the new content to be removed as well
                        if (self.itemIsRemoved($item)) {
                            // Call the removeItem function again and this time
                            // it will also mark the newly loaded content to be disabled
                            self.removeItem($item);
                        }
                        
                        // Trigger some events so other code can know we have added content
                        // and can add more controls to the form we just loaded
                        $location.trigger('create');
                        $location.trigger('load');

                    });
                }

                // Return a promise so other events can be triggered after the item is loaded
                return promise;
            },

            
            /**
             * Toggle the remove state for an item.
             * When an item is marked for removal we perform the following steps:
             * - Add toBeRemoved class on the item.
             * - Disable all inputs.
             * - Change remove link text.
             *
             * @param {Element|jQuery object} item
             * The item (LI element).
             *
             * @param {Boolean} [removeFlag]
             * Set to true to force the item to be removed.
             * Set to false to force the item to be restored.
             * If undefined then the item will toggle between remove/restore.
             */
            removeItemToggle: function(item, removeFlag) {

                var self = this;
                var $item = $(item);
                var $removeButton = $item.find('.removeButton');
                var $inputs;
                var itemNumber = $item.index();
                var carousel = self.carousel;
                var $itemCarousel;
                var $content = $();

                // Define the content where we need to disable inputs and mark toBeRemoved
                $content = $content.add($item);
                
                // If we previously set up a carousel, get the tile in the carousel
                // so we can mark it as removed
                if (carousel) {
                    
                    // Add the carousel tile so it will get the "toBeRemoved" class
                    $content = $content.add( carousel.getTileContent(itemNumber) );

                    // Add the edit form for this item so we can mark it toBeRemoved
                    // and disable those inputs
                    $content = $content.add( $item.data('editContainer') );
                }

                // Find all the input elements within the content so we can disable them
                $inputs = $content.find(':input');
                
                // If the removeFlag was not specified, determine if it should be true
                // or false, based on the current class of the item
                if (removeFlag === undefined) {
                    removeFlag = !$item.is('.toBeRemoved');
                }
                
                if (removeFlag) {
                    
                    // Add the "toBeRemoved" class to both the item and the item in the carousel
                    $content.addClass('toBeRemoved');
                    
                    // Disable all the inputs within the item so they will not be sent to the backend
                    // when the form is submitted
                    $inputs.attr('disabled', 'disabled');

                    // Change the text in the remove button
                    if (self.options.restoreButtonText) {
                        $removeButton.text(self.options.restoreButtonText);
                    }

                } else {

                    // Remove the "toBeRemoved" class to both the item and the carousel
                    $content.removeClass('toBeRemoved');

                    // Disable all the inputs within the item so they will not be sent to the backend
                    // when the form is submitted
                    $inputs.removeAttr('disabled');
                    
                    // Change the text in the remove button
                    if (self.options.removeButtonText) {
                        $removeButton.text(self.options.removeButtonText);
                    }
                }

                // Trigger a change event to notify other code
                $item.change();
            },

            
            /**
             * Mark an item to be removed.
             * 
             * @param {Element|jQuery object} item
             * The item (LI element).
             */
            removeItem: function(item) {
                var self = this;
                self.removeItemToggle(item, true);
            },

            
            /**
             * Unmark an item to be removed (that is, restore the item).
             * 
             * @param {Element|jQuery object} item
             * The item (LI element).
             */
            restoreItem: function(item) {
                var self = this;
                self.removeItemToggle(item, false);
            },


            /**
             * Determine if an item is currently removed.
             * 
             * @param {Element|jQuery object} item
             * The item (LI element).
             */
            itemIsRemoved: function(item) {
                var self = this;
                return $(item).hasClass('toBeRemoved');
            },

            
            //==================================================
            // MODE SINGLE
            //==================================================

            
            /**
             * Additional initializing for the "Add Item" controls when mode is "single".
             */
            modeSingleInitAddButton: function() {
                
                var self = this;
                var $input;
                var $toggle;
                var $singleInput;
                var $addButtonContainer = self.dom.$addButtonContainer;
                
                // Only do this for single input mode
                if (!self.modeIsSingle()) {
                    return;
                }

                // Get the single input from the template, but not the ".toggle" input.
                // We will also clone the $toggle input later
                $toggle = self.dom.$templates.find(':input[name$=".toggle"]');
                $input = self.dom.$templates.find(':input').not($toggle);

                // Make a copy of the input
                $singleInput = $input.clone();

                // Remove ID if it has it since duplicates not allowed
                $singleInput.removeAttr('id');
                
                // When user presses Enter on the input then simulate a click on Add Item button
                // so another item can be added
                $singleInput.keydown(function(event) {
                    // Check for Enter key
                    if (event.which == 13) {
                        // TODO: instead of triggering a click on the add button
                        // we should just call the method that the click button calls
                        $addButtonContainer.find('.addButton').trigger('click');
                        return false;
                    }
                });

                // Put a toggle input into the add button container
                // So it will tell the back-end that this input should be added
                // ???: this is not checked initially (but is is checked when the new input is added?)
                $('<input/>', {
                    'name': $singleInput.attr('name'),
                    'type': 'hidden',
                    'value': $toggle.attr('value')
                }).appendTo($addButtonContainer);

                // Put the copy of the single input into the add button container
                // so user can type into it and create new items
                $singleInput.appendTo($addButtonContainer);

                // Save for later use (so we can check if it contains a value)
                self.dom.$singleInput = $singleInput;
                
                // TODO: determine how these inputs are duplicated when the add button container is clicked...
            },

            
            //==================================================
            // MODE PREVIEW
            //==================================================

            /**
             * Initialize the carousel viewer for mode=preview
             *
             * TODO: needs lots of cleanup
             */
            modePreviewInit: function() {
                
                var self = this;
                var $container = self.$element;
                var carousel;
                var $viewGrid;
                var $viewCarousel;
                var $carouselTarget;
                var $carouselContainer;
                var $viewSwitcher;
                var $topButtonContainer;
                
                // We only need a carousel for "preview" mode
                if (!self.modeIsPreview()) {
                    return;
                }

                // Create controls at the top
                $topButtonContainer = $('<div/>', { 'class': 'repeatablePreviewControls' }).prependTo($container);

                // TODO: what about the "Upload Files" link that is already at the bottom and needs to move to the top?
                // We could just move all content after the list into  the top button container, but that sounds like a hack
                
                // Add a placeholder for the "Add Item" button(s) to later be added to the top.
                // Refer to initAddButton() to see how this is used.
                $('<span/>', { 'class': 'addButtonContainer' }).appendTo($topButtonContainer);

                // Create buttons to switch between grid view and gallery view
                $viewSwitcher = $('<span class="view-switcher inputSmall">' +
                                  '<a href="#" class="view-switcher-active view-switcher-grid">Grid View</a> | ' +
                                  '<a href="#" class="view-switcher-gallery">Gallery View</a>' +
                                  '</span>').appendTo($topButtonContainer);
                
                self.dom.$viewSwitcher = $viewSwitcher; // Save for later

                // The grid view will the existing UL or OL
                $viewGrid = self.dom.$list;
                self.dom.$viewGrid = $viewGrid; // save for later

                // For the carousel view, create a new placeholder but hide it initially
                $viewCarousel = $('<div/>', { 'class': 'inputLarge viewCarousel' }).insertAfter(self.dom.$list).hide();
                self.dom.$viewCarousel = $viewCarousel; // save for later

                $carouselContainer = $('<div/>', {'class': 'carousel-container'}).appendTo($viewCarousel);

                // Also create a placeholder where we can edit each item
                $carouselTarget = $('<div/>', {'class': 'carousel-target'}).appendTo($viewCarousel);
                self.dom.$carouselTarget = $carouselTarget;

                // Now create the carousel object, initialize it, and save it for later use
                carousel = Object.create(carouselUtility);
                carousel.init($carouselContainer, {numbered:true});
                self.carousel = carousel;
                
                // Add a listener so we can do something when carousel items are clicked
                $carouselContainer.on('carousel.tile', function(e, carouselData) {

                    var $item;
                    
                    // @param {Object} carouselData
                    // @param {Object} carouselData.carousel
                    // @param {jQuery object} carouselData.index = the number of the tile that was clicked
                    // @param {Element} carouselData.tile = the tile that was clicked

                    // We previously saved the item element on the carousel tile
                    $item = $(carouselData.tile).data('item');
                    if ($item) {
                        // Send "false" as the second argument because
                        // we do not want to move the carousel position to
                        // the active tile.
                        self.modePreviewEdit($item, false);
                    }
                    
                });

                // Set up events so user can switch between the carousel view and the grid view
                $viewSwitcher.on('click', '.view-switcher-grid', function(event) {
                    self.modePreviewShowGrid();
                    return false;
                });
                $viewSwitcher.on('click', '.view-switcher-gallery', function(event) {
                    self.modePreviewShowCarousel();
                    return false;
                });

                // Set up a listener for when the user drags and drops an item to change the order,
                // so we can adjust the order of tiles in the carousel
                self.dom.$list.on('sortable.end', function(event, element) {

                    var $item = $(element);
                    
                    // Find the index for the new position of the element
                    var newIndex = $item.index() + 1;

                    // Get the carousel tile that corresponds to the item that was moved
                    var carouselTile = $item.data('carouselTile');

                    var oldIndex;
                    
                    if (carouselTile) {

                        // Take the carousel tile content and find the current index in the carousel
                        oldIndex = self.carousel.getTileIndex(carouselTile) || 0;
                        
                        self.carousel.repositionTile(oldIndex, newIndex);
                    }

                });
            },


            /**
             * Initialize an item for mode=preview 
             */
            modePreviewInitItem: function(element) {
                var self = this;
                var $item = $(element);
                var imageUrl;
                var $controls;

                // Only do this for mode=preview
                if (!self.modeIsPreview()) {
                    return;
                }

                // Check for the preview image
                imageUrl = $item.attr('data-preview');
                if (!imageUrl) {
                    return;
                }

                $('<img/>', {
                    'class': 'previewable-image',
                    src: imageUrl,
                    alt: ''
                }).appendTo($item);

                // TODO: add title here

                // Add controls at bottom of preview image:
                // - Set as cover
                // - Edit image
                // - Remove slide
                
                $controls = $('<div/>', {
                    'class': 'previewable-controls',
                }).appendTo($item);

                // Add control to set cover
                // Add control to edit image
                $('<span/>', {
                    'class': 'previewable-control-edit',
                    text: 'Edit'
                }).on('click', function(event) {
                    self.modePreviewEdit($item);
                    return false;
                }).appendTo($controls);
                
                // Add control to remove slide

                // Add the item to the carousel
                self.modePreviewInitItemCarousel($item);
            },


            /**
             * Additional setup when initializing an item when a carousel is present.
             * This function adds a tile to the carousel for the item.
             */
            modePreviewInitItemCarousel: function(item) {
                
                var self = this;
                var $item = $(item);
                var carousel = self.carousel;
                var $carouselTile;
                var preview = $item.attr('data-preview');
                var label = $item.attr('data-label');
                
                if (!carousel) {
                    return;
                }
                
                $carouselTile = $('<div class="carousel-tile-content"/>');
                
                // Add the thumbnail image
                if (preview) {
                    $('<img/>', {
                        'class': 'carousel-tile-content-image',
                        src: preview,
                        alt: ''
                    }).appendTo($carouselTile);
                }
                
                // Add the text label
                if (label) {
                    $('<div/>', {
                        'class': 'carousel-tile-content-label',
                        text: label
                    }).appendTo($carouselTile);
                }

                // On the item, save a reference to the carousel tile,
                // so later if user changes sort order of the items,
                // we can determine which carousel tile needs to be moved
                $item.data('carouselTile', $carouselTile);
                
                // On the carousel tile, save a reference back to the item,
                // so later when carousel events occur we can find the item
                $carouselTile.data('item', $item);
                
                // Add the tile to the carousel
                self.carousel.addTile($carouselTile);
                
                // Note we don't call carouse.update() after each tile we add,
                // instead we wait until all tiles are added the call it once
                // at the end for best performance
            },

            
            /**
             * Edit an item for mode=preview
             *
             * @param {Element|jQuery object} item
             * The item to edit.
             *
             * @param {Boolean} [goToActiveTile]
             * Set to false if you do not want to move the carousel to the active tile.
             * Defaults to true.
             */
            modePreviewEdit: function(item, goToActiveTile) {
                var self = this;
                var $item = $(item);
                var $editContainer;

                // If necessary create the container for editing this item
                $editContainer = $item.data('editContainer');
                if (!$editContainer) {
                    $editContainer = $('<div/>', {
                        'class': 'itemEdit'
                    }).appendTo(self.dom.$carouselTarget);
                    $item.data('editContainer', $editContainer);
                }

                // Load the item into the edit container
                self.itemLoad($item, $editContainer).always(function(){

                    // Switch to the carousel view
                    self.modePreviewShowCarousel();

                    // Set the active tile in the carousel
                    self.carousel.setActive( $item.index() );

                    if (goToActiveTile !== false) {
                        self.carousel.goToActiveTile();
                    }
                    
                    // Hide all the other slide edit forms
                    self.dom.$carouselTarget.find('.itemEdit').hide();
                    $editContainer.show();
                });
            },

            
            /**
             * When in preview mode, show the grid view.
             */
            modePreviewShowGrid: function() {

                var self = this;
                
                if (!self.modeIsPreview()) {
                    return;
                }

                // Mark the "Grid View" link as active and the "Gallery View" link as inactive
                self.dom.$viewSwitcher.find('a').removeClass('view-switcher-active').filter('.view-switcher-grid').addClass('view-switcher-active');
                self.dom.$viewGrid.show();
                self.dom.$viewCarousel.hide();
            },

            
            /**
             * When in preview mode, show the carousel view.
             */
            modePreviewShowCarousel: function() {

                var self = this;

                if (!self.modeIsPreview()) {
                    return;
                }

                self.dom.$viewSwitcher.find('a').removeClass('view-switcher-active').filter('.view-switcher-gallery').addClass('view-switcher-active');
                self.dom.$viewGrid.hide();
                self.dom.$viewCarousel.show();

                // In some cases carousel update doesn't work if carousel is hidden,
                // so we'll call update whenever we show the carousel to ensure
                // it is displaying everything correctly
                self.carousel.update();
            },

            
            /**
             * After adding one or more items to the carousel,
             * call this to update the carousel display.
             */
            modePreviewUpdateCarousel: function() {
                var self = this;
                if (self.carousel) {
                    self.carousel.update();
                }
            }

            

            
        } // END repeatableUtility
        
    }); // END require

}(jQuery, window));
