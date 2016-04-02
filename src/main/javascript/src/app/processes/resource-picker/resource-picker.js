angular.module('lampek.processes.resource-picker', [
  'ui.bootstrap'
]) 

.component('resourcePicker', {
  bindings: {
    resourceName: '@',
    processName: '@'
  },
  controller: function($filter, Image, Process) {
    var ctrl = this;
    ctrl.loadingResources = false;
    ctrl.noResults = false;

    ctrl.getResources = function(current) {
      return Image.query({page: 0, size: 1000}).$promise.then(
        function(images) {
          var names = images.nameList.map(function(item) {
            return item.name;
          });
          names = $filter('filter')(names, current);
          return names.slice(0, 10);
        });
    };

    ctrl.create = function() {
      Process.replace(
        {processName: ctrl.processName}, 
        {type: 'START', image: ctrl.resourceName});
      ctrl.processName = '';
      ctrl.resourceName = '';
    };
  },
  templateUrl: 'processes/resource-picker/resource-picker.tpl.html'
})

;

