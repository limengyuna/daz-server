import http from '@ohos.net.http';

interface SleepData {
  date: string;
  sleep_hours: number;
}

interface SleepResult {
  sleep_log: SleepData[];
  objectId: string;
  createdAt: string;
  updatedAt: string;
}

interface SleepResponse {
  results: SleepResult[];
}

@Entry
@Component
struct SleepStatistics {
  @State selectedTab: number = 1; // 0: 日, 1: 周, 2: 月
  @State sleepData: SleepData[] = [];
  @State totalHours: number = 0;
  @State totalMinutes: number = 0;
  @State averageHours: number = 0;
  @State averageMinutes: number = 0;
  @State dateRange: string = '';
  @State maxSleepHours: number = 10;

  aboutToAppear() {
    this.fetchSleepData();
  }

  // 获取睡眠数据
  async fetchSleepData() {
    let httpRequest = http.createHttp();
    try {
      let response = await httpRequest.request(
        'https://api.leancloud.cn/1.1/classes/lmy_sleep',
        {
          method: http.RequestMethod.GET,
          header: {
            'X-LC-Id': 'Lk7Ea17CoVWpjbDiMn2Ajc0t-gzGzoHsz',
            'X-LC-Key': 'CzmXtXqi8Zf3pSLFPPaUGqd0',
            'Content-Type': 'application/json'
          }
        }
      );

      if (response.responseCode === 200) {
        const data: SleepResponse = JSON.parse(response.result.toString());
        console.info('Fetched sleep data:', JSON.stringify(data));
        if (data.results && data.results.length > 0) {
          this.sleepData = data.results[0].sleep_log;
          this.calculateStatistics();
          this.updateDateRange();
          console.info('Sleep data loaded, count:', this.sleepData.length);
        }
      }
    } catch (err) {
      console.error('Failed to fetch sleep data:', err);
    } finally {
      httpRequest.destroy();
    }
  }

  // 计算统计数据
  calculateStatistics() {
    if (this.sleepData.length === 0) return;

    let totalHours = 0;
    let maxHours = 0;
    
    this.sleepData.forEach(item => {
      totalHours += item.sleep_hours;
      if (item.sleep_hours > maxHours) {
        maxHours = item.sleep_hours;
      }
    });

    this.maxSleepHours = Math.ceil(maxHours);
    
    // 总时长
    this.totalHours = Math.floor(totalHours);
    this.totalMinutes = Math.round((totalHours - this.totalHours) * 60);

    // 平均时长
    const average = totalHours / this.sleepData.length;
    this.averageHours = Math.floor(average);
    this.averageMinutes = Math.round((average - this.averageHours) * 60);
  }

  // 更新日期范围
  updateDateRange() {
    if (this.sleepData.length === 0) return;
    
    const firstDate = this.sleepData[0].date;
    const lastDate = this.sleepData[this.sleepData.length - 1].date;
    
    const formatDate = (dateStr: string) => {
      const parts = dateStr.split('-');
      return `${parts[0]}.${parseInt(parts[1])}.${parseInt(parts[2])}`;
    };
    
    this.dateRange = `${formatDate(firstDate)}-${formatDate(lastDate)}`;
  }

