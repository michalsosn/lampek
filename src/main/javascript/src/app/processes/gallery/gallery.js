angular.module('lampek.processes.gallery', [
  'ui.router',
  'java-utils',
  'lampek.resources'
])

.component('processesGallery', {
  controller: function ($state, $interval, Process, javaUtils) {
    var ctrl = this;
    ctrl.fromInstant = javaUtils.fromInstant;
    
    ctrl.pageSize = 20;
    ctrl.page = 0;
    ctrl.pageRange = function() {
      if (ctrl.processes) {
        return new Array(ctrl.processes.pageCount);
      }
    };

    ctrl.selectProcess = function(process) {
      $state.go('operations', {chosenProcess: process.name});
    };
    ctrl.removeProcess = function(process) {
      Process.remove({processName: process.name});
    };

    ctrl.refresh = function() {
      var page = ctrl.page;
      var pageSize = ctrl.pageSize;
      Process.query({page: page, size: pageSize}, function (processes) {
        ctrl.processes = processes;
        ctrl.processes.page = page;
        ctrl.processes.pageSize = pageSize;
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
  },
  templateUrl: 'processes/gallery/gallery.tpl.html'
})

;

