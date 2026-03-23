Add-Type -AssemblyName System.Drawing

$inputPath = "c:\Users\vyshn\AndroidStudioProjects\Attendyn\app\src\main\res\drawable\calendar_logo.jpg"
$outputPath = "c:\Users\vyshn\AndroidStudioProjects\Attendyn\app\src\main\res\drawable\calendar_logo_radial_fade.png"

$img = [System.Drawing.Bitmap]::FromFile($inputPath)
$newImg = New-Object System.Drawing.Bitmap($img.Width, $img.Height)

$centerX = $img.Width / 2
$centerY = $img.Height / 2
# Max radius from center to nearest edge (to be safe)
$maxRadius = [Math]::Min($centerX, $centerY) - 10

# Background Color Reference (Dark Blue/Purple)
$bgR = 44
$bgG = 21
$bgB = 160

for ($x = 0; $x -lt $img.Width; $x++) {
    for ($y = 0; $y -lt $img.Height; $y++) {
        $pixel = $img.GetPixel($x, $y)
        
        # 1. Color Distance Alpha (Remove Dark Background)
        $dist = [Math]::Sqrt([Math]::Pow($pixel.R - $bgR, 2) + [Math]::Pow($pixel.G - $bgG, 2) + [Math]::Pow($pixel.B - $bgB, 2))
        $colorAlpha = 255
        if ($dist -lt 30) { $colorAlpha = 0 }
        elseif ($dist -lt 120) { $colorAlpha = [int](($dist - 30) * (255 / 90)) }
        if ($colorAlpha -gt 255) { $colorAlpha = 255 }

        # 2. Radial Distance Alpha (Force Fade at Edges)
        $distFromCenter = [Math]::Sqrt([Math]::Pow($x - $centerX, 2) + [Math]::Pow($y - $centerY, 2))
        $radialAlpha = 255
        
        # Start fading at 70% of max radius, reach 0 at 100%
        $fadeStart = $maxRadius * 0.7
        if ($distFromCenter -gt $maxRadius) {
            $radialAlpha = 0
        } elseif ($distFromCenter -gt $fadeStart) {
            $radialAlpha = [int](255 * (1 - ($distFromCenter - $fadeStart) / ($maxRadius - $fadeStart)))
        }

        # Combine Alphas
        $finalAlpha = [int]($colorAlpha * ($radialAlpha / 255))
        
        $newImg.SetPixel($x, $y, [System.Drawing.Color]::FromArgb($finalAlpha, $pixel.R, $pixel.G, $pixel.B))
    }
}

$newImg.Save($outputPath, [System.Drawing.Imaging.ImageFormat]::Png)
Write-Host "Saved radial fade image to $outputPath"
