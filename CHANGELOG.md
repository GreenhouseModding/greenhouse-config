## Major Changes
- Config languages are now separate from Greenhouse Config.
  - You will need to include these dependencies separately from Greenhouse Config, the single JAR works across both loaders, as no Minecraft code is referenced within it.
- Updated `backwardsCompat` related fields to utilise data fixers. These fields have been renamed to `dataFixer` respectively.
  - (Currently undocumented) Examples are found within the test source set.

## Minor Changes
- Added `getUnsynced` method, for getting the client config values without any server interference.
- Simplified internal JSONC writing. Thank you to Echo from [Spirit Studios](https://github.com/SpiritGameStudios) for allowing me to utilise the simplified JSONC writing code.