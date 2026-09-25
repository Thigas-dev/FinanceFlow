---
name: Emerald Ledger
colors:
  surface: '#0b1326'
  surface-dim: '#0b1326'
  surface-bright: '#31394d'
  surface-container-lowest: '#060e20'
  surface-container-low: '#131b2e'
  surface-container: '#171f33'
  surface-container-high: '#222a3d'
  surface-container-highest: '#2d3449'
  on-surface: '#dae2fd'
  on-surface-variant: '#bbcabf'
  inverse-surface: '#dae2fd'
  inverse-on-surface: '#283044'
  outline: '#86948a'
  outline-variant: '#3c4a42'
  surface-tint: '#4edea3'
  primary: '#4edea3'
  on-primary: '#003824'
  primary-container: '#10b981'
  on-primary-container: '#00422b'
  inverse-primary: '#006c49'
  secondary: '#7bd0ff'
  on-secondary: '#00354a'
  secondary-container: '#00a6e0'
  on-secondary-container: '#00374d'
  tertiary: '#ffb2b7'
  on-tertiary: '#67001b'
  tertiary-container: '#ff7886'
  on-tertiary-container: '#780021'
  error: '#ffb4ab'
  on-error: '#690005'
  error-container: '#93000a'
  on-error-container: '#ffdad6'
  primary-fixed: '#6ffbbe'
  primary-fixed-dim: '#4edea3'
  on-primary-fixed: '#002113'
  on-primary-fixed-variant: '#005236'
  secondary-fixed: '#c4e7ff'
  secondary-fixed-dim: '#7bd0ff'
  on-secondary-fixed: '#001e2c'
  on-secondary-fixed-variant: '#004c69'
  tertiary-fixed: '#ffdadb'
  tertiary-fixed-dim: '#ffb2b7'
  on-tertiary-fixed: '#40000d'
  on-tertiary-fixed-variant: '#92002a'
  background: '#0b1326'
  on-background: '#dae2fd'
  surface-variant: '#2d3449'
typography:
  headline-xl:
    fontFamily: Inter
    fontSize: 40px
    fontWeight: '700'
    lineHeight: 48px
    letterSpacing: -0.02em
  headline-xl-mobile:
    fontFamily: Inter
    fontSize: 32px
    fontWeight: '700'
    lineHeight: 40px
    letterSpacing: -0.02em
  headline-lg:
    fontFamily: Inter
    fontSize: 28px
    fontWeight: '600'
    lineHeight: 36px
    letterSpacing: -0.015em
  headline-md:
    fontFamily: Inter
    fontSize: 22px
    fontWeight: '600'
    lineHeight: 28px
    letterSpacing: -0.01em
  headline-sm:
    fontFamily: Inter
    fontSize: 18px
    fontWeight: '600'
    lineHeight: 24px
  body-lg:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
  body-md:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
  body-sm:
    fontFamily: Inter
    fontSize: 12px
    fontWeight: '400'
    lineHeight: 16px
  label-lg:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '600'
    lineHeight: 20px
    letterSpacing: 0.01em
  label-md:
    fontFamily: Inter
    fontSize: 12px
    fontWeight: '500'
    lineHeight: 16px
    letterSpacing: 0.02em
  label-sm:
    fontFamily: Inter
    fontSize: 10px
    fontWeight: '600'
    lineHeight: 14px
    letterSpacing: 0.05em
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  gutter: 1rem
  gutter-mobile: 0.75rem
  margin: 1.5rem
  margin-mobile: 1rem
  space-xs: 0.25rem
  space-sm: 0.5rem
  space-md: 1rem
  space-lg: 1.5rem
  space-xl: 2rem
---

## Brand & Style

This design system embodies a dependable, institutional-grade personal wealth and portfolio management environment tailored for mobile-first interaction. Rooted in the Corporate Modern movement with high-contrast precision, the interface projects absolute fiscal stability, rigorous security, and forward-looking clarity.

