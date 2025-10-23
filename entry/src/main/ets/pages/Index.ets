import { ToolbarItem } from '../typing'
import HealthPage from './Lmy1HealthPage'
import AnalyzePage from './Fyx1AnalyzePage'
import WatchPairingPage from  './Yxy1WatchPairingPage'
import IntelligentPlayPage from './Zcy1IntelligentPlay'
import MinePage from  './Jxy1MinePage'


import ReminderSettingPage from './Fyx4ReminderSettingPage'
import ScheduleReminderPage from './Fyx3ScheduleReminderPage'


@Entry
@Component
struct Index {
  @State selectedIndex: number = 1
  @State toolbarIcons: ToolbarItem[] = [
    {
      index: 1, icon: $r('app.media.toolbar_home'),
      selectedIcon: $r('app.media.toolbar_home_f')
    },
    {
      index: 2, icon: $r('app.media.toolbar_tongji'),
      selectedIcon: $r('app.media.toolbar_tongji_f')
    },
    {
      index: 3, icon: $r('app.media.toolbar_add'),
      selectedIcon: $r('app.media.toolbar_add')
    },
    {
      index: 4, icon: $r('app.media.toolbar_compass'),
      selectedIcon: $r('app.media.toolbar_compass_f')
    },
    {
      index: 5, icon: $r('app.media.toolbar_person'),
      selectedIcon: $r('app.media.toolbar_person_f')
    },
  ]

  @Builder
  toolbarConfigurationBuilder() {
    Row() {
      ForEach(this.toolbarIcons, (item: ToolbarItem) => {
        Column() {
          Image(this.selectedIndex === item.index ? item.selectedIcon : item.icon)
            .width(24).height(24)
        }.size({ width: '20%', height: '100%' }).justifyContent(FlexAlign.Center)
        .onClick(() => {
          this.selectedIndex = item.index
        })
      })
    }.justifyContent(FlexAlign.SpaceAround).width('100%')
    .backgroundColor(Color.White)
  }

  build() {
    Column() {
      Navigation() {
        if (this.selectedIndex === 1) {
          HealthPage()
        } else if (this.selectedIndex === 2) {
          AnalyzePage()
        } else if (this.selectedIndex === 3) {
          WatchPairingPage()
        } else if (this.selectedIndex === 4) {
          IntelligentPlayPage()
        } else if (this.selectedIndex === 5) {
          MinePage()
        }
      }
      .toolbarConfiguration(this.toolbarConfigurationBuilder())
    }
    .height('100%').width('100%')
  }
}