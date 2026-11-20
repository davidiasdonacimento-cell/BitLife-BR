import { createReadStream } from "node:fs";
import { stat } from "node:fs/promises";
import { createHash } from "node:crypto";
import path from "node:path";
import { Router } from "express";

export const contentRouter = Router();

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
  if (!info.isFile() || info.size <= 0) throw unavailable();
  const configuredHash = (process.env.CONTENT_PACK_SHA256 || "").trim().toLowerCase();
  const sha256 = configuredHash || await hashFile(filePath);
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