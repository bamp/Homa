# Projeto - Cidades ESG Inteligentes

## homa.

**Qual é o seu momento agora, e o que a sua cidade tem para isso?**

O Homa reúne dicas e avaliações de quem mora na cidade, organizadas pelo momento de vida de cada pessoa: quem está chegando por trabalho, estudo ou concurso; quem mudou de vida sem mudar de cidade (virou pai ou mãe, trocou de bairro, ficou sem carro); e quem só quer sair do automático sem gastar muito. Moradores contam o que funciona (o prato feito bom e barato, a praça com sombra, o programa de graça) e avaliam a cidade em cinco dimensões de qualidade de vida.

**Onde entra o ESG:** mobilidade a pé e espaços verdes (Ambiental); acesso a cultura, lazer e alimentação a preço acessível (Social); critérios de avaliação abertos e iguais para todos, com a soma das percepções dos moradores mostrando o que funciona e o que falta em cada cidade (Governança). Referência: ODS 11.

| Ambiente | URL |
|---|---|
| Staging | `[URL STAGING]` |
| Produção | `[URL PRODUÇÃO]` |

**Integrantes:** [NOME COMPLETO – RM]

---

## Como executar localmente com Docker

Pré-requisitos: Docker e Docker Compose.

```bash
# 1. Clonar o repositório
git clone [URL DO REPOSITÓRIO]
cd homa

# 2. Criar o arquivo de variáveis a partir do exemplo
cp .env.example .env

# 3. Subir aplicação + banco
docker compose up --build -d

# 4. Conferir
docker compose ps
curl http://localhost:8080/actuator/health
curl http://localhost:8080/api/info
```

A interface fica em **http://localhost:8080**.

Para parar: `docker compose down` (os dados continuam no volume `homa-db-data`). Para apagar também os dados: `docker compose down -v`.

**Sem Docker** (só Java 21 e Maven): `mvn spring-boot:run`. Sem `DB_URL`, a aplicação usa H2 em memória.

### Endpoints da API

| Método | Rota | O que faz |
|---|---|---|
| GET | `/api/dicas?cidade=Passo Fundo&categoria=COMIDA&bairro=Centro&gratuita=true` | Lista dicas da cidade (filtros opcionais) |
| POST | `/api/dicas` | Cadastra uma dica |
| GET | `/api/avaliacoes?cidade=Passo Fundo` | Lista avaliações da cidade |
| GET | `/api/avaliacoes/resumo?cidade=Passo Fundo` | Média das cinco dimensões e comentários recentes |
| POST | `/api/avaliacoes` | Cadastra uma avaliação (notas de 1 a 5) |
| GET | `/api/info` | Ambiente, versão e commit publicado |
| GET | `/actuator/health` | Health check |
| GET | `/swagger-ui.html` | Documentação interativa da API (Swagger) |

Exemplo:

```bash
curl -X POST http://localhost:8080/api/dicas -H "Content-Type: application/json" -d '{
  "cidade": "Passo Fundo", "bairro": "Centro", "categoria": "COMIDA", "gratuita": false,
  "titulo": "O prato feito que salva a semana",
  "descricao": "Comida caseira por um preço justo, aberto até as 14h."
}'
```

Categorias: `COMIDA`, `CULTURA`, `FAMILIA`, `ESPACOS`, `SERVICOS`.

---

## Pipeline CI/CD

**Ferramenta:** GitHub Actions (`.github/workflows/ci-cd.yml`). **Registro de imagens:** Docker Hub. **Hospedagem:** Azure App Service (Linux, container), com um Web App para staging e outro para produção.

### Fluxo de branches

`feature/*` → pull request para `develop` → merge em `develop` (deploy em **staging**) → pull request para `main` → merge em `main` (deploy em **produção**, com aprovação).

Todo pull request roda build e testes antes do merge: se um teste falha, o merge não deve ser feito.

### Etapas

