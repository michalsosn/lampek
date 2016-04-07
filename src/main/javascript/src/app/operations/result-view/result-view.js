angular.module('lampek.operations.result-view', [
  'ui.bootstrap',
  'plotly',
  'capitalize',
  'my-utils',
  'lampek.resources'
])
  
.factory('hasNumericalType', function() {
  return function(result) {
    return result.type === 'DOUBLE' || result.type === 'INTEGER';
  };
})
  
.factory('partitionResults', function(myUtils, hasNumericalType) {
  return function(resultList) {
    var partitioned = myUtils.partition(resultList, hasNumericalType);
    if (partitioned[0].length) {
      partitioned[1].push({
        role: 'Numerical',
        type: '$NUMERICAL',
        values: partitioned[0]
      });
    }
    return partitioned[1];
  };
})
  
.factory('preparePlots', function() {
  return function(resultList) {
    var len = resultList.length;
    for (var i = 0; i < len; ++i) {
      var result = resultList[i];
      if (result.type == 'HISTOGRAM') {
        result.data = [{
          type: 'bar',
          y: result.value
        }];
        result.layout = { };
        result.options = {
          showLink: false,
          displayLogo: false
        };
      }
    }
    return resultList;
  };
})
  
.factory('prepareResults', function(partitionResults, preparePlots) {
  return function (results) {
    results.resultList = partitionResults(results.resultList);
    results.resultList = preparePlots(results.resultList);
    return results;
  };
})

.component('resultView', {
  bindings: {
    processName: '<',
    results: '<',
    resultName: '<',
    seed: '<',
    onResultSelected: '&'
  },
  controller: function() {
    var ctrl = this;
    ctrl.seeded = function (string) {
      return string + '?' + ctrl.seed; 
    };
    ctrl.selectResult = function (result, event) {
      if (result.type == 'HISTOGRAM') {
        result.layout = { update: Date.now() };
      }
      ctrl.onResultSelected({role: result.role, event: event});
    };
  },
  templateUrl: 'operations/result-view/result-view.tpl.html'
})

;

