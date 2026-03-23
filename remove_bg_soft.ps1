Add-Type -AssemblyName System.Drawing

$inputPath = "c:\Users\vyshn\AndroidStudioProjects\Attendyn\app\src\main\res\drawable\calendar_logo.jpg"
$outputPath = "c:\Users\vyshn\AndroidStudioProjects\Attendyn\app\src\main\res\drawable\calendar_logo_soft_transparent.png"

$img = [System.Drawing.Bitmap]::FromFile($inputPath)
$newImg = New-Object System.Drawing.Bitmap($img.Width, $img.Height)

# Average Background Color (from previous analysis)
# R=44, G=21, B=160 (Dark Purple/Blue)
$bgR = 44
$bgG = 21
$bgB = 160

for ($x = 0; $x -lt $img.Width; $x++) {
    for ($y = 0; $y -lt $img.Height; $y++) {
        $pixel = $img.GetPixel($x, $y)
        
        # Calculate distance from background color
        $dist = [Math]::Sqrt([Math]::Pow($pixel.R - $bgR, 2) + [Math]::Pow($pixel.G - $bgG, 2) + [Math]::Pow($pixel.B - $bgB, 2))
        
        # Max distance (White to Background) is approx 400.
        # We want background (dist ~ 0-50) to be transparent.
        # We want logo (dist > 100) to be opaque.
        
        $alpha = 255
        
        if ($dist -lt 30) {
            $alpha = 0
        } elseif ($dist -lt 100) {
            # linear fade from 30 to 100
            $alpha = [int](($dist - 30) * (255 / 70))
        }
        
        # Ensure correct range
        if ($alpha -gt 255) { $alpha = 255 }
        if ($alpha -lt 0) { $alpha = 0 }
        
        $newImg.SetPixel($x, $y, [System.Drawing.Color]::FromArgb($alpha, $pixel.R, $pixel.G, $pixel.B))
    }
}

$newImg.Save($outputPath, [System.Drawing.Imaging.ImageFormat]::Png)
Write-Host "Saved soft transparent image to $outputPath"