  build() {
    Column() {
      // 顶部选项卡
      Row() {
        this.TabItem('日', 0);
        this.TabItem('周', 1);
        this.TabItem('月', 2);
      }
      .width('100%')
      .height(50)
      .justifyContent(FlexAlign.SpaceAround)

      // 日期范围
      Row() {
        Image($r('sys.symbol.chevron_left'))
          .width(20)
          .height(20)
          .fillColor('#999999')
          .onClick(() => {
            // 向前切换日期
          })

        Text(this.dateRange)
          .fontSize(14)
          .fontColor('#666666')
          .margin({ left: 20, right: 20 })

        Image($r('sys.symbol.chevron_right'))
          .width(20)
          .height(20)
          .fillColor('#999999')
          .onClick(() => {
            // 向后切换日期
          })
      }
      .width('100%')
      .height(50)
      .justifyContent(FlexAlign.Center)

      // 统计信息
      Column() {
        Text(`10月6日至10月13日累计`)
          .fontSize(14)
          .fontColor('#999999')
          .margin({ bottom: 10 })

        Text(`${this.totalHours}h${this.totalMinutes}m`)
          .fontSize(48)
          .fontWeight(FontWeight.Normal)
          .fontColor('#333333')
          .margin({ bottom: 30 })

        Column() {
          Text(`${this.averageHours}h${this.averageMinutes}m`)
            .fontSize(20)
            .fontColor('#333333')
          
          Text('平均/日')
            .fontSize(14)
            .fontColor('#999999')
            .margin({ top: 5 })
        }
        .alignItems(HorizontalAlign.Start)
      }
      .width('100%')
      .padding({ top: 20, bottom: 30 })
      .alignItems(HorizontalAlign.Start)
      .padding({ left: 30 })

      // 柱状图
      Column() {
        this.BarChart()
      }
      .width('100%')
      .height(300)
      .padding({ left: 30, right: 30 })
      .margin({ top: 80 })
    }
    .width('100%')
    .height('100%')
    .backgroundColor('#F5F5F5')
  }

  @Builder TabItem(title: string, index: number) {
    Column() {
      Text(title)
        .fontSize(16)
        .fontColor(this.selectedTab === index ? '#5B5FEF' : '#999999')
        .fontWeight(this.selectedTab === index ? FontWeight.Medium : FontWeight.Normal)
      
      if (this.selectedTab === index) {
        Divider()
          .width(20)
          .strokeWidth(2)
          .color('#5B5FEF')
          .margin({ top: 5 })
      }
    }
    .width('33%')
    .height('100%')
    .justifyContent(FlexAlign.Center)
    .onClick(() => {
      this.selectedTab = index;
    })
  }

  @Builder BarChart() {
    Column() {
      // 刻度和柱状图
      Row() {
        // Y轴刻度
        Column() {
          Text('12h')
            .fontSize(10)
            .fontColor('#CCCCCC')
          Blank()
          Text('10h')
            .fontSize(10)
            .fontColor('#CCCCCC')
          Blank()
          Text('8h')
            .fontSize(10)
            .fontColor('#CCCCCC')
          Blank()
          Text('6h')
            .fontSize(10)
            .fontColor('#CCCCCC')
          Blank()
          Text('4h')
            .fontSize(10)
            .fontColor('#CCCCCC')
          Blank()
          Text('2h')
            .fontSize(10)
            .fontColor('#CCCCCC')
        }
        .width(30)
        .height(220)
        .alignItems(HorizontalAlign.End)
        .justifyContent(FlexAlign.SpaceBetween)

        // 柱状图区域
        Row() {
          ForEach(this.sleepData, (item: SleepData, index: number) => {
            this.BarItem(item, index);
          }, (item: SleepData) => item.date)
        }
        .layoutWeight(1)
        .height(220)
        .justifyContent(FlexAlign.SpaceAround)
        .alignItems(VerticalAlign.Bottom)
        .margin({ left: 10 })
      }
      .width('100%')
      .height(220)

      // X轴日期标签
      Row() {
        Blank().width(40)
        Row() {
          ForEach(this.sleepData, (item: SleepData, index: number) => {
            Text(this.formatDateLabel(item.date))
              .fontSize(10)
              .fontColor('#CCCCCC')
              .layoutWeight(1)
              .textAlign(TextAlign.Center)
          }, (item: SleepData) => item.date)
        }
        .layoutWeight(1)
      }
      .width('100%')
      .margin({ top: 10 })
    }
  }

  @Builder BarItem(data: SleepData, index: number) {
    Column() {
      // 柱子
      Column()
        .width(30)
        .height((data.sleep_hours / 12) * 220)
        .backgroundColor('#5B5FEF')
        .borderRadius({ topLeft: 4, topRight: 4 })
    }
    .layoutWeight(1)
    .height(220)
    .justifyContent(FlexAlign.End)
  }

  formatDateLabel(dateStr: string): string {
    const parts = dateStr.split('-');
    return `${parseInt(parts[1])}.${parseInt(parts[2])}`;
  }
}

