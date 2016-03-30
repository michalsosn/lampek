angular.module('lampek.resources', [
  'ngResource'
])

.factory('Image', function($resource) {
  return $resource(
    'images/:imageName', {imageName: '@name'},
    {'query': {method:'GET'}}
  );
})

;
