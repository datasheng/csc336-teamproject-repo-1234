## Color Palette

### Primary Colors

- **Deep Warm Gray**: `#57534e`

- Use for: Main text, headings, icons, dark UI elements
- Text class: `text-stone-700`

- **Terracotta**: `#ea580c`

- Use for: Primary buttons, CTAs, badges, status indicators, links, important highlights
- Background class: `bg-orange-600`
- Text class: `text-orange-600`

### Neutral Colors

- **Warm Beige**: `#fafaf9`

- Use for: Main page background
- Background class: `bg-stone-50`

- **Light Sand**: `#e7e5e4`

- Use for: Borders, dividers, secondary backgrounds, disabled states
- Border class: `border-stone-200`
- Background class: `bg-stone-200`

### Semantic Colors

- **White**: `#ffffff` - Card backgrounds, input fields
- **Success Green**: `#16a34a` - Available tickets, success states
- **Warning Amber**: `#f59e0b` - Low stock alerts
- **Error Red**: `#dc2626` - Sold out, errors

---

## Typography

### Font Families

- **Primary**: System font stack (default sans-serif)
- No custom fonts - use browser defaults

### Type Scale

- **Heading 1**: `text-3xl` (30px), `font-bold`, `text-stone-700`
- **Heading 2**: `text-2xl` (24px), `font-bold`, `text-stone-700`
- **Heading 3**: `text-xl` (20px), `font-semibold`, `text-stone-700`
- **Body Large**: `text-lg` (18px), `font-normal`, `text-stone-700`
- **Body**: `text-base` (16px), `font-normal`, `text-stone-700`
- **Body Small**: `text-sm` (14px), `font-normal`, `text-stone-600`
- **Caption**: `text-xs` (12px), `font-medium`, `text-stone-500`

### Line Height

- Headings: `leading-tight` (1.25)
- Body text: `leading-relaxed` (1.625)

---

## Spacing System

Use Tailwind's default spacing scale:

- **Extra Small**: `gap-2` (8px) - Tight elements
- **Small**: `gap-4` (16px) - Related items
- **Medium**: `gap-6` (24px) - Section spacing
- **Large**: `gap-8` (32px) - Major sections
- **Extra Large**: `gap-12` (48px) - Page sections

### Component Padding

- **Cards**: `p-6` (24px)
- **Buttons**: `px-6 py-3` (24px horizontal, 12px vertical)
- **Input Fields**: `px-4 py-2` (16px horizontal, 8px vertical)
- **Page Container**: `p-8` (32px)

---

## Border & Corners

### Border Radius

- **Buttons**: `rounded-lg` (8px)
- **Cards**: `rounded-lg` (8px)
- **Input Fields**: `rounded-md` (6px)
- **Badges**: `rounded-full` (fully rounded)
- **Small elements**: `rounded` (4px)

### Border Width

- **Default**: `border` (1px)
- **Thick**: `border-2` (2px) - only for emphasis or active states
- **Color**: `border-stone-200`

---

## Shadows

- **Card Elevation**: `shadow-sm` - subtle shadow

- Custom: `0 1px 2px 0 rgba(0, 0, 0, 0.05)`

- **Hover State**: `shadow-md` - medium shadow on hover

- Custom: `0 4px 6px -1px rgba(0, 0, 0, 0.1)`

- **Modal/Overlay**: `shadow-lg` - larger shadow

- Custom: `0 10px 15px -3px rgba(0, 0, 0, 0.1)`

---

## Component Specifications

### Buttons

**Primary Button**

```plaintext
Background: bg-orange-600 (#ea580c)
Text: text-white
Padding: px-6 py-3
Border Radius: rounded-lg
Font: font-medium
Hover: hover:bg-orange-700
Transition: transition-colors
```

**Secondary Button**

```plaintext
Background: bg-white
Text: text-stone-700
Border: border border-stone-200
Padding: px-6 py-3
Border Radius: rounded-lg
Font: font-medium
Hover: hover:bg-stone-50
Transition: transition-colors
```

**Ghost Button**

```plaintext
Background: transparent
Text: text-stone-700
Padding: px-4 py-2
Border Radius: rounded-lg
Hover: hover:bg-stone-100
```