The visual direction replaces decorative excess with purposeful hierarchy: rich, ink-like slate backdrops anchor data-dense financial dashboards, while crisp emerald accents direct attention to liquidity, positive gains, and primary transaction flows. The aesthetic treats numbers and balance statements with cryptographic clarity, fostering peace of mind and analytical focus. Micro-surfaces maintain structural integrity through hairline borders and delicate surface luminance rather than dramatic skeuomorphism or distracting glows.

## Colors

The palette operates under a high-contrast dark mode architecture built to eliminate visual fatigue while prioritizing rapid numerical legibility.

- **Primary (`#10B981`)**: The primary emerald accent indicates positive flow, wealth accumulation, portfolio yield, and primary confirmation triggers. Its deeper companion (`#059669`) serves as an active press or hover state.
- **Secondary (`#38BDF8`)**: Electric sky blue governs analytical indicators, predictive growth forecasts, liquidity projections, and secondary navigational focal points.
- **Tertiary (`#F43F5E`)**: A precise crimson red isolates debits, balance outflows, risk thresholds, and critical confirmation states without overwhelming adjacent data.
- **Surfaces and Neutrals**:
  - `Surface Canvas (`#0B0F19`)`: Base foundation for overall viewport containment.
  - `Surface Level 1 (`#0F172A`)`: Primary background for structural modules and grouping views.
  - `Surface Level 2 (`#1E293B`)`: Interactive tiles, data tables, and distinct item cards.
  - `Surface Border (`#334155`)`: Hairline boundaries providing spatial delineation between dense analytical rows.
- **Typography Neutrals**:
  - `Text Primary (`#F8FAFC`)`: Total asset values, balance figures, and critical headlines.
  - `Text Secondary (`#94A3B8`)`: Account IDs, transaction labels, and timestamp metadata.
  - `Text Tertiary (`#64748B`)`: Inactive tab counters and input placeholder prompts.

## Typography

Inter governs the entire typographic structure, ensuring optical consistency across numerical readouts, micro-labels, and display metrics. Tabular figures (`tnum`) must be universally active across all components displaying monetary balances, percentage shifts, and exchange rates to eliminate layout shifting during live stream updates.

- **Monetary Displays**: Large asset indicators utilize `headline-xl-mobile` or `headline-xl` set to `700` weight with `-0.02em` tracking for solid compactness.
- **Section Headers & Module Titles**: Formatted with `headline-md` and `headline-sm`, anchoring user orientation across nested ledger layers.
- **Metadata and Data Annotations**: `label-md` and `label-sm` enforce upper-register clarity for currency codes (USD, EUR), risk markers, and transaction classifications.

## Layout & Spacing

The layout is built upon a rigid 8-point spatial matrix scaled specifically for compact, thumb-driven mobile workflows. 

- **Grid Architecture**: Mobile screens use a 4-column layout with `0.75rem` (`12px`) gutters and `1rem` (`16px`) outer margins. Tablet views transition to an 8-column layout with `1rem` gutters and `1.5rem` margins. Desktop administrative interfaces expand to a 12-column system locked at a maximum width of 1200px.
- **Rhythm**: Element padding and stack distances map strictly to `space-xs` (4px), `space-sm` (8px), `space-md` (16px), `space-lg` (24px), and `space-xl` (32px).
- **Dense Transaction Rows**: List row heights adhere to a fixed 64px vertical footprint with 8px internal gaps, maximizing the visible volume of transactions without triggering cognitive overload.

## Elevation & Depth

Depth is established primarily through **tonal layering** and **low-contrast borders** rather than overt dropped shadows. Because dark interfaces can easily look muddy under heavy blur effects, spatial stacking uses controlled luminance:

