@Entry
@Preview
@Component
struct ProfilePage {
  build() {
    Column() {
      // 1. 顶部导航栏 (App Bar)
      Row() {
        // ... (导航栏代码是正确的，保持不变) ...
        Text('保存')
          .fontSize(16)
          .fontColor(Color.White)
          .visibility(Visibility.Hidden)
          .padding({ left: 15 })

        Text('个人信息')
          .fontSize(18)
          .fontWeight(FontWeight.Bold)

        Text('保存')
          .fontSize(16)
          .fontColor('#007DFF')
          .padding({ right: 15 })
          .onClick(() => {
            console.info('点击了保存');
          })
      }
      .width('100%')
      .height(56)
      .backgroundColor(Color.White)
      .justifyContent(FlexAlign.SpaceBetween)
      .alignItems(VerticalAlign.Center)
      // 修正后的代码
      // 修正后的代码
      // 这是最终正确的代码
      .shadow({ radius: 3, color: 'rgba(0, 0, 0, 0.05)', offsetX: 0, offsetY: 2 })


      // --- 这是修改后的 List 组件 ---
      List({ space: 1 }) { // 大括号 { 必须立即跟随组件
        // 第1行：头像
        ListItem() {
          Row() {
            Text('头像')
              .fontSize(16)
              .fontColor(Color.Black)
            Blank()
            Image($r('app.media.userImage'))
              .width(40)
              .height(40)
              .borderRadius(20)
              .margin({ right: 8 })
              .backgroundColor('#EFEFEF')
            Text('>')
              .fontSize(16)
              .fontColor('#AAAAAA')
          }
          .width('100%')
          .height(60)
          .padding({ left: 15, right: 15 })
          .alignItems(VerticalAlign.Center)
        }
        .onClick(() => {
          console.info('点击了头像');
        })

        // 第2行：性别
        ListItem() {
          InfoRow({ label: '性别', value: '男' })
        }
        .onClick(() => {
          console.info('点击了性别');
        })

        // 第3行：生日
        ListItem() {
          InfoRow({ label: '生日', value: '1991年4月20日' })
        }
        .onClick(() => {
          console.info('点击了生日');
        })

        // 第4行：身高
        ListItem() {
          InfoRow({ label: '身高', value: '180cm' })
        }
        .onClick(() => {
          console.info('点击了身高');
        })

        // 第5行：体重
        ListItem() {
          InfoRow({ label: '体重', value: '75kg' })
        }
        .onClick(() => {
          console.info('点击了体重');
        })

      } // <-- List 的内容 } 在这里结束
      // 所有的属性都必须链在 } 之后
      .width('100%')
      .backgroundColor(Color.White)
      .margin({ top: 12 })
      .divider({ strokeWidth: 1, color: '#F1F3F5', startMargin: 15 })
      .layoutWeight(1) // 占据剩余所有空间
      // --- List 组件修改结束 ---

    }
    .width('100%')
    .height('100%')
    .backgroundColor('#F1F3F5') // 页面整体背景色
  }
}

/**
 * 封装一个可复用的 "标签 -> 值 >" 行组件
 */
@Component
struct InfoRow {
  private label: string = '';
  private value: string = '';

  build() {
    Row() {
      Text(this.label)
        .fontSize(16)
        .fontColor(Color.Black)
      Blank()
      Text(this.value)
        .fontSize(16)
        .fontColor('#007DFF')
        .margin({ right: 8 })
      Text('>')
        .fontSize(16)
        .fontColor('#AAAAAA')
    }
    .width('100%')
    .height(50)
    .padding({ left: 15, right: 15 })
    .alignItems(VerticalAlign.Center)
  }
}