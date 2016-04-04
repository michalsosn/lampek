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
    // ctrl.operations = {idList: [ // todo: remove
    //   {"id":3,"done":true,"failed":false,"type":"START"},
    //   {"id":1,"done":true,"failed":false,"type":"NEGATE"},
    //   {"id":2,"done":true,"failed":false,"type":"UNIFORM_DENSITY"},
    //   {"id":4,"done":false,"failed":false,"type":"CHANGE_BRIGHTNESS"},
    //   {"id":9,"done":true,"failed":true,"type":"VALUE_HISTOGRAM"}
    // ]};

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

