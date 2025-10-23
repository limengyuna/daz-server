// 自定义柱状图组件
@Component
struct McBarChart {
  @Prop data: number[] = []
  @Prop chartWidth: number = 350
  @Prop chartHeight: number = 200
  @Prop barColor: string = '#4DA6FF'
  @Prop maxValue: number = 6
  @Prop labels: string[] = []

  private context: CanvasRenderingContext2D = new CanvasRenderingContext2D(new RenderingContextSettings(true))

  build() {
    Canvas(this.context)
      .width(this.chartWidth)
      .height(this.chartHeight)
      .onReady(() => {
        this.drawChart()
      })
  }

  drawChart() {
    if (this.data.length === 0) return

    this.context.clearRect(0, 0, this.chartWidth, this.chartHeight)

    const paddingLeft = 20
    const paddingRight = 20
    const paddingTop = 10
    const paddingBottom = 30
    const chartWidth = this.chartWidth - paddingLeft - paddingRight
    const chartHeight = this.chartHeight - paddingTop - paddingBottom

    const barWidth = 20
    const barSpacing = (chartWidth - barWidth * this.data.length) / (this.data.length + 1)

    // 绘制Y轴刻度线（可选）
    this.context.strokeStyle = '#F0F0F0'
    this.context.lineWidth = 1
    for (let i = 0; i <= 6; i++) {
      const y = paddingTop + (chartHeight / 6) * i
      this.context.beginPath()
      this.context.moveTo(paddingLeft, y)
      this.context.lineTo(paddingLeft + chartWidth, y)
      this.context.stroke()
    }

    // 绘制柱子
    for (let i = 0; i < this.data.length; i++) {
      const x = paddingLeft + barSpacing * (i + 1) + barWidth * i
      const barHeight = (this.data[i] / this.maxValue) * chartHeight
      const y = paddingTop + chartHeight - barHeight

      // 绘制柱子
      this.context.fillStyle = this.barColor
      this.context.beginPath()
      this.context.roundRect(x, y, barWidth, barHeight, 4)
      this.context.fill()
    }

    // 绘制X轴标签
    if (this.labels.length > 0) {
      this.context.fillStyle = '#999999'
      this.context.font = '30px sans-serif'
      this.context.textAlign = 'center'

      for (let i = 0; i < this.labels.length; i++) {
        const x = paddingLeft + barSpacing * (i + 1) + barWidth * i + barWidth / 2
        const y = paddingTop + chartHeight + 15
        this.context.fillText(this.labels[i], x, y)
      }
    }
  }
}

@Entry
@Component
struct SportDataPage {
  @State currentTab: number = 0 // 0: 日, 1: 周, 2: 月
  @State todayScore: number = 18
  @State timeRemaining: string = '2h00m'
  @State todayPlan: number = 9

  // 柱状图数据（每3小时的数据点）
  @State chartData: number[] = [1, 4, 2, 3, 2, 1, 2, 0]

  // X轴标签
  private timeLabels: string[] = ['3时', '6时', '9时', '12时', '15时', '18时', '21时', '24时']

