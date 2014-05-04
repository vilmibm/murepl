// originally inspired by the clojurescript console by Fogus.
// depends on jquery, jquery.console
(function (port) {
  var prefilterSet = false, // nasty state
  errorClass       = "jquery-console-message-error",
  successClass     = "jquery-console-success",
  blankClass       = "jquery-console-message-value",
  promptSelector   = ".jquery-console-prompt:last",
  welcomeMessage   = 'Welcome to MUREPL!\nIf you already have a character, run (connect "your name" "your password").\nIf not, try (new-player "your name" "a plaintext, insecure password" "a description of yourself")';

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

    if (input.match(/^;/)) {
      return buildMessage(blankClass, "");
    }

    console.log("Attempting to eval: ", input);
    // Why not blocking, here?
    $.ajax('/eval', {
      data: {expr: input},
      async: false,
      success: function (data) { result = data; },
      error: function () { throw arguments; },
      type: "POST",
      dataType: "json",
      contentType: "application/x-www-form-urlencoded; charset=utf-8"
    });

    if (result.error) {
      return buildMessage(errorClass, result.error);
    }

    if (result.player && !prefilterSet) {
      console.log("Player data in response: ", result.player);

      $.ajaxPrefilter(function (options) {
        console.log("Adding player data json to headers");
        options.headers = {player: JSON.stringify(result.player)};
      });

      prefilterSet = true;

      console.log("Registering with events framework: ", result.player.uuid);
      socket.send(result.player.uuid); // register our existence to the events framework
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
