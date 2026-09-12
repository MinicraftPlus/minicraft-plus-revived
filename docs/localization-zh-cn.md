# Simplified Chinese

Select **Chinese (简体中文)** in the language menu. The selected language is
saved through the existing preferences system.

The bundled translation covers the English localization keys, item names,
notifications, title splashes, and the five built-in books. Menus recalculate
their bounds after a language change. Books use localized content when
available and otherwise retain their resource-pack content.

## Pixel font

Characters outside the existing sprite alphabet use an 8-by-8 monochrome
glyph from [Fusion Pixel Font](https://github.com/TakWolf/fusion-pixel-font),
release [2026.09.01](https://github.com/TakWolf/fusion-pixel-font/releases/tag/2026.09.01).
The bundled file is the unmodified `fusion-pixel-8px-monospaced-zh_hans.ttf`.
The original sprite alphabet is still used for existing supported characters.
Glyphs use the normal renderer's color and camera offset, with a bounded cache.
Unsupported code points use the font's replacement character.

Fusion Pixel Font is distributed under the SIL Open Font License 1.1.
Its license and the upstream component notices are bundled beside the font in
`src/client/resources/assets/fonts/OFL.txt` and `assets/fonts/LICENSES/`.

## Verification

Run `./gradlew :client:checkChinese` (or `gradlew.bat :client:checkChinese`
on Windows). This check also runs as part of `build`.

The checks cover translation-key and format-argument parity, glyph coverage
and clipping, color and camera offset, Unicode wrapping, book pagination,
item display names, menu layout after switching languages, and saving and
reloading the language preference. They render screenshots into
`src/client/build/reports/chinese/` and use temporary game data there instead
of the player's save directory. No game window is opened.
