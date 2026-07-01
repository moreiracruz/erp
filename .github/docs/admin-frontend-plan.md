# Plano do Frontend Administrativo

## Objetivo

Criar uma area administrativa navegavel para o ERP, separada da vitrine publica, para que super admin, gerente e perfis operacionais encontrem rapidamente os fluxos internos ja implementados.

## Estado Atual

O frontend ja possui telas operacionais, mas elas estavam acessiveis principalmente por URL direta:

- `/dashboard`
- `/admin/products`
- `/admin/system`
- `/pos`
- `/inventory`
- `/consignments`

O header principal continua sendo da loja publica. Por isso, apos login administrativo, o usuario nao tinha uma navegacao clara de backoffice.

## Escopo Desta Entrega

- Criar um shell administrativo em `/admin`.
- Exibir menu lateral com links para:
  - Dashboard
  - Produtos
  - Usuarios
  - Estoque
  - PDV
  - Consignacao
  - Loja
- Redirecionar `/admin` para `/admin/dashboard`.
- Manter `/dashboard` legado funcionando.
- Reutilizar as telas ja existentes em vez de duplicar funcionalidades.
- Mostrar informacoes do usuario autenticado e acao de sair.
- Respeitar a protecao existente de rotas e RBAC backend.
- Remover dados ficticios do dashboard administrativo inicial, deixando estado vazio/zero ate existir API agregada real.

## Rotas Planejadas

| Rota | Tela | Perfil esperado |
| --- | --- | --- |
| `/admin/dashboard` | Visao geral | `ROLE_SUPER_ADMIN`, `ROLE_MANAGER` |
| `/admin/products` | Administracao de produtos | `ROLE_SUPER_ADMIN`, `ROLE_MANAGER` |
| `/admin/system` | Usuarios do sistema | `ROLE_SUPER_ADMIN`, `ROLE_MANAGER` |
| `/inventory` | Estoque | `ROLE_SUPER_ADMIN`, `ROLE_MANAGER`, `ROLE_STOCK` |
| `/pos` | PDV | `ROLE_SUPER_ADMIN`, `ROLE_MANAGER`, `ROLE_CASHIER` |
| `/consignments` | Consignacao | `ROLE_SUPER_ADMIN`, `ROLE_MANAGER`, `ROLE_STOCK` |

## Fora do Escopo Desta Entrega

- API agregada de metricas para dashboard.
- Relatorios financeiros completos.
- Permissoes granulares por item de menu no backend.
- Redesenho completo das telas operacionais existentes.
- CRUD completo de estoque dentro do shell administrativo.

## Evolucao Recomendada

1. Criar endpoints de dashboard no backend para KPIs reais.
2. Trocar os sinais locais do dashboard por um adapter HTTP.
3. Criar um layout operacional tambem para perfis `ROLE_CASHIER`, `ROLE_STOCK` e `ROLE_FINANCE` quando eles nao forem managers.
4. Adicionar testes E2E de navegacao por perfil.
5. Consolidar breadcrumbs e estados vazios em componentes compartilhados.

## Validacao

Comandos esperados:

```bash
cd frontend
npm run build
npm run test
```
