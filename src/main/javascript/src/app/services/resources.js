angular.module('lampek.resources', [
  'ngResource',
  'lampek.account'
])

.factory('Image', function($resource, account) {
  return $resource(
    '/user/:username/image/:imageName', 
    {username: account.getUsername},
    {'query': {method:'GET'}}
  );
})

.factory('Sound', function($resource, account) {
  return $resource(
    '/user/:username/sound/:soundName',
    {username: account.getUsername},
    {'query': {method:'GET'}}
  );
})

.factory('Process', function($resource, account) {
  return $resource(
    '/user/:username/process/:processName/:subResource', 
    {username: account.getUsername},
    {
      'query': {method:'GET'},
      'replace': {method:'PUT'},
      'specify': {method:'GET', params: {subResource: 'specification'}}
    }
  );
})

.factory('Operation', function($resource, account) {
  return $resource(
    '/user/:username/process/:processName/operation/:operationId',
    {username: account.getUsername},
    {
      'query': {method:'GET'},
      'replace': {method:'PUT'}
    }
  );
})

.factory('Result', function($resource, account) {
  return $resource(
    '/user/:username/process/:processName/operation/:operationId/result/:resultName',
    {username: account.getUsername},
    {'query': {method:'GET'}}
  );
})

;
