angular.module('validators', [
])
  
.directive('integer', function() {
  return {
    require: 'ngModel',
    link: function(scope, elm, attrs, ctrl) {
      ctrl.$validators.integer = function(modelValue, viewValue) {
        if (ctrl.$isEmpty(modelValue)) {
          return true;
        }

        if (/^\-?\d+$/.test(viewValue)) {
          return true;
        }

        return false;
      };
    }
  };
})

;