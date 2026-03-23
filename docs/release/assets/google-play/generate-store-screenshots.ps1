Add-Type -AssemblyName System.Drawing

function New-RoundedRectPath {
    param(
        [System.Drawing.Rectangle]$Rect,
        [int]$Radius
    )

    $diameter = $Radius * 2
    $path = New-Object System.Drawing.Drawing2D.GraphicsPath
    $arc = New-Object System.Drawing.Rectangle($Rect.X, $Rect.Y, $diameter, $diameter)

    $path.AddArc($arc, 180, 90)
    $arc.X = $Rect.Right - $diameter
    $path.AddArc($arc, 270, 90)
    $arc.Y = $Rect.Bottom - $diameter
    $path.AddArc($arc, 0, 90)
    $arc.X = $Rect.X
    $path.AddArc($arc, 90, 90)
    $path.CloseFigure()

    return $path
}

function Draw-Chip {
    param(
        [System.Drawing.Graphics]$Graphics,
        [string]$Text,
        [System.Drawing.Font]$Font,
        [System.Drawing.Brush]$TextBrush,
        [System.Drawing.Color]$FillColor,
        [System.Drawing.Color]$BorderColor,
        [int]$X,
        [int]$Y,
        [int]$Width
    )

    $height = 46
    $rect = New-Object System.Drawing.Rectangle($X, $Y, $Width, $height)
    $path = New-RoundedRectPath -Rect $rect -Radius 23
    $brush = New-Object System.Drawing.SolidBrush($FillColor)
    $pen = New-Object System.Drawing.Pen($BorderColor, 2)

    $Graphics.FillPath($brush, $path)
    $Graphics.DrawPath($pen, $path)
    $Graphics.DrawString($Text, $Font, $TextBrush, $X + 20, $Y + 10)

    $pen.Dispose()
    $brush.Dispose()
    $path.Dispose()
}

