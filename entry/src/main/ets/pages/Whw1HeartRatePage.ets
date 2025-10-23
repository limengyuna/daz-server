import http from '@ohos.net.http'

// 自定义折线图组件
@Component
struct McLineChart {
  @Prop data: number[] = []
  @Prop chartWidth: number = 350
  @Prop chartHeight: number = 320 // <-- [FIX] 保持增加的高度以容纳更大的字体
  @Prop lineColor: string = '#FF6B6B'
  @Prop fillColor: string = '#FFF0F0'
  @Prop selectedIndex: number = -1
  @Prop showTooltip: boolean = true
  @Prop tooltipTime: string = ''
  @Prop tooltipValue: number = 0
  @Prop timeLabels: string[] = []

  private context: CanvasRenderingContext2D = new CanvasRenderingContext2D(new RenderingContextSettings(true))

  build() {
    Canvas(this.context)
      .width(this.chartWidth)
      .height(this.chartHeight)
      .onReady(() => {
        this.drawChart() // 仅在 onReady 时绘制
      })
  }

  drawChart() {
    if (!this.context) {
      return
    }
    this.context.clearRect(0, 0, this.chartWidth, this.chartHeight)

    if (this.data.length === 0) {
      return
    }

    const padding = 40
    const chartWidth = this.chartWidth - padding * 2
    // <-- [FIX] 调整 Y 轴 padding
    const chartHeight = this.chartHeight - padding * 2.5
    const startX = padding
    const startY = padding

    const maxValue = Math.max(...this.data)
    const minValue = Math.min(...this.data)
    const valueRange = maxValue - minValue || 1

    // 绘制背景填充区域
    this.context.fillStyle = this.fillColor
    this.context.beginPath()
    this.context.moveTo(startX, startY + chartHeight)
    for (let i = 0; i < this.data.length; i++) {
      const x = startX + (i / (this.data.length - 1)) * chartWidth
      const y = startY + chartHeight - ((this.data[i] - minValue) / valueRange) * chartHeight
      this.context.lineTo(x, y)
    }
    this.context.lineTo(startX + chartWidth, startY + chartHeight)
    this.context.closePath()
    this.context.fill()

    // 绘制折线
    this.context.strokeStyle = this.lineColor
    this.context.lineWidth = 2
    this.context.beginPath()
    for (let i = 0; i < this.data.length; i++) {
      const x = startX + (i / (this.data.length - 1)) * chartWidth
      const y = startY + chartHeight - ((this.data[i] - minValue) / valueRange) * chartHeight
      if (i === 0) {
        this.context.moveTo(x, y)
      } else {
        this.context.lineTo(x, y)
      }
    }
    this.context.stroke()

    // 绘制数据点
    for (let i = 0; i < this.data.length; i++) {
      const x = startX + (i / (this.data.length - 1)) * chartWidth
      const y = startY + chartHeight - ((this.data[i] - minValue) / valueRange) * chartHeight
      this.context.beginPath()
      this.context.arc(x, y, 4, 0, 2 * Math.PI)
      this.context.fillStyle = '#FFFFFF'
      this.context.fill()
      this.context.strokeStyle = this.lineColor
      this.context.lineWidth = 2
      this.context.stroke()
    }


    // 绘制选中点及提示框
    if (this.selectedIndex >= 0 && this.selectedIndex < this.data.length && this.showTooltip) {
      const selectedX = startX + (this.selectedIndex / (this.data.length - 1)) * chartWidth
      const selectedY = startY + chartHeight - ((this.data[this.selectedIndex] - minValue) / valueRange) * chartHeight
      // 垂直虚线
      this.context.setLineDash([5, 5])
      this.context.strokeStyle = '#CCCCCC'
      this.context.lineWidth = 1
      this.context.beginPath()
      this.context.moveTo(selectedX, startY)
      this.context.lineTo(selectedX, startY + chartHeight)
      this.context.stroke()
      this.context.setLineDash([])
      // 选中点
      this.context.beginPath()
      this.context.arc(selectedX, selectedY, 6, 0, 2 * Math.PI)
      this.context.fillStyle = this.lineColor
      this.context.fill()

      // <-- [FIX] 保持提示框高度
      const tooltipWidth = 140
      const tooltipHeight = 50
      let tooltipX = selectedX + 20
      let tooltipY = selectedY - (tooltipHeight + 5) // 向上偏移

      // 防止提示框超出画布
      if (tooltipX + tooltipWidth > this.chartWidth) {
        tooltipX = selectedX - tooltipWidth - 20
      }
      if (tooltipY < 0) {
        tooltipY = selectedY + 20
      }
      this.context.fillStyle = '#FFFFFF'
      this.context.strokeStyle = '#EEEEEE'
      this.context.lineWidth = 1
      this.context.fillRect(tooltipX, tooltipY, tooltipWidth, tooltipHeight)
      this.context.strokeRect(tooltipX, tooltipY, tooltipWidth, tooltipHeight)

      // <-- [FIX] 保持字体大小和位置
      this.context.fillStyle = '#999999'
      this.context.font = '12px sans-serif'
      this.context.fillText(this.tooltipTime, tooltipX + 8, tooltipY + 18)

      this.context.fillStyle = this.lineColor
      this.context.font = 'bold 16px sans-serif'
      this.context.fillText(this.tooltipValue + 'bpm', tooltipX + 8, tooltipY + 38)
    }

    // 绘制X轴标签
    if (this.timeLabels.length > 0) {
      // <-- [!!!! 修复点 !!!!] 调整字体大小和位置
      this.context.fillStyle = '#999999'
      this.context.font = '50px sans-serif' // 字体改大 (从 12px -> 14px)
      const labelCount = this.timeLabels.length
      const dataCount = this.data.length
      if (labelCount === dataCount) {
        for (let i = 0; i < labelCount; i++) {
          const x = startX + (i / (labelCount - 1)) * chartWidth
          this.context.textAlign = 'center'
          // 增加 Y 轴偏移 (从 25 -> 30)
          this.context.fillText(this.timeLabels[i], x, startY + chartHeight + 30)
        }
      } else {
        // 备用逻辑 (基本不会触发)
        for (let i = 0; i < this.timeLabels.length; i++) {
          const x = startX + (i / (this.timeLabels.length - 1)) * chartWidth
          this.context.fillText(this.timeLabels[i], x - 10, startY + chartHeight + 30)
        }
      }
    }
  }
}

