angular.module('lampek.errors', [
])

.factory('errorHttpInterceptor', function($q, $rootScope) {
  var handledErrors = {
    400: 'error:BadRequest',
    401: 'error:Unauthorized',
    403: 'error:Forbidden',
    404: 'error:NotFound'
  };
  return {
    responseError: function(response) {
      var errorType = handledErrors[response.status];
      if (errorType !== undefined) {
        $rootScope.$broadcast(errorType, response.data);
      }
      else {
        $rootScope.$broadcast('error:Unknown', response.status, response.data);
      }
      return $q.reject(response);
    }
  };
})

;
