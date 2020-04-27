# purge-manager

This is an example that demonstrates implementing a custom purge manager.

The custom purge manager can be used as a replacement for (Least Recently Used) cache.

The code demonstrates 3 purge strategies. However, you can always write your own.

1. LowestPropertyPurgeStrategy - Purge based on the lowest value of a property. In our example, the space class has a sequence id defined on a property. E.g., ` @SpaceSequenceNumber`
2. RandomPurgeStrategy - Purge based on any entries that meet a broad sql criteria.
3. FifoPurgeStrategy - Purge based on first in first out (FIFO). The space class should support FIFO operations. E.g., `@SpaceClass(fifoSupport = FifoSupport.OPERATION)`

The PurgeManager class demonstrates how we can associate a purge strategy with a particular class. This class will poll repeatedly to find objects available to be purged.

The ClassPurgeConfig class contains the thresholds used by the PurgeManager to determine when to purge objects of a class.