angular.module('lampek.navbar', [
  'ui.router',
  'lampek.account'
])

.component('navbar', {
  bindings: {
    version: '@'
  },
  controller: function(account) {
    var ctrl = this;
    ctrl.account = account;
    
    ctrl.menuCollapsed = true;
    ctrl.toggleCollapse = function() {
      ctrl.menuCollapsed = !ctrl.menuCollapsed;
    };
  },
  templateUrl: 'navbar/navbar.tpl.html'
})

;