### Cards

**Standard Card**

```plaintext
Background: bg-white
Border: border border-stone-200
Border Radius: rounded-lg
Padding: p-6
Shadow: shadow-sm
Hover: hover:shadow-md
Transition: transition-shadow
```

**Event Ticket Card** (special)

```plaintext
Same as standard card
Add left accent border: border-l-4 border-l-orange-600
```

### Badges

**Status Badge**

```plaintext
Padding: px-3 py-1
Border Radius: rounded-full
Font: text-xs font-medium
Available: bg-green-100 text-green-700
Low Stock: bg-amber-100 text-amber-700
Sold Out: bg-red-100 text-red-700
```

### Input Fields

**Text Input**

```plaintext
Background: bg-white
Border: border border-stone-200
Border Radius: rounded-md
Padding: px-4 py-2
Font: text-base
Focus: focus:outline-none focus:ring-2 focus:ring-orange-600 focus:border-orange-600
Placeholder: placeholder:text-stone-400
```

### Tables

**Table Structure**

```plaintext
Container: bg-white border border-stone-200 rounded-lg overflow-hidden
Header: bg-stone-50 text-stone-700 font-semibold text-sm
Header Cells: px-6 py-3 text-left
Body Cells: px-6 py-4 text-stone-700
Borders: border-b border-stone-200 (between rows)
Hover: hover:bg-stone-50 (on rows)
```

### Stats/Metrics Cards

**Revenue/Stats Display**

```plaintext
Large Number: text-3xl font-bold text-stone-700
Label: text-sm text-stone-500 uppercase tracking-wide
Background: bg-white
Border: border border-stone-200
Padding: p-6
Rounded: rounded-lg
```

---

## Layout Patterns

### Page Container

```plaintext
Max Width: max-w-7xl
Margin: mx-auto
Padding: p-8
Background: bg-stone-50 (page background)
```

### Grid Layouts

```plaintext
Dashboard Grid: grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6
Event List: grid grid-cols-1 gap-4
Stats Row: grid grid-cols-1 md:grid-cols-4 gap-6
```

### Flexbox Patterns

```plaintext
Header: flex items-center justify-between
Card Header: flex items-start justify-between
Button Group: flex items-center gap-3
```

---

## Interactive States

### Hover States

- Buttons: Darken background by one shade
- Cards: Increase shadow from `shadow-sm` to `shadow-md`
- Links: Underline with `hover:underline`
- Rows: Add `hover:bg-stone-50`

### Active/Focus States

- Inputs: `focus:ring-2 focus:ring-orange-600 focus:border-orange-600`
- Buttons: `active:scale-95` (subtle press effect)
- Links: `text-orange-600`

### Disabled States

- Background: `bg-stone-100`
- Text: `text-stone-400`
- Cursor: `cursor-not-allowed`
- Opacity: `opacity-50`

---

## Design Principles

1. **Warm & Inviting**: Use warm neutrals (beige, sand, warm gray) instead of cool grays
2. **Clear Hierarchy**: Use terracotta sparingly for CTAs and important actions
3. **Spacious**: Generous padding and gap spacing for breathing room
4. **Subtle Elevation**: Light shadows, nothing harsh
5. **Consistent Rounding**: 8px for major elements, 6px for inputs, 4px for small items
6. **No Gradients**: Solid colors only
7. **Accessible Contrast**: Ensure text meets WCAG AA standards against backgrounds

---

## Tailwind CSS Configuration

Add to `globals.css`:

```css
@theme inline {
  --color-primary: #57534e;
  --color-accent: #ea580c;
  --color-background: #fafaf9;
  --color-border: #e7e5e4;
  --radius: 0.5rem;
}
```

---

## Usage Examples

**Event Card**: White card with stone-200 border, rounded-lg, shadow-sm, left orange accent border, terracotta "Buy Tickets" button

**Dashboard Stats**: White cards with revenue numbers in large bold stone-700 text, labels in small stone-500 uppercase

**Table**: White background, stone-50 header, stone-200 row borders, hover:bg-stone-50 on rows

**Navigation**: Warm beige background, stone-700 text, orange-600 active links

This specification provides everything needed to consistently apply the Archive theme across your entire campus ticketing system.
