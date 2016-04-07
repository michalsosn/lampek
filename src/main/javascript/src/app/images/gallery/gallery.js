angular.module('lampek.images.gallery', [
  'ui.router',
  'lampek.resources',
  'lampek.account'
])

.component('imagesGallery', {
  bindings: {
    username: '<' 
  },
  controller: function($interval, $state, Image, account) {
    var ctrl = this;
    ctrl.pageSize = 12;
    ctrl.page = 0;
    ctrl.pageRange = function() {
      if (ctrl.images) {
        return new Array(ctrl.images.pageCount);
      }
    };
    
    ctrl.account = account;
    ctrl.isOwner = function () {
      return account.equalsUsername(ctrl.username);
    };

    ctrl.processImage = function(image) {
      $state.go('processes', {chosenResource: image.name});
    };
    ctrl.removeImage = function(image) {
      Image.remove({username: ctrl.username, imageName: image.name});
    };

    // TODO: make a service out of it (and the copy in process gallery)
    ctrl.refresh = function() {
      Image.query({
        username: ctrl.username, 
        page: ctrl.page, 
        size: ctrl.pageSize
      }, function(images) {
        ctrl.images = images;
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
  templateUrl: 'images/gallery/gallery.tpl.html'
})

;

