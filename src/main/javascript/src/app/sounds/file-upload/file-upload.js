angular.module('lampek.sounds.file-upload', [
  'angularFileUpload',
  'ui.bootstrap'
]) 

.component('soundUpload', {
  controller: function(FileUploader, account) {
    var ctrl = this;

    ctrl.uploader = new FileUploader({
      method: 'PUT',
      alias: 'file',
      removeAfterUpload: true,
      queueLimit: 20
    });
    ctrl.uploader.onBeforeUploadItem = function(item) {
      item.file.name = item.file.name.replace(/[^a-zA-Z0-9_]/g, '_');
      item.url = '/user/' + account.username + '/sound/' + item.file.name;
    };
  },
  templateUrl: 'sounds/file-upload/file-upload.tpl.html'
})

;

