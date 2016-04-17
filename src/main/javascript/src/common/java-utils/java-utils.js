angular.module('java-utils', [
])

.factory('javaUtils', function() {
  return {
    fromInstant: function(instant) {
      var second = instant.epochSecond;
      var nano = instant.nano;
      var milli = second * 1000 + nano / 1000000;
      return new Date(milli);
    }
  };
})

;
