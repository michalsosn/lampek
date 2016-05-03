angular.module('lampek.user-switch', [
  'angularFileUpload',
  'ui.bootstrap',
  'lampek.account'
]) 

.component('userSwitch', {
  bindings: {
    username: '<',
    onUserSwitch: '&'
  },
  controller: function (account) {
    var ctrl = this;
    ctrl.isOwner = function () {
      return account.equalsUsername(ctrl.username);
    };
  },
  templateUrl: 'user-switch/user-switch.tpl.html'
})

;

