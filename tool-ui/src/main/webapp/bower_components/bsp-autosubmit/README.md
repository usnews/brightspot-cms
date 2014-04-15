# Installation

Using [Bower](http://bower.io/):

`bower install bsp-autosubmit`

Manually:

- Download [jQuery 1.7.0 or above](http://jquery.com/download/)
- Download [bsp-utils.js](https://raw.githubusercontent.com/perfectsense/brightspot-js-utils/master/bsp-utils.js) ([repository](https://github.com/perfectsense/brightspot-js-utils))
- Download [bsp-autosubmit.js](https://raw.githubusercontent.com/perfectsense/brightspot-js-autosubmit/master/bsp-autosubmit.js)

# Usage

Add `data-bsp-autosubmit` attribute to any `form`, `input`, `select` or `textarea` element to trigger form submission on `change` or `input` events ([demo](http://perfectsense.github.io/brightspot-js-autosubmit/demo.html)).

[General Brightspot plugin configuration documentation](https://github.com/perfectsense/brightspot-js-utils/blob/master/PLUGIN.md)

### Options

- `disableAutocomplete` (default `true`): When `true`, the plugin will try to disable the native browser autocomplete functionality.
- `inputSubmitDelay` (default `100` milliseconds): Delay before form submission on `input` events. This allows the plugin to wait until the user finishes typing in `input` or `textarea` before the form submission triggers.
- `submitThrottle` (default `500` milliseconds): Delay between multiple form submission triggers.
