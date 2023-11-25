# DayZ CE Schema for VS Code
[![Visual Studio Marketplace Version (including pre-releases)](https://img.shields.io/visual-studio-marketplace/v/rvost.dayz-ce-schema?style=for-the-badge&logo=visualstudiocode&label=VS%20Marketplace&color=informational)
](https://marketplace.visualstudio.com/items?itemName=rvost.dayz-ce-schema)
[![Open VSX Version](https://img.shields.io/open-vsx/v/rvost/dayz-ce-schema?style=for-the-badge&logo=vscodium&color=informational)](https://open-vsx.org/extension/rvost/dayz-ce-schema)

This extension provides a streamlined way to consume the [DayZ Central Economy Schema](https://github.com/rvost/DayZ-Central-Economy-Schema/) and makes editing the XML configuration a little less tedious.

The extension automatically configures the [XML extension](https://marketplace.visualstudio.com/items?itemName=redhat.vscode-xml) for any workspace that resembles a DayZ server mission folder, so you get validation without any action required.

## Features

The extension automatically configures schema association for CE files (both XML and JSON), providing the following benefits:

- Autocompletion;
  ![Autocompletion](./assets/Autocompletion.gif)
- Validation;
  ![Validation](./assets/Validation.png)
- Tooltips on hover (WIP).
  ![Tooltips](./assets/TooltipsOnHover.gif)

If you have questions the extension can provide help on the active file:

![Open Documentation](./assets/OpenDocumentation.gif)

### Planed features

- Provide full project validation.
- Provide quick fixes for common validation errors.
- Provide actions to split and organise custom files.

## Requirements

**This extension requires [Java](https://www.java.com/)** (17 or newer), so make sure you have this installed.

It also relies on the [XML extension](https://marketplace.visualstudio.com/items?itemName=redhat.vscode-xml) for schema validation, but this can be installed automatically if you don't have it.

## Known Issues

Only open files can be validated. You won't get any validation errors until you open the file in the editor.

Schemas for modded files aren't bound automatically.
If you added new files in `cfgeconomycore.xml` you need to run `Update modded files associations` command from palette to update schema associations.

Completion and validation for Object Spawner custom lists currently only works  for json files in the `objectSpawners` folder.
For example, the `objectSpawners/nwaf.json` file will have completion and validation according to the [Object Spawner](https://community.bistudio.com/wiki?title=DayZ%3AObject_Spawner) rules, but `custom/berezino.json` or `altartrader.json` will not.
*This is due to limitations of the VS Code JSON Schema API and may be resolved in the future.* 

Completion and validation for Spawning Gear Configuration currently only works for json files in the `spawnPresets` folder **or** files which name ends with `_loadout` (e.g. `deathmatch_loadout.json`).

## Release Notes

### 0.7.1

- Added classnames completion and validation for `types`, `spawnabletypes` and `cfgrandompresets.xml` files.
- Improved completion and validation. See [CHANGELOG](CHANGELOG.md#071) for more details.
- Fixed high CPU usage at idle.
- Fixed completion for folder names in `cfgeconomycore.xml`.

### 0.7.0

- Added [Language server](https://microsoft.github.io/language-server-protocol/) for DayZ mission files. See [CHANGELOG](CHANGELOG.md#070) for more details.

### 0.6.0

- Stable release for the DayZ 1.23 Update.

### 0.5.0

- Updated schemas for DayZ 1.23 Experimental.
- Added schema for Spawning Gear Configuration.

### 0.4.0

- Added Ability for users to add documentation links.
- Added schema for the Object Spawner custom lists.
- Updated release workflow. From now on, odd minor version numbers will indicate pre-releases.

### 0.2.0

- Added schema associations for modded CE files.
- Added context aware documentation.

### 0.1.0

Initial release of the extension.

- Added schema association on activation for standard CE files.
