import 'dart:async';

import 'package:flutter/services.dart';

import 'esptouch_result.dart';

class ESPTouchSmartConfig {
  static const EventChannel _channel = EventChannel('esptouch_smartconfig');

  static Stream<ESPTouchResult>? execute(
      {required String ssid,
      required String bssid,
      String password = "",
      String deviceCount = "1",
      bool isBroad = true}) {
    return _channel.receiveBroadcastStream({
      'ssid': ssid,
      'bssid': bssid,
      'password': password,
      "deviceCount": deviceCount,
      'isBroad': (isBroad) ? "YES" : "NO"
    }).map((event) => ESPTouchResult.fromMap(event));
  }
}
