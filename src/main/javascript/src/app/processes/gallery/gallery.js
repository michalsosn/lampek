angular.module('lampek.processes.gallery', [
  'lampek.resources'
])

.component('processesGallery', {
  controller: function ($interval, Process) {
    var ctrl = this;
    ctrl.page = 0;
    ctrl.pageRange = function() {
      if (ctrl.processes) {
        return new Array(ctrl.processes.pageCount);
      }
    };
    
    ctrl.removeProcess = function(process) {
      Process.remove({processName: process.name});
    };

    ctrl.refresh = function() {
      Process.query({page: ctrl.page, size: 12}, function (processes) {
        ctrl.processes = processes;
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

