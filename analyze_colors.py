from PIL import Image

def analyze_edges(image_path):
    img = Image.open(image_path)
    width, height = img.size
    print(f"Image size: {width}x{height}")
    
    # Sample corners
    tl = img.getpixel((0, 0))
    tr = img.getpixel((width-1, 0))
    bl = img.getpixel((0, height-1))
    br = img.getpixel((width-1, height-1))
    
    print(f"Top-Left: {tl}")
    print(f"Top-Right: {tr}")
    print(f"Bottom-Left: {bl}")
    print(f"Bottom-Right: {br}")
    
    # Check if top row is uniform
    top_colors = set()
    for x in range(width):
        top_colors.add(img.getpixel((x, 0)))
    
    if len(top_colors) <= 5: # Allow small noise
        print("Top edge is effectively uniform.")
    else:
        print(f"Top edge has {len(top_colors)} unique colors. Likely a gradient.")

    # Check vertical gradient
    print(f"Center pixel: {img.getpixel((width//2, height//2))}")

if __name__ == "__main__":
    analyze_edges("c:/Users/vyshn/AndroidStudioProjects/Attendyn/app/src/main/res/drawable/calendar_logo.jpg")
