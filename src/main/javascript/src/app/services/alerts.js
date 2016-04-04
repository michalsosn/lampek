angular.module('lampek.alerts', [
])

.service('alertService', function($rootScope) {
  $rootScope.alerts = [];
  
  this.addAlert = function(type, header, message) {
    $rootScope.alerts.push({
      type: type,
      header: header,
      message: message,
      close: function() {
        var index = $rootScope.alerts.indexOf(alert);
        $rootScope.alerts.splice(index, 1);
      }
    });
  };

  this.addDanger = this.addAlert.bind(undefined, 'danger');
  this.addWarning = this.addAlert.bind(undefined, 'warning');
  this.addInfo = this.addAlert.bind(undefined, 'info');
  this.addSuccess = this.addAlert.bind(undefined, 'success');

  this.closeAlert = function(index) {
    $rootScope.alerts.splice(index, 1);
  };
})

;
