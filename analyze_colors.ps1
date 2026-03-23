Add-Type -AssemblyName System.Drawing

$inputPath = "c:\Users\vyshn\AndroidStudioProjects\Attendyn\app\src\main\res\drawable\calendar_logo.jpg"
$img = [System.Drawing.Bitmap]::FromFile($inputPath)

Write-Host "Width: $($img.Width)"
Write-Host "Height: $($img.Height)"

$tl = $img.GetPixel(0, 0)
$tr = $img.GetPixel($img.Width-1, 0)
$bl = $img.GetPixel(0, $img.Height-1)
$br = $img.GetPixel($img.Width-1, $img.Height-1)

Write-Host "Top-Left: R=$($tl.R) G=$($tl.G) B=$($tl.B)"
Write-Host "Top-Right: R=$($tr.R) G=$($tr.G) B=$($tr.B)"
Write-Host "Bottom-Left: R=$($bl.R) G=$($bl.G) B=$($bl.B)"
Write-Host "Bottom-Right: R=$($br.R) G=$($br.G) B=$($br.B)"

# Check if top edge is uniform
$first = $img.GetPixel(0, 0)
$uniform = $true
for ($x = 1; $x -lt $img.Width; $x++) {
    $p = $img.GetPixel($x, 0)
    if ($p.R -ne $first.R -or $p.G -ne $first.G -or $p.B -ne $first.B) {
        $uniform = $false
        break
    }
}
Write-Host "Top edge uniform? $uniform"
