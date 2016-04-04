angular.module('lampek', [
  'templates-app',
  'templates-common',
  'ui.bootstrap',
  'ui.router',
  'lampek.alerts',
  'lampek.errors',
  'lampek.home',
  'lampek.images',
  'lampek.operations',
  'lampek.processes',
  'lampek.navbar'
])

.config(function($urlRouterProvider, $httpProvider) {
  $urlRouterProvider.otherwise('/home');
  $httpProvider.interceptors.push('errorHttpInterceptor');
})

.run(function run() {
})

.controller('AppController', function($scope, $state, $interpolate, alertService) {
  $scope.$on('$stateChangeSuccess', function(event, toState, toParams, fromState, fromParams) {
    if (angular.isDefined(toState.data.pageTitle.string)) {
      $scope.pageTitle = toState.data.pageTitle.string + ' - lampek';
    } else if (angular.isDefined(toState.data.pageTitle.pattern)) {
      $scope.pageTitle = $interpolate(toState.data.pageTitle.pattern, true, null, true)(toParams) + ' - lampek' ;
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

