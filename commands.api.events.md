# Commands, REST API and Events

## go (:north|:south|:up|:down|:west|:east)
POST _/move/:direction_
move object somewhere
auth: moves current player in specified direction.

## look
GET _/look_
information about current room
auth: shows information about room invoking user is in

## examine <object>
GET _/examine/:obj-id_
information about object
auth: anything in the same room as current player including itself

## say <msg>
POST _/say_
(body is message)

auth: for all objects in same room as player, a message is shown from them

## yell <msg>
POST _/yell_
(body is message)

auth: for all objects in same room as player, a message is shown IN ALL CAPS from them

## whisper “player” “msg”
POST _/whisper/:obj-id_
(body is message)
sends a private message to a user object
auth: can only target object in same room

## take <object>
POST _/take/:obj-id_
take an object from current room and store in inventory
auth: can only affect objects in current room not already in an inventory

## inventory
GET _/inventory_
auth: show items associated with current player

## drop <object>
POST _/drop/:obj-id_
auth: remove item from current player's inventory

## use <object> <verbexpr>
POST _/use/:obj-id/:verb/:verb-args_
activate an owned item
auth: can only use item in inventory of current player

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

## create-room <args>
POST _/create-room_
(body is JSON rep of room)

where args are:
 * name
 * description
 * exit-map

## create-bot <args>
POST _/create-bot_
(body is JSON rep of bot)

where args are:
 * name
 * description
 * event-map
 * verb-map

### Bot verbs
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
