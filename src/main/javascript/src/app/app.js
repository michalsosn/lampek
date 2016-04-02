angular.module('lampek', [
  'templates-app',
  'templates-common',
  'ui.bootstrap',
  'ui.router',
  'lampek.alerts',
  'lampek.errors',
  'lampek.home',
  'lampek.images',
  'lampek.processes',
  'lampek.navbar'
])

.config(function($urlRouterProvider, $httpProvider) {
  $urlRouterProvider.otherwise('/home');
  $httpProvider.interceptors.push('errorHttpInterceptor');
})

.run(function run(alertService) {
})

.controller('AppController', function($scope, $state, alertService) {
  var ctrl = this;
  
  $scope.$on('$stateChangeSuccess', function(event, toState, toParams, fromState, fromParams) {
    if (angular.isDefined(toState.data.pageTitle)) {
      ctrl.pageTitle = toState.data.pageTitle + ' - lampek' ;
    }
  });
  $scope.$on('$stateNotFound', function(event, unfoundState, fromState, fromParams) {
    alertService.addDanger('Page not found', 'The page you tried to reach does not exist.');
  });

  $scope.$on('error:Forbidden', function() {
    alertService.addDanger('Access is denied', 'You do not have permission to view this page.');
    $state.go('home');
  });
})

;

