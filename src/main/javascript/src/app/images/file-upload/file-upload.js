angular.module('lampek.images.file-upload', [
  'angularFileUpload',
  'ui.bootstrap'
]) 

.component('fileUpload', {
  controller: function(FileUploader) {
    var ctrl = this;

    ctrl.uploader = new FileUploader({
      url: '/images',
      method: 'PUT',
      alias: 'file',
      removeAfterUpload: true,
      queueLimit: 10
    });
    ctrl.uploader.onBeforeUploadItem = function(item) {
      item.file.name = item.file.name.replace(/[^a-zA-Z0-9_]/g, '_');
      item.url = '/images/' + item.file.name;
    };
  },
  templateUrl: 'images/file-upload/file-upload.tpl.html'
})

;

