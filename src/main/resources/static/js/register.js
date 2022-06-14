function submit() {
    const a = document.getElementById('password');
    const b = document.getElementById('bigbox');
    const c = document.getElementById('user')
    if(c.value === ""){
        alert("请输入用户名！");
    }else{
        alert("注册成功！")
        setTimeout(jump, 0);
    }
}
function jump() {
    top.location.href = ("log_in.html");
}