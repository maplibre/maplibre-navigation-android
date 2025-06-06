Pod::Spec.new do |spec|
    spec.name                     = 'MapLibreNavigationCore'
    spec.version                  = '0.0.0'
    spec.homepage                 = 'https://github.com/maplibre/maplibre-navigation-android/'
    spec.source                   = { :http=> ''}
    spec.authors                  = ''
    spec.license                  = ''
    spec.summary                  = 'MapLibre navigation core library'
    spec.vendored_frameworks      = 'build/cocoapods/framework/MapLibreNavigationCore.framework'
    spec.libraries                = 'c++'
                
                
                
    if !Dir.exist?('build/cocoapods/framework/MapLibreNavigationCore.framework') || Dir.empty?('build/cocoapods/framework/MapLibreNavigationCore.framework')
        raise "

        Kotlin framework 'MapLibreNavigationCore' doesn't exist yet, so a proper Xcode project can't be generated.
        'pod install' should be executed after running ':generateDummyFramework' Gradle task:

            ./gradlew :maplibre-navigation-core:generateDummyFramework

        Alternatively, proper pod installation is performed during Gradle sync in the IDE (if Podfile location is set)"
    end
                
    spec.xcconfig = {
        'ENABLE_USER_SCRIPT_SANDBOXING' => 'NO',
    }
                
    spec.pod_target_xcconfig = {
        'KOTLIN_PROJECT_PATH' => ':maplibre-navigation-core',
        'PRODUCT_MODULE_NAME' => 'MapLibreNavigationCore',
    }
                
    spec.script_phases = [
        {
            :name => 'Build MapLibreNavigationCore',
            :execution_position => :before_compile,
            :shell_path => '/bin/sh',
            :script => <<-SCRIPT
                if [ "YES" = "$OVERRIDE_KOTLIN_BUILD_IDE_SUPPORTED" ]; then
                  echo "Skipping Gradle build task invocation due to OVERRIDE_KOTLIN_BUILD_IDE_SUPPORTED environment variable set to \"YES\""
                  exit 0
                fi
                set -ev
                REPO_ROOT="$PODS_TARGET_SRCROOT"
                "$REPO_ROOT/../gradlew" -p "$REPO_ROOT" $KOTLIN_PROJECT_PATH:syncFramework \
                    -Pkotlin.native.cocoapods.platform=$PLATFORM_NAME \
                    -Pkotlin.native.cocoapods.archs="$ARCHS" \
                    -Pkotlin.native.cocoapods.configuration="$CONFIGURATION"
            SCRIPT
        }
    ]
                
end