import http from '@ohos.net.http';

// 定义步数数据接口结构
interface StepData {
  today_target: string;
}

interface ApiResponse {
  results: StepData[];
}

@Entry
@Component
struct MyHomepage {
  @State stepData: StepData | null = null;
  @State isLoading: boolean = true;

  aboutToAppear() {
    this.fetchStepData();
  }

  // 调用接口获取步数数据
  fetchStepData() {
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
          this.stepData = response.results[0];
        }
      }
      this.isLoading = false;
      httpRequest.destroy();
    }).catch((err: Error) => {
      console.error('步数数据请求失败：', JSON.stringify(err));
      this.isLoading = false;
      httpRequest.destroy();
    });
  }

  build() {
    Column({ space: 0 }) {
      // 头部区域
      Stack() {
        Flex({ direction: FlexDirection.Column, alignItems: ItemAlign.Center, justifyContent: FlexAlign.Center }) {
          Image($r("app.media.avatar"))
            .width(80)
            .height(80)
            .borderRadius(40)
            .margin({ bottom: 15 });
          Row() {
            Text('Remind')
              .fontSize(20)
              .fontWeight(FontWeight.Bold)
              .fontColor(Color.White);
            Image($r("app.media.edit"))
              .width(16)
              .height(16)
              .margin({ left: 5 });
          }
          .alignItems(VerticalAlign.Center)
        }
        .width('100%')
        .height(200)
        .backgroundColor('#3B9DFF');
      }
      .width('100%')
      .height(200)

      // 运动目标区域
      Column({ space: 0 }) {
        // 运动目标标题
        Row() {
          Text('运动目标')
            .fontSize(14)
            .fontColor('#999')
        }
        .width('100%')
        .justifyContent(FlexAlign.Start)
        .padding({ left: 20, top: 20, bottom: 5 })

        // 步数显示行
        Row() {
          Row() {
            Text(this.isLoading ? '加载中...' : (this.stepData?.today_target || '0'))
              .fontSize(36)
              .fontColor('#333');
            Text('步')
              .fontSize(20)
              .fontColor('#333')
              .margin({ left: 5, bottom: 3 });
          }
          .alignItems(VerticalAlign.Bottom)

          Image($r("app.media.right_black"))
            .width(10)
            .height(10)
            .margin({ right: 20 });
        }
        .width('100%')
        .padding({ bottom: 15, left: 20 })
        .alignItems(VerticalAlign.Center)
        .justifyContent(FlexAlign.SpaceBetween)
        .border({ width: { bottom: 1 }, color: '#eee' });
      }

      // 我的设备区域
      Column({ space: 0 }) {
        // 我的设备标题
        Row() {
          Text('我的设备')
            .fontSize(14)
            .fontColor('#999')
          Text('+ 添加设备')
            .fontSize(14)
            .fontColor('#3B9DFF')
        }
        .width('100%')
        .alignItems(VerticalAlign.Center)
        .justifyContent(FlexAlign.SpaceBetween)
        .padding({ left: 20, right: 20, top: 20, bottom: 10 })

        // 设备项1 - 添加实线蓝色边框
        Row() {
          Row() {
            Image($r("app.media.watch_color"))
              .width(30)
              .height(30)
              .margin({ left: 20, right: 10 });
            Text('Glide Watch one')
              .fontSize(16)
              .fontColor('#333');
          }
          .alignItems(VerticalAlign.Center)
          Text('45%')
            .fontSize(14)
            .fontColor('#999')
            .margin({ right: 20 });
        }
        .width('90%')  // 缩短宽度为90%
        .height(60)
        .backgroundColor('#f5faff')
        .borderRadius(8)
        .margin({ bottom: 10 })
        .alignItems(VerticalAlign.Center)
        .justifyContent(FlexAlign.SpaceBetween)
        .border({ width: 2, color: '#3B9DFF' })
        .align(Alignment.Center);  // 水平居中

        // 设备项2 - 添加虚线边框
        Row() {
          Row() {
            Image($r("app.media.watch_blackWhite"))
              .width(30)
              .height(30)
              .margin({ left: 20, right: 10 });
            Text('Glide Watch s1')
              .fontSize(16)
              .fontColor('#333');
          }
          .alignItems(VerticalAlign.Center)
          Image($r("app.media.right_black"))
            .width(10)
            .height(10)
            .margin({ right: 20 });
        }
        .width('90%')  // 缩短宽度为90%
        .height(60)
        .backgroundColor('#f9f9f9')
        .borderRadius(8)
        .margin({ bottom: 20 })
        .alignItems(VerticalAlign.Center)
        .justifyContent(FlexAlign.SpaceBetween)
        .border({ width: 1, color: '#999', style: BorderStyle.Dashed })
        .align(Alignment.Center);  // 水平居中
      }

      // 设置项区域
      Column({ space: 0 }) {
        // 单位设置
        Row() {
          Text('单位设置')
            .fontSize(16)
            .fontColor('#333')
            .margin({ left: 20 });
          Image($r("app.media.right_black"))
            .width(10)
            .height(10)
            .margin({ right: 20 });
        }
        .width('100%')
        .height(60)
        .alignItems(VerticalAlign.Center)
        .justifyContent(FlexAlign.SpaceBetween)
        .border({ width: { bottom: 1 }, color: '#eee' });

        // 第三方接入
        Row() {
          Text('第三方接入')
            .fontSize(16)
            .fontColor('#333')
            .margin({ left: 20 });
          Image($r("app.media.right_black"))
            .width(10)
            .height(10)
            .margin({ right: 20 });
        }
        .width('100%')
        .height(60)
        .alignItems(VerticalAlign.Center)
        .justifyContent(FlexAlign.SpaceBetween)
        .border({ width: { bottom: 1 }, color: '#eee' });

        // 语言设置
        Row() {
          Text('语言设置')
            .fontSize(16)
            .fontColor('#333')
            .margin({ left: 20 });
          Row() {
            Text('中文')
              .fontSize(14)
              .fontColor('#3B9DFF')
              .margin({ right: 10 });
            Image($r("app.media.right_black"))
              .width(10)
              .height(10)
              .margin({ right: 20 });
          }
          .alignItems(VerticalAlign.Center)
        }
        .width('100%')
        .height(60)
        .alignItems(VerticalAlign.Center)
        .justifyContent(FlexAlign.SpaceBetween)
        .border({ width: { bottom: 1 }, color: '#eee' });

        // 意见反馈
        Row() {
          Text('意见反馈')
            .fontSize(16)
            .fontColor('#333')
            .margin({ left: 20 });
          Image($r("app.media.right_black"))
            .width(10)
            .height(10)
            .margin({ right: 20 });
        }
        .width('100%')
        .height(60)
        .alignItems(VerticalAlign.Center)
        .justifyContent(FlexAlign.SpaceBetween);
      }
    }
    .width('100%')
    .height('100%')
    .backgroundColor('#fff');
  }
}