# BitLife BR — Planejamento consolidado

Este documento consolida as decisões registradas nos planejamentos BitLife BR 0, 1, 2 e 3. As decisões marcadas como confirmadas são requisitos do produto. A presença de um item nesta lista não significa que ele já esteja implementado.

## 1. Direção do produto

- Simulador de vida no estilo BitLife: menus, cartões, textos, escolhas e histórico; não é um jogo 3D.
- Aplicativo Android nativo em Java 17, Android SDK e Gradle. Não usar Godot nem WebView.
- Idioma principal: português brasileiro.
- Conteúdo completo, entregue em módulos conectados e testados.
- Experiência online com universos isolados por conta. Não existe multiplayer nem interação entre jogadores reais.
- O servidor gera personagens fictícios para o universo privado do jogador.
- PostgreSQL na nuvem é a autoridade do save online; SQLite guarda progresso local e oferece recuperação durante falhas de conexão.
- Uma atualização do aplicativo não pode apagar saves. Migrações de banco devem ser aditivas, com controle de versão e recuperação de conflitos.

## 2. Decisões confirmadas

| Área | Decisão | Requisito consolidado |
|---|---|---|
| Estilo | BitLife | Menus, cartões, textos, escolhas e biografia. |
| Conectividade | Online isolado | Universo privado por jogador, sem interação entre jogadores. |
| Conteúdo | Completo | Todos os sistemas previstos, construídos por etapas. |
| NPCs | Avançados | Personalidade, relações, famílias, estudo, carreira, economia e história próprias. |
| Mundo offline | Ativo | O universo e os NPCs continuam evoluindo em segundo plano lógico. |
| Personagem principal offline | Eventos | O protagonista não envelhece sozinho; recebe notícias e eventos quando retorna. |
| Quantidade de vidas | Ilimitada | Várias vidas independentes por conta, respeitando limites técnicos de armazenamento. |
| Após a morte | Ambos | Continuar como descendente ou encerrar a história e começar outra vida. |
| Seleção de descendente | Qualquer idade | Escolher descendentes desde o nascimento até a velhice, mantendo idade e atributos atuais. |
| Descendentes | Avançados | Objetivos, personalidade, relações, carreira, família e acontecimentos próprios. |
| População | Dinâmica | Cresce ou diminui por nascimentos, falecimentos e migrações, com limites por universo. |
| Início | Três caminhos | Criar personagem; ser filho ou filha; escolher descendente existente. |
| Pais/responsáveis | Ambos | Geração automática ou personalização. |
| Irmãos | Personalizável | Jogador define a quantidade; cada irmão tem identidade e histórico próprios. |
| Aparência | Ambos | Geração automática ou editor personalizado para protagonistas e NPCs. |
| Vestuário | Ambos | Roupas automáticas, guarda-roupa, loja e personalização. |
| Moradia | Ambos | Moradia automática ou manual, aluguel, compra, venda, reformas e decoração. |
| Veículos | Ambos | Seleção automática ou manual, compra, venda, direção, manutenção e coleção. |
| Educação | Completa | Educação infantil, fundamental, médio, ENEM, vestibulares, cursos e universidades. |
| Carreiras | Completa | Profissões, concursos, entrevistas, empregos, promoções, demissões, empresas e aposentadoria. |
| Economia | Completa | Bancos, contas, cartões, investimentos, empréstimos, inflação, impostos, patrimônio, dívidas e herança. |
| Relacionamentos | Completos | Amizades, namoro, casamento, família, compatibilidade, conflitos, separações, reconciliações e filhos. |
| Saúde | Completa | Saúde física e mental, hospitais, consultas, tratamentos, prevenção, planos e envelhecimento. |
| Sistema jurídico | Pendente | Foi apresentada a escolha “completo”, mas ainda não houve confirmação do jogador. |

## 3. Início de uma vida

A tela inicial do produto contém:
- Nova vida.
- Continuar.
- Legado e vidas salvas.
- Conquistas.

O fluxo de nova vida oferece três opções:
1. Criar personagem.
2. Ser filho ou filha.
3. Escolher descendente existente.

A criação considera nome, sobrenome, data de nascimento, gênero, personalidade e localização no Brasil. Pais/responsáveis podem ser gerados ou personalizados. A quantidade de irmãos é escolhida pelo jogador.

## 4. Personagem, idade e escolhas

- Atributos previstos: saúde, felicidade, inteligência, aparência, carisma, disciplina e popularidade.
- As escolhas devem alterar dados persistidos, não apenas trocar textos.
- Cada evento tem identificador, condições, alternativas e resultados.
- O histórico da vida registra os acontecimentos relevantes.
- O protagonista só envelhece quando o jogador escolhe avançar o ano.
- As fases etárias de referência são infância (0–11), adolescência (12–17), adulta (18–59) e terceira idade (60+).

## 5. Família, legado e descendência

- Pais, responsáveis, irmãos, avós, amigos, parceiros, filhos e parentes.
- Árvore genealógica permanente, com vínculos e datas.
- Após a morte, salvar biografia, patrimônio, família e descendentes.
- A continuação por descendente preserva o mesmo universo, a idade atual do descendente e a história do antecessor.
- Uma nova vida independente cria um novo universo.
- Herança deve considerar testamento, beneficiários, bens, empresas e dívidas; não transferir tudo sem regras.

