# JServerInformation

JServerInformation is a Bukkit plugin that exposes live Minecraft server data over HTTP by integrating with the [JWebAPI](https://github.com/JohnyMuffin/JWebAPI) platform. It ships a small collection of JSON endpoints that let external dashboards or tooling inspect who is online, read recent chat messages, monitor server performance, and (optionally) run console commands remotely.

## Features
- Registers REST-style endpoints under `/api/v1/server/*` through JWebAPI during server startup.【F:src/main/java/com/johnymuffin/serverinformation/beta/JServerInformation.java†L55-L73】
- Streams recent public chat with in-memory buffering for the last five minutes so external clients can poll for new messages.【F:src/main/java/com/johnymuffin/serverinformation/beta/JServerInformation.java†L96-L110】【F:src/main/java/com/johnymuffin/serverinformation/beta/ChatMessage.java†L10-L57】
- Lists currently connected players, including world/location metadata and integrations with the Fundamentals plugin for respecting fake quit/vanish status.【F:src/main/java/com/johnymuffin/serverinformation/beta/routes/api/v1/PlayersRoute.java†L19-L76】
- Reports recent TPS averages pulled from the Poseidon performance monitor so dashboards can track server health.【F:src/main/java/com/johnymuffin/serverinformation/beta/routes/api/v1/TPSRoute.java†L19-L67】
- Provides an opt-in API endpoint for executing console commands via authenticated POST requests.【F:src/main/java/com/johnymuffin/serverinformation/beta/routes/api/v1/ExecuteCommand.java†L19-L74】【F:src/main/java/com/johnymuffin/serverinformation/beta/Config.java†L24-L32】

## Requirements
- A Bukkit-compatible server (Spigot, Paper, etc.).
- [JWebAPI](https://github.com/JohnyMuffin/JWebAPI) must be installed and enabled; the plugin disables itself otherwise.【F:src/main/java/com/johnymuffin/serverinformation/beta/JServerInformation.java†L41-L68】
- Poseidon (for TPS metrics) and Fundamentals (for enhanced player data) are optional soft dependencies but recommended for full functionality.【F:src/main/resources/plugin.yml†L6-L9】【F:src/main/java/com/johnymuffin/serverinformation/beta/routes/api/v1/PlayersRoute.java†L31-L55】【F:src/main/java/com/johnymuffin/serverinformation/beta/routes/api/v1/TPSRoute.java†L7-L61】

## Installation
1. Build the plugin with Maven or download a pre-built JAR.
   ```bash
   mvn package
   ```
2. Place `JServerInformation-<version>.jar` into your server's `plugins/` directory along with the required JWebAPI plugin.
3. Restart the server. Successful startup logs will confirm the API routes are registered.【F:src/main/java/com/johnymuffin/serverinformation/beta/JServerInformation.java†L33-L74】

## Configuration
The plugin generates `plugins/JServerInformation/config.yml` on first run with the following options.【F:src/main/java/com/johnymuffin/serverinformation/beta/Config.java†L24-L46】

| Key | Default | Description |
| --- | --- | --- |
| `config-version` | `1` | Internal configuration version used for upgrades. |
| `api.command.execute.enable` | `false` | Enables the command execution endpoint when set to `true`. |
| `api.command.execute.key` | random 16 character string | API key that clients must send via the `Authorization` header when executing commands. |

> **Note:** The optional `api.command.execute.info` entry is informational and can be ignored by automated tooling.【F:src/main/java/com/johnymuffin/serverinformation/beta/Config.java†L24-L31】

After editing the configuration, restart (or reload) the server so changes take effect.

## HTTP API
All endpoints are registered through JWebAPI and respond with JSON. Base URL depends on your JWebAPI configuration; examples below assume the API is exposed at `https://example.com`.

### `GET /api/v1/server/players`
Returns the list of online players along with server capacity. When Fundamentals is installed, players who are fake quitting are omitted and vanished players report zeroed coordinates to avoid leaking their location.【F:src/main/java/com/johnymuffin/serverinformation/beta/routes/api/v1/PlayersRoute.java†L27-L63】

Sample response:
```json
{
  "error": false,
  "players": [
    {
      "name": "Notch",
      "uuid": "...",
      "display_name": "Notch",
      "world": "world",
      "world_uuid": "...",
      "world_environment": "NORMAL",
      "x": 123.0,
      "y": 64.0,
      "z": -45.0
    }
  ],
  "player_count": 1,
  "max_players": 20
}
```

### `GET /api/v1/server/chat`
Returns chat messages cached by the plugin. Provide `startUnixTime` (seconds since epoch) to fetch only messages newer than that timestamp.【F:src/main/java/com/johnymuffin/serverinformation/beta/routes/api/v1/ChatRoute.java†L17-L63】

Sample response:
```json
{
  "error": false,
  "messages": [
    {
      "uuid": "...",
      "username": "Alex",
      "display_name": "Alex",
      "message": "Hello!",
      "timestamp": 1716732000,
      "channel": "global",
      "code": "1a2b3c4d"
    }
  ],
  "unixTime": 1716732060,
  "startUnixTime": 1716732000
}
```

### `GET /api/v1/server/tps`
Provides performance metrics sourced from Poseidon, including rolling averages over 5 seconds up to 15 minutes. If Poseidon isn't installed or has no data, the averages default to 20.0 TPS.【F:src/main/java/com/johnymuffin/serverinformation/beta/routes/api/v1/TPSRoute.java†L19-L70】

Sample response:
```json
{
  "error": false,
  "tps_records": [],
  "tps_5s": 20.0,
  "tps_30s": 20.0,
  "tps_1m": 20.0,
  "tps_5m": 19.6,
  "tps_10m": 19.4,
  "tps_15m": 19.1
}
```

### `POST /api/v1/server/execute`
Executes one or more console commands when the feature is enabled in the configuration. Requests must supply the API key via the `Authorization` header and send a JSON array of command strings. Execution occurs on the main thread so commands behave exactly as if typed into the server console.【F:src/main/java/com/johnymuffin/serverinformation/beta/routes/api/v1/ExecuteCommand.java†L19-L74】

Example request:
```http
POST /api/v1/server/execute HTTP/1.1
Authorization: exampleapikey123
Content-Type: application/json

[
  "say Hello from the API!",
  "time set day"
]
```

Example response:
```json
{
  "error": false,
  "results": [
    {"command": "say Hello from the API!", "executed": true},
    {"command": "time set day", "executed": true}
  ]
}
```

If the payload cannot be parsed as a JSON array or the API key is missing/incorrect, the endpoint returns an error with the appropriate HTTP status code.【F:src/main/java/com/johnymuffin/serverinformation/beta/routes/api/v1/ExecuteCommand.java†L25-L74】

## Development
Clone the repository and build using Maven. The POM already declares the required repositories and dependencies for Poseidon, JWebAPI, Jetty, and Fundamentals.【F:pom.xml†L1-L81】

```bash
git clone https://github.com/your-org/JServerInformation.git
cd JServerInformation
mvn package
```

During development, JServerInformation ensures it unregisters routes when disabled or when JWebAPI shuts down, helping avoid stale servlet registrations while testing reloads.【F:src/main/java/com/johnymuffin/serverinformation/beta/JServerInformation.java†L75-L95】

## License
This repository does not currently specify a license. Please consult the repository owner before redistributing builds.
