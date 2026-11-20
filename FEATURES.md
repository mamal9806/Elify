# Elify — Full Feature Specification

Elify is a local-first Android photo editor with a purple/white minimal UI. No AI, no ads, no subscriptions. Network is only used for downloading optional content packs; editing works offline.

## Feature status
This document defines the complete target feature set. **Only the current prototype's gallery import, preview, basic adjustments, simple filters, and export are implemented.** The rest are planned modules and must not be represented as working until implemented and tested.

## Editor core
- Non-destructive editing pipeline; original image stays unchanged
- History stack, undo/redo, before/after compare, reset per tool
- Export JPEG/PNG, resolution/quality selection, EXIF orientation preservation
- Projects and recent edits; autosave draft

## Adjustments
- Brightness, contrast, saturation, exposure
- Highlights, shadows, temperature, tint
- Sharpness, clarity, fade, vignette, grain, blur
- HSL channels, curves, color grading, LUT support

## Retouch
- Manual brush smoothing and texture preservation controls
- Blemish spot tool, healing, clone stamp
- Dark-circle correction, teeth whitening, eye brightening
- Manual dodge/burn and selective retouch

## Face reshape (manual, non-AI)
- Face-width/slim, jaw, chin, forehead
- Nose width/height, eye size/spacing, lips
- Manual mesh/liquify warp and restore brush
- Face detection-assisted placement only if non-AI and a compatible library is selected

## Makeup
- Lip color and intensity
- Blush, eyeshadow, eyeliner, eyebrow/lash overlays
- Contour, highlight, hair tint
- Manual masks, opacity, blend modes, color picker, presets

## Body reshape (manual)
- Waist, hips, legs, arms, shoulder and height proportion controls
- Mesh warp, brush and restore
- Edge-aware guard rails and preview

## Filters and effects
- Presets: cinematic, dark, vintage, flash, neon, grain, monochrome, warm/cool
- Effects: glow, bloom, lens flare, VHS, RGB split, chromatic aberration, grain/dust, light leak, glitch, motion blur, film burn, double exposure, neon, dream, retro, fisheye, tilt-shift, color splash
- Layered effect stack, intensity, mask, blend mode

## Crop and transform
- Free crop and ratios 1:1, 4:3, 3:4, 16:9, 9:16
- Rotate, flip, straighten, perspective correction

## Background and object tools (manual)
- Manual brush/lasso cutout and refine/restore
- Background blur, solid color, gradient, image replacement
- Object erase by manual mask and inpainting-free patch/clone methods
- Chroma/color selection

## Text, stickers and overlays
- Text, font, size, color, gradient, shadow, stroke, spacing, rotation and opacity
- PNG stickers, user-imported assets, categorized packs
- Frames, borders, textures and overlays

## Collage
- 2–20 photos, templates, grid layouts, spacing, rounded corners
- Backgrounds, frames, drag/scale/rotate, export

## Camera
- CameraX capture, front/back camera, timer 3/5/10s, flash, zoom, grid, ratios
- Preview filters and manual exposure/focus controls where supported

## Content packs
- Downloadable filter/effect/LUT/sticker/frame packs from a trusted pack endpoint
- Local storage, offline use, integrity checks and pack versioning
- Pack categories: Basic, Cinema, Dark, Vintage, Flash, Neon, Grain, Overlay, Makeup, Sticker, Frame, Preset

## Accessibility and settings
- Light/dark themes, Persian/English, haptic and sound settings
- Permissions guidance and storage management

## Implementation phases
1. Stabilize editor core, adjustment pipeline, crop/rotate, real filtered export, undo/redo.
2. Filters/effects and color grading.
3. Manual retouch tools: brush, healing, clone, dodge/burn.
4. Background tools and masking.
5. Manual makeup overlays.
6. Reshape/body mesh tools.
7. Text, stickers, collage.
8. Camera, pack downloads, projects/history, accessibility polish.

## Technical constraints
- Kotlin, Jetpack Compose, Android 8+ (minSdk 26)
- Local image processing; no AI/cloud editing
- Optimize memory by sampling large images and processing off-main-thread
- Use modular code and test each feature on-device before marking implemented
