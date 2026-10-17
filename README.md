# dev_SpringBoot_SplittersService

Rede óptica no Elleven: splitters, OLTs, conexões, solicitações de cliente e alertas de protocolo pendente.

Parte da migração do BFF para API Gateway (`Sebratel/dev_SpringBoot_APIGateway`, `specs/API_GATEWAY.md`).
Serviço **splitters-service**, porta **8102**, atrás do `dev_SpringBoot_Gateway`.

> **Estado: esqueleto.** Sobe, expõe `/actuator/health` (público; o resto exige JWT) e tem build, Checkstyle e JaCoCo ativos, mas **a lógica ainda está no
> monólito**. As rotas abaixo são o contrato que este serviço precisa atender, idêntico ao do BFF atual (`contract/routes.json`).

## Rotas que este serviço deve atender (8)

| Método | Rota | Acesso |
|---|---|---|
| `POST` | `/api/v1/splitters/alertas-protocolo-pendente` | autenticada |
| `GET` | `/api/v1/splitters/listarConnections` | autenticada |
| `GET` | `/api/v1/splitters/listarOlts` | autenticada |
| `GET` | `/api/v1/splitters/listarSplitters` | autenticada |
| `GET` | `/api/v1/splitters/listarSplitters-paginado` | autenticada |
| `GET` | `/api/v1/splitters/recuperarToken` | autenticada |
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
arquivo `docker-compose-stage.yml`. O container se chama `splitters-service-stage` e fica na rede externa `proxy`.
Sem porta publicada no host: só o gateway, na rede `proxy`, alcança este serviço.

A imagem é construída pelo `Dockerfile` (multi-stage, Temurin 21, usuário não-root). O `sebratel-java-parent` é construído
da fonte pública na própria imagem, então o build **não precisa de token**.

O branch `staging` recebe o código mais atualizado: `main` → `staging` antes de cada deploy.
