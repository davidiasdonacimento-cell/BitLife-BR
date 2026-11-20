import { Router } from "express";
import bcrypt from "bcryptjs";
import { z } from "zod";
import { pool } from "./db.js";
import { auth, issueToken, newId } from "./security.js";
import { createSessionNpcs } from "./npcs.js";
export const router = Router();
const credentials = z.object({ email: z.string().email().max(254), password: z.string().min(8).max(72) });
const initialState = () => ({ character: null, year: 0, money: 0, inventory: [], family: [], history: [], lastEventId: null });
router.post("/auth/register", async (req,res,next) => {
  try {
    const parsed = credentials.safeParse(req.body);
    if (!parsed.success) return res.status(400).json({ error: "Informe e-mail válido e senha com pelo menos 8 caracteres." });
    const email = parsed.data.email.trim().toLowerCase();
    const hash = await bcrypt.hash(parsed.data.password, 12);
    const id = newId();
    const client = await pool.connect();
    try {
      await client.query("BEGIN");
      const p = await client.query("INSERT INTO players(id,email,password_hash) VALUES($1,$2,$3) RETURNING id,email", [id,email,hash]);
      await client.query("INSERT INTO game_saves(player_id,state) VALUES($1,$2::jsonb)", [id,JSON.stringify(initialState())]);
      await client.query("COMMIT");
      res.status(201).json({ token: issueToken(p.rows[0]), player: p.rows[0], save: initialState() });
    } catch(e) { await client.query("ROLLBACK"); if(e.code==="23505") return res.status(409).json({error:"Este e-mail já possui conta."}); throw e; }
    finally { client.release(); }
  } catch(e) { next(e); }
});
router.post("/auth/login", async (req,res,next) => {
  try {
    const parsed = credentials.safeParse(req.body);
    if (!parsed.success) return res.status(400).json({error:"Credenciais inválidas."});
    const result = await pool.query("SELECT id,email,password_hash FROM players WHERE email=$1", [parsed.data.email.trim().toLowerCase()]);
    const p = result.rows[0];
    if (!p || !(await bcrypt.compare(parsed.data.password,p.password_hash))) return res.status(401).json({error:"E-mail ou senha incorretos."});
    const save = await pool.query("SELECT revision,state,updated_at FROM game_saves WHERE player_id=$1", [p.id]);
    res.json({token:issueToken(p),player:{id:p.id,email:p.email},save:save.rows[0] || null});
  } catch(e) { next(e); }
});
router.get("/me", auth, async (req,res,next) => {
  try { res.json({player:req.player}); } catch(e) { next(e); }
});
router.post("/sessions", auth, async (req,res,next) => {
  try {
    const id = newId(), npcs = createSessionNpcs();
    await pool.query("INSERT INTO game_sessions(id,player_id,npcs) VALUES($1,$2,$3::jsonb)",[id,req.player.id,JSON.stringify(npcs)]);
    res.status(201).json({sessionId:id,characters:npcs});
  } catch(e) { next(e); }
});
router.get("/sessions/:id", auth, async (req,res,next) => {
  try {
    const r=await pool.query("UPDATE game_sessions SET last_seen_at=NOW() WHERE id=$1 AND player_id=$2 RETURNING id,npcs",[req.params.id,req.player.id]);
    if(!r.rowCount) return res.status(404).json({error:"Sessão não encontrada."});
    res.json({sessionId:r.rows[0].id,characters:r.rows[0].npcs});
  } catch(e) { next(e); }
});
router.get("/save", auth, async (req,res,next) => {
  try {
    const r=await pool.query("SELECT revision,state,updated_at FROM game_saves WHERE player_id=$1",[req.player.id]);
    if(!r.rowCount) return res.status(404).json({error:"Save não encontrado."});
    res.json(r.rows[0]);
  } catch(e) { next(e); }
});
router.put("/save", auth, async (req,res,next) => {
  const schema=z.object({ expectedRevision:z.number().int().nonnegative(), state:z.object({}).passthrough() });
  const parsed=schema.safeParse(req.body);
  if(!parsed.success) return res.status(400).json({error:"Formato de salvamento inválido."});
  try {
    const r=await pool.query(
      "UPDATE game_saves SET state=$1::jsonb,revision=revision+1,updated_at=NOW() WHERE player_id=$2 AND revision=$3 RETURNING revision,state,updated_at",
      [JSON.stringify(parsed.data.state),req.player.id,parsed.data.expectedRevision]);
    if(!r.rowCount) {
      const current=await pool.query("SELECT revision,state FROM game_saves WHERE player_id=$1",[req.player.id]);
      return res.status(409).json({error:"O save mudou em outro dispositivo. Sincronize antes de tentar novamente.",current:current.rows[0]||null});
    }
    res.json(r.rows[0]);
  } catch(e) { next(e); }
});
const allowedChoices = {
  advance_year: { label:"Passar um ano", apply:s=>({...s,year:Math.min(120,(s.year||0)+1)}) },
  study: { label:"Estudar", apply:s=>({...s,character:{...(s.character||{}),intelligence:Math.min(100,((s.character||{}).intelligence||50)+3)}}) },
  rest: { label:"Descansar", apply:s=>({...s,character:{...(s.character||{}),health:Math.min(100,((s.character||{}).health||80)+2),happiness:Math.min(100,((s.character||{}).happiness||70)+2)}}) }
};
router.post("/choice", auth, async (req,res,next) => {
  const body=z.object({choiceId:z.enum(["advance_year","study","rest"]),expectedRevision:z.number().int().nonnegative()}).safeParse(req.body);
  if(!body.success) return res.status(400).json({error:"Escolha inválida."});
  const client=await pool.connect();
  try {
    await client.query("BEGIN");
    const r=await client.query("SELECT revision,state FROM game_saves WHERE player_id=$1 FOR UPDATE",[req.player.id]);
    if(!r.rowCount) { await client.query("ROLLBACK"); return res.status(404).json({error:"Save não encontrado."}); }
    if(Number(r.rows[0].revision)!==body.data.expectedRevision) { await client.query("ROLLBACK"); return res.status(409).json({error:"Save desatualizado. Sincronize antes de escolher.",current:r.rows[0]}); }
    const choice=allowedChoices[body.data.choiceId], state=choice.apply(r.rows[0].state);
    state.history=[...(state.history||[]),{choiceId:body.data.choiceId,at:new Date().toISOString()}].slice(-500);
    const saved=await client.query("UPDATE game_saves SET state=$1::jsonb,revision=revision+1,updated_at=NOW() WHERE player_id=$2 RETURNING revision,state,updated_at",[JSON.stringify(state),req.player.id]);
    await client.query("COMMIT");
    res.json({...saved.rows[0],result:choice.label});
  } catch(e) { await client.query("ROLLBACK"); next(e); }
  finally { client.release(); }
});
