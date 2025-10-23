@Entry
@Component
struct DeviceSelectionPage {
  @State selectedDevice: number = 1 // 0: 未选择, 1: Watch s1, 2: Watch one

  build() {
    Column() {

      Row() {
        Text('选择要绑定的设备')
          .fontSize(18)
          .fontWeight(FontWeight.Medium)
          .fontColor('#000000')
        
        Stack() {
          Circle()
            .width(24)
            .height(24)
            .stroke('#000000')
            .strokeWidth(2)
            .fill(Color.Transparent)
          
          Text('?')
            .fontSize(16)
            .fontWeight(FontWeight.Bold)
            .fontColor('#000000')
        }
        .width(24)
        .height(24)
        .onClick(() => {
          console.info('帮助图标被点击')
        })
      }
      .width('100%')
      .height(56)
      .padding({ left: 20, right: 20 })
      .justifyContent(FlexAlign.SpaceBetween)
      .alignItems(VerticalAlign.Center)

      // 设备列表
      Column() {
        // Gligo Watch one
        Row() {
          Image(this.selectedDevice === 2 ? $r('app.media.watch1') : $r('app.media.watch2'))
            .width(this.selectedDevice === 2 ? 40 : 30)
            .height(this.selectedDevice === 2 ? 60 : 50)
            .margin({ right: 15 })
          
          Text('Gligo Watch one')
            .fontSize(16)
            .fontColor(this.selectedDevice === 2 ? '#000000' : '#666666')
            .layoutWeight(1)
          
          // 选中时显示绿色圆圈+白色勾选
          if (this.selectedDevice === 2) {
            Stack() {
              Circle()
                .width(24)
                .height(24)
                .fill('#00CC66')
              
              Text('✓')
                .fontSize(16)
                .fontColor('#FFFFFF')
                .fontWeight(FontWeight.Bold)
            }
            .width(24)
            .height(24)
          }
        }
        .width('85%')
        .height(90)
        .padding({ left: 20, right: 20 })
        .borderRadius(8)
        .border({
          width: this.selectedDevice === 2 ? 2 : 1,
          color: this.selectedDevice === 2 ? '#33CCFF' : '#CCCCCC',
          style: this.selectedDevice === 2 ? BorderStyle.Solid : BorderStyle.Dashed
        })
        .backgroundColor('#FFFFFF')
        .margin({ top: 60, bottom: 20 })
        .onClick(() => {
          this.selectedDevice = 2
        })

        // Gligo Watch s1
        Row() {
          Image(this.selectedDevice === 1 ? $r('app.media.watch1') : $r('app.media.watch2'))
            .width(this.selectedDevice === 1 ? 40 : 30)
            .height(this.selectedDevice === 1 ? 60 : 50)
            .margin({ right: 15 })
          
          Text('Gligo Watch s1')
            .fontSize(16)
            .fontColor(this.selectedDevice === 1 ? '#000000' : '#666666')
            .layoutWeight(1)
          
          // 选中时显示绿色圆圈+白色勾选
          if (this.selectedDevice === 1) {
            Stack() {
              Circle()
                .width(24)
                .height(24)
                .fill('#00CC66')
              
              Text('✓')
                .fontSize(16)
                .fontColor('#FFFFFF')
                .fontWeight(FontWeight.Bold)
            }
            .width(24)
            .height(24)
          }
        }
        .width('85%')
        .height(90)
        .padding({ left: 20, right: 20 })
        .borderRadius(8)
        .border({
          width: this.selectedDevice === 1 ? 2 : 1,
          color: this.selectedDevice === 1 ? '#33CCFF' : '#CCCCCC',
          style: this.selectedDevice === 1 ? BorderStyle.Solid : BorderStyle.Dashed
        })
        .backgroundColor('#FFFFFF')
        .onClick(() => {
          this.selectedDevice = 1
        })
      }
      .width('100%')
      .layoutWeight(1)
      .alignItems(HorizontalAlign.Center)

      // 底部按钮区域
      Column() {
        // 暂不绑定
        Text('暂不绑定')
          .fontSize(16)
          .fontColor('#999999')
          .margin({ bottom: 20 })
          .onClick(() => {
            console.info('暂不绑定被点击')
            this.getUIContext().getRouter().replaceUrl({url: 'pages/Index'}).catch()
          })

        Button('绑定')
          .width('85%')
          .height(50)
          .fontSize(16)
          .fontColor('#FFFFFF')
          .backgroundColor('#33CCFF')
          .borderRadius(8)
          .margin({ bottom: 20 })
          .onClick(() => {
            console.info('绑定设备：' + this.selectedDevice)
            this.getUIContext().getRouter().replaceUrl({url: 'pages/Index'}).catch()
          })


        Text('了解GliGo手持的设备')
          .fontSize(14)
          .fontColor('#1E90FF')
          .margin({ top: 30, bottom: 30 })
          .decoration({ type: TextDecorationType.Underline, color: '#1E90FF' })
          .onClick(() => {
            console.info('了解设备被点击')
          })
      }
      .width('100%')
      .alignItems(HorizontalAlign.Center)
    }
    .width('100%')
    .height('100%')
    .backgroundColor('#F8F8F8')
  }
}

