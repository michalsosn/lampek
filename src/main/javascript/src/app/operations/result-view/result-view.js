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

.factory('handleRelayout', function(Result) {
  return function (result, processName, operationId, prepareData, prepareLayout, eventdata) {
    // $log.log('HandleRelayout (' + processName + ', ' + operationId + ', ' + result + ')'); //\nEvent data: ' + JSON.stringify(eventdata));
    var range;
    if (eventdata['xaxis.range'] !== undefined) { // set on double click
      range = eventdata['xaxis.range'];
    } else if (eventdata['xaxis.range[0]'] !== undefined ||
      eventdata['xaxis.range[1]'] !== undefined) { // set on mouse drags
      range = [
        eventdata['xaxis.range[0]'], eventdata['xaxis.range[1]']
      ];
    } else if (eventdata['xaxis.autorange'] !== undefined) { // zooming out
      range = [undefined, undefined];
    } else {
      return;
    }
    var start = range[0];
    var end = range[1];
    Result.get({
      processName: processName,
      operationId: operationId,
      resultName: result.role,
      start: start,
      end: end
    }, function(resultValue) {
      if (prepareData !== undefined) {
        result.data = prepareData(resultValue);
      }
      if (prepareLayout !== undefined) {
        result.layout = prepareLayout(resultValue);
      }
    });
  };
})

.factory('prepareSound', function(Result, handleRelayout) {
  var prepareSoundData = function(value) {
    length = value.values.length;
    range = Array.apply(null, Array(length))
      .map(function (_, i) {
        return i * value.duration + value.startTime;
      });
    return [{
      type: 'scatter', x: range, y: value.values
    }];
  };

  return function (result, processName, operationId) {
    result.data = prepareSoundData(result.value);
    result.bindings = {
      plotly_relayout: handleRelayout.bind(
        undefined, result, processName, operationId, prepareSoundData, undefined
      )
    };
  };
})
  
.factory('prepareSoundSpectrum', function(Result, handleRelayout) {
  var prepareSoundSpectrumData = function (value) {
    var length = value.real.length;
    var frequencyRange = value.endFrequency - value.startFrequency;
    var range = Array.apply(null, Array(length))
      .map(function (_, i) {
        return i * frequencyRange / length + value.startFrequency;
      });
    return [{
      type: 'scatter', x: range, y: value.real, name: 'Real'
    }, {
      type: 'scatter', x: range, y: value.imaginary, name: 'Imaginary'
    }];
  };

  return function (result, processName, operationId) {
    result.data = prepareSoundSpectrumData(result.value);
    result.bindings = {
      plotly_relayout: handleRelayout.bind(
        undefined, result, processName, operationId, prepareSoundSpectrumData, undefined
      )
    };
  };
})

.factory('prepareSignal', function(Result, handleRelayout) {
  var prepareSignalData = function (value) {
    var length = value.values.length;
    var serverStep = (value.end - value.start) / length;
    var range = Array.apply(null, Array(length))
      .map(function (_, i) { return i * serverStep + value.start; });
    return [{
      type: 'scatter', x: range, y: value.values
    }];
  };

  var prepareSignalLayout = function (value) {
    var length = value.values.length;
    var serverStep = (value.end - value.start) / length;
    var step = Math.max(1, Math.floor(length / 20));
    var cutLength = Math.floor(length / step);
    var range = Array.apply(null, Array(cutLength))
      .map(function (_, i) { return i * step * serverStep + value.start; });
    var labels = Array.apply(null, Array(cutLength))
      .map(function (_, i) { return (value.frequency / (i * step * serverStep + value.start)).toFixed(2); });
    return {xaxis: {
      tickmode: "array",
      tickvals: range,
      ticktext: labels 
    }};
  };

  return function (result, processName, operationId) {
    result.data = prepareSignalData(result.value);
    result.layout = prepareSignalLayout(result.value);
    result.bindings = {
      plotly_relayout: handleRelayout.bind(
        undefined, result, processName, operationId, prepareSignalData, prepareSignalLayout
      )
    };
  };
})

.factory('preparePlot', function(prepareSound, prepareSoundSpectrum, prepareSignal) {
  return function (result, processName, operationId) {
    // $log.log('preparePlot(' + processName + ', ' + operationId + ', ' + result + ')');
    result.layout = { };
    result.options = {
      showLink: false,
      displayLogo: false
    };
    switch (result.type) {
      case 'IMAGE_HISTOGRAM':
        result.data = [{
          type: 'bar', y: result.value
        }];
        break;
      case 'SOUND':
        prepareSound(result, processName, operationId); 
        break;
      case 'SOUND_SPECTRUM':
        prepareSoundSpectrum(result, processName, operationId);
        break;
      case 'SIGNAL':
        prepareSignal(result, processName, operationId);
        break;
    }
  };
})
  
.factory('preparePlots', function(preparePlot) {
  return function(resultList, processName, operationId) {
    var len = resultList.length;

    for (var i = 0; i < len; ++i) {
      var result = resultList[i];
      preparePlot(result, processName, operationId);
    }
    return resultList;
  };
})
  
.factory('prepareResults', function(partitionResults, preparePlots) {
  return function (results, processName, operationId) {
    results.resultList = partitionResults(results.resultList);
    results.resultList = preparePlots(results.resultList, processName, operationId);
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
      if (result.type == 'IMAGE_HISTOGRAM') {
        result.layout = { update: Date.now() };
      }
      ctrl.onResultSelected({role: result.role, event: event});
    };
  },
  templateUrl: 'operations/result-view/result-view.tpl.html'
})

;

