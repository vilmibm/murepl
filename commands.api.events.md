Preamble:

* Location is always implied as that is stored on the server
* /v1 prefix on all routes
* *all* calls signed with an auth sig (usename+password in base64. for
  convenience, not security)
* All HTTP because it's just a demo


# Commands, REST API and Events

## go (:north|:south|:up|:down|:west|:east)
POST _/move/:direction_

move object somewhere

auth: moves current player in specified direction.

    (go :north)

/move/north

> You walk north.
> You enter the dungeon.

## look
GET _/look_

information about current room  

auth: shows information about room invoking user is in

    (look)

/look

> Basement Room (room id: 7)
> You are in a dark room. You can see nothing.

## examine <object>
GET _/examine/:obj-id_

information about object

auth: anything in the same room as current player including itself

_room contains a "green banana" (id 0) a "yellow banana" (id 1), and a "shoe" (id 2)_

    (examine "shoe")

-> front-end looks at items in room. finds "shoe" string match for

shoe

/examine/2

> The shoe is red.

    (examine "banana")

-> front-end doesn't know what object name to match.

> Which "banana" do you mean?

    (examine "yellow banana")

-> front-end makes string match

/examine/1

> The banana is yellow.

## say <msg>
POST _/say_

(body is message)

auth: for all objects in same room as player, a message is shown from them

    (say "greetings, all")

/say

body: "greetings, all"

> Joe says "greetings, all"

## yell <msg>
POST _/yell_

(body is message)

auth: for all objects in same room as player, a message is shown IN ALL CAPS! from them

    (yell "hi")

/yell

body: "hi"

> Joe yells, "HI!"

## whisper <player> <msg>
POST _/whisper/:obj-id_

(body is message)

sends a private message to a user object

auth: can only target object in same room

_room contains user Joe (id 4)_

    (whisper "joe" "hi there")

-> front-end makes string match

/whisper/4

body: "hi there"

> Lucy whispers to you, "hi there"

## take <object>
POST _/take/:obj-id_

take an object from current room and store in inventory

auth: can only affect objects in current room not already in an inventory

    (take "green banana")

-> front-end makes string match

/take/0

> You take the green banana.

## inventory
GET _/inventory_

auth: show items associated with current player

    (inventory)

/inventory

> Your backpack contains:
> * a green banana

## drop <object>
POST _/drop/:obj-id_

auth: remove item from current player's inventory

    (drop "green banana")

/drop/0

> You drop the green banana

## use <object> <verbexpr>
POST _/use/:obj-id/:verb/:verb-arg_

activate an owned item

auth: can only use item in inventory of current player

_object "dagger" (id 5) can injure_

    (use "dagger" :injure "joe")

/use/5/injure/4

> Lucy uses the dagger to injure Joe!
> Joe loses one HP.

## create-object <args>
POST _/create-object_

(body is JSON rep of object)

where args are:
 * name
 * description
 * event-map
 * verb-map

Generate a new object. It goes into user's inventory.

### Events sent to objects
 * :dropped _user drops object_
 * :picked-up _user takes object_
 * :examined _user examines object_
 * :passed _user enters room that contains object_

### Object verbs
 * injure <object id>
 * defend <object id>
 * heal <object id>

### Object scripting API
 * msg _print message to screen_
 * explode _break, deal a damage_
 * break _reduce HP to 0_

        (create-obj ;; MACRO
          :name "banana peel"
          :desc "The peel of a banana. It's gross and oxidizing."
          :events {:dropped (break), :passed (comp break (partial msg "You slip on a banana peel. It is destroyed. You fall down and suffer damage."))}
/create-object/
body: {"name":..., "desc":..., "events":{"dropped": "(break)"}}
> You created a banana peel. You slip it into your backpack.

## create-room <args>
POST _/create-room_

(body is JSON rep of room)

where args are:
 * name
 * description
 * exit-map

        (create-room
          :name "Broom Closet"
          :desc "A tiny broom clost. There is a duct here that goes north. You came in from the south."
          :exit-map {:north 7, :south 8}

/create-room

body: {"name": "...", "desc":"...", "exits": {"north": 7, "south":8}}

> You've added a room! It is attached to the Dim Hallway (id 8) and Basement Room (id 7)

## create-bot <args>
POST _/create-bot_

(body is JSON rep of bot)

where args are:
 * name
 * description
 * event-map

### Bot API
 * say <msg>
 * whisper <msg>
 * yell <msg>
 * injure <object_id>
 * defend <object_id>
 * heal <object_id>

### Events sent to bots
 * :examined _user examines bot_
 * :passed _user enters room that contains bot_
 * :injured _bot is injured_
 * :healed _bot is healed_
 * :whispered _bot is whispered to_
 * :heard _bot is privy to someone's yell or say command_

        (create-bot ;; MACRO
          :name "Living Statue"
          :desc "This eerie statue glowers at you with moving eyes. It is otherwise still"
          :events {:passed (yell "boo")})

/create-bot

body: {"name": "...", "desc":"...", "events":{"passed": "(cljs)"}}

> You've summoned a bot! The Living Statue stands before you.