// LeanCloud 数据接口 (不变)
interface HeartRateLog {
  time: string;
  value: number;
}
interface HeartRateData {
  log: HeartRateLog[];
  averageHeartRate: number;
  maxHeartRate: number;
}
interface HealthData {
  heart_rate: HeartRateData;
  objectId: string;
}
interface ApiResponse {
  results: HealthData[];
}

@Entry
@Component
struct HeartRatePage {
  @State currentTab: number = 0 // 0:日, 1:周, 2:月
  @State currentHeartRate: number = 86
  @State maxHeartRate: number = 98
  @State minHeartRate: number = 60
  @State currentDate: string = '今日，10月6日'
  @State isLoading: boolean = true

  @State heartRateData: number[] = []
  @State selectedPointIndex: number = -1
  @State selectedPointValue: number = 0
  @State selectedPointTime: string = ''
  @State timeLabels: string[] = []

  // <-- [FIX 1] (保持) 添加一个变量来持有网络请求
  private httpRequest: http.HttpRequest | null = null

  aboutToAppear() {
    this.onTabChange(0) // 默认加载“日”数据
  }

  // '日' 视图数据加载 (LeanCloud)
  loadDayDataFromLeanCloud() {
    this.isLoading = true
    this.heartRateData = [] // 开始加载时，清空旧数据

    // <-- [FIX 2] (保持) 创建请求并赋值给 this.httpRequest
    this.httpRequest = http.createHttp();
    this.httpRequest.request(
      "https://api.leancloud.cn/1.1/classes/HealthData", // 确保 Class Name 是 "HealthData"
      {
        method: http.RequestMethod.GET,
        header: {
          'X-LC-Id': 'vX1PMs0sqAz1LCAJNDpjAG41-gzGzoHsz',
          'X-LC-Key': 'FnJ1vyPoVUAESyuUCOH9Ahuj',
          'Content-Type': 'application/json'
        }
      }
    ).then((data: http.HttpResponse) => {
      // (保持) 双重保险检查，防止已取消的请求覆盖新数据
      if (this.currentTab !== 0) {
        console.warn('LeanCloud "日" 数据返回了，但用户已切换。')
        return; // 忽略
      }

      if (data.responseCode === 200) {
        const response: ApiResponse = JSON.parse(data.result.toString()) as ApiResponse;
        if (response.results && response.results.length > 0) {
          const heartData = response.results[0].heart_rate;
          this.heartRateData = heartData.log.map((item: HeartRateLog) => item.value)
          this.timeLabels = heartData.log.map((item: HeartRateLog) => item.time.replace(':00', '时'))
          this.currentHeartRate = heartData.averageHeartRate
          this.maxHeartRate = heartData.maxHeartRate
          this.minHeartRate = Math.min(...this.heartRateData)
          this.currentDate = '今日，10月6日'

          if (this.heartRateData.length > 4) {
            this.selectedPointIndex = 4
            this.selectedPointValue = this.heartRateData[4]
            this.selectedPointTime = '2018年10月12日13:20'
          }

        } else {
          console.error('LeanCloud data query succeeded, but results array is empty.')
        }
      } else {
        console.error(`LeanCloud request failed with code: ${data.responseCode}`)
      }
      this.isLoading = false
      this.httpRequest = null; // <-- [FIX 3] (保持) 清理请求
    }).catch((err: Error) => {
      if (err.message.includes('abort')) {
        console.log('HTTP request was cancelled by user switching tabs.')
      } else {
        console.error('请求失败：', JSON.stringify(err));
      }
      // 仅当用户仍在“日”标签页时才停止加载
      if (this.currentTab === 0) {
        this.isLoading = false
      }
      this.httpRequest = null; // <-- [FIX 4] (保持) 清理请求
    });
  }

