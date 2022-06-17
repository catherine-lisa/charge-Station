function getTime(){
    $.ajax({
        url: "/customer/getTime",
        type: "GET",
        data: {},
        success: function (result) {
            let time = "";
            time += "<div style=\"font-size: x-large;color: white\">" + result + "</div>";
            $("#time").html(time);
        }
    })
}