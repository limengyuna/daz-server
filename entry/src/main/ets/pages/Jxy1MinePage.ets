
@Entry
@Component
export default struct MinePage {
  build() {
    Column({ space: 0 }) {
      // 顶部标题
      Row() {
        Text('我')
          .fontSize(20)
          .fontWeight(FontWeight.Bold)
      }
      .width('100%')
      .justifyContent(FlexAlign.Center)
      .padding({ top: 20, bottom: 15 })

      // 设备卡片区域
      Column({ space: 8 }) {
        // Gligo Watch one 卡片（45%严格靠右）
        Row() {
          // 左侧图片+文字组合
          Row() {
            Image($r('app.media.watch1'))
              .width(30)
              .height(30)
              .margin({ right: 10 })
            Text('Gligo Watch one')
              .fontSize(16)
          }
          .margin({ left: 15 })
          .flexGrow(1) // 占据左侧所有剩余空间，将右侧文字推到最右

          // 右侧电量文字（仅保留右侧边距，确保靠最右）
          Text('45%')
            .fontSize(14)
            .fontColor('#999')
            .margin({ right: 15 })
        }
        .width('90%')
        .height(55)
        .backgroundColor('#fff')
        .borderRadius(8)
        .border({ width: 1, color: '#007AFF' })
        .alignItems(VerticalAlign.Center) // 垂直居中

        // Gligo Watch s1 卡片
        Row() {
          Image($r('app.media.watch2'))
            .width(30)
            .height(30)
            .margin({ left: 15, right: 10 })
          Text('Gligo Watch s1')
            .fontSize(16)
        }
        .width('90%')
        .height(55)
        .backgroundColor('#fff')
        .borderRadius(8)
        .border({ width: 1, color: '#ddd' })
        .alignItems(VerticalAlign.Center)
      }
      .margin({ bottom: 15 })

      // 功能列表（箭头严格靠右）
      Column({ space: 1 }) {
        // 单位设置
        Row() {
          Text('单位设置')
            .fontSize(16)
            .margin({ left: 15 })
            .flexGrow(1)
          Image($r('app.media.jiantou'))
            .width(15)
            .height(15)
            .margin({ right: 15 })
        }
        .width('100%')
        .height(45)
        .backgroundColor('#fff')
        .alignItems(VerticalAlign.Center)

        // 第三方接入
        Row() {
          Text('第三方接入')
            .fontSize(16)
            .margin({ left: 15 })
            .flexGrow(1)
          Image($r('app.media.jiantou'))
            .width(15)
            .height(15)
            .margin({ right: 15 })
        }
        .width('100%')
        .height(45)
        .backgroundColor('#fff')
        .alignItems(VerticalAlign.Center)

        // 语言设置
        Row() {
          Text('语言设置')
            .fontSize(16)
            .margin({ left: 15 })
          Row({ space: 8 }) {
            Text('中文')
              .fontSize(14)
              .fontColor('#999')
            Image($r('app.media.jiantou'))
              .width(15)
              .height(15)
          }
          .flexGrow(1)
          .justifyContent(FlexAlign.End)
          .margin({ right: 15 })
        }
        .width('100%')
        .height(45)
        .backgroundColor('#fff')
        .alignItems(VerticalAlign.Center)

        // 意见反馈
        Row() {
          Text('意见反馈')
            .fontSize(16)
            .margin({ left: 15 })
            .flexGrow(1)
          Image($r('app.media.jiantou'))
            .width(15)
            .height(15)
            .margin({ right: 15 })
        }
        .width('100%')
        .height(45)
        .backgroundColor('#fff')
        .alignItems(VerticalAlign.Center)

        // APP版本
        Row() {
          Text('APP版本')
            .fontSize(16)
            .margin({ left: 15 })
          Row({ space: 8 }) {
            Text('当前版本v1.0.1')
              .fontSize(14)
              .fontColor('#999')
            Image($r('app.media.jiantou'))
              .width(15)
              .height(15)
          }
          .flexGrow(1)
          .justifyContent(FlexAlign.End)
          .margin({ right: 15 })
        }
        .width('100%')
        .height(45)
        .backgroundColor('#fff')
        .alignItems(VerticalAlign.Center)

        // 关于我们
        Row() {
          Text('关于我们')
            .fontSize(16)
            .margin({ left: 15 })
            .flexGrow(1)
          Image($r('app.media.jiantou'))
            .width(15)
            .height(15)
            .margin({ right: 15 })
        }
        .width('100%')
        .height(45)
        .backgroundColor('#fff')
        .alignItems(VerticalAlign.Center)
      }
    }
    .width('100%')
    .height('100%')
    .backgroundColor('#f8f8f8')
  }
}