## 6. NPCs e universo isolado

NPCs importantes (família, amigos, parceiros, filhos e personagens acompanhados) terão histórico detalhado. Personagens comuns terão simulação mais leve, mas com identidade e características.

A simulação offline deve:
- Usar timestamps, checkpoints e operações idempotentes.
- Evitar envelhecimento duplicado e eventos repetidos ao reconectar.
- Processar NPCs por lotes e atualizações seletivas, sem criar um processo permanente para cada NPC.
- Gerar um resumo dos acontecimentos desde a última visita.
- Manter o estado do protagonista separado do relógio do mundo.
- Calcular receitas e despesas de propriedades e negócios do jogador com registros auditáveis.

A população do universo deve refletir nascimentos, mortes e migrações. Estatísticas devem vir de registros reais, nunca de números inventados para preencher a interface.

## 7. Mundo brasileiro

- 26 estados e Distrito Federal.
- Municípios e códigos IBGE.
- Migrações, viagens, escolas, universidades, empresas, hospitais, serviços e bairros.
- Eventos regionais, feriados, Carnaval, festas juninas e outras referências culturais.
- Valores monetários em reais, com economia simulada consistente por região.

## 8. Arquitetura e integridade

### Android
- Java 17, Android SDK e Gradle.
- Interface nativa; nenhuma WebView para a experiência principal.
- Módulos separados para personagem, família, eventos, economia, educação, carreira, saúde, moradia, veículos e legado.
- SQLite com migrações versionadas.

### Servidor
- Node.js e PostgreSQL.
- Cadastro, autenticação, autorização por conta, saves privados e validação de escolhas no servidor.
- Revisão otimista do save para impedir sobrescrita silenciosa entre dispositivos.
- Backups, recuperação e histórico de migrações.
- O cliente nunca define sozinho o resultado final de uma escolha online.

### Regras de qualidade
- Não declarar um recurso pronto sem implementação e teste.
- Não criar botões sem ação real.
- Não preencher conteúdo com NPCs vazios ou eventos repetidos para aparentar volume.
- Não excluir saves ou substituir a branch principal durante o desenvolvimento.
- Cada etapa deve ser compilada e testada antes de ser considerada concluída.

## 9. Situação da primeira implementação

### Entregue nesta etapa
- Fluxo nativo inicial com Nova vida, Continuar, Legado e Conquistas.
- Três opções de início: criar personagem, ser filho/filha e selecionar descendente salvo.
- Formulário de personagem com nome, sobrenome, gênero, personalidade, data de nascimento, município e UF.
- Geração ou personalização dos dois responsáveis e quantidade escolhida de irmãos.
- Modelo de vida com atributos, dados familiares, vínculo de descendência e histórico.
- Eventos anuais por faixa etária, com duas opções e efeitos diferentes nos atributos.
- Histórico local das escolhas.
- Eventos anuais com textos e identificadores específicos por idade; evento antigo não pode ser aplicado de novo.
- Avanço anual salvo em transação SQLite e validado contra a vida/idade ativa.
- Central de Conteúdo nativa: download por bytes reais, retomada pelo DownloadManager, verificação SHA-256 e extração ZIP com validação de caminho.
- API de conteúdo com manifesto e download a partir de um arquivo publicado no armazenamento persistente do servidor.
- Banco preparado para múltiplas vidas e migração aditiva do save antigo.

### Ainda não entregue
- Arquivo ZIP real de recursos publicado no servidor; por isso o endpoint de manifesto responde indisponível até CONTENT_PACK_PATH estar configurado.
- Pacote de 1 GiB composto por artes, sons, textos, catálogos e outros recursos reais; não criar arquivos vazios/repetidos para inflar tamanho.
- URL de produção da API configurada no APK.
- Autenticação online integrada à interface Android.
- Sincronização online automática e resolução de conflitos na interface.
- Motor avançado de NPCs e simulação offline do universo.
- Nascimento de filhos dentro da simulação, morte, sucessão e herança completa.
- Editor visual de aparência e roupas; loja e inventário completos.
- Moradia, veículos, educação, carreira, economia, relações e saúde completos.
- População brasileira dinâmica, catálogo de municípios e eventos regionais em escala.
- Sistema jurídico, cuja decisão continua pendente.

## 10. Ordem de implementação

1. Núcleo de vida, criação, múltiplos saves, família inicial, eventos e histórico.
2. Cadastro/login, save privado online, sincronização local/nuvem e conflitos de revisão.
3. Família completa, relações, nascimento de descendentes, morte e sucessão.
4. Escola, educação, profissões, carreira, economia e patrimônio.
5. Saúde, moradia, veículos, vestuário e aparência.
6. NPCs avançados, relógio de universo, simulação offline e população dinâmica.
7. Mundo brasileiro, eventos regionais, estatísticas, conquistas e revisão final de qualidade.

A ordem pode ser ajustada quando houver dependências técnicas, mas os requisitos confirmados não devem ser descartados.
