# ADR 0002: Independência da Camada de Domínio

## Status

Aceita

## Contexto

O ERP é um monólito modular organizado com Clean Architecture / Arquitetura Hexagonal. O código de domínio deve expressar regras de negócio sem depender de frameworks técnicos como Spring MVC, Spring Data, JPA, HTTP ou APIs de mapeamento de banco de dados.

Algumas classes iniciais de domínio carregavam anotações JPA ou tipos de paginação do Spring Data. Isso tornava preocupações de persistência visíveis dentro do modelo e enfraquecia a fronteira entre domínio/aplicação e adapters.

## Decisão

Pacotes de domínio (`br.com.moreiracruz.erp.modules..domain..`) e o shared kernel não devem depender de APIs Spring ou JPA.

Metadados de persistência pertencem a entidades JPA em pacotes de adapter ou infraestrutura. Casos de uso e ports de domínio devem usar comandos, consultas, respostas e tipos de paginação neutros em relação a frameworks.

Essa regra é protegida com ArchUnit em `ModuleBoundaryTest`.

## Consequências

- Objetos de domínio ficam mais fáceis de testar sem Spring ou banco de dados.
- Adapters de persistência devem mapear explicitamente entre objetos de domínio e entidades JPA.
- Controllers ainda podem expor tipos específicos do Spring, mas apenas na fronteira do adapter web.
- Novos módulos devem adicionar dependências técnicas apenas em adapters, infraestrutura ou bootstrap.
