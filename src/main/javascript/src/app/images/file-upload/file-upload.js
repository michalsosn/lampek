angular.module('lampek.images.file-upload', [
  'angularFileUpload',
  'ui.bootstrap'
]) 

.component('fileUpload', {
  controller: function(FileUploader) {
    var ctrl = this;

    ctrl.uploader = new FileUploader({
      url: '/images',
      alias: 'file',
      removeAfterUpload: true,
      queueLimit: 10
    });
    ctrl.uploader.onBeforeUploadItem = function(item) {
        item.formData = [{name: item.file.name}];
    };
  },
  templateUrl: 'images/file-upload/file-upload.tpl.html'
})

;

