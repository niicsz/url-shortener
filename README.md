# BiniTech • Encurtador de URLs

Encurtador de URLs desenhado como um sistema de produção: arquitetura hexagonal,
**Cassandra** como store principal, **Redis** para geração de IDs e cache, short codes
**Base62/Hashids** sem colisão e um frontend **Angular** servido pela própria aplicação.

O projeto segue a arquitetura do **BiniTech PDV** (Spring Boot + Angular num único artefato,
ports & adapters) e o system design de um encurtador de URL pensado para alta volumetria
(escrita massiva, leitura ainda mais pesada, 24x7).

## Visão geral

- `POST /api/v1/shorten` recebe uma URL longa e devolve uma URL curta de 7 caracteres.
- `GET /{shortCode}` resolve o código e responde `302 Found` para a URL original.
- `GET /api/v1/stats/{shortCode}` devolve a contagem de cliques do código.
- O frontend Angular consome esses endpoints e mostra um painel de cliques.

## Arquitetura

```
              Angular SPA (servida estática pelo Spring)
                              │  /api/v1
                              ▼
            ┌──────────────────────────────────────────┐
  inbound   │  UrlController · RedirectController       │
   web      │  GlobalExceptionHandler · WebMapper       │
            └───────────────┬──────────────────────────┘
                            ▼  ShortenUrlUseCasePort
            ┌──────────────────────────────────────────┐
 application │  ShortenUrlUseCaseImpl                    │
            └──┬──────────┬──────────┬──────────┬───────┘
   outbound    ▼          ▼          ▼          ▼
            IdGenerator  Cache     UrlRepo   ClickAnalytics  ShortCodeEncoder
              (Redis)   (Redis)   (Cassandra) (Cassandra)      (Hashids)
```

A camada de domínio e os casos de uso não conhecem framework nem banco: tudo entra e sai
por **ports**. Trocar Cassandra/Redis/Hashids é trocar um adapter.

### Fluxo de encurtamento

1. `INCR url:counter` no Redis devolve um ID inteiro único (inicia em `14_000_000`).
2. O ID é convertido em Base62 com Hashids (`salt` + `minLength=7`) — código único, sem ida ao banco.
3. `{short_code, long_url, created_at}` é persistido no Cassandra (`urls`).
4. O par é gravado no cache do Redis e a URL curta é devolvida com `201 Created`.

### Fluxo de redirecionamento (cache-aside)

1. Busca o código no Redis. Em cache hit, redireciona na hora.
2. Em cache miss, busca no Cassandra, popula o cache e redireciona.
3. Cada acesso incrementa um `counter` no Cassandra (`url_clicks`) de forma assíncrona,
   sem somar latência ao `302`.

O `302` (e não `301`) é uma decisão de arquitetura: mantém todo o tráfego passando pela
aplicação, permitindo medir cliques.

## Por que Base62 + Hashids + contador

- 62 caracteres (`0-9a-zA-Z`); `62^7` passa de 3,5 trilhões de combinações — folga para anos
  de operação com códigos de 7 caracteres.
- Um contador atômico garante unicidade **sem** consultar o banco a cada escrita (ao contrário
  de truncar MD5/SHA, que gera colisões e leituras extras).
- O Hashids embaralha o mapeamento ID → código, evitando enumeração sequencial das URLs.

## Stack

| Camada      | Tecnologia                                              |
|-------------|---------------------------------------------------------|
| Backend     | Java 21, Spring Boot 3.5, arquitetura hexagonal         |
| Contrato    | OpenAPI (openapi-generator) — controllers implementam interfaces geradas |
| Store       | Cassandra 5 (acesso por chave `short_code`)             |
| IDs / Cache | Redis 7 (`INCR` + cache-aside)                          |
| Short code  | Hashids (Base62, `minLength=7`)                         |
| Frontend    | Angular 21 + Angular Material (standalone, signals)     |
| Build       | Maven, Docker multi-stage (JAR único), docker-compose   |

## Endpoints

| Método | Rota                          | Descrição                              |
|--------|-------------------------------|----------------------------------------|
| POST   | `/api/v1/shorten`             | Encurta uma URL (`{ "url": "..." }`)   |
| GET    | `/{shortCode}`                | Redireciona (`302`) para a URL original|
| GET    | `/api/v1/stats/{shortCode}`   | Estatísticas (cliques) de um código    |
| GET    | `/swagger-ui.html`            | Documentação interativa da API         |
| GET    | `/actuator/health`            | Health check                           |

## Como rodar

### Tudo containerizado (recomendado)

```bash
docker compose up --build
```

Sobe Cassandra, Redis e a aplicação (frontend já embutido). Acesse `http://localhost:8080`.

```bash
curl -X POST http://localhost:8080/api/v1/shorten \
  -H 'Content-Type: application/json' \
  -d '{"url":"https://www.example.com/uma/url/bem/comprida"}'

curl -i http://localhost:8080/<shortCode>
curl http://localhost:8080/api/v1/stats/<shortCode>
```

### Desenvolvimento local

```bash
docker compose up -d cassandra redis
./mvnw spring-boot:run

cd frontend && npm install && npm start   # http://localhost:4200 com proxy para /api
```

## Estrutura

```
url-shortener/
├── src/main/java/com/binitech/shortener/
│   ├── domain/                 ShortUrl, UrlStats, exceptions
│   ├── application/
│   │   ├── ports/inbound|outbound/
│   │   └── usecases/           ShortenUrlUseCaseImpl
│   ├── adapters/
│   │   ├── inbound/web/        controllers + mapper (+ generated)
│   │   └── outbound/           cassandra · id (redis) · cache (redis) · shortcode (hashids)
│   └── config/                 wiring, properties, schema init, SPA
├── src/main/resources/openapi/openapi.yaml
├── frontend/                   Angular 21 SPA
├── Dockerfile                  multi-stage (Angular → Maven → JRE)
└── docker-compose.yml          cassandra + redis + app
```

## Configuração

Variáveis principais (com defaults em `application.yaml`):

| Variável                   | Default                       |
|----------------------------|-------------------------------|
| `CASSANDRA_CONTACT_POINTS` | `127.0.0.1`                   |
| `REDIS_URL`                | `redis://localhost:6379`      |
| `SHORTENER_BASE_URL`       | `http://localhost:8080`       |
| `SHORTENER_SALT`           | `binitech-url-shortener`      |
| `SHORTENER_MIN_LENGTH`     | `7`                           |
| `SHORTENER_INITIAL_ID`     | `14000000`                    |
