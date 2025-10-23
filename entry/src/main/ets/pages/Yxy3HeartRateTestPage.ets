@Entry
@Component
struct HeartRateTestPage {
  @State circleScale1: number = 0.8
  @State circleScale2: number = 0.8
  @State circleScale3: number = 0.8
  @State circleOpacity1: number = 0.8
  @State circleOpacity2: number = 0.8
  @State circleOpacity3: number = 0.8
  @State heartScale: number = 1
  
  private timer1: number = -1
  private timer2: number = -1
  private timer3: number = -1
  private heartTimer: number = -1
  
  aboutToAppear() {
    this.startCircleAnimation1()
    
    setTimeout(() => {
      this.startCircleAnimation2()
    }, 500)
    
    setTimeout(() => {
      this.startCircleAnimation3()
    }, 1000)
    
    this.startHeartBeatAnimation()
  }
  
  aboutToDisappear() {
    clearInterval(this.timer1)
    clearInterval(this.timer2)
    clearInterval(this.timer3)
    clearInterval(this.heartTimer)
  }
  
  startCircleAnimation1() {
    this.timer1 = setInterval(() => {
      animateTo({
        duration: 2000,
        curve: Curve.EaseOut,
        onFinish: () => {
          this.circleScale1 = 0.8
          this.circleOpacity1 = 0.8
        }
      }, () => {
        this.circleScale1 = 2.5
        this.circleOpacity1 = 0
      })
    }, 2000)
  }
  
  startCircleAnimation2() {
    this.timer2 = setInterval(() => {
      animateTo({
        duration: 2000,
        curve: Curve.EaseOut,
        onFinish: () => {
          this.circleScale2 = 0.8
          this.circleOpacity2 = 0.8
        }
      }, () => {
        this.circleScale2 = 2.5
        this.circleOpacity2 = 0
      })
    }, 2000)
  }
  
  startCircleAnimation3() {
    this.timer3 = setInterval(() => {
      animateTo({
        duration: 2000,
        curve: Curve.EaseOut,
        onFinish: () => {
          this.circleScale3 = 0.8
          this.circleOpacity3 = 0.8
        }
      }, () => {
        this.circleScale3 = 2.5
        this.circleOpacity3 = 0
      })
    }, 2000)
  }
  
  startHeartBeatAnimation() {
    this.heartTimer = setInterval(() => {
      animateTo({
        duration: 150,
        curve: Curve.EaseInOut,
        onFinish: () => {
          animateTo({
            duration: 150,
            curve: Curve.EaseInOut,
          }, () => {
            this.heartScale = 1
          })
        }
      }, () => {
        this.heartScale = 1.3
      })
    }, 800)
  }
  
  build() {
    Column() {
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
          
          Text('请稳戴好手表，并保持与手腕进的状态…')
            .fontSize(14)
            .fontColor('#999999')
            .textAlign(TextAlign.Center)
            .width('80%')
        }
        .margin({ top: 40, bottom: 60 })

        Stack() {
          Circle()
            .width(320)
            .height(320)
            .fill(Color.Transparent)
            .stroke('#FFB3BA')
            .strokeWidth(1)
            .opacity(this.circleOpacity3)
            .scale({ x: this.circleScale3, y: this.circleScale3 })
          

          Circle()
            .width(320)
            .height(320)
            .fill(Color.Transparent)
            .stroke('#FFB3BA')
            .strokeWidth(1)
            .opacity(this.circleOpacity2)
            .scale({ x: this.circleScale2, y: this.circleScale2 })

          Circle()
            .width(320)
            .height(320)
            .fill(Color.Transparent)
            .stroke('#FFB3BA')
            .strokeWidth(1)
            .opacity(this.circleOpacity1)
            .scale({ x: this.circleScale1, y: this.circleScale1 })
          
          Circle()
            .width(200)
            .height(200)
            .fill('#FFE5E8')
          
          Circle()
            .width(140)
            .height(140)
            .fill('#FFB3BA')

          Image($r('app.media.aixin2'))
            .width(40)
            .height(40)
            .objectFit(ImageFit.Contain)
            .scale({ x: this.heartScale, y: this.heartScale })
        }
        .width(350)
        .height(350)
      }
      .layoutWeight(1)
      .width('100%')
      .justifyContent(FlexAlign.Start)
      .alignItems(HorizontalAlign.Center)
      
      Button('取消')
        .width('85%')
        .height(50)
        .fontSize(16)
        .fontColor('#33CCFF')
        .backgroundColor(Color.Transparent)
        .border({ width: 1, color: '#33CCFF' })
        .borderRadius(8)
        .margin({ bottom: 40 })
        .onClick(() => {
          console.info('取消心率测试')
          // 这里可以添加取消逻辑
        })
    }
    .width('100%')
    .height('100%')
    .backgroundColor('#FFFFFF')
  }
}

