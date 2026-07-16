# ── Trading Simulator local tunnels for Kubernetes ────────────────
# Run this script to expose the Frontend and API Gateway to your Windows host.
#
# Access URLs:
#   Frontend:    http://localhost:4200
#   API Gateway: http://localhost:8000

Write-Host "================────────────────────────────────========" -ForegroundColor Green
Write-Host "  Starting local TCP tunnels to Kubernetes services..." -ForegroundColor Green
Write-Host "================────────────────────────────────========" -ForegroundColor Green
Write-Host "  Frontend:    http://localhost:4200" -ForegroundColor Cyan
Write-Host "  API Gateway: http://localhost:8000" -ForegroundColor Cyan
Write-Host "  Press Ctrl+C to stop the tunnels and exit." -ForegroundColor Yellow
Write-Host "================────────────────────────────────========"

# Clean up any existing tunnels first
Get-Job -Name "K8sFrontendTunnel","K8sGatewayTunnel" -ErrorAction SilentlyContinue | Remove-Job -Force

# Start port forwarding in background jobs
Start-Job -Name "K8sFrontendTunnel" -ScriptBlock { 
    kubectl port-forward svc/frontend 4200:80 -n trading-sim 2>&1
} | Out-Null

Start-Job -Name "K8sGatewayTunnel" -ScriptBlock {
    kubectl port-forward svc/api-gateway 8000:8000 -n trading-sim 2>&1
} | Out-Null

# Handle termination gracefully
try {
    while ($true) {
        Start-Sleep 1
    }
}
finally {
    Write-Host "`nStopping tunnels..." -ForegroundColor Yellow
    Get-Job -Name "K8sFrontendTunnel","K8sGatewayTunnel" | Stop-Job
    Get-Job -Name "K8sFrontendTunnel","K8sGatewayTunnel" | Remove-Job -Force
    Write-Host "Tunnels stopped." -ForegroundColor Green
}