| Etapa | Job | Quando roda | O que acontece |
|---|---|---|---|
| 1 | Build e testes | sempre | Java 21 + `mvn verify`: compila e roda os testes automatizados (JUnit + MockMvc). Os relatórios e o `.jar` ficam guardados como artefato. |
| 2 | Imagem Docker | sempre (`needs: build-test`) | Monta a imagem, sobe um container e confere `/actuator/health`. Em push, publica no Docker Hub com a tag do **SHA do commit** e o nome da branch. |
| 3 | Deploy em staging | push na `develop` | `azure/webapps-deploy` publica a imagem do commit no Web App de staging. O script `aguardar-deploy.sh` espera `/api/info` responder com o mesmo commit. |
| 4 | Deploy em produção | push na `main` | Espera **aprovação manual** (environment `production` com *required reviewer*). Depois de aprovado, publica no Web App de produção e confere da mesma forma. |

Os jobs dependem uns dos outros (`needs`): sem testes aprovados não há imagem, e sem imagem não há deploy.

**Entrega contínua:** tudo é automático até a produção, e a última etapa depende de uma aprovação manual. Se o deploy em produção fosse automático a cada alteração aprovada nos testes, o pipeline seria de implantação contínua.

### Segredos e variáveis (GitHub → Settings)

Nenhuma credencial fica no código: tudo vai em GitHub Secrets ou em variáveis de ambiente.

| Nome | Tipo | Onde | Para quê |
|---|---|---|---|
| `DOCKERHUB_USERNAME` | secret | repositório | usuário do Docker Hub |
| `DOCKERHUB_TOKEN` | secret | repositório | token de acesso do Docker Hub |
| `AZURE_PROFILE_STAGING` | secret | environment `staging` | publish profile do Web App de staging |
| `AZURE_PROFILE_PRODUCTION` | secret | environment `production` | publish profile do Web App de produção |
| `STAGING_APP_NAME`, `STAGING_URL` | variável | environment `staging` | nome e URL do Web App de staging |
| `PRODUCTION_APP_NAME`, `PRODUCTION_URL` | variável | environment `production` | nome e URL do Web App de produção |

### Configuração de cada Web App (Azure → Variáveis de ambiente)

| Variável | Staging | Produção |
|---|---|---|
| `APP_ENV` | `staging` | `producao` |
| `WEBSITES_PORT` | `8080` | `8080` |
| `WEBSITES_ENABLE_APP_SERVICE_STORAGE` | `true` | `true` |
| `DB_URL` | `jdbc:h2:file:/home/data/homa;FILE_LOCK=NO` | `jdbc:h2:file:/home/data/homa;FILE_LOCK=NO` |
| `WEBSITES_CONTAINER_START_TIME_LIMIT` | `600` | `600` |

### Decisões

- **Plano F1 (gratuito):** suficiente para demonstração. A troca para B1 ou superior é só uma mudança de plano no portal, sem alterar o pipeline.
- **Banco:** localmente, o Compose usa PostgreSQL. Na nuvem, cada ambiente usa H2 em arquivo no armazenamento persistente do próprio App Service (`/home`), o que mantém os dois ambientes isolados sem custo de banco gerenciado. A aplicação troca de banco só pela variável `DB_URL`, e um banco gerenciado (Azure Database for PostgreSQL) fica como próximo passo.
- **Estratégia de deploy:** *recreate*, em que o Web App troca a imagem e reinicia. Há uma breve indisponibilidade, aceitável num plano gratuito sem slots.

### Rollback

Toda imagem fica no Docker Hub com a tag do SHA do commit. Para voltar uma versão:
1. Rodar de novo, em Actions, o workflow de um commit anterior que funcionava (*Re-run jobs*); ou
2. No portal da Azure, em Deployment Center, apontar o Web App para a tag do SHA anterior.

### Monitoramento

