# MapSite

A server-side mod for Necesse that provides a minimap viewable in the browser.

## Features

* Hosted webapp that works out-of-the-box
* Near-realtime updates keep the map in sync
    * Player locations
    * Settlement boundaries
    * Tile/Object changes
* Easily sharable urls, allowing for quick sharing of locations
* Highly configurable

## Configuration

MapSite is configurable via either in-game server commands or by editing the mod's config file.

| Name                 | Default Value | Description                                                       |
|----------------------|---------------|-------------------------------------------------------------------|
| webappPort           | 18571         | The server port used to host the webapp (ensure this is open)     |
| mapChunkSize         | 256           | Side length, in tiles, of map chunks in the browser               |
| mapChunkFetchRateMs  | 100           | How many milliseconds browsers should wait between chunk fetches  |
| playerUpdateRateMs   | 1000          | How frequently player information is refreshed in the browser     |
| mapUpdateRateMs      | 5000          | How frequently tile/object updates are refreshed in the browser   |
| mapChunkUpdateRateMs | 10000         | How frequently newly generated areas are refreshed in the browser |

### Configuring via in-game commands

The `/mapsite:config [option [value]]` command allows both viewing and changing configuration settings.

* To display all settings run `/mapsite:config` by itself
* To display the current value for a specific setting run `/mapsite:config <option>` (e.g. `/mapsite:config webappPort`)
* To change the value of a setting run `/mapsite:config <option> <value>` (e.g. `/mapsite:config mapChunkSize 128`)

Note that some settings will require the webapp to be restarted before taking effect.
Two additional commands have been added to make this easier.

* `/mapsite:start` starts the webapp if it is not running
* `/mapsite:stop` stops the webapp if it is running

### Configuring via file

Necesse stores mod configuration files in specific locations depending on the operating system.

* Windows: `%appdata%/Necesse/cfg/mods/`
* Mac: `~/Library/Application Support/Necesse/cfg/mods/`
* Linux: `~/.config/Necesse/cfg/mods/`

If not present, MapSite's configuration file (`snoobinoob.mapsite.cfg`) will be created when the game is first started.

This file can be edited using your favourite text editor.

## Accessing the map online

The webapp should start running automatically once the server starts.
Navigating to `<server-address>:<webappPort>` in any browser should display the server map. 