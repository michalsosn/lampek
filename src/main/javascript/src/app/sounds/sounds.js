angular.module('lampek.sounds', [
  'ui.router',
  'lampek.alerts',
  'lampek.sounds.file-upload',
  'lampek.sounds.gallery',
  'lampek.user-switch',
  'lampek.resources'
])

.config(function($stateProvider) {
  $stateProvider.state('sounds', {
    url: '/sounds/:username',
    views: {
      "main": {
        controller: 'SoundsController',
        templateUrl: 'sounds/sounds.tpl.html'
      }
    },
    data: { pageTitle: { pattern: '{{username}} Sounds' } }
  });
})

.controller('SoundsController', function($stateParams, $scope, $state, alerts, account) {
  $scope.username = $stateParams.username;
  $scope.isOwner = function () {
    return account.equalsUsername($scope.username);
  };
  $scope.switchUser = function (username) {
    $state.go('sounds', {username: username});
  };
  
  $scope.$on('error:Unauthorized', function() {
    alerts.addDanger('Please sign in', 'You need to be logged in to access this content.');
    $state.go('home');
  });
})

;

