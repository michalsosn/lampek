angular.module('lampek.images.file-upload', [
  'angularFileUpload',
  'ui.bootstrap'
]) 

.component('fileUpload', {
  controller: function(FileUploader, account) {
    var ctrl = this;

    ctrl.uploader = new FileUploader({
      method: 'PUT',
      alias: 'file',
      removeAfterUpload: true,
      queueLimit: 10
    });
    ctrl.uploader.onBeforeUploadItem = function(item) {
      item.file.name = item.file.name.replace(/[^a-zA-Z0-9_]/g, '_');
      item.url = '/user/' + account.username + '/image/' + item.file.name;
    };
  },
  templateUrl: 'images/file-upload/file-upload.tpl.html'
})

;

