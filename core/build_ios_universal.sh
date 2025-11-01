
set -e  

mkdir -p build-ios-sim build-ios-device build-universal

echo "Building for iOS Simulator..."
cmake -S . -B build-ios-sim -G Xcode \
  -DCMAKE_OSX_SYSROOT=iphonesimulator \
  -DCMAKE_OSX_ARCHITECTURES="arm64;x86_64" \
  -DCMAKE_OSX_DEPLOYMENT_TARGET=13.0
cmake --build build-ios-sim --config Release

echo "Building for iOS Device..."
cmake -S . -B build-ios-device -G Xcode \
  -DCMAKE_OSX_SYSROOT=iphoneos \
  -DCMAKE_OSX_ARCHITECTURES="arm64" \
  -DCMAKE_OSX_DEPLOYMENT_TARGET=13.0
cmake --build build-ios-device --config Release

echo "Creating universal library..."
lipo -create \
  build-ios-device/Release-iphoneos/libbpm_core.a \
  build-ios-sim/Release-iphonesimulator/libbpm_core.a \
  -output build-universal/libbpm_core_universal.a

echo "Success! Universal lib:"
lipo -info build-universal/libbpm_core_universal.a