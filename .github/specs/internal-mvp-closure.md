# Fechamento do MVP Interno - ERP Loja de Roupas

## Status

Implementado como escopo operacional interno do MVP. A vitrine e o checkout publico nao fazem parte deste fechamento, exceto pelos endpoints publicos de catalogo ja existentes.

## Escopo

O MVP interno cobre autenticacao, RBAC, catalogo administrativo, imagens de produto, estoque, PDV, clientes, pricing, financeiro, eventos internos, observabilidade basica e testes criticos de regressao.

## Papeis

- `ROLE_MANAGER`: acesso administrativo amplo aos modulos internos.
- `ROLE_CASHIER`: operacao de PDV, consulta operacional de catalogo/clientes e calculo de descontos.
- `ROLE_STOCK`: operacao de estoque e consulta operacional de catalogo.
- `ROLE_FINANCE`: consulta e operacao financeira permitida.

O backend usa authorities JWT com prefixo completo `ROLE_*`. Controllers devem autorizar com `hasAuthority('ROLE_*')`; o frontend pode ocultar fluxos por papel, mas a autorizacao decisiva e sempre no backend.

## Interfaces

Endpoints base mantidos:

- `/api/v1/auth`
- `/api/v1/products`
- `/api/v1/products/{uuid}/images`
- `/api/v1/inventory`
- `/api/v1/sales`
- `/api/v1/customers`
- `/api/v1/pricing`
- `/api/v1/finance`

Endpoints publicos de catalogo continuam sem autenticacao quando ja expostos para consulta. Endpoints administrativos e operacionais exigem JWT e papel compativel.

## Fluxos MVP

### Login e Lockout

- Falhas de login incrementam contador persistente.
- Usuario bloqueado nao autentica ate a janela de lockout expirar ou ate intervencao administrativa futura.
- Login bem-sucedido reseta falhas e lockout.

### Catalogo e Imagens

- Manager cria, altera e desativa produtos e variantes.
- Manager gerencia imagens de produto.
- Cashier e stock podem consultar catalogo operacional quando necessario para PDV e estoque.

### Estoque

- Movimentacoes ajustam saldo fisico e registram historico.
- PDV reserva estoque por venda.
- Finalizacao confirma reservas e baixa estoque fisico.
- Cancelamento libera todas as reservas ativas da venda por `saleUuid`.

### Pricing

- Calculo de desconto usa o modulo de pricing via port compartilhada.
- Confirmacao de cupom usa controle concorrente no backend para impedir uso acima do limite.
- Impostos permanecem zero no MVP ate regra fiscal validada.

### PDV

- Caixa abre venda, adiciona item por codigo de barras, aplica desconto/cupom quando informado e finaliza pagamento.
- Finalizacao persiste o estado da venda, confirma estoque, confirma cupom e publica evento interno financeiro dentro do fluxo transacional.
- Cancelamento libera reservas e persiste o estado cancelado.

### Financeiro e Eventos Internos

- Venda finalizada gera lancamento financeiro de receita.
- Processamento de evento de venda finalizada e idempotente: duplicatas nao criam lancamentos financeiros duplicados.
- Eventos internos persistem identificador e payload para rastreabilidade basica.

### Frontend Operacional

- Admin de produtos consome adapters HTTP em vez de mocks locais.
- Terminal PDV consome adapters HTTP para abrir, adicionar item, finalizar e cancelar venda.
- Guards Angular continuam como UX e protecao de navegacao, sem substituir RBAC backend.

## Criterios de Aceite

- Controllers usam uma convencao unica de authority JWT: `hasAuthority('ROLE_*')`.
- Testes de lockout, reset de lockout, concorrencia de cupom, idempotencia de evento, fluxo completo de PDV e matriz RBAC estao habilitados.
- Finalizacao de venda com cupom/desconto real gera uma unica receita financeira mesmo com evento duplicado.
- Cancelamento de venda libera reservas ativas por `saleUuid`.
- Frontend operacional deixa de depender de dados estaticos para admin de produtos e PDV.

## Fora do Escopo

- Checkout publico e pedidos de e-commerce.
- Regras fiscais, contabeis ou legais definitivas.
- Reativacao administrativa de produto desativado, salvo endpoint futuro especifico.
- Edicao/remocao granular de item no carrinho PDV no frontend enquanto o contrato backend nao oferecer essa operacao.

## Verificacao Recomendada

- `./mvnw test`
- `./mvnw -pl infrastructure,bootstrap -am -Dtest=LockoutAfterFailuresIT,LockoutResetOnSuccessIT,CouponOptimisticLockIT,EventIdempotencyIT,FullPosFlowE2ETest,RbacEnforcementE2ETest -Dsurefire.failIfNoSpecifiedTests=false -DfailIfNoTests=false clean test`
- `npm run build` em `frontend/`
