//钉钉扫码登录
function show()  //显示隐藏层和弹出层
{
    //初始加载
    var appId = $("#appId").val();
    var projectUrl = $("#projectUrl").val();
    /*
     * 解释一下goto参数，参考以下例子：
     * var url = encodeURIComponent('http://localhost.me/index.php?test=1&aa=2');
     * var goto = encodeURIComponent('https://oapi.dingtalk.com/connect/oauth2/sns_authorize?appid=appid&response_type=code&scope=snsapi_login&state=STATE&redirect_uri='+url)
     */
    var redirectUrl = projectUrl+'loginScan';
    var goto = encodeURIComponent('https://oapi.dingtalk.com/connect/oauth2/sns_authorize?appid='+appId+'&response_type=code&scope=snsapi_login&state=STATE&redirect_uri='+redirectUrl);
    var obj = DDLogin({
        id:"login_container",//这里需要你在自己的页面定义一个HTML标签并设置id，例如<div id="login_container"></div>或<span id="login_container"></span>
        goto: goto, //请参考注释里的方式
        style: "border:none;background-color:#FFFFFF;",
        width : "300",
        height: "300"
    });
    var handleMessage = function (event) {
        var origin = event.origin;
        //console.log("origin", event.origin);
        if( origin == "https://login.dingtalk.com" ) { //判断是否来自ddLogin扫码事件。
            var loginTmpCode = event.data; //拿到loginTmpCode后就可以在这里构造跳转链接进行跳转了
            //console.log("loginTmpCode", loginTmpCode);
            window.location.href = 'https://oapi.dingtalk.com/connect/oauth2/sns_authorize?appid='+appId+'&response_type=code&scope=snsapi_login&state=STATE&redirect_uri='+redirectUrl+'&loginTmpCode='+ loginTmpCode;
        }
    };
    if (typeof window.addEventListener != 'undefined') {
        window.addEventListener('message', handleMessage, false);
    } else if (typeof window.attachEvent != 'undefined') {
        window.attachEvent('onmessage', handleMessage);
    }
}
