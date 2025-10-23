@Entry
@Component
export default struct AnalyzePage {
  build() {
    Column({ space: 30 }) {
      Image($r('app.media.analyze_page'))
        .width('100%')
        .margin({ top: 60 })

      Column() {
        // 文本区域
        Column({ space: 8 }) {
          Text('设备未绑定')
            .fontSize(20)
            .fontWeight(FontWeight.Bold)
            .margin({ top: 20 })
          Text('链接设备获取更多信息')
            .fontSize(14)
        }

        // 按钮区域
        Column({ space: 16 }) {
          Button('链接设备')
            .width(200)
            .height(44)
            .backgroundColor('#43B8FF')
            .fontColor(Color.White)
            .borderRadius(8)
            .onClick(() => {
              this.getUIContext().getRouter().pushUrl({ url: 'pages/Yxy1WatchPairingPage' }).catch()
            })
          Text('了解GlGo支持的设备')
            .fontSize(14)
            .fontColor('#43B8FF')
            .onClick(() => {
              console.info('查看支持的设备列表')
            })
        }
        .margin({ top: 40 })
      }.position({ y: 350 }).width('100%')
    }
    .width('100%')
    .height('100%')
    .backgroundColor(Color.White)
  }
}