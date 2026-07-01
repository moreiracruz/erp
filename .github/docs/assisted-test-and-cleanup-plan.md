# Plano de Limpeza e Teste Assistido

Este roteiro orienta a estabilizacao da base e a validacao assistida do MVP interno do ERP. Ele complementa `.github/specs/internal-mvp-closure.md` e deve ser usado como checklist operacional antes de fechar uma entrega.

## Objetivos

- Separar mudancas intencionais de lixo local, arquivos gerados e experimentos.
- Validar que a arquitetura modular continua respeitando dependencias para dentro.
- Confirmar que os fluxos criticos do MVP funcionam por testes automatizados e teste assistido.
- Registrar evidencias objetivas para cada funcionalidade validada.
- Transformar falhas encontradas em correcoes rastreaveis, sem remover testes apenas para passar build.

## Pre-requisitos

- Java 21 disponivel para Maven.
- Docker disponivel quando testes usam PostgreSQL/Testcontainers.
- Node/npm compativel com `frontend/package.json`.
- Variaveis sensiveis fora do codigo-fonte.
- Banco local ou containers limpos quando a validacao exigir estado previsivel.

## Fase 1: Baseline da Base

1. Conferir a arvore de trabalho:

   ```bash
   git status --short
   ```

2. Classificar cada mudanca local:

   | Item | Acao | Observacao |
   | --- | --- | --- |
   | Mudanca intencional | Manter | Validar com teste ou revisao |
   | Arquivo gerado | Remover ou ignorar | Nunca apagar sem confirmar origem |
   | Experimento | Isolar | Mover para branch/commit proprio quando necessario |
   | Divergencia de docs | Atualizar docs ou codigo | Preferir comportamento implementado como fonte |

3. Rodar verificacoes iniciais:

   ```bash
   git diff --check
   ./mvnw -pl infrastructure -am -Dtest=ModuleBoundaryTest -Dsurefire.failIfNoSpecifiedTests=false -DfailIfNoTests=false test
   ./mvnw test
   ```

4. Rodar frontend:

   ```bash
   cd frontend
   npm run build
   npm run test
   ```

## Fase 2: Limpeza Guiada

Validar os pontos abaixo antes de mudar codigo:

- Domain nao depende de Spring, JPA, HTTP, banco ou adapters.
- Application contem casos de uso, portas, comandos e queries.
- Infrastructure concentra persistencia, seguranca, mensageria, storage e clientes externos.
- Controllers fazem validacao/mapeamento e delegam para casos de uso.
- Regras de negocio, autorizacao e validacoes criticas estao no backend.
- Endpoints administrativos usam authorities completas `ROLE_*`.
- Documentacao em `.github/docs/` e `.github/specs/` descreve comportamento implementado.
- Testes falhando foram classificados como bug real, ambiente, teste desatualizado ou baixo valor.

## Fase 3: Suite Critica Automatizada

Executar a suite critica do MVP:

```bash
./mvnw -pl infrastructure,bootstrap -am -Dtest=LockoutAfterFailuresIT,LockoutResetOnSuccessIT,CouponOptimisticLockIT,EventIdempotencyIT,FullPosFlowE2ETest,RbacEnforcementE2ETest -Dsurefire.failIfNoSpecifiedTests=false -DfailIfNoTests=false clean test
```

Quando houver mudancas nas areas abaixo, incluir testes direcionados:

| Area | Testes sugeridos |
| --- | --- |
| Auth/JWT/RBAC | `JwtSecurityE2ETest`, `RbacEnforcementE2ETest`, `SystemUserAdminIT`, `PublicRegistrationIT`, `RefreshTokenRotationIT` |
| Estoque | `ConcurrentStockReservationIT`, `StockInvariantSequenceIT`, propriedades do modulo `inventory` |
| Pricing | `CouponOptimisticLockIT`, propriedades do modulo `pricing` |
| PDV | `FullPosFlowE2ETest`, propriedades do modulo `sales` |
| Financeiro/eventos | `EventIdempotencyIT`, propriedades do modulo `finance` |
| Catalogo/imagens | testes de `modules/product` e endpoints publicos do catalogo |
| Frontend | `npm run build`, `npm run test` |

## Fase 4: Teste Assistido

Registrar a execucao nesta matriz:

