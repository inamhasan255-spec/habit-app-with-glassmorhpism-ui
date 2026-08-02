import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:sqflite/sqflite.dart';
import 'package:firebase_auth/firebase_auth.dart';

/// Service to handle synchronization between local SQLite (sqflite) and Cloud Firestore
/// for user habits, focus session history, and blocked app settings.
class FirebaseSyncService {
  final FirebaseFirestore _firestore = FirebaseFirestore.instance;
  final FirebaseAuth _auth = FirebaseAuth.instance;

  /// Get current authenticated user's ID
  String? get _uid => _auth.currentUser?.uid;

  /// Check if a user is authenticated
  bool get isAuthenticated => _uid != null;

  /// Synchronizes all local SQLite tables to Cloud Firestore (Push and Pull merge)
  Future<void> synchronizeData(Database db) async {
    final userId = _uid;
    if (userId == null) {
      throw Exception('User must be authenticated to sync data.');
    }

    try {
      // 1. Push Local Habits and Completions to Firestore
      await _syncHabits(db, userId);

      // 2. Push Local Focus Sessions to Firestore
      await _syncSessions(db, userId);

      // 3. Push Local Blocked App Settings to Firestore
      await _syncBlockedApps(db, userId);

      // 4. Update the Sync Metadata
      await _firestore.collection('users').doc(userId).set({
        'lastSyncedAt': FieldValue.serverTimestamp(),
        'platform': 'flutter_dart',
      }, SetOptions(merge: true));

    } catch (e) {
      print('Error during Firebase synchronization: $e');
      rethrow;
    }
  }

  /// Sync habits and habit completions
  Future<void> _syncHabits(Database db, String userId) async {
    // Read habits from SQLite
    final List<Map<String, dynamic>> localHabits = await db.query('habits');
    final List<Map<String, dynamic>> localCompletions = await db.query('habit_completions');

    // Convert local maps to serialized structures compatible with Firestore
    final List<Map<String, dynamic>> habitsPayload = localHabits.map((habit) {
      return {
        'id': habit['id'],
        'name': habit['name'],
        'category': habit['category'],
        'colorHex': habit['colorHex'],
        'isObsidianLinked': habit['isObsidianLinked'] == 1,
        'obsidianUri': habit['obsidianUri'] ?? '',
        'streakCount': habit['streakCount'] ?? 0,
        'lastCompletedDate': habit['lastCompletedDate'] ?? '',
      };
    }).toList();

    final List<Map<String, dynamic>> completionsPayload = localCompletions.map((comp) {
      return {
        'habitId': comp['habitId'],
        'date': comp['date'],
      };
    }).toList();

    // Store in Firestore user's sync document
    await _firestore
        .collection('users')
        .doc(userId)
        .collection('data')
        .doc('habits_data')
        .set({
          'habits': habitsPayload,
          'completions': completionsPayload,
          'updatedAt': FieldValue.serverTimestamp(),
        });
  }

  /// Sync focus session history
  Future<void> _syncSessions(Database db, String userId) async {
    // Read sessions from SQLite
    final List<Map<String, dynamic>> localSessions = await db.query('work_sessions');

    final List<Map<String, dynamic>> sessionsPayload = localSessions.map((session) {
      return {
        'id': session['id'],
        'label': session['label'],
        'durationMinutes': session['durationMinutes'],
        'focusTimeMinutes': session['focusTimeMinutes'],
        'timestamp': session['timestamp'],
        'category': session['category'] ?? 'Work',
      };
    }).toList();

    await _firestore
        .collection('users')
        .doc(userId)
        .collection('data')
        .doc('sessions_data')
        .set({
          'sessions': sessionsPayload,
          'updatedAt': FieldValue.serverTimestamp(),
        });
  }

