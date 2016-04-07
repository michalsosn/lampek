angular.module('lampek.account', [
  'ngCookies'
])

.service('account', function($httpParamSerializer, $http, $cookies, alerts) {
  var self = this;
  self.username = $cookies.get('account.username');
  self.logged = !!self.username;

  self.set = function(username) {
    self.username = username;
    self.logged = true;
    var nextYear = new Date();
    nextYear.setFullYear(nextYear.getFullYear() + 1);
    $cookies.put('account.username', self.username, {expires: nextYear});
  };

  self.unset = function() {
    self.username = null;
    self.logged = false;
    $cookies.remove('account.username');
  };

  function postUserData(url, username, password) {
    return $http({
      method: 'POST',
      url: url,
      data: {username: username, password: password},
      headers: {'Content-Type': 'application/x-www-form-urlencoded'},
      transformRequest: $httpParamSerializer
    });
  }
  
  self.getUsername = function() { 
    return self.username; 
  };
  self.isLogged = function() {
    return self.logged;
  };
  self.equalsUsername = function (otherName) {
    return self.username === otherName; 
  };

  self.signIn = function(username, password) {
    postUserData('/user/login', username, password)
      .then(function() {
        alerts.addSuccess('Login succeeded');
        self.set(username);
      }, function(response) {
        alerts.addDanger('Login failed', response.message);
      });
  };

  self.signOut = function() {
    $http({
      method: 'POST',
      url: '/user/logout'
    });
    self.unset();
  };

  self.register = function(username, password) {
    postUserData('/user/register', username, password)
      .then(function() {
        alerts.addSuccess('Registration succeeded', 'You may now sign in');
      });
  };
})

;
