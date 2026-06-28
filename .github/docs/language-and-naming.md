# Política de Idioma e Nomenclatura

## Idioma Padrão

A documentação do projeto para comportamento de negócio, requirements, design e tasks deve ser escrita em português brasileiro.

Nomes técnicos consolidados podem permanecer em inglês quando identificarem tecnologias ou protocolos concretos, por exemplo: `JWT`, `OpenAPI`, `Swagger`, `endpoint`, `adapter`, `frontend`, `backend`, `Docker` e `CI`.

## Termos de Negócio

Prefira português para conceitos de negócio no código de domínio e na documentação:

- Produto, variante, estoque, venda, cliente, financeiro, campanha, cupom
- consignação recebida, consignação enviada, acerto de consignação
- método de pagamento, tipo de campanha, tipo de alvo

Quando um elemento de código já fizer parte de API pública ou schema persistido, renomeie apenas em uma tarefa dedicada de migração/refatoração.

## Código e Banco de Dados

- Packages Java permanecem em minúsculas e seguem o prefixo `br.com.moreiracruz.erp.*`.
- Classes e métodos Java devem usar um único idioma de forma consistente dentro do mesmo bounded context.
- Tabelas e colunas de banco devem usar `snake_case`.
- Novos objetos de banco devem preferir termos de negócio em português, exceto tabelas de infraestrutura como `domain_events` ou tabelas de autenticação/tokens já estabelecidas por convenção.

## Regra de Migração

Não misture limpeza de idioma com mudanças comportamentais, exceto quando a alteração for pequena e coberta por testes. Renomeações grandes devem ser isoladas para que migrations de banco, compatibilidade de API e contratos de frontend possam ser revisados com segurança.
