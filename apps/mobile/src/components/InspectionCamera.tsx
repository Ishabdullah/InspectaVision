import React, { useState, useRef } from 'react';
import { StyleSheet, Text, View, TouchableOpacity, Image } from 'react-native';
import { Camera, CameraType, FlashMode } from 'expo-camera';
import { Camera as LucideCamera, RotateCcw, Zap, ZapOff, Play } from 'lucide-react-native';

export default function InspectionCamera({ onCapture }: { onCapture: (uri: string) => void }) {
  const [type, setType] = useState(CameraType.back);
  const [flash, setFlash] = useState(FlashMode.off);
  const [permission, requestPermission] = Camera.useCameraPermissions();
  const cameraRef = useRef<Camera>(null);

  if (!permission) {
    return <View />;
  }

  if (!permission.granted) {
    return (
      <View style={styles.container}>
        <Text style={{ textAlign: 'center' }}>We need your permission to show the camera</Text>
        <TouchableOpacity onPress={requestPermission} style={styles.button}>
          <Text>Grant Permission</Text>
        </TouchableOpacity>
      </View>
    );
  }

  const takePicture = async () => {
    if (cameraRef.current) {
      const photo = await cameraRef.current.takePictureAsync({
        quality: 0.8,
        base64: true,
      });
      onCapture(photo.uri);
    }
  };

  return (
    <View style={styles.container}>
      <Camera 
        style={styles.camera} 
        type={type} 
        flashMode={flash}
        ref={cameraRef}
      >
        <View style={styles.overlay}>
          {/* Top Controls */}
          <View style={styles.topControls}>
            <TouchableOpacity onPress={() => setFlash(flash === FlashMode.off ? FlashMode.on : FlashMode.off)}>
              {flash === FlashMode.on ? <Zap color="white" /> : <ZapOff color="white" />}
            </TouchableOpacity>
            <TouchableOpacity onPress={() => setType(type === CameraType.back ? CameraType.front : CameraType.back)}>
              <RotateCcw color="white" />
            </TouchableOpacity>
          </View>

          {/* Bottom Controls */}
          <View style={styles.bottomControls}>
            <View style={styles.shutterContainer}>
              <TouchableOpacity onPress={takePicture} style={styles.shutter}>
                <View style={styles.shutterInner} />
              </TouchableOpacity>
            </View>
          </View>
        </View>
      </Camera>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: 'black',
  },
  camera: {
    flex: 1,
  },
  overlay: {
    flex: 1,
    backgroundColor: 'transparent',
    justifyContent: 'space-between',
    padding: 40,
  },
  topControls: {
    flexDirection: 'row',
    justifyContent: 'space-between',
  },
  bottomControls: {
    alignItems: 'center',
    marginBottom: 20,
  },
  shutterContainer: {
    borderWidth: 4,
    borderColor: 'white',
    borderRadius: 100,
    padding: 4,
  },
  shutter: {
    width: 70,
    height: 70,
    backgroundColor: 'white',
    borderRadius: 100,
    justifyContent: 'center',
    alignItems: 'center',
  },
  shutterInner: {
    width: 60,
    height: 60,
    borderRadius: 100,
    borderWidth: 2,
    borderColor: 'black',
  },
  button: {
    backgroundColor: '#2563eb',
    padding: 15,
    borderRadius: 8,
    marginTop: 10,
  }
});
