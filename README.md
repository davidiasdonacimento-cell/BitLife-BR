# BitLife BR 🇧🇷

Simulador de vida ambientado no Brasil, desenvolvido como aplicativo Android nativo em Java 17.

## Direção oficial

- Android nativo: Java 17 + Android SDK + Gradle; sem Godot e sem WebView.
- Experiência online planejada: conta individual, universos isolados e saves privados.
- Sem multiplayer ou interação jogador-jogador.
- Backend Node.js + PostgreSQL; SQLite local para persistência e recuperação.
- Português brasileiro como idioma principal.
- Não declarar recursos concluídos sem implementação e teste.

## Primeira etapa jogável

Na branch `feature/android-foundation-v1`, a primeira etapa inclui:
- Tela inicial com Nova vida, Continuar, Legado e Conquistas.
- Fluxo para criar personagem, iniciar como filho/filha ou selecionar um descendente salvo.
- Formulário com identidade, nascimento, gênero, personalidade e localização.
- Geração ou personalização dos responsáveis e escolha da quantidade de irmãos.
- Múltiplas vidas salvas localmente, com migração aditiva do save antigo.
- Eventos anuais com duas escolhas, acontecimentos distintos por idade, efeitos nos atributos e bloqueio de avanço duplicado.
- Central de Conteúdo Android com consulta ao manifesto, download real, barra baseada em bytes recebidos, verificação SHA-256 e extração segura do ZIP.

## Planejamento completo

Consulte [docs/PLANEJAMENTO_COMPLETO.md](docs/PLANEJAMENTO_COMPLETO.md) para todas as decisões confirmadas, a arquitetura e os módulos ainda pendentes.

## API online

Consulte [server/README.md](server/README.md). Configure `DATABASE_URL` e `JWT_SECRET` no provedor de hospedagem. Nunca coloque segredos no Git.

A API já possui uma base de autenticação, sessões isoladas, save com revisão, validação inicial de escolhas e rotas de manifesto/download do pacote Android. O manifesto só fica disponível quando um ZIP real está publicado no servidor. O instalador não cria nem simula um pacote de 1 GB. O cliente Android ainda precisa de URL de produção, tela de conta e fluxo de sincronização antes que o jogo possa ser considerado online em produção.

## Compilar e testar

Requisitos: JDK 17, Android SDK 35 e Gradle 8.9.

```bash
gradle testDebugUnitTest
gradle assembleDebug

Para ligar a Central de Conteúdo a uma API publicada, compile com `-PBITLIFE_API_BASE_URL=https://seu-dominio-da-api`.
```

O workflow `.github/workflows/android-build.yml` executa testes do servidor, testes unitários Android e compilação do APK.

## Integridade dos dados

- Migrações aditivas; não apagar saves existentes.
- Não sobrescrever saves desatualizados sem resolução de conflitos.
- Manter o histórico de decisões.
- Implementar NPCs e eventos com dados e regras reais, não placeholders apresentados como recursos prontos.
