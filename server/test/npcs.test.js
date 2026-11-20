import test from "node:test";
import assert from "node:assert/strict";
import { createSessionNpcs } from "../src/npcs.js";

test("cada sessão recebe 12 personagens fictícios com IDs próprios", () => {
  const a = createSessionNpcs();
  const b = createSessionNpcs();
  assert.equal(a.length, 12);
  assert.equal(b.length, 12);
  assert.equal(new Set(a.map(x => x.id)).size, 12);
  assert.equal(a.some(x => b.some(y => y.id === x.id)), false,
    "sessões isoladas não podem compartilhar IDs de NPCs");
  for (const npc of a) {
    assert.match(npc.id, /^[0-9a-f]{8}-[0-9a-f]{4}-[1-8][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i);
    assert.ok(npc.name.length > 2);
    assert.ok(npc.age >= 18 && npc.age <= 65);
    assert.ok(npc.city.length > 0);
    assert.ok(npc.role.length > 0);
  }
});