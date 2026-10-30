# dev_SpringBoot_SplittersService

Rede óptica no Elleven: splitters, OLTs, conexões, solicitações de cliente e alertas de protocolo pendente.

Parte da migração do BFF para API Gateway (`Sebratel/dev_SpringBoot_APIGateway`, `specs/API_GATEWAY.md`).
Serviço **splitters-service**, porta **8102**, atrás do `dev_SpringBoot_Gateway`.

> **Estado: esqueleto.** Sobe, expõe `/actuator/health` (público; o resto exige JWT) e tem build, Checkstyle e JaCoCo ativos, mas **a lógica ainda está no
> monólito**. As rotas abaixo são o contrato que este serviço precisa atender, idêntico ao do BFF atual (`contract/routes.json`).

## Rotas que este serviço deve atender (9)

| Método | Rota | Acesso |
|---|---|---|
| `POST` | `/api/v1/splitters/alertas-protocolo-pendente` | autenticada |
| `GET` | `/api/v1/splitters/listarConnections` | autenticada |
| `GET` | `/api/v1/splitters/listarOlts` | autenticada |
| `GET` | `/api/v1/splitters/listarSplitters` | autenticada |
| `GET` | `/api/v1/splitters/listarSplitters-paginado` | autenticada |
| `GET` | `/api/v1/splitters/recuperarToken` | autenticada |
| `POST` | `/api/v1/splitters/rotas-por-pppoe` | autenticada |
| `GET` | `/api/v1/splitters/solicitacoes/cliente/{clientId}` | autenticada |
| `GET` | `/api/v1/splitters/{splitterId}` | autenticada |

## Desenvolvimento

O `sebratel-java-parent` vem do GitHub Packages (o Maven exige token `read:packages` até para pacotes públicos). Antes do primeiro build, crie no `~/.m2/settings.xml` um servidor
`github` com um token `read:packages`:

```xml
<settings><servers><server>
  <id>github</id><username>SEU_USUARIO</username><password>SEU_TOKEN</password>
</server></servers></settings>
```

```bash
./mvnw verify          # testes + Checkstyle + JaCoCo (cobertura mínima 0.50, herdada do parent)
./mvnw spring-boot:run
```

Variáveis de ambiente: ver `.env.example` (nunca commite o `.env`).

## Regras do repositório
- `main` só recebe PR, com 1 aprovação e o check `ci-ok` verde. Commits no padrão Conventional Commits.
- Dependências compartilhadas (`common-web`, `common-security`, `common-elleven`) vêm de `dev_Maven_Commons`
  e entram quando houver código a compartilhar.
- Remover ou renomear rota é mudança incompatível: o contrato é o manifesto do monólito (`docs/contract/manifest.json`).

## Deploy em staging (Portainer)

Stack criada **a partir do Git**: repositório `https://github.com/Sebratel/dev_SpringBoot_SplittersService`, branch `staging`,
arquivo `docker-compose-stage.yml`. A stack se chama `dev_springboot_splittersservice` e o serviço `backend`
(container `dev_springboot_splittersservice-backend-1`); fica na rede externa `gateway-backend`, com o alias `splitters-service`.
Sem porta publicada no host: só o gateway, na rede `gateway-backend`, alcança este serviço em `http://splitters-service:8102`.

A imagem `ghcr.io/sebratel/splitters-service:staging` é construída pelo GitHub Actions (`.github/workflows/staging-image.yml`, a cada push em `staging`) a partir
do `Dockerfile` (multi-stage, Temurin 21, usuário não-root) e publicada no GHCR; o Portainer só a baixa. Construir a imagem dentro do host
passa de 90 s, e o proxy do stage derruba a requisição de deploy. O `sebratel-java-parent` é construído da fonte pública na própria imagem, sem token.

O branch `staging` recebe o código mais atualizado: `main` → `staging` antes de cada deploy.

## Estado da migração

O código dos endpoints deste serviço **já foi movido** do BFF original (`dev_SpringBoot_APIGateway`) para cá: controllers, services, repositórios e testes, com a mesma resposta (o contrato fica em `contract/routes.json`). O gateway roteia estas rotas para este serviço.

Variáveis de ambiente de banco/integração: ver `.env.example`. No stage elas são definidas na stack do Portainer.
