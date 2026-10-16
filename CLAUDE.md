# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project overview

JPen is a Java library for accessing pen/digitizer tablets and pointing devices via JNI. It is a fork of the upstream `net.sourceforge.jpen` (GroupId `net.sourceforge.jpen`, artifactId `jpen`, version `2.1-SNAPSHOT`). Main upstream docs: http://jpen.sf.net.

The build produces a NAR (Native ARchive) that bundles Java classes + a platform-specific native library (`.so` / `.jnilib` / `.dll`) that talks to the OS tablet APIs: XInput (Linux), Cocoa/Carbon (macOS), Wintab (Windows).

## Build

Build system: Maven with `nar-maven-plugin` (packaging = `nar`). Default goal is `integration-test`.

- Full build (compile Java + compile native + package NAR + run integration tests): `mvn`
- Clean build: `mvn clean install`
- Skip surefire unit tests (already skipped by default via `<skip>true</skip>`); real test is run as nar-integration-test (`SmokeTest`).
- Produce only the jar/nar without integration test: `mvn package`

The active profile is selected automatically by OS/arch:
- `amd64` — Linux x86_64 (links `-lXi`)
- `i386-on-amd64` — if `/usr/include/gnu/stubs-32.h` exists, also builds a 32-bit `.so` via a manual `gcc -m32` antrun task
- `osx` — macOS (requires legacy `JavaVM.framework` headers path to exist; builds x86_64 with `-mmacosx-version-min=10.9`, links `Cocoa`/`Carbon`/`JavaVM`)
- `win-64` / `win-32` — Windows (MinGW-w64 toolchain; links `WINTAB64`/`WINTAB32` from `src/main/c/windows/lib[-64]`, then re-links manually with `-Wl,-kill-at` because `nar-maven-plugin` emits the wrong linker switch)

`build.properties` and `module.properties` are **generated** by `build-scripts/updateNativeBuildProperties.sh` and read into Maven via `properties-maven-plugin` at `initialize`. Do not hand-edit `build.properties` — the header says so. If native code changes, bump the matching `*.nativeVersion` / `*.nativeBuild` through the script.

Target bytecode is Java 1.6 (`module.javacTarget=1.6`); keep source compatible unless intentionally raising the floor.

## Native code layout quirks

- C sources live under `src/main/c/{linux,osx,windows}/`; the active one is selected by setting `source.dir` per profile.
- JNI headers are hand-maintained under `src/main/c/` (e.g. `jpen_provider_wintab_WintabAccess.h`). The `javah` goal of `nar-maven-plugin` is **disabled** (`<phase>none</phase>`) on osx/windows profiles because `javah` was removed from modern JDKs; do not re-enable it. If you add a new `native` method, regenerate the header manually with `javac -h` and commit it.
- `WintabAccess.class` is excluded from `javah` even on the base config (see pom comment).
- On macOS, `pre-integration-test` copies the built `.jnilib` to the project root and `target/` as a workaround for `nar-integration-test` not setting `java.library.path` on 10.12+. Keep that copy step if touching the osx profile.

## Code architecture

Top-level package `jpen` contains the public API; `jpen.provider.*` contains the per-OS JNI backends.

- `Pen` + `PenManager` — core façade. `PenManager` wires one or more `PenProvider`s (OS backends) into a single `Pen` instance that fires `PenEvent`s. Consumers typically only touch `PenManager` and `PenListener`.
- `PenState` / `PenStateCopy` — snapshot of pen levels/buttons/kind at a point in time.
- `PLevel`, `PButton`, `PKind`, `PScroll` — the value types; each has a matching `*Event`.
- `PLevelEmulator` + `EmulationProvider` — synthesize missing levels (e.g. tilt from pressure deltas) when the hardware/driver does not report them.
- `PenScheduler` — thread/timing layer that drains the native event queue and dispatches to listeners on a well-defined thread.
- `jpen.provider.AbstractPenProvider` / `AbstractPenDevice` — base classes every OS backend extends.
- `jpen.provider.NativeLibraryLoader` — loads the correct `jpen-<ver>-<nativeVersion>[-<arch>]` library out of the NAR; the library name is parametrized by `module.version` and the provider's `nativeVersion` property, so renaming either breaks loading.
- `jpen.provider.system` — fallback provider backed by `java.awt` mouse events (no native code).
- `jpen.provider.xinput` / `osx` / `wintab` — the real providers; each has a Java class with `native` methods whose C implementation lives in the matching `src/main/c/<os>/` dir.
- `jpen.demo` — Swing demo app (`jpen.demo.JPenDemoControl` is the `module.mainClass`); useful for manually smoke-testing a build.
- `jpen.internal` — not part of the public API, do not expose.

### When adding a provider feature

Any new value coming from the OS must flow: C code → JNI method on the provider's `Access` class → provider's `PenDevice` subclass that pushes a `PLevel`/`PButton`/`PScroll` into `Pen` → consumers get the event via `PenManager`. If the new value isn't universally available, add a fallback path in `PLevelEmulator` / `EmulationProvider` instead of making it provider-specific in the public API.

## Testing

- JUnit 3.8.2, scope `test`. Surefire is deliberately skipped; the gating test is `jpen.demo.SmokeTest` under `src/test/java/jpen/`, run by the NAR integration-test phase. Run with: `mvn integration-test` (also the default goal).
- `DISPLAY` env var is forwarded into the integration test runner — on headless Linux you need an X server (or `xvfb-run mvn`).

## Conventions

- All code, identifiers, and comments in English (upstream convention; also matches global rules).
- This is a critical/legacy library consumed by other Viafirma products; preserve binary compatibility (class names, method signatures, native library names, `nativeVersion` numbers). Breaking changes require coordinated bumps of `nativeVersion` + consumers.
