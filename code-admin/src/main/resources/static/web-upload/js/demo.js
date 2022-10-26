//devin87@qq.com
(function () {
    "use strict";



    //------------------- export -------------------

    var UPLOAD_URL = window.localStorage ? localStorage.getItem("UPLOAD_URL") : undefined;

    window.UPLOAD_URL = ctx + "project/webUpload/upload/";

})();