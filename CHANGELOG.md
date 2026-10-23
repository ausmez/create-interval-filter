# Changelog

## 1.1.0 - 2026-10-23

### Changed

- Keep the same item at each spot in Sequential mode, even when driving back over a pattern.

### Added

- Restart the pattern when a train is picked up and moved with a wrench, instead of counting the move as distance. (#2)
- Pause placement while a contraption travels in reverse, keeping the pattern aligned when moving forward again.
  Forward is the front of a train, or the first direction other contraptions move.
- Simplified Chinese (zh_cn) translation, contributed by Zarijaden.

### Fixed

- Prevent slopes from adding extra distance and disrupting spacing. (#2)
- Prevent fast-moving contraptions and trains from skipping blocks or placing them incorrectly.
- Prevent teleporting or relocating a contraption from counting as distance traveled.
- Keep the pattern aligned when a train travels through unloaded areas or a portal.

## 1.0.0 - 2026-05-01

_Initial release._
