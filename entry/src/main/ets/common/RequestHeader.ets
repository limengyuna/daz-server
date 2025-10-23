import { HerderType } from '../typing'

const appId = "Lk7Ea17CoVWpjbDiMn2Ajc0t-gzGzoHsz"
const appKey = "CzmXtXqi8Zf3pSLFPPaUGqd0"
const masterKey = "VDfOAfC5exDYv5PUJWJTBKzS"
const contentType = "application/json"

export const AppKeyHerder: HerderType = {
  "X-LC-Id": appId,
  "X-LC-Key": appKey,
  "Content-Type": contentType
}

export const MasterHerder: HerderType = {
  "X-LC-Id": appId,
  "X-LC-Key": masterKey + ",master",
  "Content-Type": contentType
}