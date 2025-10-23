// 定义提醒项数据结构
interface ReminderItem {
  id: number // 唯一标识
  icon: ResourceStr // 图标资源
  name: string // 提醒名称
  isOn: boolean // 开关状态
  isExpandable?: boolean // 是否可展开（跳转详情）
}

@Entry
@Component
export default struct ReminderSettingPage {
  // 页面标题
  private pageTitle: string = '提醒设置'
  // 提醒项数据列表
  @State reminderList: ReminderItem[] = [
    {
      id: 1,
      icon: $r('app.media.ic_call'),
      name: '来电提醒',
      isOn: true
    },
    {
      id: 2,
      icon: $r('app.media.ic_sms'),
      name: '短信提醒',
      isOn: true
    },
    {
      id: 3,
      icon: $r('app.media.ic_calendar'),
      name: '日程提醒',
      isOn: true
    },
    {
      id: 4,
      icon: $r('app.media.ic_alarm'),
      name: '闹钟提醒',
      isOn: true,
      isExpandable: true
    },
    {
      id: 5,
      icon: $r('app.media.ic_app'),
      name: '应用提醒',
      isOn: true,
      isExpandable: true
    },
    {
      id: 6,
      icon: $r('app.media.ic_water'),
      name: '喝水提醒',
      isOn: true,
      isExpandable: true
    },
    {
      id: 7,
      icon: $r('app.media.ic_sit'),
      name: '久坐提醒',
      isOn: true,
      isExpandable: true
    },
    {
      id: 8,
      icon: $r('app.media.ic_anti_lost'),
      name: '防丢提醒',
      isOn: true
    },
    {
      id: 9,
      icon: $r('app.media.ic_mail'),
      name: '邮件提醒',
      isOn: true
    },
  ]

  build() {
    Column() {
      // 标题栏
      this.buildTitleBar()

      // 提醒列表
      List({ space: 12 }) {
        ForEach(this.reminderList, (item: ReminderItem) => {
          ListItem() {
            this.buildReminderItem(item)
          }
          .padding({ left: 16, right: 16 })
        }, (item: ReminderItem) => item.id.toString()) // 唯一键
      }
      .padding({ top: 16 })
      .backgroundColor('#F5F5F5')
    }
    .width('100%')
    .height('90%')  // TODO 很多页面都要设置成90%，不然会被底部 Navigation 遮挡
    .backgroundColor('#F5F5F5')

  }

  // 构建标题栏
  @Builder
  buildTitleBar() {
    Row() {
      Text(this.pageTitle)
        .fontSize(20)
        .fontWeight(FontWeight.Bold)
    }
    .width('100%')
    .height(56)
    .justifyContent(FlexAlign.Center)
    .backgroundColor('#FFFFFF')
    .shadow({ radius: 2, color: '#00000010' })
  }

  // 构建单个提醒项
  @Builder
  buildReminderItem(item: ReminderItem) {
    Row({ space: 12 }) {
      // 图标（带背景色）
      Stack() {
        Circle()
          .fill(this.getIconBgColor(item.id)) // 圆形背景
          .width(40)
          .height(40)
        Image(item.icon)
          .width(22)
          .height(22)
          .objectFit(ImageFit.Contain)
      }

      // 名称
      Text(item.name)
        .fontSize(16)
        .flexGrow(1) // 占满剩余空间

      // 右侧操作区（开关/展开箭头）
      if (item.isExpandable) {
        Row({ space: 8 }) {
          Text('已开启')
            .fontSize(14)
            .fontColor('#888888')
          Image($r('app.media.ic_arrow_right'))
            .width(12)
            .height(12)
            .objectFit(ImageFit.Contain)
        }
      } else {
        Toggle({ type: ToggleType.Switch, isOn: item.isOn })
      }
    }
    .width('100%')
    .height(60)
    .alignItems(VerticalAlign.Center)
    .backgroundColor('#FFFFFF')
    .borderRadius(12)
    .padding({ right: 10, left: 10 })
  }

  // 根据ID设置图标背景色（可自定义）
  private getIconBgColor(id: number): string {
    const colors = [
      '#E1F5FE', // 来电提醒（浅蓝）
      '#F3E5F5', // 短信提醒（浅紫）
      '#FCE4EC', // 日程提醒（浅粉）
      '#FFF3E0', // 闹钟提醒（浅橙）
      '#FFF3E0', // 应用提醒（浅橙）
      '#E1F5FE', // 喝水提醒（浅蓝）
      '#E8F5E9', // 久坐提醒（浅绿）
      '#E1F5FE', // 防丢提醒（浅蓝）
      '#E1F5FE'// 邮件提醒（浅蓝）
    ]
    return colors[id - 1] || '#F5F5F5'
  }
}