define({
  random: function() {
    var color;
    var fieldHue = Math.random();
    var GOLDEN_RATIO = 0.618033988749895;

    fieldHue += GOLDEN_RATIO;
    fieldHue %= 1.0;
    color = 'hsl(' + (fieldHue * 360) + ', 50%, 50%)';
    return color;
  }
});