const firstNames = ["Ana","Bruno","Camila","Davi","Eduarda","Felipe","Giovana","Heitor","Isabela","João","Lara","Miguel","Nina","Pedro","Rafaela","Thiago"];
const surnames = ["Almeida","Barbosa","Costa","Dias","Ferreira","Lima","Oliveira","Pereira","Ribeiro","Santos","Souza"];
const cities = ["Salvador","Feira de Santana","São Paulo","Rio de Janeiro","Belo Horizonte","Recife","Curitiba","Manaus","Brasília","Fortaleza"];
const pick = a => a[Math.floor(Math.random() * a.length)];
export function createSessionNpcs() {
  return Array.from({ length: 12 }, (_, i) => ({
    id: `npc_${i + 1}`,
    name: `${pick(firstNames)} ${pick(surnames)}`,
    age: 18 + Math.floor(Math.random() * 48),
    city: pick(cities),
    role: pick(["vizinho","colega","professor","médico","chefe","amigo da família"]),
    personality: pick(["gentil","ambicioso","reservado","engraçado","impulsivo"])
  }));
}
