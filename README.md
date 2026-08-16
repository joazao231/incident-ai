# Incident AI

Backend de portfólio em Java 17 e Spring Boot 4.1 para cadastrar aplicações, verificar endpoints HTTP periodicamente, medir latência, registrar eventos e gerenciar incidentes.

**Demo online:** https://incident-ai-ad5i.onrender.com

## Recursos

- CRUD validado de aplicações monitoradas.
- Autenticação stateless por token assinado e senhas protegidas com BCrypt.
- Controle de acesso com perfis `ADMIN` e `VIEWER`.
- Dashboard web responsivo para operação dos serviços e incidentes.
- Estados `UNKNOWN`, `HEALTHY`, `DEGRADED` e `DOWN`.
- Verificação manual e agendada com timeout e limite de degradação configuráveis.
- Histórico das 100 verificações/eventos mais recentes por aplicação.
- Incidentes automáticos ao detectar indisponibilidade e resolução automática na recuperação.
- Incidentes manuais, reconhecimento e resolução.
- Respostas de erro padronizadas, Actuator e Swagger/OpenAPI.
- H2 persistente por padrão; PostgreSQL por variáveis de ambiente.

## Executar

Requisitos: JDK 17+ e `JAVA_HOME` apontando para a instalação do JDK.

```powershell
.\mvnw.cmd clean test
.\mvnw.cmd spring-boot:run
```

Sem configuração adicional, os dados são salvos em `./data`. Para PostgreSQL:

```powershell
$env:DATABASE_URL="jdbc:postgresql://localhost:5432/incident_ai"
$env:DATABASE_USERNAME="postgres"
$env:DATABASE_PASSWORD="sua_senha"
.\mvnw.cmd spring-boot:run
```

Swagger: `http://localhost:8080/swagger-ui.html`
Health: `http://localhost:8080/actuator/health`
Dashboard: `http://localhost:8080/`

No primeiro uso local, entre no dashboard com:

```text
Usuário: admin
Senha: admin123
```

Troque essas credenciais por variáveis de ambiente antes de publicar o sistema.

## Autenticação e acesso

O login em `POST /api/auth/login` devolve um token. Nas chamadas diretas à API, envie-o no cabeçalho:

```text
Authorization: Bearer SEU_TOKEN
```

- `ADMIN`: consulta e executa todas as operações, inclusive cadastro de aplicações e usuários.
- `VIEWER`: consulta aplicações, incidentes, eventos e status, sem permissão de alteração.

## Endpoints

| Método | Rota | Uso |
|---|---|---|
| POST | `/api/auth/login` | Autenticar e obter token |
| GET | `/api/auth/me` | Consultar usuário autenticado |
| GET/POST | `/api/users` | Listar ou criar usuários (`ADMIN`) |
| POST | `/api/applications` | Cadastrar aplicação |
| GET | `/api/applications` | Listar aplicações |
| GET/PUT/DELETE | `/api/applications/{id}` | Consultar, atualizar ou excluir |
| POST | `/api/applications/{id}/check` | Executar verificação agora |
| GET | `/api/applications/{id}/events` | Consultar eventos recentes |
| POST | `/api/incidents` | Abrir incidente manual |
| GET | `/api/incidents?status=OPEN` | Listar/filtrar incidentes |
| GET | `/api/incidents/{id}` | Consultar incidente |
| PATCH | `/api/incidents/{id}/acknowledge` | Reconhecer incidente |
| PATCH | `/api/incidents/{id}/resolve` | Resolver incidente |
| GET | `/api/status` | Resumo do ambiente monitorado |

Exemplo:

```powershell
$body = @{ name="Minha API"; url="https://example.com"; environment="PRODUCTION"; monitoringEnabled=$true } | ConvertTo-Json
Invoke-RestMethod http://localhost:8080/api/applications -Method POST -ContentType application/json -Body $body
Invoke-RestMethod http://localhost:8080/api/applications/1/check -Method POST
```

## Configuração

| Variável | Padrão |
|---|---|
| `PORT` | `8080` |
| `DATABASE_URL` | H2 local |
| `DATABASE_USERNAME` | `sa` |
| `DATABASE_PASSWORD` | vazio |
| `DATABASE_POOL_MAX_SIZE` | `3` |
| `SPRING_LAZY_INITIALIZATION` | `false` (`true` no contêiner) |
| `SERVER_MAX_THREADS` | `20` |
| `MONITORING_INTERVAL_MS` | `60000` |
| `MONITORING_TIMEOUT_MS` | `5000` |
| `DEGRADED_THRESHOLD_MS` | `1500` |
| `JWT_SECRET` | segredo local de demonstração |
| `TOKEN_EXPIRATION_SECONDS` | `28800` |
| `INITIAL_ADMIN_NAME` | `Administrador` |
| `INITIAL_ADMIN_USERNAME` | `admin` |
| `INITIAL_ADMIN_PASSWORD` | `admin123` |

> Em produção, defina obrigatoriamente `JWT_SECRET` com um valor longo e aleatório e altere `INITIAL_ADMIN_PASSWORD` antes da primeira inicialização.

## Hospedagem com poucos recursos

O contêiner limita o uso de heap, metaspace, cache de código e pilhas de threads para operar com mais estabilidade em instâncias de 512 MB. Ele também ativa inicialização preguiçosa e usa o coletor Serial GC, priorizando um cold start menor em ambientes com pouca CPU. Os pools de conexões e de requisições são reduzidos para o perfil de demonstração; todos os limites podem ser ajustados pelas variáveis documentadas acima.

No plano gratuito do Render, o serviço entra em suspensão após um período sem acessos. Por isso, o primeiro acesso ainda pode levar alguns instantes, mesmo com as otimizações.

