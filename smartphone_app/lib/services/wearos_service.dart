import 'package:flutter/services.dart';

class WearOSService {
  static const MethodChannel _channel = MethodChannel(
    'com.example.energy/wearos',
  );

  static Future<void> sendNetEnergy(String netEnergy) async {
    try {
      final measuredAt = DateTime.now().millisecondsSinceEpoch;
      await _channel.invokeMethod('sendNetEnergy', {
        'netEnergy': netEnergy,
        'measuredAt': measuredAt,
      });
    } on PlatformException catch (e) {
      print("Failed to send to Wear OS: ${e.message}");
    }
  }
}
