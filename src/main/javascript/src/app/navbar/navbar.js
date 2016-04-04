angular.module('lampek.navbar', [
  'ui.router'
])

.component('navbar', {
  bindings: {
    version: '@'
  },
  controller: function() {
    var ctrl = this;
    
    ctrl.states = [
      {name: 'home', title: 'Home', icon: 'home'},
      {name: 'images', title: 'Images', icon: 'camera'},
      {name: 'sounds', title: 'Sounds', icon: 'headphones', disabled: true},
      {name: 'processes', title: 'Processes', icon: 'cog'}
    ];

    ctrl.menuCollapsed = true;
    ctrl.toggleCollapse = function() {
      ctrl.menuCollapsed = !ctrl.menuCollapsed;
    };
  },
  templateUrl: 'navbar/navbar.tpl.html'
})

;