  /// Sync blocked app settings
  Future<void> _syncBlockedApps(Database db, String userId) async {
    // Read blocked apps from SQLite
    final List<Map<String, dynamic>> localBlockedApps = await db.query('blocked_apps');

    final List<Map<String, dynamic>> appsPayload = localBlockedApps.map((app) {
      return {
        'id': app['id'],
        'packageName': app['packageName'],
        'appName': app['appName'],
        'isBlocked': app['isBlocked'] == 1,
        'category': app['category'],
        'blockMode': app['blockMode'] ?? 'Session only',
      };
    }).toList();

    await _firestore
        .collection('users')
        .doc(userId)
        .collection('data')
        .doc('blocked_apps_data')
        .set({
          'blockedApps': appsPayload,
          'updatedAt': FieldValue.serverTimestamp(),
        });
  }

  /// Pulls all synchronized data from Firestore and replaces/merges into local SQLite
  Future<void> pullAndRestoreFromCloud(Database db) async {
    final userId = _uid;
    if (userId == null) {
      throw Exception('User must be authenticated to pull data.');
    }

    // 1. Retrieve Habits Data
    final habitsDoc = await _firestore
        .collection('users')
        .doc(userId)
        .collection('data')
        .doc('habits_data')
        .get();

    if (habitsDoc.exists) {
      final habitsData = habitsDoc.data();
      if (habitsData != null) {
        final List<dynamic> habitsList = habitsData['habits'] ?? [];
        final List<dynamic> completionsList = habitsData['completions'] ?? [];

        await db.transaction((txn) async {
          // Clear current tables
          await txn.delete('habits');
          await txn.delete('habit_completions');

          // Re-insert Habits
          for (var habit in habitsList) {
            await txn.insert('habits', {
              'name': habit['name'],
              'category': habit['category'],
              'colorHex': habit['colorHex'],
              'isObsidianLinked': habit['isObsidianLinked'] == true ? 1 : 0,
              'obsidianUri': habit['obsidianUri'] ?? '',
              'streakCount': habit['streakCount'] ?? 0,
              'lastCompletedDate': habit['lastCompletedDate'] ?? '',
            });
          }

          // Re-insert completions
          for (var comp in completionsList) {
            await txn.insert('habit_completions', {
              'habitId': comp['habitId'],
              'date': comp['date'],
            });
          }
        });
      }
    }

    // 2. Retrieve Sessions Data
    final sessionsDoc = await _firestore
        .collection('users')
        .doc(userId)
        .collection('data')
        .doc('sessions_data')
        .get();

    if (sessionsDoc.exists) {
      final sessionsData = sessionsDoc.data();
      if (sessionsData != null) {
        final List<dynamic> sessionsList = sessionsData['sessions'] ?? [];

        await db.transaction((txn) async {
          await txn.delete('work_sessions');
          for (var session in sessionsList) {
            await txn.insert('work_sessions', {
              'label': session['label'],
              'durationMinutes': session['durationMinutes'],
              'focusTimeMinutes': session['focusTimeMinutes'],
              'timestamp': session['timestamp'],
              'category': session['category'] ?? 'Work',
            });
          }
        });
      }
    }

    // 3. Retrieve Blocked Apps Data
    final blockedAppsDoc = await _firestore
        .collection('users')
        .doc(userId)
        .collection('data')
        .doc('blocked_apps_data')
        .get();

    if (blockedAppsDoc.exists) {
      final blockedAppsData = blockedAppsDoc.data();
      if (blockedAppsData != null) {
        final List<dynamic> appsList = blockedAppsData['blockedApps'] ?? [];

        await db.transaction((txn) async {
          await txn.delete('blocked_apps');
          for (var app in appsList) {
            await txn.insert('blocked_apps', {
              'packageName': app['packageName'],
              'appName': app['appName'],
              'isBlocked': app['isBlocked'] == true ? 1 : 0,
              'category': app['category'],
              'blockMode': app['blockMode'] ?? 'Session only',
            });
          }
        });
      }
    }
  }
}