- **Base Layer (`#0B0F19`)**: The canvas canvas tier for system navigation and underlays.
- **Layer 1 (`#0F172A`)**: Base panel background for tab sheets and dashboard zones.
- **Layer 2 (`#1E293B`)**: Interactive surface cards, ledger rows, and elevated modal panels.
- **Structural Outlines**: Every elevated card, sheet, and floating module features a precise `1px` border of `#334155`.
- **Floating Modals & Sheets**: Floating bottom sheets incorporate an ambient shadow composed of `0 12px 32px -4px rgba(0, 0, 0, 0.65)` alongside the perimeter border to ensure absolute detachment from background content.

## Shapes

With a roundedness index of `2`, the system delivers a disciplined, modern fintech silhouette:

- **Base Components**: Standard interactive touchpoints—including text inputs, default buttons, segmented controls, and transaction tiles—utilize a `0.5rem` (`8px`) corner radius.
- **Cards and Containers**: Analytical metrics cards and modular group panels apply `rounded-lg` (`1rem` / `16px`).
- **Overlays and Sheets**: Bottom sheets, balance inspection trays, and modal dialogs adopt `rounded-xl` (`1.5rem` / `24px`) on exposed exterior corners.
- **Status Pills and Badges**: Percentage tags and indicator chips break from the base scale to adopt full pill geometry (`9999px`) to visually differentiate numerical attributes from operational containers.

## Components

### Buttons
- **Primary Action**: Emerald background (`#10B981`) paired with ink-dark text (`#0B0F19`), styled in `label-lg` with a `0.5rem` border radius. Active state darkens to `#059669`.
- **Secondary Action**: Background `#1E293B`, subtle border `#334155`, text `#F8FAFC`. Active state lightens to `#334155`.
- **Destructive Action**: Surface `#1E293B` bounded by `#F43F5E` border, crimson typography (`#F43F5E`).

### Cards & Ledger Tiles
- Composed of `#1E293B` fill, `#334155` 1px border, and `1rem` corner rounding.
- Internal padding is locked to `1.25rem` (`space-lg` adjusted).
- Tap states trigger a background transition to `#283548`.

### Lists & Transaction Rows
- Arranged with horizontal auto-layout: left-aligned icon/avatar (40x40px, `#0F172A` background with `#334155` border), central merchant/category stack (`body-md` in `#F8FAFC` over `body-sm` in `#94A3B8`), and right-aligned balance readout (`label-lg` with tabular figures).
- Inflows are prefixed with `+` and set in `#10B981`. Outflows are rendered neutral or in `#F43F5E` based on account context.
- Rows are separated by 1px horizontal dividers (`#334155` at 50% opacity).

### Input Fields
- Fill `#0F172A` with a 1px `#334155` border and `0.5rem` border radius.
- Height: 48px to accommodate mobile touch accuracy.
- Placeholder text: `#64748B`. Value text: `#F8FAFC`.
- Focus state: Border transitions to `#10B981` with an outer 2px glow ring (`rgba(16, 185, 129, 0.2)`).

### Chips & Badges
- Compact indicators for balance state (`+4.25%`, `-1.12%`, `Pending`).
- Background: 10% opacity tint of the respective status color (e.g., `rgba(16, 185, 129, 0.1)` for positive returns).
- Text: Full saturation tint (`#10B981` or `#F43F5E`) in `label-sm`.
- Padding: 4px horizontal, 2px vertical, with a continuous rounded pill contour.

### Checkboxes & Segmented Controls
- **Segmented Control**: Base track set in `#0F172A` with a selected thumb in `#1E293B`, bordered by `#334155` and text highlighted in `#F8FAFC`.
- **Checkboxes**: 20x20px square, 4px corner radius. Unchecked has a 1px border in `#334155`; checked state fills with `#10B981` featuring a white check vector.

### Metric Trend Widgets
- Special purpose mini-sparkline component nested inside cards. Chart baseline sits on `#1E293B`, stroke mapped to `#10B981` or `#38BDF8` with a 2px stroke width, free from cluttered gridlines.