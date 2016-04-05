angular.module('lampek.operations.operation-history', [
  'capitalize',
  'replace',
  'lampek.resources'
])

.component('operationHistory', {
  bindings: {
    processName: '<',
    selectedOperation: '<',
    onOperationSelected: '&',
    onCopySelected: '&'
  },
  controller: function($interval, Operation) {
    var ctrl = this;

    // todo usługa async
    ctrl.refresh = function() {
      Operation.query({processName: ctrl.processName}, function(operations) {
        ctrl.operations = operations;
        var len = ctrl.operations.idList.length;
        if (len && !ctrl.selectedOperation) {
          ctrl.onOperationSelected({
            operation: ctrl.operations.idList[len - 1]
          });
        }
      });
    };
    var refreshPromise;
    ctrl.$onInit = function() {
      ctrl.refresh(); 
      refreshPromise = $interval(ctrl.refresh, 1000);
    };
    ctrl.$onDestroy = function() {
      $interval.cancel(refreshPromise);
    };

    ctrl.selectedIndex = function() {
      var idList = ctrl.operations.idList;
      var len = idList.length;
      for (var i = 0; i < len; ++i) {
        if (idList[i].id == ctrl.selectedOperation.id) {
          return i;
        }
      }
    };
    
    ctrl.removeSelected = function() {
      if (!ctrl.operations || !ctrl.selectedOperation) {
        return;
      }
      
      var nextIndex = ctrl.selectedIndex() + 1;
      if (nextIndex >= ctrl.operations.idList.length) {
        nextIndex = nextIndex - 2;
      }
      var nextSelected = ctrl.operations.idList[nextIndex];
      
      Operation.remove({
        processName: ctrl.processName,
        operationId: ctrl.selectedOperation.id
      });
      
      ctrl.onOperationSelected({operation: nextSelected});
    };
  },
  templateUrl: 'operations/operation-history/operation-history.tpl.html'
})

;

