Add-Type -AssemblyName System.Drawing

$inputPath = "c:\Users\vyshn\AndroidStudioProjects\Attendyn\app\src\main\res\drawable\calendar_logo.jpg"
$outputPath = "c:\Users\vyshn\AndroidStudioProjects\Attendyn\app\src\main\res\drawable\calendar_logo_transparent.png"
# Increase threshold to capture more off-white pixels if needed, or decrease if it eats into the logo.
# 200 is a safe start for light backgrounds.
$threshold = 240 

$img = [System.Drawing.Bitmap]::FromFile($inputPath)
$newImg = New-Object System.Drawing.Bitmap($img.Width, $img.Height)

for ($x = 0; $x -lt $img.Width; $x++) {
    for ($y = 0; $y -lt $img.Height; $y++) {
        $pixel = $img.GetPixel($x, $y)
        # Check for white/near-white background
        if ($pixel.R -ge $threshold -and $pixel.G -ge $threshold -and $pixel.B -ge $threshold) {
             $newImg.SetPixel($x, $y, [System.Drawing.Color]::Transparent)
        } else {
             $newImg.SetPixel($x, $y, $pixel)
        }
    }
}

$newImg.Save($outputPath, [System.Drawing.Imaging.ImageFormat]::Png)
Write-Host "Saved transparent image to $outputPath"
