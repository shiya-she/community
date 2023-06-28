function like(btn, entityType, entityId, entityUserId) {
    $.post(CONTEXT_PATH + '/like', {
        "entityType": entityType, "entityId": entityId, "entityUserId": entityUserId
    }, function (data) {
        data = $.parseJSON(data);
        if (data.code === 0) {
            $(btn).children("i").text(data.likeCount);
            $(btn).children("b").text(data.likeStatus == 1 ? "已赞" : "赞");
        } else if (data.code === 401) {
            alert(data.msg);
            setTimeout(function () {
                window.location.href = CONTEXT_PATH + data.url;
            }, 2000);
        } else {
            alert(data.msg);
        }
    });
}