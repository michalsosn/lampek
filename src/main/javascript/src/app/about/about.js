angular.module('lampek.about', [
  'placeholders.txt',
  'placeholders.img',
  'ui.bootstrap',
  'ui.router'
])

.config(function($stateProvider) {
  $stateProvider.state('about', {
    url: '/about',
    views: {
      "main": {
        controller: 'AboutController',
        templateUrl: 'about/about.tpl.html'
      }
    },
    data: { pageTitle: 'What is It?' }
  });
})

.controller('AboutController', function($scope) {
  // This is simple a demo for UI Boostrap.
  $scope.dropdownDemoItems = [
    "The first choice!",
    "And another choice for you.",
    "but wait! A third!"
  ];
})

;
