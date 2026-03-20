# UI Visual And Localization

## 1. Purpose
This document defines the UI modernization pass for the Android app.

Goals:
- make the app feel closer to a music tool than a generic CRUD app
- keep familiar actions icon-first and add text only where the icon is not obvious
- improve usability on small screens without increasing cognitive load
- support English and Japanese automatically from device locale
- ensure modal sheets expand fully instead of stopping halfway
- align top-level titles to the top-left for faster scanning

## 2. Visual Direction
The app should look like a personal performance notebook.

Direction:
- deep navy and indigo surfaces as the base
- warm amber and coral accents for interactive emphasis
- rounded cards and chips to make song metadata feel tangible
- soft gradients in the app background instead of flat white
- stronger visual separation between song info, metadata, and actions

## 3. Component Rules
### Top App Bar
- Use a left-aligned `TopAppBar`
- Keep the title prominent and stable
- Move feature actions into the screen body when labels are needed

### Primary Actions
- Use standard icon-first action buttons where the meaning is already obvious
- Add visible text only for actions that are ambiguous out of context
- Floating actions may stay icon-only when the current screen makes the intent obvious

### Song / Artist / Playlist Cards
- Show the main identity first
  - song title and artist
  - artist name and song count
  - playlist name and song count
- Show metadata as compact tags
- Use icon-only actions for edit, favorite, delete, pin, add, and expand/collapse
- Keep text on actions like playlist song management when the icon alone is not obvious

### Modal Sheets
- Use a shared bottom sheet wrapper
- Configure the sheet to skip the partial state
- Keep the title left-aligned at the top of the sheet
- Use consistent spacing and a single save area at the bottom

## 4. Localization Policy
- Default resources are English in `res/values/strings.xml`
- Japanese resources live in `res/values-ja/strings.xml`
- The app supports only English and Japanese
- When the device locale is not Japanese, Android falls back to English
- UI text must use `stringResource(...)`
- ViewModel-driven snackbar messages must be resolved from string resources through an injected resolver

## 5. Accessibility And UX Rules
- Icon-only actions must still provide clear accessibility descriptions
- Actions that are not immediately recognizable should keep a visible label
- Search, sort, random pick, and settings should remain reachable without overflow menus
- Cards should preserve clear tap targets and spacing
- Dialog copy should state the affected entity explicitly
- Empty states should tell the user what to do next

## 6. Implementation Map
```mermaid
flowchart TD
    A["Theme Layer"] --> B["Custom Color Scheme"]
    A --> C["Typography / Shapes"]
    D["Shared Components"] --> E["Labeled Action Chips"]
    D --> F["Extended FAB"]
    D --> G["Full Height Bottom Sheet"]
    H["Localization"] --> I["values/strings.xml"]
    H --> J["values-ja/strings.xml"]
    H --> K["String Resolver For ViewModel"]
    L["Screens"] --> M["Songs"]
    L --> N["Artists"]
    L --> O["Playlists"]
    L --> P["Settings"]
    B --> L
    C --> L
    D --> L
    H --> L
```

## 7. Acceptance Criteria
- Main screens look visually consistent with the music-oriented theme
- Top-level titles are left-aligned
- Familiar actions are icon-first and ambiguous actions still show text
- Bottom sheets open in full mode without stopping midway
- English and Japanese switch automatically from device locale
- Snackbar and dialog messages follow the selected locale
