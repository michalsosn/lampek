angular.module('replace', [
])

.filter('replace', function() {
  return function(input, pattern, newValue) {
    return input.replace(new RegExp(pattern), newValue);
  };
})

.filter('replaceMod', function() {
  return function(input, pattern, modifiers,  newValue) {
    return input.replace(new RegExp(pattern, modifiers), newValue);
  };
})

;