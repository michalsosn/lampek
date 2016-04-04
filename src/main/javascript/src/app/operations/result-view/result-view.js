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
    onResultSelected: '&'
  },
  controller: function() {
    var ctrl = this;
    // ctrl.testResults2 = {"resultList":[{"role":"image","type":"IMAGE","_links":{"image":{"href":"http://localhost:8080/processes/llll/operations/3/results/image"},"self":{"href":"http://localhost:8080/processes/llll/operations/3/results"},"operation":{"href":"http://localhost:8080/processes/llll/operations/3"}}}],"_links":{"self":{"href":"http://localhost:8080/processes/llll/operations/3/results"}}};
    // ctrl.testResults = {"resultList":[
    //   {"role":"image1","type":"IMAGE","_links":{"image":{"href":"http://localhost:8080/processes/llll/operations/3/results/image"},"self":{"href":"http://localhost:8080/processes/llll/operations/3/results"},"operation":{"href":"http://localhost:8080/processes/llll/operations/3"}}},
    //   {"role":"double1","type":"DOUBLE",value:"10.666","_links":{"image":{"href":"http://localhost:8080/processes/llll/operations/3/results/image"},"self":{"href":"http://localhost:8080/processes/llll/operations/3/results"},"operation":{"href":"http://localhost:8080/processes/llll/operations/3"}}},
    //   {"role":"double2","type":"DOUBLE",value:"10.777","_links":{"image":{"href":"http://localhost:8080/processes/llll/operations/3/results/image"},"self":{"href":"http://localhost:8080/processes/llll/operations/3/results"},"operation":{"href":"http://localhost:8080/processes/llll/operations/3"}}},
    //   {"role":"histogram1","type":"HISTOGRAM",value:[0, 2, 4, 6, 8],"_links":{"image":{"href":"http://localhost:8080/processes/llll/operations/3/results/image"},"self":{"href":"http://localhost:8080/processes/llll/operations/3/results"},"operation":{"href":"http://localhost:8080/processes/llll/operations/3"}}},
    //   {"role":"image2","type":"IMAGE","_links":{"image":{"href":"http://localhost:8080/processes/llll/operations/3/results/image"},"self":{"href":"http://localhost:8080/processes/llll/operations/3/results"},"operation":{"href":"http://localhost:8080/processes/llll/operations/3"}}},
    //   {"role":"dupa","type":"DUPA","_links":{"image":{"href":"http://localhost:8080/processes/llll/operations/3/results/image"},"self":{"href":"http://localhost:8080/processes/llll/operations/3/results"},"operation":{"href":"http://localhost:8080/processes/llll/operations/3"}}}
    // ],"_links":{"self":{"href":"http://localhost:8080/processes/llll/operations/3/results"}}};
    // ctrl.results = function(dummy) { return ctrl.testResults; };
    // ctrl.testPlot = {
    //   data: [{y: [1, 2, 4, 8, 16]}],
    //   layout: {},
    //   options: {showLink: false, displayLogo: false}
    // };
  },
  templateUrl: 'operations/result-view/result-view.tpl.html'
})

;

