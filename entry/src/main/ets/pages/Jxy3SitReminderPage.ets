import { getUserInfo, updateSittingTime } from '../http/jxy_request'
import { UserInfo } from '../typing/LeanCloudType'

@Entry
@Component
struct SitReminderPage {
  @State isReminderOn: boolean = true
  @State selectedTime: number = 30
  private timeOptions: string[] = []

  async aboutToAppear() {
    for (let i = 0; i < 23; i++) {
      const time = 10 + i * 5
      this.timeOptions.push(`${time}分钟`)
    }

    let rs = await getUserInfo()
    let user: UserInfo = JSON.parse(rs)
    this.selectedTime = user.sittingTime
    console.log(this.selectedTime.toString())
  }

  getDefaultIndex(): number {
    // return this.timeOptions.findIndex(item => item === '30分钟')
    return this.timeOptions.findIndex(item => item === `${this.selectedTime}分钟`)
  }


  build() {
    Column() {
      // 顶部标题栏（精确居中方案）
      Row() {
        // 左侧占位，与右侧"保存"宽度对称
        Text('')
          .width(40) // 与"保存"按钮宽度一致，确保标题居中

        // 标题居中
        Text('久坐提醒')
          .fontSize(20)
          .fontWeight(500)
          .flexGrow(1) // 占据剩余空间，实现绝对居中
          .textAlign(TextAlign.Center) // 文字自身居中

        // 保存按钮右上角
        Text('保存')
          .fontSize(16)
          .fontColor('#007AFF')
          .width(40) // 固定宽度，与左侧占位对齐
          .textAlign(TextAlign.End) // 文字右对齐
          .onClick(() => {
            console.info(`保存设置：${this.isReminderOn ? '开启' : '关闭'}，${this.selectedTime}分钟`)
            updateSittingTime(this.selectedTime)
          })
      }
      .width('100%')
      .padding({ top: 20, left: 20, right: 20, bottom: 10 })

      // 中间图片区域
      Column() {
        Image($r('app.media.bandeng'))
          .width(100)
          .height(100)
          .objectFit(ImageFit.Contain)
        Text('久坐提醒时间设置')
          .fontSize(14)
          .fontColor('#999')
          .margin({ top: 10 })
      }
      .width('100%')
      .alignItems(HorizontalAlign.Center)
      .margin({ top: 10, bottom: 10 })

      // 分割线
      Row()
        .width('100%')
        .height(1)
        .backgroundColor('#f0f0f0')

      // 久坐提醒开关
      Row() {
        Text('开启久坐提醒')
          .fontSize(16)
          .flexGrow(1)
        Toggle({
          type: ToggleType.Switch,
          isOn: this.isReminderOn
        })
          .onChange((isOn: boolean) => {
            this.isReminderOn = isOn
          })
      }
      .width('100%')
      .padding({ left: 20, right: 20, top: 15, bottom: 15 })
      .backgroundColor('#fff')

      // 时间选择区域
      Column() {
        Text('提醒间隔时间')
          .fontSize(16)
          .width('100%')
          .padding({ left: 20, top: 15 })
        TextPicker({
          range: this.timeOptions,
          selected: this.getDefaultIndex()
        })
          .width('100%')
          .padding({ left: 20, right: 20, top: 10, bottom: 15 })
          .onChange((value: string | string[], index: number | number[]) => {
            if (typeof value === 'string') {
              this.selectedTime = parseInt(value.replace('分钟', ''))
            }
          })
          .backgroundColor('#fff')
      }
    }
    .width('100%')
    .height('100%')
    .backgroundColor('#f8f8f8')
  }
}