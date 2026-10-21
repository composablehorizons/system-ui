# Changelog

## [Unreleased]

## [1.1.1] - 2026-10-21

- [Changes since v0.1.0](https://github.com/composablehorizons/system-ui/compare/v0.1.0...v1.1.1).

## [0.1.1] - 2026-10-21

### Fixed

- Keep Android system bar icons correct when outgoing screens leave during navigation, and restore the original appearance after the last override closes.
- Apply Android dialog icon changes to the dialog window without changing the screen underneath.

## [0.1.0] - 2026-10-16

### Added

- Control status-bar and navigation-bar icon appearance on Android.
- Control iOS status-bar appearance with `SystemUiViewController`.
- Expose appearance state for custom hosts and restore underlying requests when overrides leave composition.
- Support JVM and Wasm with no-op native system-bar implementations.
