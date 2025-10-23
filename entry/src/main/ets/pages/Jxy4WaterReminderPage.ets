@Entry
@Component
struct WaterReminderPage {
  // 状态变量
  @State isTotalOn: boolean = true
  @State is630On: boolean = true
  @State is830On: boolean = true
  @State is1100On: boolean = false
  @State is1250On: boolean = true
  @State is1500On: boolean = false
  @State is1730On: boolean = false
  @State is2200On: boolean = true

  build() {
    Column() {
      // 顶部标题栏（标题居中，新增在最右侧）
      Row() {
        // 左侧占位，平衡右侧空间
        Text('')
          .flexGrow(1)

        // 标题居中
        Text('喝水提醒')
          .fontSize(20)
          .fontWeight(500)

        // 右侧"新增"按钮
        Row() {
          Text('新增')
            .fontSize(16)
            .fontColor('#007AFF')
        }
        .flexGrow(1)
        .justifyContent(FlexAlign.End)
      }
      .width('100%')
      .padding({ top: 20, left: 20, right: 20, bottom: 10 })

      // 中间图片区域（移除橙色边框）
      Column() {
        Image($r('app.media.heshui'))
          .width(100)
          .height(100)
          .objectFit(ImageFit.Contain)
          .borderRadius(50) // 仅保留圆形裁剪，移除边框

        Text('喝水提醒')
          .fontSize(14)
          .fontColor('#999')
          .margin({ top: 10 })
      }
      .width('100%')
      .alignItems(HorizontalAlign.Center)
      .margin({ top: 10, bottom: 10 })

      // 科学喝水总开关
      Row() {
        Text('科学喝水时间表')
          .fontSize(16)
          .flexGrow(1)
        Toggle({
          type: ToggleType.Switch,
          isOn: this.isTotalOn
        })
          .onChange((isOn: boolean) => {
            this.isTotalOn = isOn
          })
      }
      .width('100%')
      .padding({ left: 20, right: 20, top: 15, bottom: 15 })
      .backgroundColor('#f5f5f5')

      // 6:30
      Row() {
        Text('6:30')
          .fontSize(16)
          .flexGrow(1)
        Toggle({
          type: ToggleType.Switch,
          isOn: this.is630On
        })
          .enabled(this.isTotalOn)
          .onChange((isOn: boolean) => {
            this.is630On = isOn
          })
      }
      .width('100%')
      .padding({ left: 20, right: 20, top: 15, bottom: 15 })
      Row().width('100%').height(1).backgroundColor('#f0f0f0').margin({ left: 20, right: 20 })

      // 8:30
      Row() {
        Text('8:30')
          .fontSize(16)
          .flexGrow(1)
        Toggle({
          type: ToggleType.Switch,
          isOn: this.is830On
        })
          .enabled(this.isTotalOn)
          .onChange((isOn: boolean) => {
            this.is830On = isOn
          })
      }
      .width('100%')
      .padding({ left: 20, right: 20, top: 15, bottom: 15 })
      Row().width('100%').height(1).backgroundColor('#f0f0f0').margin({ left: 20, right: 20 })

      // 11:00
      Row() {
        Text('11:00')
          .fontSize(16)
          .flexGrow(1)
        Toggle({
          type: ToggleType.Switch,
          isOn: this.is1100On
        })
          .enabled(this.isTotalOn)
          .onChange((isOn: boolean) => {
            this.is1100On = isOn
          })
      }
      .width('100%')
      .padding({ left: 20, right: 20, top: 15, bottom: 15 })
      Row().width('100%').height(1).backgroundColor('#f0f0f0').margin({ left: 20, right: 20 })

      // 12:50
      Row() {
        Text('12:50')
          .fontSize(16)
          .flexGrow(1)
        Toggle({
          type: ToggleType.Switch,
          isOn: this.is1250On
        })
          .enabled(this.isTotalOn)
          .onChange((isOn: boolean) => {
            this.is1250On = isOn
          })
      }
      .width('100%')
      .padding({ left: 20, right: 20, top: 15, bottom: 15 })
      Row().width('100%').height(1).backgroundColor('#f0f0f0').margin({ left: 20, right: 20 })

      // 15:00
      Row() {
        Text('15:00')
          .fontSize(16)
          .flexGrow(1)
        Toggle({
          type: ToggleType.Switch,
          isOn: this.is1500On
        })
          .enabled(this.isTotalOn)
          .onChange((isOn: boolean) => {
            this.is1500On = isOn
          })
      }
      .width('100%')
      .padding({ left: 20, right: 20, top: 15, bottom: 15 })
      Row().width('100%').height(1).backgroundColor('#f0f0f0').margin({ left: 20, right: 20 })

      // 17:30
      Row() {
        Text('17:30')
          .fontSize(16)
          .flexGrow(1)
        Toggle({
          type: ToggleType.Switch,
          isOn: this.is1730On
        })
          .enabled(this.isTotalOn)
          .onChange((isOn: boolean) => {
            this.is1730On = isOn
          })
      }
      .width('100%')
      .padding({ left: 20, right: 20, top: 15, bottom: 15 })
      Row().width('100%').height(1).backgroundColor('#f0f0f0').margin({ left: 20, right: 20 })

      // 22:00
      Row() {
        Text('22:00')
          .fontSize(16)
          .flexGrow(1)
        Toggle({
          type: ToggleType.Switch,
          isOn: this.is2200On
        })
          .enabled(this.isTotalOn)
          .onChange((isOn: boolean) => {
            this.is2200On = isOn
          })
      }
      .width('100%')
      .padding({ left: 20, right: 20, top: 15, bottom: 15 })
    }
    .width('100%')
    .height('100%')
    .backgroundColor('#f8f8f8')
  }
}