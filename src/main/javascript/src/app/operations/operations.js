angular.module('lampek.operations', [
  'ui.router',
  'lampek.defaults',
  'lampek.resources',
  'lampek.operations.operation-history',
  'lampek.operations.operation-tools',
  'lampek.operations.result-view'
])

.config(function($stateProvider) {
  $stateProvider.state('operations', {
    url: '/processes/:chosenProcess',
    views: {
      "main": {
        controller: 'OperationsController',
        templateUrl: 'operations/operations.tpl.html'
      }
    },
    data: { 
      pageTitle: { pattern: 'Process {{chosenProcess}}' } 
    },
    params: { 
      chosenProcess: '' 
    }
  });
})

.controller('OperationsController', function($scope, $state, $stateParams, alerts, fillDefaults, prepareResults, Operation, Result, specsHolder) {
  $scope.chosenProcess = $stateParams.chosenProcess;
  $scope.$on('error:Unauthorized', function () {
    alerts.addDanger('Please sign in', 'You need to be logged in to access this content.');
    $state.go('home');
  });

  $scope.selectOperation = function(operation) {
    $scope.selectedOperation = operation;

    if (operation === null || operation.id === undefined) {
      $scope.results = undefined;
    }
    else {
      Result.query({
        processName: $scope.chosenProcess,
        operationId: operation.id
      }, function(results) {
        $scope.results = prepareResults(results, $scope.chosenProcess, operation.id);
        $scope.seed = Date.now();
      });
    }
  };

  $scope.selectSpec = function(spec) {
    $scope.selectedSpec = spec;
    $scope.parameters = fillDefaults(spec);
  };
  
  $scope.selectResult = function(role) {
    $scope.resultName = role; 
  };

  $scope.copySelected = function() {
    if ($scope.selectedOperation === undefined) {
      return;
    }
    Operation.get({
      processName: $scope.chosenProcess,
      operationId: $scope.selectedOperation.id
    }, function (operation) {
      $scope.selectedSpec = operation.operationRequest.specification;
      operation.operationRequest.specification = undefined;
      $scope.parameters = operation.operationRequest;
      $scope.selectedCategory = specsHolder.findSpecIndex($scope.selectedSpec);
    });
  };
  
})

;

