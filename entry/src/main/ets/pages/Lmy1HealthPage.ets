import http from '@ohos.net.http';

interface DeviceInfo {
  name: string;
  battery: number;
}

interface SleepPhase {
  start: string;
  end: string;
  type: string;
}

interface SleepData {
  totalDurationMinutes: number;
  deepSleepMinutes: number;
  lightSleepMinutes: number;
  awakeMinutes: number;
  sleepEfficiency: number;
  sleepQualityRating: string;
  sleepPhases: SleepPhase[];
}

interface HeartRateLog {
  time: string;
  value: number;
}

interface HeartRateData {
  log: HeartRateLog[];
  averageHeartRate: number;
  maxHeartRate: number;
}

interface DueDate {
  __type: string;
  iso: string;
}

interface Task {
  title: string;
  due: DueDate;
  done: boolean;
}

interface HealthData {
  devicce: DeviceInfo;
  temperature: number;
  weather: string;
  today_step: string;
  today_target: string;
  how_long: string;
  active_time: string;
  pwoer: string;
  sleep: SleepData;
  heart_rate: HeartRateData;
  wait_task: Task[];
}

interface ApiResponse {
  results: HealthData[];
}

@Entry
@Component
export default  struct HealthPage {
  @State healthData: HealthData | null = null;
  @State isLoading: boolean = true;

  aboutToAppear() {
    this.fetchData();
  }

  fetchData() {
    let httpRequest = http.createHttp();
    httpRequest.request(
      "https://api.leancloud.cn/1.1/classes/hmkf",
      {
        method: http.RequestMethod.GET,
        header: {
          'X-LC-Id': 'Lk7Ea17CoVWpjbDiMn2Ajc0t-gzGzoHsz',
          'X-LC-Key': 'CzmXtXqi8Zf3pSLFPPaUGqd0',
          'Content-Type': 'application/json'
        }
      }
    ).then((data: http.HttpResponse) => {
      if (data.responseCode === 200) {
        const response: ApiResponse = JSON.parse(data.result.toString()) as ApiResponse;
        if (response.results && response.results.length > 0) {
          this.healthData = response.results[0];
        }
      }
      this.isLoading = false;
      httpRequest.destroy();
    }).catch((err: Error) => {
      console.error('请求失败：', JSON.stringify(err));
      this.isLoading = false;
      httpRequest.destroy();
    });
  }

  // 格式化睡眠时长
  formatSleepDuration(minutes: number): string {
    const hours = Math.floor(minutes / 60);
    const mins = minutes % 60;
    return `${hours}h${mins}m`;
  }

  // 格式化日期时间
  formatDateTime(isoString: string): string {
    const date = new Date(isoString);
    const month = date.getMonth() + 1;
    const day = date.getDate();
    const hours = date.getHours();
    const minutes = date.getMinutes();
    return `今天${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}`;
  }

  // 计算步数进度百分比
  getStepProgress(): number {
    if (!this.healthData) return 0;
    const steps = parseInt(this.healthData.today_step);
    const target = parseInt(this.healthData.today_target);
    return Math.min((steps / target) * 100, 100);
  }

