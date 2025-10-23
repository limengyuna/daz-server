import http from '@ohos.net.http';

// 定义目标步数接口结构
interface GoalData {
  today_target: string;
}

interface ApiResponse {
  results: GoalData[];
}

@Entry
@Component
struct GoalSetting {
  @State goalData: GoalData | null = null;
  @State isLoading: boolean = true;

  aboutToAppear() {
    this.fetchGoalData();
  }

  // 调用接口获取目标步数数据
  fetchGoalData() {
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
      }, (err, data) => {
      if (err) {
        console.error('目标步数数据请求失败：', JSON.stringify(err));
        this.isLoading = false;
        httpRequest.destroy();
        return;
      }

      if (data && data.responseCode === 200) {
        try {
          const response: ApiResponse = JSON.parse(data.result as string) as ApiResponse;
          if (response.results && response.results.length > 0) {
            this.goalData = response.results[0];
          }
        } catch (error) {
          console.error('JSON解析失败：', error);
        }
      } else {
        console.error('请求失败，状态码：', data?.responseCode);
      }
      this.isLoading = false;
      httpRequest.destroy();
    }
    );
  }

  build() {
    Column() {
      // 顶部导航栏
      Row() {
        Image($r('app.media.return'))
          .width(28)
          .height(20)
          .margin({ left: 16 })
        Text('设置目标')
          .fontSize(20)
          .fontWeight(FontWeight.Bold)
        Text('保存')
          .fontSize(16)
          .fontColor('#3B9DFF')
          .margin({ right: 16 })
      }
      .width('100%')
      .height(60)
      .justifyContent(FlexAlign.SpaceBetween)
      .alignItems(VerticalAlign.Center)

      // 中间内容区
      Column({space:20}) {
        Image($r('app.media.goal'))
          .width(120)
          .height(90)
          .fillColor('#FF9966')
          .margin({ top: 20, bottom: 8 })
        Text('每日目标步数')
          .fontSize(14)
          .fontColor('#999')
        Row() {
          Text(this.isLoading ? '加载中...' : (this.goalData?.today_target || '0'))
            .fontSize(40)
          Text('步')
            .fontSize(16)
            .fontColor('#999')
            .margin({ left: 4 })
        }
        .alignItems(VerticalAlign.Bottom)
      }
      .width('100%')
      .alignItems(HorizontalAlign.Center)
      .flexGrow(1)
    }
    .width('100%')
    .height('100%')
    .backgroundColor('#FFFFFF')
    .padding({ top: 16, bottom: 16 })
  }
}