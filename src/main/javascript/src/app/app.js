angular.module('lampek', [
  'templates-app',
  'templates-common',
  'ui.bootstrap',
  'ui.router',
  'lampek.alerts',
  'lampek.account',
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

.controller('AppController', function($scope, $state, $interpolate, alerts, account) {
  $scope.$on('$stateChangeSuccess', function(event, toState, toParams, fromState, fromParams) {
    if (angular.isDefined(toState.data.pageTitle.string)) {
      $scope.pageTitle = toState.data.pageTitle.string + ' - lampek';
    } else if (angular.isDefined(toState.data.pageTitle.pattern)) {
      $scope.pageTitle = $interpolate(toState.data.pageTitle.pattern, true, null, true)(toParams) + ' - lampek' ;
    }
  });

  this.alerts = alerts.alerts;
  $scope.$on('$stateNotFound', function() {
    alerts.addDanger('Page not found', 'The page you tried to reach does not exist.');
  });

  $scope.$on('error:Forbidden', function() {
    alerts.addDanger('Access is denied', 'You do not have permission to view this page.');
    $state.go('home');
  });
  
  $scope.$on('error:BadRequest', function(event, data) {
    var message = data.map(function(item) { return item.message; }).join(', ');
    alerts.addDanger('You have sent invalid request', message);
  });

  $scope.$on('error:Unauthorized', function() {
    account.unset();
  });
})

;

