import http from '@ohos.net.http';

interface HeartRateData {
  currunt_heart: number;
  createdAt: string;
  updatedAt: string;
  objectId: string;
}

interface HeartRateResponse {
  results: HeartRateData[];
}

@Entry
@Component
struct HeartRateResultPage {
  @State heartRate: number = 0
  @State isLoading: boolean = true
  @State errorMessage: string = ''

  aboutToAppear() {
    this.fetchHeartRate()
  }

  fetchHeartRate() {
    let httpRequest = http.createHttp()
    
    httpRequest.request(
      'https://api.leancloud.cn/1.1/classes/yxyhert',
      {
        method: http.RequestMethod.GET,
        header: {
          'X-LC-Id': 'Lk7Ea17CoVWpjbDiMn2Ajc0t-gzGzoHsz',
          'X-LC-Key': 'CzmXtXqi8Zf3pSLFPPaUGqd0',
          'Content-Type': 'application/json'
        },
        expectDataType: http.HttpDataType.STRING,
        connectTimeout: 60000,
        readTimeout: 60000
      }
    ).then((data: http.HttpResponse) => {
      if (data.responseCode === 200) {
        const response = JSON.parse(data.result.toString()) as HeartRateResponse
        if (response.results && response.results.length > 0) {
          this.heartRate = response.results[0].currunt_heart
          this.isLoading = false
        } else {
          this.errorMessage = '未获取到心率数据'
          this.isLoading = false
        }
      } else {
        this.errorMessage = '获取心率失败'
        this.isLoading = false
      }
      httpRequest.destroy()
    }).catch((err: Error) => {
      console.error('请求失败：' + JSON.stringify(err))
      this.errorMessage = '网络请求失败'
      this.isLoading = false
      httpRequest.destroy()
    })
  }

  getHeartRateStatus(): string {
    if (this.heartRate < 60) {
      return '你的心率心跳偏低，建议咨询医生'
    } else if (this.heartRate >= 60 && this.heartRate <= 100) {
      return '你的心率正常，保持健康的生活方式'
    } else {
      return '你的心率心跳偏高，建议将平日己的心志，尽量避免做剧烈运动'
    }
  }

  build() {
    Column() {
      Row() {
        Text('×')
          .fontSize(32)
          .fontColor('#333333')
          .fontWeight(FontWeight.Lighter)
          .onClick(() => {
            console.info('关闭心率结果页')
          })
      }
      .width('100%')
      .height(56)
      .padding({ left: 20 })
      .justifyContent(FlexAlign.Start)
      .alignItems(VerticalAlign.Center)

      Column() {
        Column() {
          Image($r('app.media.aixin'))
            .width(31)
            .height(28)
            .margin({ bottom: 20 })
            .objectFit(ImageFit.Contain)

          Text('心率测试中…')
            .fontSize(20)
            .fontColor('#333333')
            .fontWeight(FontWeight.Medium)
            .margin({ bottom: 10 })

          Text('请稳戴好手表，并保持与手腕贴的状态…')
            .fontSize(14)
            .fontColor('#999999')
            .textAlign(TextAlign.Center)
            .width('80%')
        }
        .margin({ top: 20, bottom: 40 })

        if (this.isLoading) {
          LoadingProgress()
            .width(60)
            .height(60)
            .color('#33CCFF')
            .margin({ top: 60, bottom: 60 })
        } else if (this.errorMessage !== '') {
          Text(this.errorMessage)
            .fontSize(16)
            .fontColor('#FF0000')
            .margin({ top: 60, bottom: 60 })
        } else {
          Column() {
            Text('本次心率结果')
              .fontSize(14)
              .fontColor('#999999')
              .margin({ bottom: 10 })

            Row() {
              Text(this.heartRate.toString())
                .fontSize(60)
                .fontColor('#000000')
                .fontWeight(FontWeight.Bold)
              
              Text('bpm')
                .fontSize(24)
                .fontColor('#000000')
                .margin({ left: 5, bottom: 5 })
                .alignSelf(ItemAlign.End)
            }
            .alignItems(VerticalAlign.Bottom)
            .margin({ bottom: 40 })

            Text(this.getHeartRateStatus())
              .fontSize(14)
              .fontColor('#666666')
              .textAlign(TextAlign.Center)
              .width('85%')
              .lineHeight(22)
          }
          .margin({ top: 40, bottom: 40 })
        }
      }
      .layoutWeight(1)
      .width('100%')
      .justifyContent(FlexAlign.Start)
      .alignItems(HorizontalAlign.Center)

      Column() {
        Button('确定')
          .width('85%')
          .height(50)
          .fontSize(16)
          .fontColor('#FFFFFF')
          .backgroundColor('#33CCFF')
          .borderRadius(8)
          .margin({ bottom: 15 })
          .onClick(() => {
            console.info('确定按钮被点击')
          })

        Button('重新测试')
          .width('85%')
          .height(50)
          .fontSize(16)
          .fontColor('#33CCFF')
          .backgroundColor(Color.Transparent)
          .border({ width: 1, color: '#33CCFF' })
          .borderRadius(8)
          .margin({ bottom: 40 })
          .onClick(() => {
            console.info('重新测试被点击')
            this.isLoading = true
            this.errorMessage = ''
            this.fetchHeartRate()
          })
      }
      .width('100%')
      .alignItems(HorizontalAlign.Center)
    }
    .width('100%')
    .height('100%')
    .backgroundColor('#FFFFFF')
  }
}

