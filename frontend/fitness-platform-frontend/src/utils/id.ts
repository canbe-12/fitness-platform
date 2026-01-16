export function newClientId(): string {
  const c: any = globalThis.crypto
  if (c?.randomUUID) return c.randomUUID()
  return `${Date.now()}-${Math.random().toString(16).slice(2)}`
}
