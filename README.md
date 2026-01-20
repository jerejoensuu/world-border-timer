# World Border Timer

World Border Timer is a lightweight client-side Fabric mod that displays a configurable on-screen timer for world border shrink events. It shows how long remains until the border reaches its final size, or how long before the shrinking border reaches the player's current position.

This mod is useful for border-shrinking survival servers, minigames, events, and competitive scenarios where precise timing matters.

---

## Features

- Displays a HUD timer whenever the world border is actively shrinking.
- Shows:
  - Time until the border finishes shrinking (if the player is safely inside the final border size).
  - Time until the border reaches the player (if the player is in the path of the shrink).
- Customizable display position and text formatting through a simple JSON config.
- Fully client-side. No server installation required.

---

## Installation

1. Install the Fabric Loader (version compatible with your Minecraft version).
2. Install the Fabric API.
3. Download the latest release of **World Border Timer** from the releases page.
4. Place the `.jar` file in your `mods/` directory.

---

## Downloads

The latest release can be found here:

**https://github.com/jerejoensuu/world-border-timer/releases**

The release page includes the compiled mod `.jar` file required for installation.

---

## Configuration

The configuration file is created automatically on first launch:
`.minecraft/config/world_border_timer.json`

### Available settings

| Key                     | Type   | Description |
|-------------------------|--------|-------------|
| `timerAnchorX`          | float  | Horizontal anchor position as a fraction of screen width (0 = left, 1 = right). |
| `timerAnchorY`          | float  | Vertical anchor position as a fraction of screen height (0 = top, 1 = bottom). |
| `timerPixelOffsetX`     | int    | Horizontal pixel offset added to the anchor position. |
| `timerPixelOffsetY`     | int    | Vertical pixel offset subtracted from the anchor position. |
| `timerFormat`           | string | Format string for minutes and seconds (e.g., `%02d:%02d`). |
| `timerPrefix`           | string | Text displayed before the timer. |

Example (default):

```json
{
  "timerAnchorX": 0.0,
  "timerAnchorY": 1.0,
  "timerPixelOffsetX": 5,
  "timerPixelOffsetY": 15,
  "timerFormat": "%02d:%02d",
  "timerPrefix": "Border here in: "
}
