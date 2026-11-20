# BitLife BR 🇧🇷

Simulador de vida ambientado no Brasil, desenvolvido como aplicativo Android nativo em Java 17.

## Direção oficial do projeto

- **Android nativo:** Java 17 + Android SDK + Gradle; sem Godot e sem WebView.
- **Modelo online:** conta individual, salvamento na nuvem e escolhas validadas pelo servidor.
- **Sem multiplayer:** jogadores não se encontram nem interagem entre si.
- **NPCs:** personagens fictícios gerados pelo servidor para cada sessão e persistidos durante aquela sessão.
- **Persistência:** PostgreSQL na nuvem e SQLite local como proteção/cache; atualizações não devem apagar saves.
- **Segurança:** senha com hash, token de sessão, validação no backend e controle de revisão para evitar sobrescrita de progresso.
- **Idiomas:** português brasileiro e inglês.
- **Build Android:** GitHub Actions.

## Sistemas planejados para o jogo

Personagem e atributos; família, parentes e gerações; amizades, namoro, casamento e filhos; escola, notas, ENEM, faculdade e cursos técnicos; profissões, empresas, promoções e aposentadoria; dinheiro, despesas, impostos simulados e herança; imóveis e veículos; saúde e atividades; todos os estados brasileiros, DF e municípios; eventos regionais e milhares de acontecimentos com escolhas, variações e consequências; fama, redes sociais fictícias, pets, conquistas e múltiplos saves.

Os sistemas serão implementados em módulos e conectados entre si. A lista acima é o escopo planejado, não uma afirmação de que todos já estão prontos.

## Estrutura atual

- `app/`: aplicativo Android.
- `server/`: API Node.js + PostgreSQL para contas, saves na nuvem, sessões e escolhas validadas.
- `.github/workflows/`: testes e compilação automatizada.

## API online

Consulte [server/README.md](server/README.md). Configure `DATABASE_URL` e `JWT_SECRET` no provedor de hospedagem. Nunca coloque segredos no Git.

Rotas principais: cadastro/login, perfil, criação e recuperação de sessão de NPCs, leitura e gravação de save com revisão, e envio de escolhas para validação no servidor.

## Compilar Android

Requisitos: JDK 17, Android SDK 35 e Gradle 8.9.

```bash
gradle testDebugUnitTest
gradle assembleDebug
```

O endereço HTTPS da API será configurado em `API_BASE_URL` no build Android depois que a hospedagem estiver publicada. Até essa configuração e a integração da tela de conta/sincronização, o aplicativo não deve ser considerado online em produção.

## Princípios de engenharia

- Não declarar recursos concluídos sem implementação e teste.
- Não sobrescrever saves desatualizados: conflitos precisam ser sincronizados.
- Manter cache local para não perder alterações quando a conexão cair.
- Fazer migrações aditivas no banco e preservar dados entre versões.
- Não criar interação jogador-jogador.
- Não usar eventos ou NPCs vazios/repetidos para fingir conteúdo.
