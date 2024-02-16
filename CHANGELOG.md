# Change Log

## [Unreleased]

## [1.2.0]

### Added

- Support for copying external event spawns configurations.
- Extract/inline functionality for random presets in `cfgspawnabletypes.xml` files.
- Diagnostics for value and usage flags in `types.xml` files that can be replaced with user flags.
- Quick fix for flags that can be replaced with user flags.
- Extract/inline functionality for value and usage flags in `types.xml` files.
- Support for renaming:
    - events;
    - user-defined flags;
    - random presets;
    - event groups.
- `Find Reference`/`Find All References` implementation for:
    - Flags: lists references from `types.xml` (including custom files);
    - User-defined flags: lists references from `types.xml` (including custom files).
- References Code Lens for:
    - random presets;
    - flags;
    - user-defined flags. 

## [1.1.0]

### Added

- Hover with flag content tooltips for user-defined flags in `types.xml`(and custom files).
- Hover tooltips with human-readable time format for time values in following files:
    - `types.xml` (and custom files);
    - `events.xml` (and custom files);
    - `globals.xml` (and custom files);
    - `cfgweather.xml`.
- Link navigation for:
    - files in `cfgeconomycore.xml`;
    - files in `cfgenvironment.xml`.
- `Go To Declaration` implementation for:
    - Random presets in `cfgspawnabletypes.xml`;
    - User-defined flags in `types.xml`;
    - Event groups in `cfgeventspawns.xml`;
    - Map group prototypes in `cfgeventgroups.xml`.
- `Find Reference`/`Find All References` implementation for:
    - Types in `types.xml`: lists references from other types files and `cfgspawnabletypes.xml` (including custom files);
    - Events in `events.xml`: lists references from other events files and `cfgeventspawns.xml`;
    - Random presets: list references from `cfgspawnabletypes.xml` (including custom files);
    - Event groups: lists references from `cfgeventspawns.xml`;
    - Map group prototypes: lists references from `cfgeventgroups.xml`.
- Diagnostics for events that not referenced in `cfgeventspawns.xml`.
- Diagnostics for group references in `cfgeventspawns.xml`.
- Diagnostics for map group references in `cfgeventgroups.xml`.
- Autocompletion for map group names in `cfgeventgroups.xml`.

### Fixed

- Added `Ambient` to the list of valid event prefixes.

## [1.0.0]

### Added

- Support for copying elements from external custom files.
- Snippets for `types.xml`, `events.xml`, `messages.xml`, `cfgspawnabletypes.xml` and spawning gear configuration.
- Automatic schema resolution for custom files.

### Fixed

- Added support for new files when moving and copying elements of custom files.
- Disable mission-scoped validation for files outside the mission folder. This reduces error noise.

## [0.9.0]

### Added

- Completion and validation for `events.xml`:
    - Completion for classnames in `events.xml`;
    - Validation of event names;
    - Validation for classnames referenced in events.xml.
- Completion and validation for `cfgeventspawns.xml`:
    - Completion for event names;
    - Validation for event references.
- Validation for `globals.xml`.
- Completion and validation for `cfgenvironment.xml`:
    - Completion for file paths;
    - Context-sensitive completion for file references;
    - Validation for file paths and references.
- Completion and validation for `mapgroupproto.xml`:
    - Context-sensitive completion for limit flags;
    - Validation for event references.
    - Validation for limit flags.
- Completion and validation for `mapgrouppos.xml`.
- Mission file diagnostics:
    - Warning for unused custom files (not registered in `cfgeconomycore.xml`);
    - Notice message for files outside the mission folder.
    - Error if the type registered in `cfgeconomycore.xml` doesn't match the file content.
- Code actions:
    - Quick fix for registration of unused custom files in `cfgeconomycore.xml`;
    - Copy external files to the mission folder and register in `cfgeconomycore.xml`;
    - Quick fix for file type mismatch.
    - Move and copy elements between registered custom files.

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