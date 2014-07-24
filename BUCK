# LightVPN Debug Binary
android_binary(
  name = 'debug',
  package_type = 'DEBUG',
  manifest = './AndroidManifest.xml',
  target = 'android-19',
  keystore = ':debug_keystore',
  deps = [
    ':lv-res',
	':lv-src',
	'//jni:lv-ndk',
  ],
)

# LightVPN Debug Binary
android_binary(
  name = 'release',
  package_type = 'RELEASE',
  proguard_config = './proguard.cfg',
  manifest = './AndroidManifest.xml',
  target = 'android-19',
  keystore = ':release_keystore',
  deps = [
    ':lv-res',
	':lv-src',
	'//jni:lv-ndk',
  ],
)

# LightVPN Resources
android_resource(
  name = 'lv-res',
  res = './res',
  assets = './assets',
  package = 'us.shandian.vpn',
  visibility = [ 'PUBLIC' ],
)

# LightVPN Source Code
android_library(
  name = 'lv-src',
  srcs = glob(['./src/**/*.java']),
  deps = [
    ':build_config',
	':lv-res',
	':android-support-v4',
  ],
)

# LightVPN Build Config
android_build_config(
  name = 'build_config',
  package = 'us.shandian.vpn',
)

# Android Support Library v4
prebuilt_jar(
  name = 'android-support-v4',
  binary_jar = './libs/android-support-v4.jar',
  visibility = [ 'PUBLIC' ],
)

# Debug Keystore
keystore(
  name = 'debug_keystore',
  store = './keystore/debug.keystore',
  properties = './keystore/debug.keystore.properties',
)

# Release Keystore (Private)
keystore(
  name = 'release_keystore',
  store = './keystore/publish.keystore',
  properties = './keystore/publish.keystore.properties',
)
