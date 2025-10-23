import { get, put } from './request'
import { api_user_info } from '../api'
import { AppKeyHerder, MasterHerder } from '../common/RequestHeader'

export function getUserInfo(): Promise<string> {
  return get(api_user_info, AppKeyHerder)
}

export function updateSittingTime(sittingTime: number) {
  let params = JSON.stringify({ sittingTime })
  put(api_user_info, params, MasterHerder)
}