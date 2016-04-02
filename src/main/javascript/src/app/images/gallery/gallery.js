angular.module('lampek.images.gallery', [
    'ui.router',
    'lampek.resources'
])

.component('imagesGallery', {
  controller: function ($interval, $state, Image) {
    var ctrl = this;
    ctrl.page = 0;
    ctrl.pageRange = function() {
      if (ctrl.images) {
        return new Array(ctrl.images.pageCount);
      }
    };

    ctrl.processImage = function(image) {
      $state.go('processes', { chosenResource: image.name});
    };
    ctrl.removeImage = function(image) {
      Image.remove({imageName: image.name});
    };

    ctrl.refresh = function() {
      Image.query({page: ctrl.page, size: 12}, function(images) {
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

