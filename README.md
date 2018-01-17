# ce-mopub-sample-android

## Mediate CrystalExpress Ads through MoPub

The full integration guide: https://intowow.gitbooks.io/crystalexpress-documentation-v3-x/content/mediation/mopub/android.html

The CrystalExpress MoPub Adapter allows MoPub publishers to add CrystalExpress as a Custom Ad Network within the MoPub platform.

CrystalExpress provides four ad formats for MoPub mediation. The relationship between MoPub ad unit and ad format in CrystalExpress is as following:

| MoPub ad unit | AD format from CrystalExpress |
| --- | --- |
| Banner or Medium | Card AD |
| Rewarded Video | Rewarded Video AD |
| Interstitial | Splash AD |
| Native | Native AD |

Before you start add CrystalExpress as Custom network, you have to integrate MoPub SDK by following the instructions on the [MoPub website](https://github.com/mopub/mopub-android-sdk/wiki/Getting-Started#app-transport-security-settings).

** NOTICE: This porject does not contain CrystalExpress SDK. Please contact your Intowow account manager. We will provide the appropriate version of SDK and Crystal ID to fit your needs.**

The custom event is under folder 'app/src/main/java/com/mopub/mobileads' and 'app/src/main/java/com/mopub/nativeads'


## CHANGELOG

#### Version 4 (2018-01-05)

#### Features
* MoPub custom event support interstitial format


#### Version 3 (2017-10-25)

#### Features
* MoPub custom event supports audience tag.


#### Version 2 (2017-08-02)

#### Features
* Remove timeout setting in MoPub custom event.

#### Version 1 (2017-06-23)

#### Features
* Implement MoPub SDK adapter and sample code.