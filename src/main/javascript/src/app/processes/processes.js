angular.module('lampek.processes', [
  'ui.router',
  'lampek.alerts',
  'lampek.processes.resource-picker',
  'lampek.processes.gallery',
  'lampek.resources' 
])

.config(function($stateProvider) {
  $stateProvider.state('processes', {
    url: '/processes',
    views: {
      "main": {
        controller: 'ProcessesController',
        templateUrl: 'processes/processes.tpl.html'
      }
    },
    data: { 
      pageTitle: { string: 'Processes' } 
    },
    params: {
      chosenResource: '' 
    }
  });
})

.controller('ProcessesController', function($scope, $state, $stateParams, alertService) {
  $scope.chosenResource = $stateParams.chosenResource;
  $scope.$on('error:Unauthorized', function() {
    alertService.addDanger('Please sign in', 'You need to be logged in to access this content.');
    $state.go('home');
  });
})

;

