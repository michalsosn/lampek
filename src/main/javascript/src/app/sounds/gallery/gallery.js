angular.module('lampek.sounds.gallery', [
  'ui.router',
  'java-utils',
  'lampek.resources',
  'lampek.account'
])

.component('soundsGallery', {
  bindings: {
    username: '<' 
  },
  controller: function($interval, $state, Sound, account, javaUtils) {
    var ctrl = this;
    ctrl.fromInstant = javaUtils.fromInstant;
    
    ctrl.pageSize = 16;
    ctrl.page = 0;
    ctrl.pageRange = function() {
      if (ctrl.sounds) {
        return new Array(ctrl.sounds.pageCount);
      }
    };
    
    ctrl.account = account;
    ctrl.isOwner = function () {
      return account.equalsUsername(ctrl.username);
    };

    ctrl.processSound = function(sound) {
      $state.go('processes', {chosenResource: sound.name});
    };
    ctrl.removeSound = function(sound) {
      Sound.remove({username: ctrl.username, soundName: sound.name});
    };

    // TODO: make a service out of it (and the copy in process gallery)
    ctrl.refresh = function() {
      var page = ctrl.page;
      var pageSize = ctrl.pageSize;
      Sound.query({
        username: ctrl.username, 
        page: page, 
        size: pageSize
      }, function(sounds) {
        ctrl.sounds = sounds;
        ctrl.sounds.page = page;
        ctrl.sounds.pageSize = pageSize;
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
  templateUrl: 'sounds/gallery/gallery.tpl.html'
})

;

