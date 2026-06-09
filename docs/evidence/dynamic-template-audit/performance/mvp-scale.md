# MVP Scale Performance

Target scale: a few thousand records, not 100k+.

Checkpoints:
- template schema load
- data-source query execution
- contract echo
- nested structure parse/render

For each checkpoint record the current path, risk, and whether EXPLAIN is needed before migration.
