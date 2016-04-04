angular.module('lampek.resources', [
  'ngResource'
])

.factory('Image', function($resource) {
  return $resource(
    'images/:imageName', {imageName: '@name'},
    {'query': {method:'GET'}}
  );
})
  
.factory('Process', function($resource) {
  return $resource(
    'processes/:processName/:subResource', {processName: '@name'},
    {
      'query': {method:'GET'},
      'replace': {method:'PUT'},
      'specify': {method:'GET', params: {subResource: 'specifications'}}
    }
  );
})

.factory('Operation', function($resource) {
  return $resource(
    'processes/:processName/operations/:operationId', 
    {processName: '@process', operationId: '@id'},
    {
      'query': {method:'GET'},
      'replace': {method:'PUT'}
    }
  );
})

.factory('Result', function($resource) {
  return $resource(
    'processes/:processName/operations/:operationId/results/:resultName',
    {processName: '@process', operationId: '@operation', resultName: '@name'},
    {'query': {method:'GET'}}
  );
})

;