- Execução e logs de cada job: aba **Actions** do GitHub, com resumo dos testes e da imagem publicada.
- Logs do container e do deploy: **Deployment Center** e **Log stream** de cada Web App na Azure.
- Saúde da aplicação: `/actuator/health`. Versão publicada: `/api/info`.

### Documentação da API

Swagger UI em `/swagger-ui.html`, com a especificação OpenAPI em `/v3/api-docs`.

---

## Containerização

### Dockerfile

```dockerfile
# ---------- Etapa 1: build ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -q package -DskipTests

# ---------- Etapa 2: runtime ----------
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S homa && adduser -S homa -G homa
COPY --from=build /app/target/homa.jar app.jar
USER homa
ARG GIT_COMMIT=desconhecido
ENV GIT_COMMIT=${GIT_COMMIT} \
    PORT=8080 \
    JAVA_OPTS="-XX:MaxRAMPercentage=75"
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD wget -qO- http://localhost:${PORT}/actuator/health | grep -q UP || exit 1
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
```

**Estratégias adotadas:**

- **Multi-stage build:** o Maven e o JDK ficam só na etapa de build. A imagem final leva apenas o JRE Alpine e o `.jar`.
- **Cache de dependências:** o `pom.xml` é copiado antes do código, então as dependências só são baixadas de novo quando ele muda.
- **Usuário sem privilégios:** o container não roda como root.
- **Health check** em `/actuator/health`, usado pelo Docker e pelo pipeline.
- **Commit gravado na imagem** (`GIT_COMMIT`), exposto em `/api/info`, para confirmar qual versão está em cada ambiente.
- **Configuração por variáveis de ambiente** (porta, banco, ambiente), sem nada fixo no código.

### Docker Compose

`docker-compose.yml` sobe dois serviços:

- **db:** PostgreSQL 16, com health check (`pg_isready`).
- **app:** a aplicação, que só sobe depois que o banco está saudável (`depends_on: condition: service_healthy`).

Os dois serviços usam a **rede** `homa-net` (bridge) e o **volume** nomeado `homa-db-data`, onde ficam os dados do Postgres. As **variáveis de ambiente** vêm do arquivo `.env` (modelo em `.env.example`).

---

## Prints do funcionamento

> Prints em `docs/prints/`.

1. Pipeline completo no GitHub Actions: `docs/prints/01-pipeline.png`
2. Testes aprovados: `docs/prints/02-testes.png`
3. Imagem publicada no Docker Hub: `docs/prints/03-imagem.png`
4. Aprovação do deploy de produção: `docs/prints/04-aprovacao.png`
5. Staging funcionando: `docs/prints/05-staging.png`
6. Produção funcionando: `docs/prints/06-producao.png`
7. Execução local com `docker compose ps`: `docs/prints/07-compose.png`

---

## Tecnologias utilizadas

- **Java 21** e **Spring Boot 3.3**: Web, Data JPA, Validation, Actuator
- **springdoc-openapi** (Swagger UI)
- **PostgreSQL 16** (local, via Compose) e **H2** (testes e ambientes na nuvem)
- **JUnit 5** e **MockMvc** (testes automatizados)
- **Docker** (multi-stage) e **Docker Compose**
- **GitHub Actions** (CI/CD) e **Docker Hub** (registro de imagens)
- **Azure App Service** (Linux, containers), plano F1, para staging e produção
- **HTML, CSS e JavaScript** puros na interface, servidos pela própria aplicação

---

## Checklist de entrega

| Item | OK |
|---|---|
| Projeto compactado em .ZIP com estrutura organizada | ☐ |
| Dockerfile funcional | ☐ |
| docker-compose.yml ou arquivos Kubernetes | ☐ |
| Pipeline com etapas de build, teste e deploy | ☐ |
| README.md com instruções e prints | ☐ |
| Documentação técnica com evidências (PDF ou PPT) | ☐ |
| Deploy realizado nos ambientes staging e produção | ☐ |
