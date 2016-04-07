angular.module('lampek.alerts', [
])

.service('alerts', function() {
  var self = this;
  self.alerts = [];
  
  self.addAlert = function(type, header, message) {
    self.alerts.push({
      type: type,
      header: header,
      message: message,
      close: function() {
        var index = self.alerts.indexOf(this);
        self.alerts.splice(index, 1);
      }
    });
  };

  self.addDanger = self.addAlert.bind(undefined, 'danger');
  self.addWarning = self.addAlert.bind(undefined, 'warning');
  self.addInfo = self.addAlert.bind(undefined, 'info');
  self.addSuccess = self.addAlert.bind(undefined, 'success');

  self.closeAlert = function(index) {
    self.alerts.splice(index, 1);
  };
})

;
