var murepl = {};
(function () {
    var PORT = 7999;
    var hostname = window.location.hostname;

    function main() {
        var socket;

        socket = new WebSocket("ws://" + hostname + ":" + PORT);
        socket.onmessage = function () {
            console.log(arguments);
        };

        window.beforeunload = function() {
            socket.close();
        };

        murepl.socket = socket;
    }

    window.onload = main;

})();
