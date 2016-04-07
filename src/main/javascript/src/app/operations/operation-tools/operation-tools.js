angular.module('lampek.operations.operation-tools', [
  'ngHandsontable',
  'ui.bootstrap',
  'capitalize',
  'my-utils',
  'validators',
  'lampek.resources'
])
  
.factory('prepareParams', function() {
  return function (params) {
    for (var param in params) {
      if (params[param].description === undefined) {
        params[param].description = param;
      }
    } 
    return params;
  };
})

.factory('prepareSpecDescription', function($filter) {
  var typeCleaner = new RegExp('_', 'g');
  return function (spec) {
    if (spec.description === undefined) {
      var description = $filter('capitalize')(spec.type);
      description = description.replace(typeCleaner, ' ');
      spec.description = description;
    }
    return spec;
  };
})

.factory('specsToCatList', function(prepareSpecDescription) {
  return function (specs) {
    var categories = { };
    var specList = specs.specifications;
    var specLen = specList.length;
    for (var i = 0; i < specLen; ++i) {
      var spec = specList[i];
      prepareSpecDescription(spec);
      
      var category = spec.category;
      if (category === undefined) {
        category = spec.category = 'Default';
      }
      if (categories[category] === undefined) {
        categories[category] = [spec];
      }
      else {
        categories[category].push(spec);
      }
    }
    
    var catList = [];
    for (var name in categories) {
      catList.push({name: name, specList: categories[name]});
    }
    return catList;
  };
})
  
.service('specsHolder', function() {
  this.specs = {};

  this.findSpecIndex = function (spec) {
    var category = spec.category;
    if (category === undefined) {
      category = 'Default';
    }
    
    var len = this.specs.catList.length;
    for (var i = 0; i < len; ++i) {
      if (this.specs.catList[i].name === category) {
        return i;
      }
    }
  };
})

.component('operationTools', {
  bindings: {
    processName: '<',
    selectedOperation: '<',
    selectedCategory: '<',
    resultName: '<',
    selectedSpec: '<',
    parameters: '<',
    onSpecSelected: '&',
    onOperationInserted: '&'
  },
  controller: function($filter, myUtils, Image, Process, Operation, prepareParams, specsToCatList, specsHolder) {
    var ctrl = this;
    ctrl.isObjectEmpty = myUtils.isObjectEmpty;
    ctrl.specs = specsHolder.specs;

    Process.specify({processName: ctrl.processName}, function (specs) {
      ctrl.specs.catList = specsToCatList(specs);
    });

    ctrl.preparedParams = function () {
      if (ctrl.selectedSpec) {
        return prepareParams(ctrl.selectedSpec.parameters);
      }
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
        }, ctrl.parameters, function (operation) {
          ctrl.onOperationInserted({operation: operation});
        });
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
        Operation.save({
          processName: ctrl.processName,
          operationId: ctrl.selectedOperation.id,
          replace: true
        }, ctrl.parameters, function (operation) {
          ctrl.onOperationInserted({operation: operation});
        });
      });
    };
  },
  templateUrl: 'operations/operation-tools/operation-tools.tpl.html'
})

;

