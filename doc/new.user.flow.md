# Workflow for new users

_no credentials present (cookie or localstorage):_

 1. Prompt for new player information

    > Hello! blah blah, make a new character with the (new-player) function like so:
    > (new-player :name "Joe" :password "FooBar" :desc "Just a random person")

 11. User runs new-player

 2. Generate uuid for auth details

 3. Store auth credentials in localStorage: {"name":"...",
    "password":"...", "desc":"...", "uuid": "..."}

 4. *Any API request uses a make-api function that signs everything
    with the auth object.*

 5. Backend will create user objects for new uuids.

 6. Put player (in room 0)

        /new-player
        body: auth details

 7. Tell user

    > You find yourself in a windowless room.