  // '周' 和 '月' 视图假数据 (不变)
  loadFakeData(index: number) {
    this.isLoading = false
    if (index === 1) { // 周
      this.heartRateData = [80, 85, 78, 90, 82, 88, 76]
      this.timeLabels = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
      this.currentHeartRate = 84
      this.maxHeartRate = 90
      this.minHeartRate = 76
      this.currentDate = '本周, 10月1日 - 10月7日'
      this.selectedPointIndex = 3
      this.selectedPointValue = 90
      this.selectedPointTime = '2018年10月4日 10:00'
    } else { // 月
      this.heartRateData = [85, 88, 82, 90, 87, 83, 85, 88, 86, 84]
      this.timeLabels = ['1', '4', '7', '10', '13', '16', '19', '22', '25', '28']
      this.currentHeartRate = 86
      this.maxHeartRate = 90
      this.minHeartRate = 82
      this.currentDate = '本月, 10月'
      this.selectedPointIndex = 3
      this.selectedPointValue = 90
      this.selectedPointTime = '2018年10月10日 15:30'
    }
  }

  // 总的数据加载切换函数
  onTabChange(index: number) {
    // <-- [FIX 5] (保持) 核心修复：取消任何正在进行的请求
    if (this.httpRequest) {
      this.httpRequest.destroy(); // 立即取消
      this.httpRequest = null;
      console.log('Previous HTTP request cancelled.')
    }

    this.currentTab = index
    if (index === 0) {
      this.loadDayDataFromLeanCloud()
    } else {
      this.loadFakeData(index)
    }
  }

