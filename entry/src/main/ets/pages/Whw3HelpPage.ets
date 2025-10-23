// 存储常见问题的数据
const faqData = [
  '在哪里改变手表的颜色？',
  '怎么将自带日历的待办导入到APP里？',
  '手表指针时间和数字时间对应不上怎么办？',
  '如何用APP连接家人的手表？',
  '如何分享我的数据到我的朋友圈？',
  '如何提交问题？'
];

// 第5个问题的答案
const faqAnswer = '只需点开右上角的分享图标，即可选择相应的朋友圈图标分享即可';

@Entry
@Preview
@Component
struct HelpPage {
  build() {
    Column() {
      // 1. 顶部导航栏 (App Bar)
      Row() {
        // 根据您的要求，不添加返回箭头
        Text('帮助')
          .fontSize(18)
          .fontWeight(FontWeight.Bold)
      }
      .width('100%')
      .height(56) // 导航栏标准高度
      .backgroundColor(Color.White)
      .justifyContent(FlexAlign.Center) // 标题居中
      .alignItems(VerticalAlign.Center)

      // 2. 顶部图标网格
      Row() {
        // --- 修改点：GridItem 重命名为 IconGridItem ---
        IconGridItem({ icon: $r('app.media.userBook'), label: '用户手册' })
        IconGridItem({ icon: $r('app.media.runInBackground'), label: '后台运行' })
        IconGridItem({ icon: $r('app.media.watchMaintenance'), label: '手表保养' })
      }
      .width('100%')
      .backgroundColor(Color.White)
      .margin({ top: 10 }) // 与顶部导航栏拉开间距

      // 3. 常见问题列表 (使用一个单独的组件来管理状态)
      FaqSection()
        .layoutWeight(1) // 占据剩余所有空间

    }
    .width('100%')
    .height('100%')
    .backgroundColor('#F1F3F5') // 页面整体背景色
  }
}

/**
 * 封装一个 "图标 + 文字" 的网格项
 */
// --- 修改点：GridItem 重命名为 IconGridItem ---
@Component
struct IconGridItem {
  // --- 修改点：为 icon 添加默认初始值 = '' ---
  private icon: Resource = $r('app.media.userBook');
  private label: string = '';

  build() {
    Column({ space: 8 }) {
      Image(this.icon)
        .width(28)
        .height(28)
        .fillColor('#666666') // 图标颜色
      Text(this.label)
        .fontSize(14)
        .fontColor(Color.Black)
    }
    .width('33.33%') // 均分
    .padding({ top: 16, bottom: 16 })
    .alignItems(HorizontalAlign.Center)
  }
}

/**
 * 封装 "常见问题" 列表
 * 这个组件包含 @State，用于管理哪一项被展开了
 */
@Component
struct FaqSection {
  // @State 变量用于追踪当前展开的列表项索引
  // -1 表示所有项都已折叠
  @State expandedIndex: number = 4; // 默认展开第5项 (索引为4)

  build() {
    Column() {
      // 1. "常见问题" 标题
      Text('常见问题')
        .fontSize(16)
        .fontWeight(FontWeight.Bold)
        .width('100%')
        .padding({ top: 20, bottom: 12, left: 15 })

      // 2. 问题列表
      List({ space: 1 }) {
        // 循环渲染问题列表
        ForEach(faqData, (question: string, index: number) => {
          ListItem() {
            Column() {
              // 问题行 (序号 + 问题 + 箭头)
              Row() {
                Text(`${index + 1}`) // 序号
                  .fontSize(14)
                  .fontColor('#999999')
                  .width(20) // 给序号一个固定宽度
                  .textAlign(TextAlign.Start)

                Text(question) // 问题
                  .fontSize(16)
                  .fontColor(Color.Black)
                  .layoutWeight(1) // 占据剩余空间
                  .margin({ left: 8 })

                // 箭头：根据当前项是否为展开项来显示 ∧ 或 >
                Text(this.expandedIndex === index ? '∧' : '>')
                  .fontSize(16)
                  .fontColor('#AAAAAA')
              }
              .width('100%')
              .padding(15)
              .alignItems(VerticalAlign.Center)

              // 答案区域：仅当第5项(index 4)且为展开状态时显示
              if (this.expandedIndex === index && index === 4) {
                Text(faqAnswer)
                  .fontSize(14)
                  .fontColor('#666666')
                  .width('100%')
                  // 左侧缩进，与问题文字对齐 (15 padding + 20 width + 8 margin)
                  .padding({ left: 43, right: 15, bottom: 15 })
              }
            }
          }
          .onClick(() => {
            // 只有第5项(index 4)可以被点击展开/收起
            if (index === 4) {
              if (this.expandedIndex === index) {
                this.expandedIndex = -1; // 再次点击已展开的，将其折叠
              } else {
                this.expandedIndex = index; // 点击未展开的，将其展开
              }
            } else {
              // 点击其他项 (暂时无操作)
              console.info(`点击了第 ${index + 1} 项`);
            }
          })
        })
      }
      .backgroundColor(Color.White)
      // 分割线从文字处开始 (15 padding + 20 width + 8 margin)
      .divider({ strokeWidth: 1, color: '#F1F3F5', startMargin: 43 })
      .width('100%')
    }
  }
}