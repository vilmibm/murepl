# Murepl Framework

Pronounced _murpull_. Rhymes with _purple_.

## Features

### secure user data

User profiles are kept safe from unauthenticated mutation (though are world
readable). Distributed client side code creates a keypair and registers the
public half + a username with a murepl server. Once this is done, communication
that modifies user state is done using signed JWTs.

### multiple access modes

* streaming _websockets, telnet_
* async _http_

### hackable

Gameworld can be modified by players.

### persistent

Gameworld and user information is periodically synced to disk. All commands by
all players are also logged.

### scripting / extension

Rich language of game functions / macros exposed. Possible to expand with new
functions. 

### multi tenant

Can define multiple gameworlds in a single running JVM.

### as needed encryption

communication can be encrypted on demand. users get a keypair as part of their
user profile.

### Per user namespaces

Users can define functions and vars that are persisted in a private namespace.

## Elements of State

All state (except transaction log) is stored in memory. Each collection of state
can be serialized on demand into persistent storage. Transaction log is written
to disk immediately.

* per-user namespaces
* user information
* item information
* room information
* temporal information (what elements are in what room)
* transaction log

## External (playing) API

This API consists of Clojure code that is serialized and eval'd on the server
side.

## External (low level) API

This API is a set of HTTP routes that allow someone to observe and mutate the
world through means other than Clojure. This API can be used, for example, to
build non-REPL UIs over a Murepl gameworld.

## Internal (setup, management) API

This API is for Gods who are setting up and managing gameworlds. It is a set of
Clojure functions and macros.

## Libraries and Tools

### Trapperkeeper

The various communication mechanisms are exposed as TK services. Each gameworld
is itself a service that encapsulates game state. A service can use multiple
communication mechanisms by opting into the ones it wants.

### Sqlite

The gameworld is serialized to a sqlite database. User information is stored as
encrypted strings which are decrypted post-query. SQLite is never directly
queried; it is merely cold storage for in memory refs.

### core.async

At its center, murepl uses core.async to handle incoming commands.

### TODO routing library

Murepl uses _TODO_ to expose its HTTP API.

### Clojail

Murepl prevents system-crashing commands using clojail.

### clojure.tools.namespace

The creation and management of namespaces is done via the built-in namespaces
library.

### clj-jwt

WTs are used for communication of sensitive information with a murepl server.

