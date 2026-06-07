#!/usr/bin/env python3
"""
Generate store assets for Number Tap.
Issue #84: Creates app icon (512x512) and feature graphic (1024x500).
Requires: pip install Pillow
"""

import os
import sys

try:
    from PIL import Image, ImageDraw, ImageFont
except ImportError:
    print("Pillow not installed. Installing...")
    os.system(f"{sys.executable} -m pip install Pillow")
    from PIL import Image, ImageDraw, ImageFont

# Output directory
SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
PROJECT_ROOT = os.path.dirname(SCRIPT_DIR)
OUTPUT_DIR = os.path.join(PROJECT_ROOT, "assets", "store")

def get_font(size: int, bold: bool = False) -> ImageFont.FreeTypeFont | ImageFont.ImageFont:
    """Try to get a nice font, fall back to default."""
    font_candidates = [
        "/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf" if bold else "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf",
        "/usr/share/fonts/truetype/liberation/LiberationSans-Bold.ttf" if bold else "/usr/share/fonts/truetype/liberation/LiberationSans-Regular.ttf",
        "/usr/share/fonts/TTF/DejaVuSans-Bold.ttf" if bold else "/usr/share/fonts/TTF/DejaVuSans.ttf",
    ]
    for path in font_candidates:
        if os.path.exists(path):
            return ImageFont.truetype(path, size)
    return ImageFont.load_default()


def generate_app_icon(output_path: str) -> None:
    """Generate 512x512 app icon with dark slate background and 'NT' text."""
    size = 512
    img = Image.new("RGBA", (size, size), (30, 41, 59, 255))  # Dark slate #1E293B
    draw = ImageDraw.Draw(img)

    # Draw a subtle grid pattern (3x3)
    tile_size = size // 3
    padding = 12
    corner_radius = 16

    # Grid colors
    tile_bg = (51, 65, 85, 200)  # Slate-700
    target_bg = (14, 165, 233, 255)  # Sky-500
    text_color = (255, 255, 255, 255)

    for row in range(3):
        for col in range(3):
            x = col * tile_size + padding
            y = row * tile_size + padding
            w = tile_size - padding * 2
            h = tile_size - padding * 2

            if row == 1 and col == 1:
                # Center tile — target color
                draw.rounded_rectangle([x, y, x + w, y + h], radius=corner_radius, fill=target_bg)
                # Draw NT text
                font = get_font(int(w * 0.45), bold=True)
                text = "NT"
                bbox = draw.textbbox((0, 0), text, font=font)
                tw = bbox[2] - bbox[0]
                th = bbox[3] - bbox[1]
                draw.text((x + (w - tw) / 2, y + (h - th) / 2 - 4), text, fill=text_color, font=font)
            else:
                draw.rounded_rectangle([x, y, x + w, y + h], radius=corner_radius, fill=tile_bg)
                # Draw number in tile
                num = row * 3 + col + 1
                if num > 4:
                    num -= 1  # skip center
                if num <= 8:
                    font = get_font(int(h * 0.4), bold=True)
                    text = str(num)
                    bbox = draw.textbbox((0, 0), text, font=font)
                    tw = bbox[2] - bbox[0]
                    th = bbox[3] - bbox[1]
                    draw.text((x + (w - tw) / 2, y + (h - th) / 2 - 2), text, fill=(148, 163, 184, 180), font=font)

    img.save(output_path, "PNG")
    print(f"✓ App icon saved to {output_path} ({size}x{size})")


def generate_feature_graphic(output_path: str) -> None:
    """Generate 1024x500 feature graphic with branding."""
    width, height = 1024, 500
    img = Image.new("RGBA", (width, height), (15, 23, 42, 255))  # Slate-900
    draw = ImageDraw.Draw(img)

    # Background gradient effect — subtle horizontal stripes
    for i in range(height):
        alpha = int(20 + (i / height) * 15)
        draw.line([(0, i), (width, i)], fill=(30, 41, 59, alpha))

    # Title text
    font_large = get_font(72, bold=True)
    font_small = get_font(28, bold=False)
    font_tagline = get_font(22, bold=False)

    # "NUMBER TAP" title
    title = "NUMBER TAP"
    bbox = draw.textbbox((0, 0), title, font=font_large)
    tw = bbox[2] - bbox[0]
    x = (width - tw) / 2
    draw.text((x, 120), title, fill=(255, 255, 255, 255), font=font_large)

    # Tagline
    tagline = "The Ordered Grid"
    bbox2 = draw.textbbox((0, 0), tagline, font=font_tagline)
    tw2 = bbox2[2] - bbox2[0]
    draw.text(((width - tw2) / 2, 210), tagline, fill=(148, 163, 184, 255), font=font_tagline)

    # Fake game grid mockup — centered small grid
    grid_x = (width - 200) // 2
    grid_y = 270
    tile_s = 44
    pad = 6
    for row in range(3):
        for col in range(3):
            tx = grid_x + col * (tile_s + pad)
            ty = grid_y + row * (tile_s + pad)
            if row == 1 and col == 1:
                draw.rounded_rectangle([tx, ty, tx + tile_s, ty + tile_s], radius=8, fill=(14, 165, 233, 255))
                num_font = get_font(int(tile_s * 0.5), bold=True)
                num_text = "NT"
                bbox3 = draw.textbbox((0, 0), num_text, font=num_font)
                nw = bbox3[2] - bbox3[0]
                nh = bbox3[3] - bbox3[1]
                draw.text((tx + (tile_s - nw) / 2, ty + (tile_s - nh) / 2 - 2), num_text, fill=(255, 255, 255), font=num_font)
            else:
                draw.rounded_rectangle([tx, ty, tx + tile_s, ty + tile_s], radius=8, fill=(51, 65, 85, 200))
                num = row * 3 + col + 1
                if num > 4:
                    num -= 1
                if num <= 8:
                    num_font = get_font(int(tile_s * 0.5), bold=True)
                    num_text = str(num)
                    bbox3 = draw.textbbox((0, 0), num_text, font=num_font)
                    nw = bbox3[2] - bbox3[0]
                    nh = bbox3[3] - bbox3[1]
                    draw.text((tx + (tile_s - nw) / 2, ty + (tile_s - nh) / 2 - 1), num_text, fill=(148, 163, 184, 200), font=num_font)

    img.save(output_path, "PNG")
    print(f"✓ Feature graphic saved to {output_path} ({width}x{height})")


def main() -> None:
    os.makedirs(OUTPUT_DIR, exist_ok=True)

    icon_path = os.path.join(OUTPUT_DIR, "app_icon_512.png")
    feature_path = os.path.join(OUTPUT_DIR, "feature_graphic_1024x500.png")

    generate_app_icon(icon_path)
    generate_feature_graphic(feature_path)
    print(f"\nAll store assets generated in {OUTPUT_DIR}")


if __name__ == "__main__":
    main()
