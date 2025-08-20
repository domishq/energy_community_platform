import 'dart:async';
import 'dart:convert';
import 'dart:math';
import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;
import 'package:smartphone_app/services/wearos_service.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Energy Community App',
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        primarySwatch: Colors.lightGreen,
        scaffoldBackgroundColor: Colors.grey[100],
      ),
      home: const HomeScreen(),
    );
  }
}

class HomeScreen extends StatefulWidget {
  const HomeScreen({Key? key}) : super(key: key);

  @override
  _HomeScreenState createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  String? netData;
  bool isLoading = true;
  StreamSubscription? _subscription;
  final String communityId = "community1";

  @override
  void initState() {
    super.initState();
    fetchInitialData();
    listenToSSE();
  }

  // Fetch initial data
  void fetchInitialData() async {
    final uri = Uri.parse('http://10.0.2.2:8000/communities/$communityId');
    try {
      final response = await http.get(uri);
      if (response.statusCode == 200) {
        final data = json.decode(response.body);
        String netEnergy = "${calculateNet(data["genW"], data["conW"])}";
        setState(() {
          netData = netEnergy;
          isLoading = false;
        });
        WearOSService.sendNetEnergy(netEnergy);
      } else {
        setState(() {
          netData = "N/A";
          isLoading = false;
        });
      }
    } catch (e) {
      setState(() {
        netData = "Error";
        isLoading = false;
      });
    }
  }

  double calculateNet(int genW, int conW) {
    return (((genW - conW) / 1000) * 100).round() / 100;
  }

  // Listen to SSE
  void listenToSSE() async {
    final uri = Uri.parse(
      'http://10.0.2.2:8000/communities/$communityId/stream',
    );
    final request = http.Request('GET', uri);
    request.headers['Accept'] = 'text/event-stream';

    final response = await request.send();
    _subscription = response.stream
        .transform(utf8.decoder)
        .transform(const LineSplitter())
        .listen((line) {
          if (line.startsWith('data:')) {
            final jsonStr = line.replaceFirst('data: ', '');
            final data = json.decode(jsonStr);
            String netEnergy = "${calculateNet(data["genW"], data["conW"])}";
            setState(() {
              netData = netEnergy;
            });
            WearOSService.sendNetEnergy(netEnergy);
          }
        });
  }

  @override
  void dispose() {
    _subscription?.cancel();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Center(
        child: isLoading
            ? const CircularProgressIndicator(
                valueColor: AlwaysStoppedAnimation(Colors.blueAccent),
              )
            : Column(
                mainAxisSize: MainAxisSize.min,
                mainAxisAlignment: MainAxisAlignment.center,
                crossAxisAlignment: CrossAxisAlignment.center,
                children: [
                  Text(
                    netData ?? "",
                    style: const TextStyle(
                      fontSize: 64,
                      fontWeight: FontWeight.w900,
                      color: Colors.black,
                    ),
                  ),
                  const SizedBox(height: 0), // <-- spacing
                  const Text(
                    "kWh",
                    style: const TextStyle(
                      fontSize: 32,
                      fontWeight: FontWeight.normal,
                      color: Colors.blueGrey,
                    ),
                  ),
                ],
              ),
      ),
    );
  }
}
