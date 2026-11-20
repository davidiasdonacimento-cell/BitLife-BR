import test from "node:test";
import assert from "node:assert/strict";
import { createSessionNpcs } from "../src/npcs.js";

test("cada sessão recebe 12 personagens fictícios com IDs próprios", () => {
  const a=createSessionNpcs(), b=createSessionNpcs();
  assert.equal(a.length,12);
  assert.equal(b.length,12);
  assert.equal(new Set(a.map(x=>x.id)).size,12);
  assert.deepEqual(a.map(x=>x.id),b.map(x=>x.id));
  for(const npc of a) {
    assert.match(npc.id,/^npc_\d+$/);
    assert.ok(npc.name.length>2);
    assert.ok(npc.age>=18 && npc.age<=65);
    assert.ok(npc.city.length>0);
    assert.ok(npc.role.length>0);
  }
});
