/*
  objectId: string  主键
  createdAt: Date
  updatedAt: Date
  以上三个是 LeanCloud 自带的三个字段，自动生成。
  注意：本次练习 一般不用createdAt、updatedAt，
       而是应该使用 mockTime （模拟的记录时间）
  ---------------------------------
 */

export interface HeartRate {
  objectId: string
  createdAt: Date
  updatedAt: Date
  /**
   * 心率值
   */
  rate: number
  /**
   * 模拟时间：年月日、时分秒
   */
  mockTime: Date
}

export interface SleepDuration {
  objectId: string
  createdAt: Date
  updatedAt: Date
  /**
   * 睡眠时长
   */
  duration: number
  /**
   * 模拟时间：年月日-有效
   */
  mockTime: Date
}

export interface StepCount {
  objectId: string
  createdAt: Date
  updatedAt: Date
  /**
   * 运动步数
   */
  count: number
  /**
   * 运动时长
   */
  duration: number
  /**
   * 模拟时间：年月日-有效
   */
  mockTime: Date
}

export interface UserInfo {
  objectId: string
  createdAt: string
  updatedAt: string
  /**
   * 生日：年月日-有效数字，时分秒-均为0
   */
  birthday: string
  /**
   * 性别，直接用中文：男、女
   */
  gender: string
  /**
   * 昵称
   */
  nickname: string
  /**
   * 目标步数
   */
  target: number
  /**
   * 坐着的时长
   */
  sittingTime: number
}