# BarrigaRest

Projeto de testes REST em Java usando Rest-Assured.

Resumo
------
Conjunto de testes automatizados para a API "BarrigaRest" (endpoint público em `http://barrigarest.wcaquino.me`). O projeto usa Maven e JUnit (4) e demonstra chamadas de API, validações de respostas e uso de modelos/úteis para manipular dados de teste.

Principais tecnologias
----------------------
- Java 21
- Maven
- Rest-Assured (5.5.5)
- JUnit 4.13.2
- Gson (2.11.0)

Estrutura do projeto
--------------------
(src/main/java)
- org.example.Main                 - classe gerada pelo template do projeto
- rest.core.BaseTest               - configuração global do Rest-Assured (baseURI, port, headers, timeouts)
- rest.core.Constantes             - constantes de ambiente (URL, porta, content-type, timeout)
- rest.tests.BarrigaTest           - suíte principal de testes (login, criação/alteração/exclusão de contas e transações)
- rest.tests.Movimentacao          - modelo (POJO) usado nas requisições de transações
- rest.utils.DataUtils             - utilitário para formatação/manipulação de datas

Rodando na IDE
-------------
- Abra o projeto na sua IDE (IntelliJ IDEA, Eclipse, etc.) como um projeto Maven.
- Importe as dependências (Maven).</n- Execute as classes de teste (`Right click -> Run`) ou configure uma execução JUnit.

O que os testes cobrem (resumo)
-------------------------------
- Autenticação (obtenção de token JWT)
- Criação de contas
- Validação de regra: não duplicar contas com o mesmo nome
- Inserção de movimentações (transações)
- Validações de campos obrigatórios e formato de dados
- Validações de regras de negócio (ex.: não permitir data de movimentação futura)
- Cálculo de saldo e remoção de movimentações
- Verificação de acesso sem token

Boas práticas e pontos de atenção
--------------------------------
- Os testes dependem da disponibilidade do serviço remoto. Se o serviço estiver offline, os testes falharão.
- Timeout de resposta está configurado em `Constantes.MAX_TIMEOUT` (atualmente 5000ms). Ajuste se necessário.
- Para isolar os testes do ambiente externo, considere usar um stub/local mock da API durante o desenvolvimento.

Dicas de debug
--------------
- O `BaseTest` já ativa logging de requisição/resposta quando a validação falha: isso ajuda a inspecionar payloads e respostas.
- Para inspecionar apenas um teste localmente, execute a classe de teste isoladamente na IDE.
