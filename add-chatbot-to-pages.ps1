# Script para añadir chatbot widget a todas las páginas HTML importantes
# Inserta el widget antes de </body> en páginas que no lo tengan

$templatesPath = "c:\Users\jesus\Desktop\SOLO_TRABAJO\jesusleon\src\main\resources\templates"

# Páginas importantes donde debe aparecer el chatbot (usuarios autenticados)
$pages = @(
    # Repositorios
    "repository\dashboard.html",
    "repository\detail.html",
    "repository\files.html",
    "repository\create.html",
    "repository\edit.html",
    
    # Proyectos
    "project\overview.html",
    "project\dashboard.html",
    "project\detail.html",
    "project\create.html",
    
    # Tickets
    "ticket\mis-tickets.html",
    "ticket\tickets-disponibles.html",
    "ticket\detalle-ticket.html",
    "ticket\nuevo-ticket.html",
    
    # Teams
    "teams\teams-list.html",
    "teams\team-detail.html",
    "teams\team-edit.html",
    
    # Feedback
    "feedback\overview.html",
    "feedback\list-feedbacks.html",
    "feedback\create.html",
    "feedback\detail.html",
    
    # Documentation
    "documentation\overview.html",
    "documentation\detail.html",
    
    # API
    "api\overview.html",
    "api\detail.html",
    
    # Reports
    "report\overview.html",
    "report\list-reports.html",
    "report\create.html",
    
    # Test Environment
    "test-environment\sandbox.html",
    
    # PO específicas
    "po\platform-user-management.html",
    "po\user-profile-view.html",
    
    # SA específicas
    "sa\dashboard-panel.html",
    "sa\superadmin.html"
)

$widgetCode = @'
    <!-- Chatbot Widget -->
    <div th:replace="~{fragments/chatbot-widget :: chatbot-widget}"></div>
'@

$totalModified = 0
$totalSkipped = 0
$totalNotFound = 0

foreach ($page in $pages) {
    $filePath = Join-Path $templatesPath $page
    
    if (-not (Test-Path $filePath)) {
        Write-Host "Not found: $page" -ForegroundColor DarkGray
        $totalNotFound++
        continue
    }
    
    Write-Host "Processing: $page" -ForegroundColor Cyan
    
    # Leer contenido
    $content = Get-Content $filePath -Raw
    
    # Verificar si ya tiene el chatbot widget
    if ($content -match 'chatbot-widget') {
        Write-Host "  Already has chatbot widget" -ForegroundColor DarkGray
        $totalSkipped++
        continue
    }
    
    # Buscar </body>
    if ($content -notmatch '</body>') {
        Write-Host "  No </body> tag found, skipping" -ForegroundColor Yellow
        $totalSkipped++
        continue
    }
    
    # Insertar widget antes de </body>
    $content = $content -replace '([\s\r\n]*)</body>', "$widgetCode`r`n`$1</body>"
    
    # Guardar archivo
    Set-Content -Path $filePath -Value $content -NoNewline
    Write-Host "  Added chatbot widget" -ForegroundColor Green
    $totalModified++
}

Write-Host ""
Write-Host "====== SUMMARY ======" -ForegroundColor Magenta
Write-Host "Modified: $totalModified" -ForegroundColor Green
Write-Host "Skipped: $totalSkipped" -ForegroundColor Yellow
Write-Host "Not found: $totalNotFound" -ForegroundColor DarkGray
Write-Host "Total processed: $($pages.Count)" -ForegroundColor Cyan
