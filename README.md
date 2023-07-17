# DayZ CE Schema for VS Code

This extension provides a streamlined way to consume the [DayZ Central Economy Schema](https://github.com/rvost/DayZ-Central-Economy-Schema/) and makes editing the XML configuration a little less tedious.

The extension automatically configures the [XML extension](https://marketplace.visualstudio.com/items?itemName=redhat.vscode-xml) for any workspace that resembles a DayZ server mission folder, so you get validation without any action required.

## Features

- Automatically configures schema association for standard CE files.

## Planed features

- Make schema mappings aware of custom CE files (defined in `cfgeconomycore.xml`)
- Make validation context aware (e.g. you can't use flags that are not defined in `cfglimitsdefinition.xml`)
- Provide full project validation.

## Requirements

This extension relies on the [XML extension](https://marketplace.visualstudio.com/items?itemName=redhat.vscode-xml) for schema validation, so make sure you have this installed.

## Known Issues

Only open files can be validated. You won't get any validation errors until you open the file in the editor.

At the moment only standard CE files are mapped to the appropriate schemas. If you use custom files, you *may* need to map them manually. See the instructions in the XML extension [documentation](https://github.com/redhat-developer/vscode-xml/blob/main/docs/Validation.md#XML-file-association-with-xsd) and the DayZ Central Economy schema [repo](https://github.com/rvost/DayZ-Central-Economy-Schema/#how-to).
<!-- ## Release Notes

### 0.1.0

Initial release of the extension.

- Added schema association on activation for standard CE files. -->
