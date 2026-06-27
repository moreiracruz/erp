# Exemplo — ERP Loja de Roupas

Entrada:

```markdown
Quero um ERP para loja de roupas com produto, estoque, vendas, clientes,
financeiro, autenticação e relatórios.
```

Saída esperada:

- Monólito Modular.
- Arquitetura Hexagonal por módulo.
- Módulos: auth, product, inventory, sales, customer, finance, reporting.
- RBAC.
- Lock transacional no estoque.
- Testes de fachada para venda.
- Métricas de vendas, estoque e erros.
- Sem microsserviços inicialmente.
