@Entry
@Component
export default struct ScheduleReminderPage {
  @State repeat: string[] = ['一次', '两次', '三次']

  build() {
    Column({ space: 20 }) {
      // 日程标题输入框
      TextInput({ placeholder: '输入日程标题' })
        .width('100%')
        .height(40)
        .backgroundColor(Color.White)
        .borderRadius(8)
        .padding({ left: 10 })

      // 时间行
      Column() {
        Text('日期')
          .fontSize(16).margin({ bottom: 10 })

        DatePicker({
          start: new Date('1970-1-1'),
          end: new Date('2100-1-1'),
        })

        Text('时间')
          .fontSize(16).margin({ bottom: 10, top: 10 })

        TimePicker()
          .useMilitaryTime(true)
      }
      .width('100%')
      .padding(10)

      // 重复行
      Column() {
        Text('重复')
          .fontSize(16).margin({ bottom: 10 })
        Row({ space: 20 }) {
          ForEach(this.repeat, (item: string) => {
            Button(item)
          })
        }
      }
      .width('100%')
      .padding(10)

      // 保存按钮（右上角）
      Text('保存')
        .fontSize(16)
        .fontColor(Color.Blue)
        .position({ right: 10, top: 10 })
    }
    .width('100%')
    .height('100%')
    .backgroundColor('#F5F5F5')
  }
}