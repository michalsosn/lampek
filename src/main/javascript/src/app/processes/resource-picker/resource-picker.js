angular.module('lampek.processes.resource-picker', [
  'utils.autofocus',
  'ui.bootstrap'
]) 

.component('resourcePicker', {
  bindings: {
    resourceName: '@',
    processName: '@'
  },
  controller: function($filter, Image, Sound, Process) {
    var ctrl = this;
    ctrl.loadingResources = false;
    ctrl.noResults = false;

    ctrl.getResources = function(current) {
      var imageP = Image.query({page: 0, size: 1000}).$promise;
      var soundP = Sound.query({page: 0, size: 1000}).$promise;
      return Promise.all([imageP, soundP]).then(function(results) {
        var images = results[0].imageList.map(function(item) {
          return item.name;
        });
        var sounds = results[1].soundList.map(function(item) {
          return item.name;
        });
        images = $filter('filter')(images, current).slice(0, 5);
        sounds = $filter('filter')(sounds, current).slice(0, 5);
        return images.concat(sounds);
      });
    };

    ctrl.create = function() {
      var complete = function (request) {
        Process.replace(
          {processName: ctrl.processName},
          request
        );
        ctrl.processName = '';
        ctrl.resourceName = '';
      };
      Image.get({imageName: ctrl.resourceName}, function () {
        complete({type: 'LOAD_IMAGE', image: ctrl.resourceName});
      }, function () {
        Sound.get({soundName: ctrl.resourceName}, function () {
          complete({type: 'LOAD_SOUND', sound: ctrl.resourceName});
        });
      });
    };
  },
  templateUrl: 'processes/resource-picker/resource-picker.tpl.html'
})

;

