function submit() {
    let userName = document.getElementById("user")[0].value;
    let password = document.getElementById("password")[0].value;
    $.ajax({
        url: "/login",
        type: "POST",
        data: {"userName": userName, "password": password},
        success: function (result) {
            if (result == "success") {
                window.location.href = "/requestRecharge";
            } else if(result == "usernotexist") {
                alert("用户还未注册！");
            } else if(result == "logInFailed") {
                alert("登录失败！");
            }
        }
    });

    // const a = document.getElementById('password');
    // const b = document.getElementById('bigbox');
    // const c = document.getElementById('user')
    // if(c.value === ""){
    //     alert("请输入用户名！");
    // }else if (a.value === '123') {
    //     console.log('yes');
    //     b.style.animation = 'myAnimation 5s';
    //     b.style.animationFillMode = 'forwards';
    //     setTimeout(jump, 4000);
    // }else{
    //     alert("密码错误！");
    // }
}
// function jump() {
//     top.location.href = ("submitRequest.html");
// }