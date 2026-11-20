# BitLife BR API

API online do BitLife BR. Não existe interação jogador-jogador: cada conta tem um save privado e cada sessão recebe personagens fictícios próprios.

## Requisitos
- Node.js 20+
- PostgreSQL
- `DATABASE_URL`
- `JWT_SECRET` (segredo aleatório forte; nunca commitar)

## Executar localmente
```bash
cd server
cp .env.example .env
# configure DATABASE_URL e JWT_SECRET no ambiente
npm install
npm start
```

## Rotas
- `GET /health`
- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/me`
- `POST /api/sessions` cria 12 NPCs fictícios exclusivos para aquela sessão
- `GET /api/sessions/:id` recupera os NPCs da sessão
- `GET /api/save`
- `PUT /api/save` grava com controle otimista de revisão
- `POST /api/choice` valida e aplica escolhas no servidor

Todas as rotas exceto health, cadastro e login exigem Bearer token. O servidor é a autoridade para as escolhas: o cliente não envia valores finais de atributos. Conflitos de revisão retornam HTTP 409 para impedir sobrescrita silenciosa de saves.

## Variáveis de ambiente
Veja `.env.example`. Configure as variáveis no painel do provedor, não no Git.

## Pacote de conteúdo para Android

A API publica o pacote apenas quando um ZIP real está presente em armazenamento persistente:
- `CONTENT_PACK_PATH`: caminho absoluto do ZIP no volume persistente do provedor.
- `CONTENT_PACK_VERSION`: versão mostrada dentro do aplicativo.
- `CONTENT_PACK_SHA256`: SHA-256 do ZIP (opcional; se omitido, a API calcula a assinatura ao consultar o manifesto).

Rotas públicas:
- `GET /api/content/manifest`: retorna versão, tamanho real em bytes, SHA-256 e URL do download.
- `GET /api/content/download`: transmite o ZIP.

O ZIP precisa conter `manifest.json` na raiz, com `version` igual a `CONTENT_PACK_VERSION` e `schemaVersion` maior ou igual a 1. O app só instala depois de comparar tamanho, SHA-256 e versão interna e extrair arquivos sem permitir caminhos fora da pasta do jogo.

**Não configure um tamanho fictício:** o manifesto usa o tamanho do arquivo realmente hospedado. Para publicar um pacote de 1 GiB, é necessário enviar os recursos reais para o volume persistente e configurar a assinatura correspondente.
