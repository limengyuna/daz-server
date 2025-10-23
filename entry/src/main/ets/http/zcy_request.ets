import { get, put } from './request'
import { api_hmkf } from '../api'
import { AppKeyHerder, MasterHerder } from '../common/RequestHeader'

export function updateTarget(target: number) {
  let params = JSON.stringify({ target })
  put(api_hmkf, params, MasterHerder)
}