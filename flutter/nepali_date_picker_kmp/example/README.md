# nepali_date_picker_kmp example

A compact, single-file demo of the plugin: today's date through the conversion engine, the
embedded Material3 picker, and the native dialog. The full multi-page showcase, covering every
picker variant, events, working days and theming, lives in the repository at
[`sample/flutterApp`](https://github.com/shivathapaa/Nepali-Date-Picker/tree/main/sample/flutterApp).

## Running

Android:

```sh
flutter run
```

While the wrapped library version is unpublished, publish it locally first from the repository
root (`./gradlew publishToMavenLocal`); the example already resolves `mavenLocal()`.

iOS additionally needs the Kotlin framework staged once:

```sh
../tool/stage_ios_framework.sh
flutter run
```

The integration tests run the golden conversion vectors against the real engine on a device or
simulator:

```sh
flutter test integration_test
```