function Draw-StoreShot {
    param(
        [string]$SourcePath,
        [string]$DestinationPath,
        [string]$Title,
        [string]$Subtitle,
        [string[]]$Chips,
        [System.Drawing.Color]$BackgroundStart,
        [System.Drawing.Color]$BackgroundEnd,
        [System.Drawing.Color]$AccentColor,
        [System.Drawing.Color]$AccentSoft
    )

    $width = 1080
    $height = 1920
    $canvas = New-Object System.Drawing.Bitmap $width, $height
    $graphics = [System.Drawing.Graphics]::FromImage($canvas)
    $graphics.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
    $graphics.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
    $graphics.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::HighQuality
    $graphics.TextRenderingHint = [System.Drawing.Text.TextRenderingHint]::AntiAliasGridFit

    $backgroundRect = New-Object System.Drawing.Rectangle(0, 0, $width, $height)
    $background = New-Object System.Drawing.Drawing2D.LinearGradientBrush($backgroundRect, $BackgroundStart, $BackgroundEnd, 45)
    $graphics.FillRectangle($background, $backgroundRect)

    $blobA = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb(70, 255, 255, 255))
    $blobB = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb(55, $AccentSoft.R, $AccentSoft.G, $AccentSoft.B))
    $blobC = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb(32, 39, 57, 86))
    $graphics.FillEllipse($blobA, -120, -40, 520, 360)
    $graphics.FillEllipse($blobB, 620, 120, 380, 300)
    $graphics.FillEllipse($blobC, 110, 1460, 320, 180)

    $icon = [System.Drawing.Image]::FromFile((Join-Path $PSScriptRoot '..\..\..\..\app\src\main\res\mipmap-xxxhdpi\ic_launcher.png'))
    $iconX = 72
    $iconY = 78
    $iconSize = 88
    $iconShadowBrush = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb(26, 22, 31, 51))
    $iconShadowX = $iconX + 10
    $iconShadowY = $iconY + 16
    $iconShadowRect = New-Object System.Drawing.Rectangle($iconShadowX, $iconShadowY, $iconSize, $iconSize)
    $iconShadowPath = New-RoundedRectPath -Rect $iconShadowRect -Radius 24
    $graphics.FillPath($iconShadowBrush, $iconShadowPath)
    $graphics.DrawImage($icon, $iconX, $iconY, $iconSize, $iconSize)

    $brandFont = New-Object System.Drawing.Font('Segoe UI', 18, [System.Drawing.FontStyle]::Bold)
    $titleFont = New-Object System.Drawing.Font('Meiryo UI', 54, [System.Drawing.FontStyle]::Bold)
    $subtitleFont = New-Object System.Drawing.Font('Meiryo UI', 22, [System.Drawing.FontStyle]::Regular)
    $chipFont = New-Object System.Drawing.Font('Meiryo UI', 18, [System.Drawing.FontStyle]::Bold)
    $brandBrush = New-Object System.Drawing.SolidBrush([System.Drawing.ColorTranslator]::FromHtml('#24314F'))
    $subtitleBrush = New-Object System.Drawing.SolidBrush([System.Drawing.ColorTranslator]::FromHtml('#56647F'))
    $chipTextBrush = New-Object System.Drawing.SolidBrush([System.Drawing.ColorTranslator]::FromHtml('#24314F'))
    $accentBrush = New-Object System.Drawing.SolidBrush($AccentColor)

    $graphics.DrawString('KaraMemo', $brandFont, $brandBrush, 178, 104)

    $accentRect = New-Object System.Drawing.Rectangle(72, 208, 132, 12)
    $accentPath = New-RoundedRectPath -Rect $accentRect -Radius 6
    $graphics.FillPath($accentBrush, $accentPath)

    $titleRect = New-Object System.Drawing.RectangleF(72, 250, 936, 180)
    $subtitleRect = New-Object System.Drawing.RectangleF(72, 440, 900, 140)
    $format = New-Object System.Drawing.StringFormat
    $format.Alignment = [System.Drawing.StringAlignment]::Near
    $format.LineAlignment = [System.Drawing.StringAlignment]::Near
    $graphics.DrawString($Title, $titleFont, $brandBrush, $titleRect, $format)
    $graphics.DrawString($Subtitle, $subtitleFont, $subtitleBrush, $subtitleRect, $format)

    $chipX = 72
    foreach ($chip in $Chips) {
        $chipWidth = [Math]::Max(180, [int]([Math]::Ceiling($chip.Length * 28)))
        Draw-Chip -Graphics $graphics -Text $chip -Font $chipFont -TextBrush $chipTextBrush -FillColor ([System.Drawing.Color]::FromArgb(215, 255, 250, 240)) -BorderColor ([System.Drawing.Color]::FromArgb(70, $AccentColor.R, $AccentColor.G, $AccentColor.B)) -X $chipX -Y 590 -Width $chipWidth
        $chipX += $chipWidth + 16
    }

    $shot = [System.Drawing.Image]::FromFile($SourcePath)
    $targetHeight = 1180
    $targetWidth = [int][Math]::Round($shot.Width * ($targetHeight / [double]$shot.Height))
    $phoneX = [int][Math]::Floor(($width - $targetWidth) / 2)
    $phoneY = 690
    $shadowX = $phoneX + 12
    $shadowY = $phoneY + 22
    $shadowRect = New-Object System.Drawing.Rectangle($shadowX, $shadowY, $targetWidth, $targetHeight)
    $phoneRect = New-Object System.Drawing.Rectangle($phoneX, $phoneY, $targetWidth, $targetHeight)
    $shadowPath = New-RoundedRectPath -Rect $shadowRect -Radius 38
    $phonePath = New-RoundedRectPath -Rect $phoneRect -Radius 38
    $shadowBrush = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb(34, 29, 43, 71))
    $phoneFill = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb(248, 255, 252, 246))
    $phoneBorder = New-Object System.Drawing.Pen([System.Drawing.Color]::FromArgb(115, 255, 255, 255), 3)

    $graphics.FillPath($shadowBrush, $shadowPath)
    $graphics.FillPath($phoneFill, $phonePath)
    $graphics.DrawImage($shot, $phoneX + 8, $phoneY + 8, $targetWidth - 16, $targetHeight - 16)
    $graphics.DrawPath($phoneBorder, $phonePath)

    $accentCircleBrush = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb(120, $AccentColor.R, $AccentColor.G, $AccentColor.B))
    $graphics.FillEllipse($accentCircleBrush, 850, 1500, 120, 120)
    $graphics.FillEllipse($accentCircleBrush, 165, 1290, 70, 70)

    $canvas.Save($DestinationPath, [System.Drawing.Imaging.ImageFormat]::Png)

    $accentCircleBrush.Dispose()
    $phoneBorder.Dispose()
    $phoneFill.Dispose()
    $shadowBrush.Dispose()
    $phonePath.Dispose()
    $shadowPath.Dispose()
    $shot.Dispose()
    $format.Dispose()
    $accentPath.Dispose()
    $accentBrush.Dispose()
    $chipTextBrush.Dispose()
    $subtitleBrush.Dispose()
    $brandBrush.Dispose()
    $chipFont.Dispose()
    $subtitleFont.Dispose()
    $titleFont.Dispose()
    $brandFont.Dispose()
    $iconShadowPath.Dispose()
    $iconShadowBrush.Dispose()
    $icon.Dispose()
    $blobA.Dispose()
    $blobB.Dispose()
    $blobC.Dispose()
    $background.Dispose()
    $graphics.Dispose()
    $canvas.Dispose()
}

