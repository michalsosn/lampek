angular.module('my-utils', [
])

.factory('myUtils', function() {
  return {
    partition: function(a, fun) {
        var good = [];
        var bad = [];
        var len = a.length;
        for (var i = 0; i < len; i++) {
          if (fun(a[i])) {
            good.push(a[i]);
          } else {
            bad.push(a[i]);
          }
        }
      return [good, bad];
    },
    isObjectEmpty: function (object) {
      return Object.keys(object).length === 0;
    },
    getDefault: function (object, dflt) {
      if (object === undefined) {
        return dflt;
      }
      return object;
    }
  };
})

;
