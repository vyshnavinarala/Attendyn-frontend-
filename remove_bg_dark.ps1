Add-Type -AssemblyName System.Drawing

$inputPath = "c:\Users\vyshn\AndroidStudioProjects\Attendyn\app\src\main\res\drawable\calendar_logo.jpg"
$outputPath = "c:\Users\vyshn\AndroidStudioProjects\Attendyn\app\src\main\res\drawable\calendar_logo_transparent_v2.png"

# Threshold for "Darkness" to Identify Background
# Background is dark blue/purple. Calendar is white/light.
# We will treat pixels that are NOT bright as background.
# Or better: Distance from known background colors.
# TL: 25, 24, 152. BR: 73, 21, 168.

$img = [System.Drawing.Bitmap]::FromFile($inputPath)
$newImg = New-Object System.Drawing.Bitmap($img.Width, $img.Height)

for ($x = 0; $x -lt $img.Width; $x++) {
    for ($y = 0; $y -lt $img.Height; $y++) {
        $pixel = $img.GetPixel($x, $y)
        
        # Simple brightness check: R+G+B
        $brightness = $pixel.R + $pixel.G + $pixel.B
        
        # Background R(~50) + G(~25) + B(~160) approx 235.
        # Glow/White will be higher.
        
        # Let's try a threshold. If sum < 350, make transparent?
        # Need to be careful about the "Glow" fading into background.
        # Instead of hard cutoff, maybe just hard cutoff for now.
        
        if ($brightness -lt 300) { 
             $newImg.SetPixel($x, $y, [System.Drawing.Color]::Transparent)
        } else {
             $newImg.SetPixel($x, $y, $pixel)
        }
    }
}

$newImg.Save($outputPath, [System.Drawing.Imaging.ImageFormat]::Png)
Write-Host "Saved transparent image to $outputPath"
