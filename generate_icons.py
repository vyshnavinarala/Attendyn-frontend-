import os
from PIL import Image, ImageOps, ImageDraw

def create_icons(source_path, base_res_dir):
    # Android Standard Icon Sizes (48, 72, 96, 144, 192)
    densities = {
        "mipmap-mdpi": 48,
        "mipmap-hdpi": 72,
        "mipmap-xhdpi": 96,
        "mipmap-xxhdpi": 144,
        "mipmap-xxxhdpi": 192
    }

    img = Image.open(source_path)
    
    for folder, size in densities.items():
        folder_path = os.path.join(base_res_dir, folder)
        if not os.path.exists(folder_path):
            os.makedirs(folder_path)
            print(f"Created directory: {folder_path}")

        # 1. Square Icon (ic_launcher.png)
        # Using Resampling.LANCZOS for high quality
        square_img = img.resize((size, size), Image.Resampling.LANCZOS)
        square_path = os.path.join(folder_path, "ic_launcher.png")
        # Convert to RGBA if needed (though jpg is RGB)
        square_img.convert("RGBA").save(square_path, "PNG")
        print(f"Saved: {square_path}")

        # 2. Round Icon (ic_launcher_round.png)
        # Create a circular mask
        mask = Image.new('L', (size, size), 0)
        draw = ImageDraw.Draw(mask)
        draw.ellipse((0, 0, size, size), fill=255)
        
        round_img = Image.new("RGBA", (size, size), (0, 0, 0, 0))
        round_img.paste(square_img, (0, 0), mask=mask)
        
        round_path = os.path.join(folder_path, "ic_launcher_round.png")
        round_img.save(round_path, "PNG")
        print(f"Saved: {round_path}")

if __name__ == "__main__":
    source = r"c:\Users\vyshn\AndroidStudioProjects\Attendyn\app\src\main\res\drawable\calendar_logo.jpg"
    res_dir = r"c:\Users\vyshn\AndroidStudioProjects\Attendyn\app\src\main\res"
    
    if os.path.exists(source):
        create_icons(source, res_dir)
        print("\nIcon generation complete!")
    else:
        print(f"Error: Source image not found at {source}")