  build() {
    Column() {
      if (this.isLoading) {
        Column() {
          LoadingProgress()
            .width(50)
            .height(50)
        }
        .width('100%')
        .height('100%')
        .justifyContent(FlexAlign.Center)
      } else if (this.healthData) {
        // 顶部状态栏
        Row() {
          Text(`⌚ ${this.healthData.devicce.name}`)
            .fontSize(14)
            .fontWeight(500)

          Text(`🔋 ${this.healthData.devicce.battery}%`)
            .fontSize(14)
            .margin({ left: 10 })

          Blank()

          Text(`☀️ ${this.healthData.temperature}°C ${this.healthData.weather}`)
            .fontSize(14)
        }
        .width('100%')
        .padding({ left: 16, right: 16, top: 10, bottom: 10 })
        .backgroundColor('#f5f5f5')

        Scroll() {
          Column() {
            // 步数圆环
            Column() {
              Stack() {
                Progress({
                  value: parseInt(this.healthData.today_step),
                  total: parseInt(this.healthData.today_target),
                  type: ProgressType.Ring
                })
                  .width(200)
                  .height(200)
                  .color('#3B9DFF')
                  .style({ strokeWidth: 15 })

                Column() {
                  Text('今日步数')
                    .fontSize(12)
                    .fontColor('#999')
                    .margin({ bottom: 5 })

                  Text(this.healthData.today_step)
                    .fontSize(48)
                    .fontWeight(FontWeight.Bold)
                    .fontColor('#333')

                  Text(`🎯 ${this.healthData.today_target}步`)
                    .fontSize(12)
                    .fontColor('#FF9966')
                    .margin({ top: 5 })
                }
              }
              .margin({ top: 30, bottom: 20 })

              Row() {
                Column() {
                  Text(this.healthData.how_long)
                    .fontSize(24)
                    .fontWeight(FontWeight.Bold)
                  Text('公里')
                    .fontSize(12)
                    .fontColor('#999')
                    .margin({ top: 4 })
                }
                .layoutWeight(1)

                Column() {
                  Text(this.healthData.active_time.replace('.', 'h') + 'm')
                    .fontSize(24)
                    .fontWeight(FontWeight.Bold)
                  Text('活跃时长')
                    .fontSize(12)
                    .fontColor('#999')
                    .margin({ top: 4 })
                }
                .layoutWeight(1)

                Column() {
                  Text(this.healthData.pwoer)
                    .fontSize(24)
                    .fontWeight(FontWeight.Bold)
                  Text('卡')
                    .fontSize(12)
                    .fontColor('#999')
                    .margin({ top: 4 })
                }
                .layoutWeight(1)
              }
              .width('100%')
              .padding({ left: 20, right: 20 })
            }
            .width('100%')

            // 睡眠数据卡片
            Column() {
              Row() {
                Text('🌙')
                  .fontSize(24)
                  .margin({ right: 10 })

                Column() {
                  Text('睡眠')
                    .fontSize(12)
                    .fontColor('#999')
                  Text(this.formatSleepDuration(this.healthData.sleep.totalDurationMinutes))
                    .fontSize(20)
                    .fontWeight(FontWeight.Bold)
                }
                .alignItems(HorizontalAlign.Start)

                Blank()

                // 睡眠阶段条形图
                Row() {
                  ForEach(this.healthData.sleep.sleepPhases, (phase: SleepPhase, index: number) => {
                    Column()
                      .width(8)
                      .height(phase.type === 'deep' ? 40 : phase.type === 'light' ? 30 : 20)
                      .backgroundColor(phase.type === 'deep' ? '#5B8FF9' :
                        phase.type === 'light' ? '#7DB3FF' : '#D3E5FF')
                      .margin({ left: index > 0 ? 4 : 0 })
                      .borderRadius(2)
                  }, (phase: SleepPhase, index: number) => index.toString())
                }
                .alignItems(VerticalAlign.Bottom)
              }
              .width('100%')
              .padding(16)
              .backgroundColor('#FFF')
              .borderRadius(12)
              .shadow({ radius: 8, color: '#f0f0f0' })
            }
            .width('90%')
            .margin({ top: 20, left: '5%', right: '5%' })

            Column() {
              Row() {
                Text('❤️')
                  .fontSize(24)
                  .margin({ right: 10 })

                Column() {
                  Text('心率')
                    .fontSize(12)
                    .fontColor('#999')
                  Text(`${this.healthData.heart_rate.averageHeartRate}bpm`)
                    .fontSize(20)
                    .fontWeight(FontWeight.Bold)
                }
                .alignItems(HorizontalAlign.Start)

                Blank()

                Row() {
                  ForEach(this.healthData.heart_rate.log, (log: HeartRateLog, index: number) => {
                    Column()
                      .width(3)
                      .height((log.value - 60) * 2)
                      .backgroundColor('#FF6B9D')
                      .margin({ left: index > 0 ? 6 : 0 })
                      .borderRadius(2)
                  }, (log: HeartRateLog) => log.time)
                }
                .height(40)
                .alignItems(VerticalAlign.Bottom)
              }
              .width('100%')
              .padding(16)
              .backgroundColor('#FFF')
              .borderRadius(12)
              .shadow({ radius: 8, color: '#f0f0f0' })
            }
            .width('90%')
            .margin({ top: 15, left: '5%', right: '5%' })

            // 待办事项
            Column() {
              Row() {
                Text('待办事项')
                  .fontSize(16)
                  .fontWeight(FontWeight.Bold)
                Text(this.healthData.wait_task.filter(t => !t.done).length.toString())
                  .fontSize(14)
                  .fontColor('#999')
                  .margin({ left: 5 })

                Blank()

                Text('+ 新增')
                  .fontSize(14)
                  .fontColor('#3B9DFF')
              }
              .width('100%')
              .margin({ bottom: 15 })

              Column() {
                ForEach(this.healthData.wait_task, (task: Task, index: number) => {
                  Row() {
                    Checkbox()
                      .select(task.done)
                      .selectedColor('#FF9966')
                      .width(20)
                      .height(20)

                    Column() {
                      Text(task.title)
                        .fontSize(14)
                        .fontColor(task.done ? '#999' : '#333')
                        .decoration({ type: task.done ? TextDecorationType.LineThrough : TextDecorationType.None })

                      Text(this.formatDateTime(task.due.iso))
                        .fontSize(12)
                        .fontColor('#999')
                        .margin({ top: 2 })
                    }
                    .alignItems(HorizontalAlign.Start)
                    .margin({ left: 12 })
                    .layoutWeight(1)
                  }
                  .width('100%')
                  .padding({ top: 12, bottom: 12 })
                  .borderRadius(8)
                  .backgroundColor('#FAFAFA')
                  .margin({ bottom: index < this.healthData!.wait_task.length - 1 ? 10 : 0 })
                }, (task: Task, index: number) => index.toString())
              }
            }
            .width('90%')
            .margin({ top: 20, left: '5%', right: '5%', bottom: 20 })
          }
        }
        .scrollBar(BarState.Auto)
        .layoutWeight(1)
      } else {
        Column() {
          Text('加载失败')
            .fontSize(16)
            .fontColor('#999')
        }
        .width('100%')
        .height('100%')
        .justifyContent(FlexAlign.Center)
      }
    }
    .width('100%')
    .height('100%')
    .backgroundColor('#F5F5F5')
  }
}