  build() {
    Column() {
      // 顶部时间维度选择器
      Row() {
        this.TabButton('日', 0)
        this.TabButton('周', 1)
        this.TabButton('月', 2)
      }
      .width('100%')
      .justifyContent(FlexAlign.SpaceEvenly)
      .padding({ top: 20, bottom: 10 })

      // 日期导航
      Row() {
        Image($r('sys.symbol.chevron_left'))
          .width(20)
          .height(20)
          .fillColor('#999999')
        Text('今日')
          .fontSize(16)
          .fontColor('#333333')
          .margin({ left: 10, right: 10 })
        Image($r('sys.symbol.chevron_right'))
          .width(20)
          .height(20)
          .fillColor('#999999')
      }
      .width('100%')
      .justifyContent(FlexAlign.Center)
      .padding({ top: 10, bottom: 20 })
      .visibility(this.currentTab === 0 ? Visibility.Visible : Visibility.Hidden)

      // 当前日期
      Text(this.currentDate)
        .fontSize(14)
        .fontColor('#999999')
        .width('90%')
        .margin({ bottom: 10 })

      // 当前心率显示
      Row() {
        Text(this.currentHeartRate + 'bpm')
          .fontSize(60)
          .fontWeight(FontWeight.Medium)
          .fontColor('#333333')
        Blank()
        Button('测试')
          .fontSize(16)
          .fontColor('#FF6B6B')
          .backgroundColor('#FFF5F5')
          .borderRadius(20)
          .padding({ left: 20, right: 20, top: 8, bottom: 8 })
          .onClick(() => {
            this.currentHeartRate = Math.floor(Math.random() * 40) + 60
          })
      }
      .width('90%')
      .alignItems(VerticalAlign.Center)
      .margin({ bottom: 30 })

      // 最高和最低心率
      Row() {
        Column() {
          Text(this.maxHeartRate + 'bpm')
            .fontSize(20)
            .fontWeight(FontWeight.Medium)
            .fontColor('#333333')
          Text('最高')
            .fontSize(14)
            .fontColor('#999999')
            .margin({ top: 5 })
        }
        .alignItems(HorizontalAlign.Start)
        Blank()
        Column() {
          Text(this.minHeartRate + 'bpm')
            .fontSize(20)
            .fontWeight(FontWeight.Medium)
            .fontColor('#333333')
          Text('最低')
            .fontSize(14)
            .fontColor('#999999')
            .margin({ top: 5 })
        }
        .alignItems(HorizontalAlign.Start)
      }
      .width('90%')
      .margin({ bottom: 30 })


      // 心率折线图
      Column() {
        // <-- [FIX 6] (保持) 简化的 Loading 逻辑
        if (this.isLoading) {
          LoadingProgress()
            .width(50)
            .height(50)
            .margin({ top: 80, bottom: 80 })
        } else {
          McLineChart({
            data: this.heartRateData,
            chartWidth: 350,
            chartHeight: 320, // <-- [FIX 7] (保持) 匹配组件的新高度
            lineColor: '#FF6B6B',
            fillColor: '#FFF0F0',
            selectedIndex: this.selectedPointIndex,
            showTooltip: false,
            tooltipTime: this.selectedPointTime,
            tooltipValue: this.selectedPointValue,
            timeLabels: this.timeLabels
          })
            .key(this.currentTab.toString()) // (保持) .key() 强制刷新
        }
      }
      .width('90%')
      .height(320) // <-- [FIX 8] (保持) 匹配组件的新高度
      .justifyContent(FlexAlign.Center)

    }
    .width('100%')
    .height('100%')
    .backgroundColor('#FFFFFF')
  }

  // TabButton 构造器
  @Builder TabButton(text: string, index: number) {
    Column() {
      Text(text)
        .fontSize(16)
        .fontColor(this.currentTab === index ? '#333333' : '#999999')
        .fontWeight(this.currentTab === index ? FontWeight.Medium : FontWeight.Normal)
        .onClick(() => {
          this.onTabChange(index) // 调用切换函数
        })

      // 下划线
      if (this.currentTab === index) {
        Divider()
          .width(20)
          .strokeWidth(2)
          .color('#1E90FF')
          .margin({ top: 5 })
      }
    }
  }
}