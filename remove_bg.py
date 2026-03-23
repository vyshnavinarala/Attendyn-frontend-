from PIL import Image

def remove_background(input_path, output_path):
    img = Image.open(input_path)
    img = img.convert("RGBA")
    datas = img.getdata()

    new_data = []
    # Assuming the background is light/white/mint, we'll make near-white pixels transparent
    # Adjust threshold as needed. 
    for item in datas:
        # Check if the pixel is very light (background)
        # You might need to adjust these values based on the specific background color
        if item[0] > 200 and item[1] > 200 and item[2] > 200:
            new_data.append((255, 255, 255, 0))
        else:
            new_data.append(item)

    img.putdata(new_data)
    img.save(output_path, "PNG")
    print(f"Saved transparent image to {output_path}")

if __name__ == "__main__":
    remove_background(
        "c:/Users/vyshn/AndroidStudioProjects/Attendyn/app/src/main/res/drawable/calendar_logo.jpg",
        "c:/Users/vyshn/AndroidStudioProjects/Attendyn/app/src/main/res/drawable/calendar_logo_transparent.png"
    )
