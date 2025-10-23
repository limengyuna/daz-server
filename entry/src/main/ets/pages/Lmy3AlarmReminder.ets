interface AlarmItem {
  id: number;
  time: string;
  days: string;
  enabled: boolean;
}

@Entry
@Component
struct AlarmReminder {
  @State alarmList: AlarmItem[] = [
    { id: 1, time: '7:00', days: '周一  周二  周三  周四', enabled: true },
    { id: 2, time: '15:00', days: '周一  周二  周三  周四', enabled: true },
    { id: 3, time: '21:00', days: '周一  周二  周三  周四', enabled: true }
  ];

  build() {
    Column() {
      // 顶部导航栏
      Row() {
        // 返回按钮
        Image($r('sys.symbol.chevron_left'))
          .width(24)
          .height(24)
          .fillColor('#333333')
          .onClick(() => {
            // 返回上一页
          })

        // 标题
        Text('闹钟提醒')
          .fontSize(18)
          .fontWeight(FontWeight.Medium)
          .fontColor('#333333')
          .layoutWeight(1)
          .textAlign(TextAlign.Center)

        // 新增按钮
        Text('新增')
          .fontSize(16)
          .fontColor('#5B9EFF')
          .onClick(() => {
            // 新增闹钟
          })
      }
      .width('100%')
      .height(56)
      .padding({ left: 16, right: 16 })
      .backgroundColor('#FFFFFF')

      Scroll() {
        Column() {
          // 闹钟图标区域
          Column() {
            Image($r('app.media.alarm'))
              .width(100)
              .height(100)
              .fillColor('#FF9933')
              .margin({ bottom: 15 })

            Text('闹钟提醒')
              .fontSize(14)
              .fontColor('#CCCCCC')
          }
          .width('100%')
          .alignItems(HorizontalAlign.Center)
          .margin({ top: 10, bottom: 20 })

          // 闹钟列表
          Column() {
            ForEach(this.alarmList, (alarm: AlarmItem, index: number) => {
              Column() {
                Row() {
                  // 时间
                  Text(alarm.time)
                    .fontSize(32)
                    .fontWeight(FontWeight.Medium)
                    .fontColor('#333333')

                  Blank()

                  // 开关
                  Toggle({ type: ToggleType.Switch, isOn: alarm.enabled })
                    .selectedColor('#5B9EFF')
                    .width(48)
                    .height(28)
                    .onChange((isOn: boolean) => {
                      alarm.enabled = isOn;
                    })
                }
                .width('100%')
                .margin({ bottom: 8 })

                // 星期
                Text(alarm.days)
                  .fontSize(12)
                  .fontColor('#999999')
                  .width('100%')
              }
              .width('100%')
              .padding({ left: 20, right: 20, top: 20, bottom: 20 })
              .backgroundColor('#FFFFFF')
              .borderRadius(0)
              .margin({ bottom: index < this.alarmList.length - 1 ? 1 : 0 })
            }, (alarm: AlarmItem) => alarm.id.toString())
          }
          .width('100%')
        }
      }
      .layoutWeight(1)
      .scrollBar(BarState.Off)
    }
    .width('100%')
    .height('100%')
    .backgroundColor('#F5F5F5')
  }
}

