# Change Log

## [Unreleased]

## [0.7.1]

### Added

- Classnames completion and validation based on *registered* types files:
    - In *custom* `types` files;
    - In `spawnabletypes` files;
    - In `cfgrandompresets.xml`.

### Improved

- Context aware completion and diagnostics for flags:
    - Account for already used values in completion for `cfglimitsdefinitionsuser.xml`.
    - Diagnose duplicate values definitions in `cfglimitsdefinitionsuser.xml`.
    - Diagnose empty definitions in `cfglimitsdefinitionsuser.xml`.
- Completion and diagnostics for `types`:
    - Diagnose simultaneous use of name and user attributes in `types.xml`.
    - Account for already used values in completion for flag names.
    - Diagnose multiple category tags in `types.xml`.
- Diagnostics for `spawnabletypes`: 
    - Diagnose attributes and configuration when preset is specified.

### Fixed

- High CPU usage at idle.
- Completion for `cfgeconomycore.xml`:
    - Remove deleted folders from suggestions.
    - Add folder to suggestions if xml file created in it.

## [0.7.0]

### Added

- [Language server](https://microsoft.github.io/language-server-protocol/) for DayZ mission files that requires [Java](https://www.java.com/) (17 or newer) but offers the following features:
- Completion and validation for folders and filenames in the `ce` section of `cfgeconomycore.xml`.
- Context aware completion and validation for `category`, `tag`, `usage` and `value` flags in types.xml.
    - Available flags are updated after saving changes to `cfglimitsdefinitions.xml` and `cfglimitsdefinitionsuser.xml`
- Completion and validation for `usage` and `value` flags in the `cfglimitsdefinitionsuser.xml`.
- Completion and validation for presets in `cfgspawnabletypes.xml` based on `cfgrandompresets.xml`.

## [0.6.0]

*Stable release for the DayZ 1.23 Update.*

## [0.5.0]

### Added

- Schema for Spawning Gear Configuration for 1.23 game update.

## [0.4.0]

### Added

- Ability for users to add documentation links.
- Schema for the Object Spawner custom lists.

## [0.2.0]

### Added

- Schema associations for modded CE files.
- Context aware documentation.

## [0.1.0]

### Added 

- Schema association on activation for standard CE files.