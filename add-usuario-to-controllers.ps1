# Script para agregar model.addAttribute("Usuario", ...) a todos los controladores
# Este script busca líneas que contengan model.addAttribute("user", y añade la línea Usuario justo después

$controllersPath = "c:\Users\jesus\Desktop\SOLO_TRABAJO\jesusleon\src\main\java\org\project\project\controller"

# Lista de archivos de controladores a modificar
$controllers = @(
    "ProjectController.java",
    "RepositoryController.java",
    "TicketViewController.java",
    "FeedbackController.java",
    "DocumentationController.java",
    "PlatformUserManagementController.java",
    "RepositoryFilesController.java",
    "ProjectRepositoryFilesController.java",
    "TeamController.java",
    "ApiController.java",
    "ForumController.java",
    "TestEnvironmentController.java"
)

$totalModified = 0

foreach ($controller in $controllers) {
    $filePath = Join-Path $controllersPath $controller
    
    if (-not (Test-Path $filePath)) {
        Write-Host "Skip $controller (not found)" -ForegroundColor Yellow
        continue
    }
    
    Write-Host "Processing: $controller" -ForegroundColor Cyan
    
    # Leer el contenido del archivo
    $content = Get-Content $filePath -Raw
    
    # Buscar todas las líneas model.addAttribute("user", variable);
    $regex = [regex]'model\.addAttribute\("user",\s*(\w+)\);'
    $matches = $regex.Matches($content)
    
    if ($matches.Count -eq 0) {
        Write-Host "  No 'user' attributes found" -ForegroundColor Gray
        continue
    }
    
    Write-Host "  Found $($matches.Count) occurrence(s)" -ForegroundColor Green
    
    # Procesar de atrás hacia adelante para no afectar posiciones
    $modified = $false
    for ($i = $matches.Count - 1; $i -ge 0; $i--) {
        $match = $matches[$i]
        $fullMatch = $match.Value
        $variableName = $match.Groups[1].Value
        $position = $match.Index + $match.Length
        
        # Buscar si ya existe Usuario después
        $afterMatch = $content.Substring($position, [Math]::Min(100, $content.Length - $position))
        if ($afterMatch -match 'model\.addAttribute\("Usuario",\s*' + $variableName) {
            Write-Host "  Already has Usuario for '$variableName'" -ForegroundColor DarkGray
            continue
        }
        
        # Insertar nueva línea después
        $newLine = "`n        model.addAttribute(""Usuario"", $variableName);  // Para chatbot widget"
        $content = $content.Insert($position, $newLine)
        $modified = $true
        $totalModified++
        
        Write-Host "  Added Usuario for '$variableName'" -ForegroundColor Green
    }
    
    # Guardar el archivo si fue modificado
    if ($modified) {
        Set-Content -Path $filePath -Value $content -NoNewline
        Write-Host "  Saved $controller" -ForegroundColor Magenta
    }
    
    Write-Host ""
}

Write-Host "Completed! Modified $totalModified locations" -ForegroundColor Green
