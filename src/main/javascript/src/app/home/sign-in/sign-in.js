angular.module('lampek.home.sign-in', [
  'lampek.alerts'
])

.component('signIn', {
  controller: function ($http, $httpParamSerializer, alertService) {
    var ctrl = this;
    ctrl.username = '';
    ctrl.password = '';

    ctrl.serializedData = function() {
      $httpParamSerializer({
        username: ctrl.username,
        password: ctrl.password
      });
    };

    function postUserData(url) {
      return $http({
        method: 'POST',
        url: url,
        data: {username: ctrl.username, password: ctrl.password},
        headers: {'Content-Type': 'application/x-www-form-urlencoded'},
        transformRequest: $httpParamSerializer
      });
    }
    
    ctrl.login = function() {
      postUserData('/login')
      .then(function(response) {
        alertService.addSuccess('Login succeeded');
      }, function(response) {
        alertService.addDanger('Login failed', response.message);
      });
    };

    ctrl.register = function() {
      postUserData('/register')
      .then(function(response) {
        alertService.addSuccess('Registration succeeded', 'You may now sign in');
      }, function(response) {
        alertService.addDanger('Registration failed', response.message);
      });
    };
  },
  templateUrl: 'home/sign-in/sign-in.tpl.html'
})

;
