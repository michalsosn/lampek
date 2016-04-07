angular.module('lampek.home.sign-in', [
  'utils.autofocus',
  'lampek.account',
  'lampek.alerts'
])

.component('signIn', {
  controller: function (account) {
    var ctrl = this;
    ctrl.account = account;
    clearForm();

    function clearForm() {
      ctrl.username = '';
      ctrl.password = '';
    }
    
    ctrl.signIn = function() {
      account.signIn(ctrl.username, ctrl.password);
      clearForm();
    };
    ctrl.register = function() {
      account.register(ctrl.username, ctrl.password);
      clearForm();
    };
  },
  templateUrl: 'home/sign-in/sign-in.tpl.html'
})

;

