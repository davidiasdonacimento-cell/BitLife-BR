import { createReadStream } from "node:fs";
import { stat } from "node:fs/promises";
import { createHash } from "node:crypto";
import path from "node:path";
import { Router } from "express";

export const contentRouter = Router();
let cachedHash = null;
const MIN_CONTENT_PACK_BYTES = 800 * 1024 * 1024;

function unavailable(message = "O pacote de conteúdo ainda não foi publicado neste servidor.") {
  const error = new Error(message);
  error.status = 503;
  return error;
}

async function hashFile(filePath) {
  const hash = createHash("sha256");
  await new Promise((resolve, reject) => {
    const stream = createReadStream(filePath);
    stream.on("data", chunk => hash.update(chunk));
    stream.on("error", reject);
    stream.on("end", resolve);
  });
  return hash.digest("hex");
}

async function getPublishedPack() {
  const configuredPath = process.env.CONTENT_PACK_PATH;
  if (!configuredPath) throw unavailable();
  const filePath = path.resolve(configuredPath);
  let info;
  try { info = await stat(filePath); } catch { throw unavailable(); }
  if (!info.isFile() || info.size < MIN_CONTENT_PACK_BYTES) throw unavailable("O pacote publicado precisa ter pelo menos 800 MiB de conteúdo real. Tamanho atual: " + (info.isFile() ? info.size + " bytes" : "arquivo inválido") + ".");
  if (info.size > 2 * 1024 * 1024 * 1024) throw unavailable("O pacote excede o limite de 2 GiB suportado pelo instalador Android.");
  const configuredHash = (process.env.CONTENT_PACK_SHA256 || "").trim().toLowerCase();
  const cacheKey = [filePath, info.size, info.mtimeMs, info.ino].join("|");
  let sha256 = configuredHash;
  if (!sha256) {
    if (cachedHash && cachedHash.key === cacheKey) {
      sha256 = cachedHash.value;
    } else {
      sha256 = await hashFile(filePath);
      cachedHash = { key: cacheKey, value: sha256 };
    }
  }
  if (!/^[a-f0-9]{64}$/.test(sha256)) throw unavailable("A assinatura SHA-256 não está configurada corretamente.");
  return { filePath, sizeBytes: info.size, sha256,
    version: process.env.CONTENT_PACK_VERSION || "1.0.0",
    fileName: "bitlife-br-content.zip" };
}

contentRouter.get("/manifest", async (_req, res, next) => {
  try {
    const pack = await getPublishedPack();
    res.set("Cache-Control", "no-store");
    res.json({ version: pack.version, fileName: pack.fileName, sizeBytes: pack.sizeBytes,
      sha256: pack.sha256, format: "zip", downloadUrl: "/api/content/download", minimumAppVersionCode: 1 });
  } catch (error) {
    if (error.status === 503) return res.status(503).json({ error: error.message });
    next(error);
  }
});

contentRouter.get("/download", async (_req, res, next) => {
  try {
    const pack = await getPublishedPack();
    res.set("X-Content-SHA256", pack.sha256);
    res.set("Cache-Control", "public, max-age=3600");
    res.download(pack.filePath, pack.fileName, { acceptRanges: true }, error => {
      if (error && !res.headersSent) next(error);
    });
  } catch (error) {
    if (error.status === 503) return res.status(503).json({ error: error.message });
    next(error);
  }
});