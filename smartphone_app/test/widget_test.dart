import 'package:flutter_test/flutter_test.dart';
import 'package:smartphone_app/main.dart';

void main() {
  testWidgets('HomeScreen button is displayed and navigates', (
    WidgetTester tester,
  ) async {
    // Build our app without const
    await tester.pumpWidget(MyApp());

    // Verify button exists
    expect(find.text('Go to Community 1'), findsOneWidget);

    // Tap the button
    await tester.tap(find.text('Go to Community 1'));
    await tester.pumpAndSettle();

    // Verify that CommunityScreen is displayed
    expect(find.textContaining('Community community1'), findsOneWidget);
  });
}