$sourceDir = Join-Path $PSScriptRoot 'ja-JP'
$outputDir = Join-Path $sourceDir 'store-ready'
New-Item -ItemType Directory -Force $outputDir | Out-Null

$items = @(
    @{
        Source = 'phone-01-song-list.png'
        Output = 'phone-store-01-song-list.png'
        Title = "曲を一覧で`nサッと確認"
        Subtitle = "アーティスト・キー・点数・お気に入りを`n1画面で見やすく管理できます。"
        Chips = @('キー管理', 'お気に入り', '点数メモ')
        BackgroundStart = [System.Drawing.ColorTranslator]::FromHtml('#FFF8F1')
        BackgroundEnd = [System.Drawing.ColorTranslator]::FromHtml('#FBE1C8')
        AccentColor = [System.Drawing.ColorTranslator]::FromHtml('#FF8B66')
        AccentSoft = [System.Drawing.ColorTranslator]::FromHtml('#FFD1B8')
    }
    @{
        Source = 'phone-02-artist-list.png'
        Output = 'phone-store-02-artist-list.png'
        Title = "アーティストごとに`n見つけやすい"
        Subtitle = "ピン留めや曲数表示で、よく歌う曲へ`nすぐたどり着けます。"
        Chips = @('ピン留め', '曲数表示', '一覧管理')
        BackgroundStart = [System.Drawing.ColorTranslator]::FromHtml('#FFF8F2')
        BackgroundEnd = [System.Drawing.ColorTranslator]::FromHtml('#F8DDD9')
        AccentColor = [System.Drawing.ColorTranslator]::FromHtml('#F07D7D')
        AccentSoft = [System.Drawing.ColorTranslator]::FromHtml('#F8C8C8')
    }
    @{
        Source = 'phone-03-playlist-list.png'
        Output = 'phone-store-03-playlist-list.png'
        Title = "プレイリストで`nまとめて整理"
        Subtitle = "練習用・お気に入り・高音曲など`n目的別にまとめて保存できます。"
        Chips = @('練習用', 'お気に入り', '高音曲')
        BackgroundStart = [System.Drawing.ColorTranslator]::FromHtml('#FFF9F0')
        BackgroundEnd = [System.Drawing.ColorTranslator]::FromHtml('#F6E3B9')
        AccentColor = [System.Drawing.ColorTranslator]::FromHtml('#F0B14E')
        AccentSoft = [System.Drawing.ColorTranslator]::FromHtml('#F7D98D')
    }
    @{
        Source = 'phone-04-song-editor.png'
        Output = 'phone-store-04-song-editor.png'
        Title = "キーも点数もメモも`n1曲ごとに保存"
        Subtitle = "歌った記録を残しながら、次に調整したい`nポイントもまとめて管理できます。"
        Chips = @('キー', '点数', 'メモ')
        BackgroundStart = [System.Drawing.ColorTranslator]::FromHtml('#FFF8F7')
        BackgroundEnd = [System.Drawing.ColorTranslator]::FromHtml('#EADCF8')
        AccentColor = [System.Drawing.ColorTranslator]::FromHtml('#9A7AE6')
        AccentSoft = [System.Drawing.ColorTranslator]::FromHtml('#D5C2F6')
    }
    @{
        Source = 'phone-05-karaoke-settings.png'
        Output = 'phone-store-05-karaoke-settings.png'
        Title = "機種ごとの設定も`nまとめて保存"
        Subtitle = "DAM / JOYSOUND の BGM・マイク・エコー・Music を`nあとから見返せます。"
        Chips = @('DAM', 'JOYSOUND', '設定メモ')
        BackgroundStart = [System.Drawing.ColorTranslator]::FromHtml('#F8F7FF')
        BackgroundEnd = [System.Drawing.ColorTranslator]::FromHtml('#DFE8FF')
        AccentColor = [System.Drawing.ColorTranslator]::FromHtml('#6F90FF')
        AccentSoft = [System.Drawing.ColorTranslator]::FromHtml('#C9D6FF')
    }
)

foreach ($item in $items) {
    Draw-StoreShot `
        -SourcePath (Join-Path $sourceDir $item.Source) `
        -DestinationPath (Join-Path $outputDir $item.Output) `
        -Title $item.Title `
        -Subtitle $item.Subtitle `
        -Chips $item.Chips `
        -BackgroundStart $item.BackgroundStart `
        -BackgroundEnd $item.BackgroundEnd `
        -AccentColor $item.AccentColor `
        -AccentSoft $item.AccentSoft
}
