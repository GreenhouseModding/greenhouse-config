## Changes
-Added `isClient` field to GreenhouseConfigHolder.Builder#postRegistryPopulation.
- GreenhouseConfigHolder.Builder#postRegistryPopulation and GreenhouseConfigHolder.Builder#postRegistryDepopulation now utilize unique functional interfaces.

## Bugfixes
### JSONC Language
- Fixed JSONC elements not implementing `equals` or `hashCode`.
- Fixed JSONCOps not creating a copy of the JSONC in the `remove` method.
- Fixed JSONC objects crashing when adding to an array using JSONCOps due to not having a internal JSON implementation.
