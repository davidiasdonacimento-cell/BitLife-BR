# BitLife BR 🇧🇷

Simulador de vida ambientado no Brasil, desenvolvido como aplicativo Android nativo.

## Definições do projeto

- **Plataforma:** Android nativo
- **Linguagem:** Java 17
- **Interface:** Android Views, estilo minimalista
- **Banco local:** SQLite
- **Conectividade:** totalmente offline
- **Idiomas:** português brasileiro e inglês (seleção conforme idioma do aparelho)
- **Build:** Gradle + GitHub Actions

## Primeira fundação

A fundação inicial inclui:
- Criação de personagem local.
- Idade e passagem de ano.
- Atributos básicos.
- Persistência local em SQLite.
- Interface em português e inglês.
- Testes unitários e compilação automatizada.

## Compilar

Requisitos: JDK 17, Android SDK 35 e Gradle 8.9.

```bash
gradle testDebugUnitTest
gradle assembleDebug
```

APK de depuração:
`app/build/outputs/apk/debug/app-debug.apk`

## Princípios de engenharia

- Não declarar recursos concluídos sem implementação e teste.
- Preservar salvamentos entre atualizações.
- Alterações de banco devem usar migrações aditivas.
- Manter sistemas separados por domínio.
- O jogo principal não deve depender de rede.

## Roadmap

1. Fundação Android, personagem, passagem de ano e salvamento.
2. Família e relacionamentos.
3. Educação e carreira.
4. Finanças, imóveis e veículos.
5. Municípios brasileiros e eventos regionais.
6. Conteúdo narrativo, conquistas e gerações.
