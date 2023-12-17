# Contributing

If you have a usage question pertaining to the Maplibre Navigation SDK for Android, or any of our other products, contact us through [our support page](https://www.mapbox.com/contact/).

If you want to contribute code:

1. Please familiarize yourself with the [install process](README.md).

2. Ensure that existing [pull requests](https://github.com/maplibre/maplibre-navigation-android/pulls) and [issues](https://github.com/maplibre/maplibre-navigation-android/issues) don’t already cover your contribution or question.

3. Pull requests are gladly accepted. If there are any changes that developers should be aware of, please update the [change log](CHANGELOG.md)

4. ~~We use checkstyle to enforce good coding standards. CI will fail if your PR contains any issues.~~ Lints are currently disabled; PRs welcome to improve the situation. Unit tests still run though!

## Getting started building

You can check out the repo and build locally using Android Studio or the gradle wrapper CLI,
just as with any other Kotlin/Java project.
Do note however that currently the lints fail, so you should run build excluding lints.
For example: `./gradlew build -x lint`.

# Code of conduct

Everyone is invited to participate in MapLibre’s open source projects and public discussions: we want to create a welcoming and friendly environment. Harassment of participants or other unethical and unprofessional behavior will not be tolerated in our spaces. The [Contributor Covenant](http://contributor-covenant.org) applies to all projects under the Mapbox organization and we ask that you please read [the full text](http://contributor-covenant.org/version/1/2/0/).

You can learn more about MapLibre at [maplibre.org](https://maplibre.org/).
