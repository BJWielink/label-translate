# Label Translate
Managing translation files can be a chore. This plugin aims to make it just a tad bit easier.

### Things that can be improved:
- When switching between translations files with unsaved work, the translation tab
    should update based on the 'draft' changes.
- The root node should not be recalculated for the whole file every time. Only
    the first time the root node has to be calculated completely. After that we
    should only update the nodes that were updated.
- Ideally we would only update the table model with nodes that were changed. So
    we basically have to compare two file nodes, and only update the translations
    that changed. This will prevent the UI from losing a certain state and is more
    efficient.
- Off-loading tree parsing to a different thread to prevent unnecessary blocking.