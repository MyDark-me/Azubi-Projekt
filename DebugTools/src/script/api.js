$(function () {
    $('#register').on('submit', function (event) {
        event.preventDefault();
        const data = $("#register").serialize();
        console.log("query: " + data);
        $("#register #first").html("query: " + data);
        $.ajax({
            type: 'GET',
            url: "http://127.0.0.1:8001/api/user/create",
            data: data,
            async: true,
            dataType: "json",
            success: function (data, response) {
                console.log(response);
                $("#register #second").html(response);
            },
            error: function (xhr, status, errorThrown) {
                console.log(xhr.status + ": "+ errorThrown);
                console.log(xhr.responseText);
                $("#register #second").html(xhr.status + ": " + errorThrown + "<br>" + xhr.responseText);
            }

        });
    });
});