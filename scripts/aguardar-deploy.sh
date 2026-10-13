#!/usr/bin/env bash
# Espera até o ambiente responder com o commit que acabou de ser publicado.
# Uso: ./scripts/aguardar-deploy.sh <url-do-ambiente> <sha-do-commit>
set -euo pipefail

URL="${1:?informe a URL do ambiente}"
SHA="${2:?informe o commit}"
TENTATIVAS=60   # 60 x 15s = 15 minutos (o plano gratuito F1 da Azure demora para baixar a imagem e iniciar o Java)

for i in $(seq 1 "$TENTATIVAS"); do
  INFO=$(curl -fsS --max-time 20 "$URL/api/info" || true)
  if echo "$INFO" | grep -q "$SHA"; then
    echo "Deploy confirmado em $URL"
    echo "$INFO"
    curl -fsS "$URL/actuator/health"
    echo
    {
      echo "## Deploy confirmado"
      echo "- URL: $URL"
      echo "- Resposta de /api/info: \`$INFO\`"
    } >> "${GITHUB_STEP_SUMMARY:-/dev/null}"
    exit 0
  fi
  echo "[$i/$TENTATIVAS] ainda não está no ar com o commit ${SHA:0:7}... (${INFO:-sem resposta})"
  sleep 15
done

echo "O ambiente não respondeu com o commit $SHA a tempo."
exit 1
