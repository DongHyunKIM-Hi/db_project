#!/usr/bin/env bash
# 수업 시연: docker compose 한 방으로 DB + 서점 API + Prometheus + Grafana 전부 띄운다
set -e
cd "$(dirname "$0")/.."   # 프로젝트 루트 기준

# demo-up.sh 로 띄운 단독 컨테이너가 남아 있으면 이름이 겹치므로 먼저 치운다
docker rm -f prometheus grafana 2>/dev/null || true

docker compose up -d --build

echo ""
echo "⏳ 전부 뜨는 데 잠시 걸립니다..."
for i in $(seq 1 90); do
  if curl -sf http://localhost:8080/actuator/health > /dev/null 2>&1 \
     && curl -sf http://localhost:3000/api/health > /dev/null 2>&1; then break; fi
  sleep 1
done

echo ""
docker compose ps
echo ""
echo "✅ 서점 API   : http://localhost:8080/actuator/health"
echo "✅ Prometheus : http://localhost:9090/targets   (bookstore 가 UP 인지)"
echo "✅ Grafana    : http://localhost:3000/d/jvm-demo (로그인 없음, 대시보드 바로)"
open "http://localhost:3000/d/jvm-demo" 2>/dev/null || true
