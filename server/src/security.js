import jwt from "jsonwebtoken";
import { randomUUID } from "node:crypto";
export function issueToken(player) {
  return jwt.sign({ sub: player.id, email: player.email }, process.env.JWT_SECRET, { expiresIn: "7d", issuer: "bitlife-br" });
}
export function auth(req, res, next) {
  const value = req.headers.authorization || "";
  const token = value.startsWith("Bearer ") ? value.slice(7) : "";
  try {
    if (!token || !process.env.JWT_SECRET) throw new Error("token");
    const claims = jwt.verify(token, process.env.JWT_SECRET, { issuer: "bitlife-br" });
    req.player = { id: claims.sub, email: claims.email };
    next();
  } catch {
    res.status(401).json({ error: "Sessão inválida ou expirada. Entre novamente." });
  }
}
export const newId = () => randomUUID();
