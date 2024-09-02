class ESPTouchResult {
  /// IP address of the connected device on the local network in string representation.
  ///
  /// Example: 127.0.0.55
  final String ip;

  /// BSSID (MAC address) of the connected device.
  ///
  /// Example: `ab:cd:ef:c0:ff:33`, or without colons `abcdefc0ff33`
  final String bssid;

  ESPTouchResult(this.ip, this.bssid);

  ESPTouchResult.fromMap(Map<dynamic, dynamic> m)
      : ip = m['ip'],
        bssid = m['bssid'];
  @override
  bool operator ==(other) => other is ESPTouchResult && other.ip == ip && other.bssid == bssid;
  @override
  int get hashCode => ip.hashCode ^ bssid.hashCode;
}
