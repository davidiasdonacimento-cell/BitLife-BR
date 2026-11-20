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
