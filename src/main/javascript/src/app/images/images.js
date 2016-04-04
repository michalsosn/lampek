angular.module('lampek.images', [
  'ui.router',
  'lampek.alerts',
  'lampek.images.file-upload',
  'lampek.images.gallery',
  'lampek.resources' 
])

.config(function($stateProvider) {
  $stateProvider.state('images', {
    url: '/images',
    views: {
      "main": {
        controller: 'ImagesController',
        templateUrl: 'images/images.tpl.html'
      }
    },
    data: { pageTitle: { string: 'Images' } }
  });
})

.controller('ImagesController', function($scope, $state, alertService) {
  $scope.$on('error:Unauthorized', function() {
    alertService.addDanger('Please sign in', 'You need to be logged in to access this content.');
    $state.go('home');
  });
})

;

