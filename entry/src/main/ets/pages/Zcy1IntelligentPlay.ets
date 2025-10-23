// import { FlexAlign, ToggleType, HorizontalAlign, FontWeight } from '@ohos.arkui.types';

@Entry
@Component
export default struct IntelligentPlay {
  // 状态变量管理开关状态
  @State isCallRemindOn: boolean = true; // 来电提醒默认打开
  @State isMessageRemindOn: boolean = false; // 短信提醒默认关闭
  @State isScheduleRemindOn: boolean = false; // 日程提醒默认关闭

  build() {
    Column() {
      // 顶部区域
      Stack() {
        Row()
          .width('100%')
          .height(220)
          .backgroundColor('#ff4b92d9')

        // 设备名称和常见问题在同一行
        Row() {
          Text("Gligo Watch one Plus")
            .fontSize(18)
            .fontWeight(FontWeight.Medium)
            .fontColor('#FFFFFF')
            .layoutWeight(1)
            .textAlign(TextAlign.Center)

          Text("常见问题")
            .fontSize(14)
            .fontColor('#FFFFFF')
        }
        .width('100%')
        .position({ y: 1 })
        .padding({left: 16, right: 16 })

        // 电量信息水平居中
        Column({ space: 5 }) {
          Text("89%")
            .fontSize(42)
            .fontWeight(FontWeight.Medium)
            .fontColor('#FFFFFF')
          Text("(已连接)")
            .fontSize(14)
            .fontColor('#FFFFFF')
        }
        .width('100%')
        .position({ y: 40 })
        .alignItems(HorizontalAlign.Center)

        // 箭头图标
        Image($r("app.media.right"))
          .width(16)
          .height(16)
          .position({
            x: '85%',
            y: 55
          })
      }
      .width('100%')
      .height(140)
      .margin({ top: 0 })

      // 内容区域
      Column() {
        // 提醒区域
        Column({space:5}) {
          Text("提醒")
            .fontSize(16)
            .fontWeight(FontWeight.Medium)
            .margin({ left: 16, top: 15, bottom: 15 })
            .textAlign(TextAlign.Start)
            .width('100%')

          // 传递状态变量到设置项
          this.createSettingItem($r("app.media.icon_phone"), '#0099FF', "来电提醒", true, false, this.isCallRemindOn)
          this.createSettingItem($r("app.media.icon_message"), '#9966FF', "短信提醒", true, false, this.isMessageRemindOn)
          this.createSettingItem($r("app.media.icon_schedule"), '#FF99CC', "日程提醒", true, false, this.isScheduleRemindOn)
          this.createSettingItem($r("app.media.icon_alarmClock"), '#FF9933', "闹钟提醒", false, true, false)

          // 更多标题
          Text("更多")
            .fontSize(16)
            .fontColor('#666666')
            .margin({ left: 16, top: 15, bottom: 10 })
            .textAlign(TextAlign.Center)
            .width('100%')
        }
        .width('100%')
        .backgroundColor('#FFFFFF')
        .padding({ bottom: 15 })

        // 间隔
        Column()
          .width('100%')
          .height(5)
          .backgroundColor('#F5F5F5')

        // 智能区域
        Column() {
          Text("智能")
            .fontSize(16)
            .fontWeight(FontWeight.Medium)
            .margin({ left: 16, top: 15, bottom: 15 })
            .textAlign(TextAlign.Start)
            .width('100%')

          Row() {
            this.createSmartItem($r("app.media.intelligent_vibration"), '#0099FF', "震动模式")
            this.createSmartItem($r("app.media.intelligent_savingPower"), '#CCCCCC', "省电模式")
            this.createSmartItem($r("app.media.intelligent_heartRate"), '#CCCCCC', "自动心率")
            this.createSmartItem($r("app.media.intelligent_timeZone"), '#CCCCCC', "自动时区")
            this.createSmartItem($r("app.media.intelligent_dontDisturb"), '#CCCCCC', "自动勿扰")
          }
          .width('100%')
          .padding({ left: 10, right: 10 })
          .justifyContent(FlexAlign.SpaceEvenly)
        }
        .width('100%')
        .backgroundColor('#FFFFFF')
        .padding({ bottom: 20 })
        .layoutWeight(1)
      }
      .width('100%')
      .backgroundColor('#F5F5F5')
      .layoutWeight(1)
      .margin({ top: 10 })
    }
    .width('100%')
    .height('100%')
    .backgroundColor('#0066CC')
    .justifyContent(FlexAlign.Start)
  }

  @Builder
  createSettingItem(
    icon: Resource,
    bgColor: string,
    text: string,
    showSwitch: boolean,
    showArrow: boolean,
    isOn: boolean = false // 状态参数，默认false
  ) {
    Row({ space: 12 }) {
      Image(icon)
        .width(40)
        .height(40)
        .borderRadius(20)
        .backgroundColor(bgColor)
      Text(text)
        .fontSize(16)
        .layoutWeight(1)

      Blank()

      if (showSwitch) {
        Toggle({ type: ToggleType.Switch, isOn: isOn })
          .margin({ right: 16 })
          .onChange((value: boolean) => {
            // 根据不同的text更新对应的状态
            if (text === "来电提醒") {
              this.isCallRemindOn = value;
            } else if (text === "短信提醒") {
              this.isMessageRemindOn = value;
            } else if (text === "日程提醒") {
              this.isScheduleRemindOn = value;
            }
          })
      } else if (showArrow) {
        Row({ space: 8 }) {
          Text("已开启")
            .fontSize(14)
            .fontColor('#999999')
          Image($r("app.media.enter"))
            .width(16)
            .height(16)
        }
        .margin({ right: 16 })
      }
    }
    .width('100%')
    .height(50)
    .padding({ left: 16, right: 16 })
  }

  @Builder
  createSmartItem(icon: Resource, bgColor: string, text: string) {
    Column({ space: 6 }) {
      Image(icon)
        .width(45)
        .height(45)
        .borderRadius(22.5)
        .backgroundColor(bgColor)
      Text(text)
        .fontSize(12)
    }
    .alignItems(HorizontalAlign.Center)
    .width('20%')
  }
}