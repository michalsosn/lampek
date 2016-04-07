angular.module('lampek.images.user-switch', [
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
  templateUrl: 'images/user-switch/user-switch.tpl.html'
})

;

