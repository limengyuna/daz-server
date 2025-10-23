@Entry
@Component
export default struct WatchPairingPage {
  build() {
    Column() {
      Column() {
        Image($r('app.media.watch_color'))
          .width(120)
          .height(120)
          .margin({ top: 100, bottom: 50, left: -40 })
          .objectFit(ImageFit.Contain)
          .alignSelf(ItemAlign.Start)

        Text('你好')
          .fontSize(32)
          .fontWeight(FontWeight.Bold)
          .fontColor('#000000')
          .margin({ bottom: 5 })

        Text('智能手表陪伴您')
          .fontSize(32)
          .fontWeight(FontWeight.Bold)
          .fontColor('#000000')
          .margin({ bottom: 20 })

        // 分割线
        Row()
          .width(30)
          .height(4)
          .backgroundColor('#000000')
          .margin({ bottom: 15 })

        Text('手表为你而设，将帮助您根据您的需求量身定做...')
          .fontSize(12)
          .fontColor('#999999')
          .lineHeight(22)
          .margin({ bottom: 10 })

        Text('了解更多')
          .fontSize(14)
          .fontColor('#1E90FF')
          .margin({ bottom: 80 })
          .decoration({ type: TextDecorationType.Underline, color: '#1E90FF' })
          .onClick(() => {
            console.info('了解更多被点击')
          })
      }
      .width('100%')
      .layoutWeight(1)
      .alignItems(HorizontalAlign.Start)
      .padding({ left: 60 })

      // 底部按钮
      Button('绑定设备')
        .width('85%')
        .height(50)
        .fontSize(16)
        .fontColor('#FFFFFF')
        .backgroundColor('#33CCFF')
        .borderRadius(8)
        .margin({ bottom: 40 })
        .onClick(() => {
          // 处理绑定设备的点击事件
          this.getUIContext().getRouter().pushUrl({url: 'pages/Yxy2DeviceSelectionPage'}).catch()
        })
    }
    .width('100%')
    .height('100%')
    .backgroundColor('#F8F8F8')
  }
}
