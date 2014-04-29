// originally inspired by the clojurescript console by Fogus.
// depends on jquery, jquery.console
(function (port) {
  var main,
  playerData,
  socket,
  console,
  validateCommand,
  handleCommand,
  handleSocketMessage,
  prompt,
  buildMessage,
  errorClass     = "jquery-console-message-error",
  successClass   = "jquery-console-success",
  blankClass     = "jquery-console-message-value",
  promptSelector = ".jquery-console-prompt:last",
  welcomeMessage = 'Welcome to MUREPL!\nIf you already have a character, run (connect "you name" "your password").\nIf not, try (ew-player "your name" "a plaintext, insecure password" "a description of yourself")';

  prompt = function (html) {
    // Get or set the prompt.
    var $prompt = $(promptSelector);

    if (html) {
      $prompt.html(html);
    }
    else {
      $prompt.html();
    }
  };

  buildMessage = function (className, msg) {
    // Build a message for display by the console.
    return {
      className: className
      msg: msg
    };
  };

  validateCommand = function (input) {
    // Prevalidates a command before it is sent to backend.
    return input.length > 0;
  };

  handleCommand = function (socket, line) {
    // Process a submitted command
    var input = line.trim(),
    result;

    if (line.startswith(";")) {
      return buildMessage(blankClass, "");
    }

    $.ajax({
      url: '/eval',
      data: {expr: line},
      async: false,
      success: function (data) { result = data; },
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

  handleSocketMessage = function (console_, data) {
    // Deal with data coming from the websocket. Should be bound with
    // an active console before use as an onMessage handler.

    lastPrompt = prompt();
    console_.msg(buildMessage(successClass, data.data));
    prompt(lastPrompt);
  };

  main = function () {

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

    socket.onMessage = handleSocketMessage.bind(null, console_);
  };

  $(main);
})(8889);
