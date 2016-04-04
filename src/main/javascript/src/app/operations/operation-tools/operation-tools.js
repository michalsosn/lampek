angular.module('lampek.operations.operation-tools', [
  'ngHandsontable',
  'ui.bootstrap',
  'capitalize',
  'validators',
  'lampek.resources'
])

.component('operationTools', {
  bindings: {
    processName: '<',
    selectedOperation: '<',
    resultName: '<',
    selectedSpec: '<',
    parameters: '<',
    onSpecSelected: '&'
  },
  controller: function($filter, Image, Process, Operation) {
    var ctrl = this;
    // ctrl.testSpecs = {"specifications":[{"lastResult":[],"parameters":{"imageEntity":{"type":"IMAGE"}},"type":"START"},{"lastResult":["IMAGE"],"parameters":{},"type":"NEGATE"},{"lastResult":["IMAGE"],"parameters":{"change":{"type":"INTEGER","min":-255,"max":255}},"type":"CHANGE_BRIGHTNESS"},{"lastResult":["IMAGE"],"parameters":{"minValue":{"type":"INTEGER","min":0,"max":255},"maxValue":{"type":"INTEGER","min":0,"max":255}},"type":"UNIFORM_DENSITY"},{"lastResult":["IMAGE"],"parameters":{},"type":"VALUE_HISTOGRAM"},{"lastResult":["IMAGE"],"parameters":{"kernel":{"type":"MATRIX"}},"type":"CONVOLUTION"}],"_links":{"self":{"href":"http://localhost:8080/processes/llll/specifications"},"processes":{"href":"http://localhost:8080/processes/llll"}}};
    // ctrl.specs = ctrl.testSpecs;
    ctrl.specs = Process.specify({processName: ctrl.processName});

    // todo to utils
    ctrl.isObjectEmpty = function(object) {
      return Object.keys(object).length === 0;
    };

    // TODO zrobić z tego komponent tu i w processes
    ctrl.loadingImages = false;
    ctrl.noImageResults = false;
    ctrl.getImages = function(current) {
      return Image.query({page: 0, size: 1000}).$promise.then(
        function(images) {
          var names = images.nameList.map(function(item) {
            return item.name;
          });
          names = $filter('filter')(names, current);
          return names.slice(0, 10);
        });
    };

    ctrl.insertOperation = function () {
      if (!ctrl.selectedSpec) {
        return;
      }
      
      ctrl.parameters.type = ctrl.selectedSpec.type;
      if (ctrl.selectedSpec.lastResult) {  // if the spec.lastResult != [] copy it
        ctrl.parameters.lastResult = ctrl.resultName;
      } else {
        ctrl.parameters.lastResult = undefined;
      }
      if (ctrl.selectedOperation) {
        Operation.save({
          processName: ctrl.processName,
          operationId: ctrl.selectedOperation.id
        }, ctrl.parameters);
      } else {
        Process.replace({
          processName: ctrl.processName
        }, ctrl.parameters);
      }
    };

    ctrl.replaceOperation = function () {
      if (!ctrl.selectedSpec || !ctrl.selectedOperation) {
        return;
      }
      Operation.get({
        processName: ctrl.processName,
        operationId: ctrl.selectedOperation.id
      }, function (operation) {
        ctrl.parameters.type = ctrl.selectedSpec.type;
        if (ctrl.selectedSpec.lastResult) {
          ctrl.parameters.lastResult = operation.operationRequest.lastResult;
        } else {
          ctrl.parameters.lastResult = undefined;
        }
        Operation.replace({
          processName: ctrl.processName,
          operationId: ctrl.selectedOperation.id
        }, ctrl.parameters);
      });
    };
  },
  templateUrl: 'operations/operation-tools/operation-tools.tpl.html'
})

;

