// 导入必要的枚举类型（根据IDE提示补充，通常无需手动导入）
@Entry
@Component
struct MyInfoPage {
  @State nickname: string = ''
  @State gender: string = '' // 存储选中的性别，'male'或'female'
  @State birthYear: number = 1991
  @State birthMonth: number = 4
  @State birthDay: number = 20

  build() {
    Column({ space: 20 }) {
      // 标题区域
      Column({ space: 5 }) {
        Text('我的信息')
          .fontSize(24)
          .fontWeight(FontWeight.Bold)
        Text('精确的信息可以获取更准确的数据分析')
          .fontSize(12)
          .fontColor('#999')
      }
      .width('100%')
      .padding({ top: 30, left: 20, right: 20 })

      // 昵称输入
      Column({ space: 5 }) {
        Text('昵称')
          .fontSize(16)
          .width('100%')
          .padding({ left: 10 })
        TextInput({ placeholder: '输入昵称' })
          .width('100%')
          .height(40)
          .backgroundColor('#f5f5f5')
          .borderRadius(8)
          .padding({ left: 10 })
          .onChange((value: string) => {
            this.nickname = value
          })
      }
      .width('90%')

      // 性别选择（带图片）
      Column({ space: 5 }) {
        Text('性别')
          .fontSize(16)
          .width('100%')
          .padding({ left: 10 })
        Row({ space: 10 }) {
          // 男性按钮（带nan.png）
          Button({ type: ButtonType.Normal, stateEffect: true }) {
            Row({ space: 8 }) {
              Image($r('app.media.nan')) // 引用男性图片（确保nan.png在media目录）
                .width(20)
                .height(20)
                .objectFit(ImageFit.Contain)
              Text('男')
                .fontSize(16)
            }
            .justifyContent(FlexAlign.Center)
            .width('100%')
          }
          .width('45%')
          .height(40)
          .backgroundColor(this.gender === 'male' ? '#fff' : '#f5f5f5')
          .borderRadius(8)
          .border({ width: 1, color: this.gender === 'male' ? '#007AFF' : '#ddd' })
          .onClick(() => {
            this.gender = 'male'
          })

          // 女性按钮（带nv.png）
          Button({ type: ButtonType.Normal, stateEffect: true }) {
            Row({ space: 8 }) {
              Image($r('app.media.nv')) // 引用女性图片（确保nv.png在media目录）
                .width(20)
                .height(20)
                .objectFit(ImageFit.Contain)
              Text('女')
                .fontSize(16)
            }
            .justifyContent(FlexAlign.Center)
            .width('100%')
          }
          .width('45%')
          .height(40)
          .backgroundColor(this.gender === 'female' ? '#fff' : '#f5f5f5')
          .borderRadius(8)
          .border({ width: 1, color: this.gender === 'female' ? '#007AFF' : '#ddd' })
          .onClick(() => {
            this.gender = 'female'
          })
        }
        .width('100%')
      }
      .width('90%')

      // 年龄选择（日期选择器）
      Column({ space: 5 }) {
        Text('年龄')
          .fontSize(16)
          .width('100%')
          .padding({ left: 10 })
        DatePicker({
          start: new Date('1900-01-01'),
          end: new Date(),
          selected: new Date(`${this.birthYear}-${this.birthMonth}-${this.birthDay}`)
        })
          .width('100%')
          .onChange((value: DatePickerResult) => {
            this.birthYear = value.year || 1991
            this.birthMonth = value.month || 4
            this.birthDay = value.day || 20
          })
      }
      .width('90%')

      // 下一步按钮
      Button('下一步')
        .width('90%')
        .height(45)
        .backgroundColor('#007AFF')
        .fontColor('#fff')
        .borderRadius(8)
        .margin({ top: 40, bottom: 30 })
        .onClick(() => {
          // 点击下一步的逻辑
          console.info(`提交信息：昵称=${this.nickname}，性别=${this.gender || '未选择'}，生日=${this.birthYear}-${this.birthMonth}-${this.birthDay}`)
        })
    }
    .width('100%')
    .height('100%')
    .backgroundColor('#fff')
    .padding({ left: 20, right: 20 })
  }
}