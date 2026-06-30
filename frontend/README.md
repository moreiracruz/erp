# Frontend

Frontend Angular do ERP Reino & Flor.

Este projeto usa Angular 21, TypeScript strict, SCSS, Vitest e build multi-stage com Docker/Nginx.

## Pre-requisitos

- Node.js 20+
- npm 10.8.2+
- Docker 20+ e Docker Compose 2+, para subir via containers
- Backend disponivel em `http://localhost:8080` quando rodar o frontend em modo dev local

As versoes esperadas pelo projeto estao em `package.json` e `package-lock.json`.

## Instalar Dependencias

Dentro da pasta `frontend`:

```bash
npm ci
```

Use `npm ci` para reproduzir exatamente as dependencias travadas no `package-lock.json`.

## Subir em Desenvolvimento

Para rodar o Angular dev server:

```bash
cd frontend
npm start
```

Depois acesse:

```text
http://localhost:4200
```

Em desenvolvimento, o frontend chama a API configurada em `src/environments/environment.ts`:

```text
http://localhost:8080
```

Se precisar da API local, suba pelo Maven ou Docker a partir da raiz do repositorio. Um fluxo comum e:

```bash
docker compose up -d postgres
./mvnw spring-boot:run -pl bootstrap
```

## Subir com Docker

Na raiz do repositorio, para subir a stack completa:

```bash
docker compose up -d
```

Servicos principais:

- Frontend: `http://localhost`
- API: `http://localhost:8080`
- Prometheus: `http://localhost:9090`
- Grafana: `http://localhost:3000`

O frontend em Docker e servido por Nginx. Em producao, `src/environments/environment.prod.ts` usa `apiUrl: ''`; as chamadas para `/api/` sao encaminhadas pelo Nginx para o servico `app:8080`.

Para reconstruir apenas a imagem do frontend:

```bash
docker compose build frontend
```

Para subir apenas o container do frontend, sem esperar o backend:

```bash
docker compose up -d --no-deps frontend
```

Nesse modo, a interface sobe, mas chamadas para `/api/` so funcionam quando o backend estiver disponivel na rede do Compose.

## Build

Build de producao:

```bash
cd frontend
npm run build
```

Build de desenvolvimento com watch:

```bash
cd frontend
npm run watch
```

Os artefatos ficam em:

```text
frontend/dist/frontend
```

## Testes

Executar os testes unitarios com Vitest pelo builder do Angular:

```bash
cd frontend
npm test
```

Para rodar um subconjunto, use filtros do Angular/Vitest quando aplicavel:

```bash
cd frontend
npx ng test --include "src/app/features/auth/**/*.spec.ts"
```

O projeto tambem possui testes property-based com `fast-check`, identificados principalmente por arquivos `*.pbt.spec.ts`.

### E2E Frontend + Backend

Para validar a integracao real entre frontend servido por Nginx e backend Spring Boot via Docker Compose, rode a partir da raiz do repositorio:

```bash
./scripts/e2e-frontend-backend.sh
```

Esse smoke test sobe `postgres`, `app` e `frontend`, depois verifica:

- readiness do backend em `/actuator/health/readiness`
- HTML do frontend em `http://localhost`
- proxy do frontend para o backend em `/api/v1/products`
- seguranca basica: `POST /api/v1/products` sem token deve retornar `401`

## Comandos Uteis

```bash
# Angular CLI local
npm run ng -- version

# Servidor dev
npm start

# Build de producao
npm run build

# Build em watch/development
npm run watch

# Testes
npm test

# E2E frontend + backend via Docker Compose
../scripts/e2e-frontend-backend.sh

# Docker: build da imagem do frontend
docker compose build frontend

# Docker: subir apenas o frontend
docker compose up -d --no-deps frontend

# Docker: logs do frontend
docker compose logs -f frontend
```

## Estrutura

- `src/app/core`: modelos e ports do frontend
- `src/app/infrastructure`: adapters HTTP, auth/interceptors e storage
- `src/app/features`: telas e fluxos por area funcional
- `src/app/shared`: componentes e layout compartilhados
- `src/environments`: configuracao por ambiente
- `public`: assets estaticos copiados para o build

## Troubleshooting

### `npm: command not found`

Instale Node.js 20+ e npm, ou use o fluxo Docker:

```bash
docker compose build frontend
docker compose up -d --no-deps frontend
```

### Frontend sobe, mas API nao responde

Em dev local, confirme que a API esta em:

```text
http://localhost:8080
```

Em Docker, confirme os containers:

```bash
docker compose ps
docker compose logs -f app
```

### Warnings de Sass `darken()` / `lighten()`

O build atual pode emitir avisos de deprecacao do Sass para funcoes como `darken()` e `lighten()`. Eles nao impedem o build, mas devem ser migrados gradualmente para `color.adjust` ou `color.scale`.

### Warnings de budget de SCSS

Alguns componentes podem exceder o limite de warning de `4kB` configurado em `angular.json`. Isso nao quebra o build enquanto ficar abaixo do `maximumError`, mas indica pontos bons para enxugar estilos.

### Porta 80 ja em uso

Se `docker compose up` falhar por porta ocupada, pare o servico local que usa a porta 80 ou altere temporariamente o mapeamento no `docker-compose.yml`, por exemplo:

```yaml
ports:
  - "8081:80"
```
