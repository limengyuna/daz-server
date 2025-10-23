@Entry
@Component
struct myInformation {
  build() {
    Column({ space: 16 }) {
      // 顶部返回图标和标题区域
      Column({ space: 8 }) {
        Image($r('app.media.return'))
          .width(26)
          .height(20)
        Text('我的信息')
          .fontSize(24)
          .fontWeight(FontWeight.Bold)
      }
      .alignItems(HorizontalAlign.Start)
      .width('100%')
      .margin({ top: 16, bottom: 4 })

      // 副标题
      Text('精确的信息可以获取更准确的数据分析')
        .fontSize(14)
        .fontColor(Color.Gray)
        .alignSelf(ItemAlign.Start)
        .margin({ top: -10, bottom: 16 })

      // 身高输入项
      Column({ space: 8 }) {
        Text('身高')
          .fontSize(16)
          .alignSelf(ItemAlign.Start)
        Row({ space: 0 }) { // 将间距设为0，使输入框和单位紧挨着
          TextInput({ placeholder: '输入身高                                       cm' })
            .width('90%') // 与按钮同宽
            .height(45)
            .borderRadius(4)
            .borderWidth(1)
            .borderColor('#fff4f6f5')
        }
        .alignItems(VerticalAlign.Center)
        .width('100%')
        .justifyContent(FlexAlign.Start)
      }
      .width('100%')
      .alignItems(HorizontalAlign.Start)

      // 体重输入项
      Column({ space: 8 }) {
        Text('体重')
          .fontSize(16)
          .alignSelf(ItemAlign.Start)
        Row({ space: 0 }) { // 将间距设为0，使输入框和单位紧挨着
          TextInput({ placeholder: '输入体重                                       kg' })
            .width('90%') // 与按钮同宽
            .height(45)
            .borderRadius(4)
            .borderWidth(1)
            .borderColor('#fff4f6f5')
        }
        .alignItems(VerticalAlign.Center)
        .width('100%')
        .justifyContent(FlexAlign.Start)
      }
      .width('100%')
      .alignItems(HorizontalAlign.Start)
      .margin({ top: 16 })

      // 使用空白空间将按钮推到合适位置
      Blank()
        .layoutWeight(10)

      // 完成按钮 - 水平居中
      Button('完成')
        .width('90%')
        .height(48)
        .backgroundColor('#029BFF')
        .fontColor(Color.White)
        .borderRadius(8)
        .margin({ top: 12, bottom: 30 })
        .alignSelf(ItemAlign.Center)
    }
    .width('100%')
    .height('100%')
    .padding({ left: 16, right: 16, top: 15 })
    .alignItems(HorizontalAlign.Center) // 将整体对齐方式改为居中，使按钮居中
  }
}