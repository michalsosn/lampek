angular.module('lampek.images', [
  'ui.router',
  'lampek.alerts',
  'lampek.images.file-upload',
  'lampek.images.gallery',
  'lampek.user-switch',
  'lampek.resources'
])

.config(function($stateProvider) {
  $stateProvider.state('images', {
    url: '/images/:username',
    views: {
      "main": {
        controller: 'ImagesController',
        templateUrl: 'images/images.tpl.html'
      }
    },
    data: { pageTitle: { pattern: '{{username}} Images' } }
  });
})

.controller('ImagesController', function($stateParams, $scope, $state, alerts, account) {
  $scope.username = $stateParams.username;
  $scope.isOwner = function () {
    return account.equalsUsername($scope.username);
  };
  $scope.switchUser = function (username) {
    $state.go('images', {username: username});
  };
  
  $scope.$on('error:Unauthorized', function() {
    alerts.addDanger('Please sign in', 'You need to be logged in to access this content.');
    $state.go('home');
  });
})

;

