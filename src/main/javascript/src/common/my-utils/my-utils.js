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
    }
  };
})

;