| Status | Area | Cenario | Perfil | Evidencia | Observacao |
| --- | --- | --- | --- | --- | --- |
| Pendente | Auth | Registrar cliente publico | Anonimo |  |  |
| Pendente | Auth | Login valido retorna tokens | Usuario valido |  |  |
| Pendente | Auth | Login invalido aciona lockout | Anonimo |  |  |
| Pendente | Auth | Refresh token rotaciona credenciais | Usuario autenticado |  |  |
| Pendente | Auth | Usuario inativo nao autentica | Usuario inativo |  |  |
| Pendente | RBAC | `ROLE_USER` nao acessa admin/operacional | Cliente |  |  |
| Pendente | RBAC | `ROLE_MANAGER` acessa administracao | Manager |  |  |
| Pendente | RBAC | `ROLE_CASHIER` acessa PDV | Cashier |  |  |
| Pendente | RBAC | `ROLE_STOCK` acessa estoque | Stock |  |  |
| Pendente | RBAC | `ROLE_FINANCE` acessa financeiro | Finance |  |  |
| Pendente | Sistema | Manager cria usuario interno | Manager |  |  |
| Pendente | Sistema | Manager altera perfil | Manager |  |  |
| Pendente | Sistema | Manager redefine senha | Manager |  |  |
| Pendente | Sistema | Manager ativa/desativa conta | Manager |  |  |
| Pendente | Sistema | Manager limpa lockout | Manager |  |  |
| Pendente | Sistema | Respostas nao expoem hash de senha | Manager |  |  |
| Pendente | Catalogo | Criar produto | Manager |  |  |
| Pendente | Catalogo | Criar variante | Manager |  |  |
| Pendente | Catalogo | Consultar catalogo publico | Anonimo |  |  |
| Pendente | Catalogo | Alterar produto | Manager |  |  |
| Pendente | Catalogo | Desativar produto | Manager |  |  |
| Pendente | Imagens | Upload de imagem | Manager |  |  |
| Pendente | Imagens | Listar imagens publicas | Anonimo |  |  |
| Pendente | Imagens | Definir imagem principal | Manager |  |  |
| Pendente | Imagens | Remover imagem | Manager |  |  |
| Pendente | Estoque | Registrar entrada | Stock |  |  |
| Pendente | Estoque | Registrar saida valida | Stock |  |  |
| Pendente | Estoque | Bloquear saida negativa | Stock |  |  |
| Pendente | Estoque | Reservar estoque por venda | Cashier |  |  |
| Pendente | Estoque | Liberar reserva por cancelamento | Cashier |  |  |
| Pendente | Pricing | Calcular desconto | Cashier |  |  |
| Pendente | Pricing | Aplicar cupom | Cashier |  |  |
| Pendente | Pricing | Impedir uso acima do limite | Cashier |  |  |
| Pendente | PDV | Abrir venda | Cashier |  |  |
| Pendente | PDV | Adicionar item por codigo de barras | Cashier |  |  |
| Pendente | PDV | Finalizar pagamento | Cashier |  |  |
| Pendente | PDV | Cancelar venda | Cashier |  |  |
| Pendente | Financeiro | Venda finalizada gera receita | Finance |  |  |
| Pendente | Eventos | Evento duplicado nao duplica receita | Sistema |  |  |
| Pendente | Frontend | Login/cadastro nao recebem Authorization indevido | Anonimo |  |  |
| Pendente | Frontend | Guards ocultam rotas por papel | Todos |  |  |
| Pendente | Frontend | Admin produtos usa API real | Manager |  |  |
| Pendente | Frontend | Admin sistema usa API real | Manager |  |  |
| Pendente | Frontend | PDV usa API real | Cashier |  |  |
| Pendente | Frontend | Telas exibem loading, erro, vazio e sucesso | Todos |  |  |

## Fase 5: Registro de Falhas

Para cada falha, registrar:

- Cenario afetado.
- Perfil usado.
- Passos minimos para reproduzir.
- Resultado esperado.
- Resultado obtido.
- Evidencia: teste, log, request/response, screenshot ou erro de build.
- Classificacao: bug, ambiente, contrato divergente, documentacao ou melhoria.
- Teste de regressao necessario.

## Criterios de Saida

- `git diff --check` sem erro.
- Teste de fronteira modular passando.
- Suite critica do MVP passando ou com falhas classificadas e documentadas.
- Frontend compila.
- Teste assistido tem evidencia para todos os cenarios criticos.
- Falhas bloqueantes foram corrigidas ou explicitamente removidas do escopo com justificativa.

## Execucao Inicial: 2026-07-01

Baseline realizado em ambiente local com Java 21, Docker Compose disponivel e npm 10.8.2.

| Comando | Resultado |
| --- | --- |
| `git diff --check` | Passou |
| `./mvnw -pl infrastructure -am -Dtest=ModuleBoundaryTest -Dsurefire.failIfNoSpecifiedTests=false -DfailIfNoTests=false test` | Passou: 9 testes |
| `./mvnw -pl infrastructure,bootstrap -am -Dtest=LockoutAfterFailuresIT,LockoutResetOnSuccessIT,CouponOptimisticLockIT,EventIdempotencyIT,FullPosFlowE2ETest,RbacEnforcementE2ETest -Dsurefire.failIfNoSpecifiedTests=false -DfailIfNoTests=false clean test` | Passou: 77 testes |
| `npm run build` em `frontend/` | Passou |
| `npm run test` em `frontend/` | Passou: 37 arquivos, 288 testes |
| `./mvnw test` | Passou apos estabilizar testes do modulo `product` |

Limpeza tecnica aplicada:

- Surefire e Failsafe agora executam testes com `java.awt.headless=true`, preservando o `argLine` do JaCoCo.
- Testes do modulo `product` usam mock maker `subclass` do Mockito para evitar dependencia de attach da JVM em testes que mockam portas/interfaces.
