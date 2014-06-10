(function() {
  function removeUnit(number) {
    return number.unit && number.unit.is ? parseFloat(number.unit.is('%') ? number.value / 100 : number.value) : number;
  }

  function color2husl(color) {
    var rgb = color.rgb;
    return HUSL.fromRGB(rgb[0] / 255, rgb[1] / 255, rgb[2] / 255);
  }

  less.functions = {
    'husl': function(hue, saturation, lightness) {
      var rgb = HUSL.toRGB(removeUnit(hue), removeUnit(saturation), removeUnit(lightness));
      return this.rgb(rgb[0] * 255, rgb[1] * 255, rgb[2] * 255);
    },

    'husl-lightness-set': function(color, lightness) {
      var husl = color2husl(color);
      return this.husl(husl[0], husl[1], removeUnit(lightness));
    },

    'husl-lightness-change': function(color, amount) {
      var husl = color2husl(color);
      return this.husl(husl[0], husl[1], Math.min(100, Math.max(0, husl[2] + removeUnit(amount))));
    },

    'husl-darken': function(color, amount) {
      return this['husl-lightness-change'](color, -removeUnit(amount));
    },

    'husl-lighten': function(color, amount) {
      return this['husl-lightness-change'](color, amount);
    }
  };
})();
