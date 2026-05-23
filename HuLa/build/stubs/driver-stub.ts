export type Config = Record<string, unknown>
export type DriveStep = Record<string, unknown>
export type Driver = { drive: () => void; destroy: () => void }

export const driver = (): Driver => ({
  drive: () => {},
  destroy: () => {}
})