  build() {
    Column() {
      // 顶部标签栏
      Row() {
        Text('日')
          .fontSize(16)
          .fontColor(this.currentTab === 0 ? '#007DFF' : '#666666')
          .fontWeight(this.currentTab === 0 ? FontWeight.Bold : FontWeight.Normal)
          .onClick(() => {
            this.currentTab = 0
          })
          .width('33%')
          .textAlign(TextAlign.Center)
          .padding({ bottom: 8 })
          .border({
            width: { bottom: this.currentTab === 0 ? 2 : 0 },
            color: '#007DFF'
          })

        Text('周')
          .fontSize(16)
          .fontColor(this.currentTab === 1 ? '#007DFF' : '#666666')
          .fontWeight(this.currentTab === 1 ? FontWeight.Bold : FontWeight.Normal)
          .onClick(() => {
            this.currentTab = 1
          })
          .width('33%')
          .textAlign(TextAlign.Center)
          .padding({ bottom: 8 })
          .border({
            width: { bottom: this.currentTab === 1 ? 2 : 0 },
            color: '#007DFF'
          })

        Text('月')
          .fontSize(16)
          .fontColor(this.currentTab === 2 ? '#007DFF' : '#666666')
          .fontWeight(this.currentTab === 2 ? FontWeight.Bold : FontWeight.Normal)
          .onClick(() => {
            this.currentTab = 2
          })
          .width('33%')
          .textAlign(TextAlign.Center)
          .padding({ bottom: 8 })
          .border({
            width: { bottom: this.currentTab === 2 ? 2 : 0 },
            color: '#007DFF'
          })
      }
      .width('100%')
      .padding({ top: 10, left: 20, right: 20 })

      // 今日标题区域
      Row() {
        Text('<')
          .fontSize(20)
          .fontColor('#999999')
          .onClick(() => {
            console.log('前一天')
          })

        Blank()

        Text('今日')
          .fontSize(16)
          .fontColor('#333333')

        Blank()

        Text('>')
          .fontSize(20)
          .fontColor('#999999')
          .onClick(() => {
            console.log('后一天')
          })
      }
      .width('100%')
      .padding({ top: 20, left: 20, right: 20, bottom: 10 })

      // 主要数据展示区域
      Column() {
        Text('得分')
          .fontSize(14)
          .fontColor('#999999')
          .margin({ top: 10 })

        Row() {
          Text(this.todayScore.toString())
            .fontSize(60)
            .fontWeight(FontWeight.Bold)
            .fontColor('#333333')

          Text('项')
            .fontSize(20)
            .fontColor('#333333')
            .margin({ top: 30, left: 5 })
        }

        // 两个数据卡片
        Row() {
          // 距离下次
          Column() {
            Text(this.timeRemaining)
              .fontSize(20)
              .fontColor('#333333')
              .fontWeight(FontWeight.Medium)

            Text('距离下次')
              .fontSize(12)
              .fontColor('#999999')
              .margin({ top: 5 })
          }
          .width('45%')
          .alignItems(HorizontalAlign.Start)

          Blank()

          // 今日计划
          Column() {
            Text(this.todayPlan.toString())
              .fontSize(20)
              .fontColor('#333333')
              .fontWeight(FontWeight.Medium)

            Text('今日计划')
              .fontSize(12)
              .fontColor('#999999')
              .margin({ top: 5 })
          }
          .width('45%')
          .alignItems(HorizontalAlign.Start)
        }
        .width('100%')
        .margin({ top: 30 })
      }
      .width('100%')
      .padding({ left: 20, right: 20 })

      // 柱状图区域
      Column() {
        // Y轴标签
        Row() {
          // Y轴刻度
          Column() {
            Text('6')
              .fontSize(10)
              .fontColor('#CCCCCC')
            Text('5')
              .fontSize(10)
              .fontColor('#CCCCCC')
              .margin({ top: 20 })
            Text('4')
              .fontSize(10)
              .fontColor('#CCCCCC')
              .margin({ top: 20 })
            Text('3')
              .fontSize(10)
              .fontColor('#CCCCCC')
              .margin({ top: 20 })
            Text('2')
              .fontSize(10)
              .fontColor('#CCCCCC')
              .margin({ top: 20 })
            Text('1')
              .fontSize(10)
              .fontColor('#CCCCCC')
              .margin({ top: 20 })
          }
          .alignItems(HorizontalAlign.Start)
          .height(200)
          .justifyContent(FlexAlign.SpaceBetween)
          .margin({ right: 10 })

          // 柱状图
          McBarChart({
            data: this.chartData,
            chartWidth: 310,
            chartHeight: 230,
            barColor: '#4DA6FF',
            maxValue: 6,
            labels: this.timeLabels
          })
        }
        .width('100%')
      }
      .width('100%')
      .padding({ left: 20, right: 20, top: 30 })

      Blank()
    }
    .width('100%')
    .height('100%')
    .backgroundColor('#F5F5F5')
  }
}

