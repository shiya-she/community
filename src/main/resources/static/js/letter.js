$(function () {
    $("#sendBtn").click(send_letter);
    $(".close").click(delete_msg);
});

function send_letter() {
    $("#sendModal").modal("hide");
    var toName = $("#recipient-name").val();
    var content = $("#message-text").val();
    $.post(
        CONTEXT_PATH + "/letter/send",
        {
            "toName": toName,
            "content": content
        },
        function (data) {
            data = $.parseJSON(data);
            var codeHandlers = {
                0: function () {
                    $("#hintBody").text("发送成功!");
                },
                401: function () {
                    $("#hintBody").text(data.msg);
                },
                1: function () {
                    $("#hintBody").text(data.msg);
                },
                default: function () {
                    $("#hintBody").text("网站异常，请联系客服！");
                }
            };
            var code = data.code;
            var handler = codeHandlers[code] || $("#hintBody").text(data.msg);
            handler();
            $("#hintModal").modal("show");
            setTimeout(function () {
                if (code === 401) {
                    window.location.href = CONTEXT_PATH + data.url;
                    return; // 提前结束函数执行
                }
                location.reload();
            }, 5000);
        }
    )

}

function delete_msg() {
    var messageId=$(this).val();
    var that=this;
    $.post(
        CONTEXT_PATH+"/letter/del",
        {
            "messageId":messageId,
        },
        function(data) {
            data=$.parseJSON(data);
            if (data.code ===200){
                // TODO 删除数据
                $(that).parents(".media").remove();
                alert(data.msg);
            }else{
                alert(data.msg);
            }

        }
    );

}