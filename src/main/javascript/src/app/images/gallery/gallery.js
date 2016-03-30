angular.module('lampek.images.gallery', [
  'lampek.resources'
])

.component('imagesGallery', {
  controller: function ($interval, Image) {
    var ctrl = this;
    ctrl.page = 0;
    ctrl.pageRange = function() {
      if (ctrl.images) {
        return new Array(ctrl.images.pageCount);
      }
    };

    ctrl.refresh = function() {
      Image.query({page: ctrl.page, size: 12}, function (images) {
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

