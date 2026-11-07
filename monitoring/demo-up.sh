#!/usr/bin/env bash
# 수업 시연: Prometheus + Grafana 를 docker run 두 줄로 띄운다
set -e
cd "$(dirname "$0")/.."   # 프로젝트 루트 기준

docker run -d --name prometheus -p 9090:9090 \
  -v "$(pwd)/monitoring/prometheus.yml:/etc/prometheus/prometheus.yml" \
  prom/prometheus

docker run -d --name grafana -p 3000:3000 \
  -e GF_AUTH_ANONYMOUS_ENABLED=true \
  -e GF_AUTH_ANONYMOUS_ORG_ROLE=Admin \
  -e GF_AUTH_DISABLE_LOGIN_FORM=true \
  -v "$(pwd)/monitoring/grafana/provisioning:/etc/grafana/provisioning" \
  -v "$(pwd)/monitoring/grafana/dashboards:/var/lib/grafana/dashboards" \
  grafana/grafana

echo ""
echo "⏳ Grafana 가 뜨는 데 몇 초 걸립니다..."
for i in $(seq 1 30); do
  if curl -sf http://localhost:3000/api/health > /dev/null 2>&1; then break; fi
  sleep 1
done

echo "✅ Prometheus : http://localhost:9090/targets   (bookstore 가 UP 인지)"
echo "✅ Grafana    : http://localhost:3000/d/jvm-demo (로그인 없음, 대시보드 바로)"
open "http://localhost:3000/d/jvm-demo" 2>/dev/null || true
