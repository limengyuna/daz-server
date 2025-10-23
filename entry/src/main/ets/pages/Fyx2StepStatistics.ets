import http from '@ohos.net.http';

// 步数数据接口定义
interface StepData {
  date: string;
  steps: number;
}

interface StepResult {
  step_log: StepData[];
  objectId: string;
  createdAt: string;
  updatedAt: string;
}

interface StepResponse {
  results: StepResult[];
}

@Entry
@Component
struct StepStatistics {
  @State selectedTab: number = 1; // 0: 日, 1: 周, 2: 月
  @State stepData: StepData[] = [];
  @State totalSteps: number = 0;
  @State averageSteps: number = 0;
  @State dateRange: string = '';
  @State maxSteps: number = 12000;

  aboutToAppear() {
    this.fetchStepData();
  }

  // 获取步数数据
  async fetchStepData() {
    let httpRequest = http.createHttp();
    try {
      let response = await httpRequest.request(
        'https://api.leancloud.cn/1.1/classes/fyx_step',
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
        const data: StepResponse = JSON.parse(response.result.toString());
        console.info('Fetched step data:', JSON.stringify(data));
        if (data.results && data.results.length > 0) {
          this.stepData = data.results[0].step_log;
          this.calculateStatistics();
          this.updateDateRange();
          console.info('Step data loaded, count:', this.stepData.length);
        }
      }
    } catch (err) {
      console.error('Failed to fetch step data:', err);
    } finally {
      httpRequest.destroy();
    }
  }

  // 计算统计数据
  calculateStatistics() {
    if (this.stepData.length === 0) return;

    let totalSteps = 0;
    let maxSteps = 0;
    
    this.stepData.forEach(item => {
      totalSteps += item.steps;
      if (item.steps > maxSteps) {
        maxSteps = item.steps;
      }
    });

    this.totalSteps = totalSteps;
    this.maxSteps = Math.ceil(maxSteps / 2000) * 2000; // 向上取整到2000的倍数
    
    // 平均步数
    this.averageSteps = Math.round(totalSteps / this.stepData.length);
  }

  // 更新日期范围
  updateDateRange() {
    if (this.stepData.length === 0) return;
    
    const firstDate = this.stepData[0].date;
    const lastDate = this.stepData[this.stepData.length - 1].date;
    
    const formatDate = (dateStr: string) => {
      const parts = dateStr.split('-');
      return `${parts[0]}.${parseInt(parts[1])}.${parseInt(parts[2])}`;
    };
    
    this.dateRange = `${formatDate(firstDate)}-${formatDate(lastDate)}`;
  }

  formatDateLabel(dateStr: string): string {
    const parts = dateStr.split('-');
    return `${parseInt(parts[1])}.${parseInt(parts[2])}`;
  }

  // 格式化累计日期范围显示
  getAccumulatedDateText(): string {
    if (this.stepData.length === 0) return '';
    
    const firstDate = this.stepData[0].date;
    const lastDate = this.stepData[this.stepData.length - 1].date;
    
    const formatDateChinese = (dateStr: string) => {
      const parts = dateStr.split('-');
      return `${parseInt(parts[1])}月${parseInt(parts[2])}日`;
    };
    
    return `${formatDateChinese(firstDate)}至${formatDateChinese(lastDate)}累计`;
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
      .backgroundColor('#FFFFFF')

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
      .backgroundColor('#FFFFFF')

      // 统计信息
      Column() {
        Text(this.getAccumulatedDateText())
          .fontSize(14)
          .fontColor('#999999')
          .margin({ bottom: 10 })

        Row() {
          Text(this.totalSteps.toLocaleString())
            .fontSize(48)
            .fontWeight(FontWeight.Bold)
            .fontColor('#333333')
          
          Text(' 步')
            .fontSize(20)
            .fontColor('#333333')
            .margin({ top: 20 })
        }
        .margin({ bottom: 30 })

        Row() {
          Text(`${this.averageSteps.toLocaleString()} 步`)
            .fontSize(20)
            .fontColor('#333333')
          
          Text('平均/日')
            .fontSize(14)
            .fontColor('#999999')
            .margin({ left: 10 })
        }
      }
      .width('100%')
      .padding({ top: 20, bottom: 30, left: 30 })
      .alignItems(HorizontalAlign.Start)
      .backgroundColor('#FFFFFF')

      // 柱状图区域
      Column() {
        this.BarChart()
      }
      .width('100%')
      .height(300)
      .padding({ left: 30, right: 30 })
      .backgroundColor('#FFFFFF')

      Blank()
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
          Text('12k')
            .fontSize(11)
            .fontColor('#666666')
            .fontWeight(FontWeight.Medium)
          Blank()
          Text('10k')
            .fontSize(11)
            .fontColor('#666666')
            .fontWeight(FontWeight.Medium)
          Blank()
          Text('8k')
            .fontSize(11)
            .fontColor('#666666')
            .fontWeight(FontWeight.Medium)
          Blank()
          Text('6k')
            .fontSize(11)
            .fontColor('#666666')
            .fontWeight(FontWeight.Medium)
          Blank()
          Text('4k')
            .fontSize(11)
            .fontColor('#666666')
            .fontWeight(FontWeight.Medium)
          Blank()
          Text('2k')
            .fontSize(11)
            .fontColor('#666666')
            .fontWeight(FontWeight.Medium)
        }
        .width(30)
        .height(220)
        .alignItems(HorizontalAlign.End)
        .justifyContent(FlexAlign.SpaceBetween)

        // 柱状图区域
        Row() {
          ForEach(this.stepData, (item: StepData, index: number) => {
            this.BarItem(item, index);
          }, (item: StepData) => item.date)
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
          ForEach(this.stepData, (item: StepData, index: number) => {
            Text(this.formatDateLabel(item.date))
              .fontSize(11)
              .fontColor('#666666')
              .fontWeight(FontWeight.Medium)
              .layoutWeight(1)
              .textAlign(TextAlign.Center)
          }, (item: StepData) => item.date)
        }
        .layoutWeight(1)
      }
      .width('100%')
      .margin({ top: 10 })
    }
  }

  @Builder BarItem(data: StepData, index: number) {
    Column() {
      Column()
        .width(20)
        .height((data.steps / this.maxSteps) * 220)
        .backgroundColor('#00D178')
        .borderRadius({ topLeft: 8, topRight: 8 })
    }
    .layoutWeight(1)
    .height(220)
    .justifyContent(FlexAlign.End)
  }
}

