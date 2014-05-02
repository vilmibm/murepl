// originally inspired by the clojurescript console by Fogus.
// depends on jquery, jquery.console
(function (port) {
  var playerData, // nasty state
  errorClass     = "jquery-console-message-error",
  successClass   = "jquery-console-success",
  blankClass     = "jquery-console-message-value",
  promptSelector = ".jquery-console-prompt:last",
  welcomeMessage = 'Welcome to MUREPL!\nIf you already have a character, run (connect "your name" "your password").\nIf not, try (new-player "your name" "a plaintext, insecure password" "a description of yourself")';

  function prompt (html) {
    // Get or set the prompt.
    var $prompt = $(promptSelector);

    if (html) {
      $prompt.html(html);
    }
    else {
      $prompt.html();
    }
  };

  function buildMessage (className, msg) {
    // Build a message for display by the console.
    return [{
      className: className,
      msg: msg
    }];
  };

  function validateCommand (input) {
    // Prevalidates a command before it is sent to backend.
    return input.length > 0;
  };

  function handleCommand (socket, line) {
    // Process a submitted command
    var input = line.trim(),
    result;

    if (line.startsWith(";")) {
      return buildMessage(blankClass, "");
    }

    // Why not blocking, here?
    $.ajax('/eval', {
      data: {expr: line},
      async: false,
      success: function (data) { result = data; },
      error: function () { throw arguments; },
      type: "POST",
      dataType: "json",
      contentType: "application/json; charset=utf-8"
    });

    if (result.error) {
      return buildMessage(errorClass, result.error);
    }

    if (result.player) {
      // handle setting player data
      if (!playerData) {
        playerData = result.player;
        socket.send(playerData.uuid); // register our existence to the events framework
      }
    }

    return buildMessage(successClass, result.msg);
  };

  function handleSocketMessage (console_, data) {
    // Deal with data coming from the websocket. Should be bound with
    // an active console before use as an onMessage handler.

    lastPrompt = prompt();
    console_.msg(buildMessage(successClass, data.data));
    prompt(lastPrompt);
  };

  function _main_ () {
    var socket, console_;

    socket = new WebSocket("ws://" + window.location.hostname + ":" + port + "/socket");

    $.ajaxPrefilter(function (options) {
      options.headers = {player: JSON.stringify(playerData)};
    });

    console_ = $("#console").console({
      welcomeMessage: welcomeMessage,
      promptLabel: "> ",
      commandValidate: validateCommand,
      commandHandle: handleCommand.bind(null, socket),
      autofocus: true,
      animateScroll: true,
      promptHistory: true
    });

    console__ = console_;

    socket.onMessage = handleSocketMessage.bind(null, console_);
  };

  $(_main_);
})(8889);
