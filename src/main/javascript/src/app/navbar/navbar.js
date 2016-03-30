angular.module('lampek.navbar', [
  'ui.router'
])

.component('navbar', {
  bindings: {
    version: '@'
  },
  controller: function() {
    var ctrl = this;
    
    ctrl.menuCollapsed = true;
    
    ctrl.toggleCollapse = function() {
      ctrl.menuCollapsed = !ctrl.menuCollapsed;
    };
  },
  templateUrl: 'navbar/navbar.tpl.html'
})

;

