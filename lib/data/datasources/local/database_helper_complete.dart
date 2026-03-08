// data/datasources/local/database_helper_complete.dart
import 'package:sqflite/sqflite.dart';
import 'package:path/path.dart';
import '../../../core/constants/bible_versions.dart';

class DatabaseHelper {
  static Database? _database;
  static const String _databaseName = 'life_issues.db';
  static const int _databaseVersion = 1;

  // Tables
  static const String tableVerses = 'verses';
  static const String tableIssues = 'issues';
  static const String tableFavorites = 'favorites';
  static const String tableIssueVerses = 'issue_verses';
  static const String tableDailyVerses = 'daily_verses';

  // Verses table columns
  static const String columnVerseId = 'id';
  static const String columnReference = 'reference';
  static const String columnKJV = 'kjv';
  static const String columnMSG = 'msg';
  static const String columnAMP = 'amp';
  static const String columnImageUrl = 'image_url';
  static const String columnCreatedAt = 'created_at';

  // Issues table columns
  static const String columnIssueId = 'id';
  static const String columnTitle = 'title';
  static const String columnDescription = 'description';
  static const String columnIconPath = 'icon_path';

  static Future<Database> get database async {
    if (_database != null) return _database!;
    _database = await _initDatabase();
    return _database!;
  }

  static Future<Database> _initDatabase() async {
    final documentsDirectory = await getDatabasesPath();
    final path = join(documentsDirectory, _databaseName);

    return await openDatabase(
      path,
      version: _databaseVersion,
      onCreate: _createTables,
      onOpen: (db) => _populateInitialData(db),
    );
  }

  static Future<void> _createTables(Database db, int version) async {
    // Create verses table (now with KJV, MSG, AMP columns)
    await db.execute('''
      CREATE TABLE $tableVerses (
        $columnVerseId TEXT PRIMARY KEY,
        $columnReference TEXT NOT NULL,
        $columnKJV TEXT NOT NULL,
        $columnMSG TEXT,
        $columnAMP TEXT,
        $columnImageUrl TEXT,
        $columnCreatedAt TEXT NOT NULL
      )
    ''');

    // Create issues table
    await db.execute('''
      CREATE TABLE $tableIssues (
        $columnIssueId TEXT PRIMARY KEY,
        $columnTitle TEXT NOT NULL,
        $columnDescription TEXT NOT NULL,
        $columnIconPath TEXT
      )
    ''');

    // Create favorites table
    await db.execute('''
      CREATE TABLE $tableFavorites (
        verse_id TEXT PRIMARY KEY,
        created_at TEXT NOT NULL,
        FOREIGN KEY (verse_id) REFERENCES $tableVerses($columnVerseId)
      )
    ''');

    // Create issue_verses junction table
    await db.execute('''
      CREATE TABLE $tableIssueVerses (
        issue_id TEXT,
        verse_id TEXT,
        PRIMARY KEY (issue_id, verse_id),
        FOREIGN KEY (issue_id) REFERENCES $tableIssues($columnIssueId),
        FOREIGN KEY (verse_id) REFERENCES $tableVerses($columnVerseId)
      )
    ''');

    // Create daily_verses table
    await db.execute('''
      CREATE TABLE $tableDailyVerses (
        date TEXT PRIMARY KEY,
        verse_id TEXT NOT NULL,
        inspirational_message TEXT,
        FOREIGN KEY (verse_id) REFERENCES $tableVerses($columnVerseId)
      )
    ''');
  }

  static Future<void> _populateInitialData(Database db) async {
    // Check if data already exists
    final count = await db.rawQuery('SELECT COUNT(*) as count FROM $tableIssues');
    final existingCount = count.first['count'] as int;

    if (existingCount > 0) return; // Data already exists

    // Insert all issues
    final issues = _getAllIssues();
    for (final issue in issues) {
      await db.insert(tableIssues, issue);
    }

    // Insert all verses with multiple versions
    final verses = _getAllVersesWithVersions();
    for (final verse in verses) {
      await db.insert(tableVerses, verse);
    }

    // Insert issue-verse relationships
    final relationships = _getAllIssueVerseRelationships();
    for (final relationship in relationships) {
      await db.insert(tableIssueVerses, relationship);
    }

    // Insert daily verses for the current month
    final dailyVerses = _getDailyVerses();
    for (final dailyVerse in dailyVerses) {
      await db.insert(tableDailyVerses, dailyVerse);
    }
  }

  // Complete list of all issues from Life_Issues.db
  static List<Map<String, dynamic>> _getAllIssues() {
    return [
      {'id': 'abortion', 'title': 'Abortion', 'description': 'Life and the sanctity of life', 'icon_path': null},
      {'id': 'addiction', 'title': 'Addiction', 'description': 'Breaking free from addiction', 'icon_path': null},
      {'id': 'adultery', 'title': 'Adultery', 'description': 'Faithfulness in marriage', 'icon_path': null},
      {'id': 'anger', 'title': 'Anger', 'description': 'Finding peace when you\'re angry', 'icon_path': null},
      {'id': 'anxiety', 'title': 'Anxiety', 'description': 'Overcoming worry and fear', 'icon_path': null},
      {'id': 'believing', 'title': 'Believing', 'description': 'Growing in belief and faith', 'icon_path': null},
      {'id': 'betrayal', 'title': 'Betrayal', 'description': 'Healing from betrayal', 'icon_path': null},
      {'id': 'change', 'title': 'Change', 'description': 'Embracing and navigating change', 'icon_path': null},
      {'id': 'community', 'title': 'Community', 'description': 'Living in community with others', 'icon_path': null},
      {'id': 'compassion', 'title': 'Compassion', 'description': 'Showing compassion to others', 'icon_path': null},
      {'id': 'confidence', 'title': 'Confidence', 'description': 'Finding confidence in God', 'icon_path': null},
      {'id': 'contentment', 'title': 'Contentment', 'description': 'Finding contentment in all circumstances', 'icon_path': null},
      {'id': 'courage', 'title': 'Courage', 'description': 'Finding strength in difficult times', 'icon_path': null},
      {'id': 'endurance', 'title': 'Endurance', 'description': 'Persevering through trials', 'icon_path': null},
      {'id': 'enemies', 'title': 'Enemies', 'description': 'Loving and forgiving enemies', 'icon_path': null},
      {'id': 'evil', 'title': 'Evil', 'description': 'Overcoming evil with good', 'icon_path': null},
      {'id': 'faith', 'title': 'Faith', 'description': 'Strengthening your trust in God', 'icon_path': null},
      {'id': 'family', 'title': 'Family', 'description': 'Building strong family relationships', 'icon_path': null},
      {'id': 'fasting', 'title': 'Fasting', 'description': 'The discipline and power of fasting', 'icon_path': null},
      {'id': 'fear', 'title': 'Fear', 'description': 'Conquering fear with faith', 'icon_path': null},
      {'id': 'food', 'title': 'Food', 'description': 'Biblical perspective on food', 'icon_path': null},
      {'id': 'forgiveness', 'title': 'Forgiveness', 'description': 'Learning to forgive and be forgiven', 'icon_path': null},
      {'id': 'friendship', 'title': 'Friendship', 'description': 'Building godly friendships', 'icon_path': null},
      {'id': 'generosity', 'title': 'Generosity', 'description': 'The joy of giving', 'icon_path': null},
      {'id': 'gentleness', 'title': 'Gentleness', 'description': 'Cultivating a gentle spirit', 'icon_path': null},
      {'id': 'giving', 'title': 'Giving', 'description': 'Generous giving to others', 'icon_path': null},
      {'id': 'goodness', 'title': 'Goodness', 'description': 'Pursuing goodness in life', 'icon_path': null},
      {'id': 'gossip', 'title': 'Gossip', 'description': 'Guarding your tongue', 'icon_path': null},
      {'id': 'gratitude', 'title': 'Gratitude', 'description': 'Living with a thankful heart', 'icon_path': null},
      {'id': 'greed', 'title': 'Greed', 'description': 'Overcoming greed and materialism', 'icon_path': null},
      {'id': 'healing', 'title': 'Healing', 'description': 'Finding healing in God', 'icon_path': null},
      {'id': 'health', 'title': 'Health', 'description': 'Caring for your body and health', 'icon_path': null},
      {'id': 'heart', 'title': 'Heart', 'description': 'Guarding and renewing your heart', 'icon_path': null},
      {'id': 'heaven', 'title': 'Heaven', 'description': 'The hope of eternal life', 'icon_path': null},
      {'id': 'holiness', 'title': 'Holiness', 'description': 'Living a holy life', 'icon_path': null},
      {'id': 'honesty', 'title': 'Honesty', 'description': 'Living with integrity', 'icon_path': null},
      {'id': 'hope', 'title': 'Hope', 'description': 'Finding hope in God', 'icon_path': null},
      {'id': 'humility', 'title': 'Humility', 'description': 'Walking in humility', 'icon_path': null},
      {'id': 'idols', 'title': 'Idols', 'description': 'Avoiding idolatry', 'icon_path': null},
      {'id': 'jesus', 'title': 'Jesus', 'description': 'Knowing Jesus Christ', 'icon_path': null},
      {'id': 'joy', 'title': 'Joy', 'description': 'Finding joy in the Lord', 'icon_path': null},
      {'id': 'judgement', 'title': 'Judgement', 'description': 'Avoiding judgment of others', 'icon_path': null},
      {'id': 'life', 'title': 'Life', 'description': 'Living life abundantly', 'icon_path': null},
      {'id': 'listening', 'title': 'Listening', 'description': 'Listening to God and others', 'icon_path': null},
      {'id': 'love', 'title': 'Love', 'description': 'Understanding God\'s love and loving others', 'icon_path': null},
      {'id': 'marriage', 'title': 'Marriage', 'description': 'Building a strong marriage', 'icon_path': null},
      {'id': 'materialism', 'title': 'Materialism', 'description': 'Freedom from materialism', 'icon_path': null},
      {'id': 'peace', 'title': 'Peace', 'description': 'Experiencing God\'s perfect peace', 'icon_path': null},
    ];
  }

  // Sample verses with all three versions (KJV, MSG, AMP)
  static List<Map<String, dynamic>> _getAllVersesWithVersions() {
    final now = DateTime.now();
    return [
      // John 3:16 - Love
      {
        'id': 'john_3_16',
        'reference': 'John 3:16',
        'kjv': 'For God so loved the world, that he gave his only begotten Son, that whosoever believeth in him should not perish, but have everlasting life.',
        'msg': 'This is how much God loved the world: He gave his Son, his one and only Son. And this is why: so that no one need be destroyed; by believing in him, anyone can have a whole and lasting life.',
        'amp': 'For God so greatly loved and dearly prized the world that He [even] gave up His only begotten ( unique) Son, so that whoever believes in (trusts in, clings to, relies on) Him shall not perish (come to destruction, be lost) but have eternal (everlasting) life.',
        'image_url': null,
        'created_at': now.toIso8601String(),
      },

      // Philippians 4:6-7 - Anxiety
      {
        'id': 'philippians_4_6_7',
        'reference': 'Philippians 4:6-7',
        'kjv': 'Be careful for nothing; but in every thing by prayer and supplication with thanksgiving let your requests be made known unto God. And the peace of God, which passeth all understanding, shall keep your hearts and minds through Christ Jesus.',
        'msg': 'Don\'t fret or worry. Instead of worrying, pray. Let petitions and praises shape your worries into prayers, letting God know your concerns. Before you know it, a sense of God\'s wholeness, everything coming together for good, will come and settle you down.',
        'amp': 'Do not fret or have any anxiety about anything, but in every circumstance and in everything, by prayer and petition ( definite requests), with thanksgiving, continue to make your wants known to God. And God\'s peace [shall be yours, that tranquil state of a soul assured of its salvation through Christ, and so fearing nothing from God and being content with its earthly lot of whatever sort that is, that peace] which transcends all understanding shall garrison and mount guard over your hearts and minds in Christ Jesus.',
        'image_url': null,
        'created_at': now.toIso8601String(),
      },

      // Ephesians 4:26-27 - Anger
      {
        'id': 'ephesians_4_26_27',
        'reference': 'Ephesians 4:26-27',
        'kjv': 'Be ye angry, and sin not: let not the sun go down upon your wrath: Neither give place to the devil.',
        'msg': 'Go ahead and be angry. You do well to be angry — but don\'t use your anger as fuel for revenge. And don\'t stay angry. Don\'t go to bed angry. Don\'t give the Devil that kind of foothold in your life.',
        'amp': 'When angry, do not sin; do not ever let your wrath (your exasperation, your fury or indignation) last until the sun goes down. Leave no [such] room or foothold for the devil [give no opportunity to him].',
        'image_url': null,
        'created_at': now.toIso8601String(),
      },

      // Hebrews 11:1 - Faith
      {
        'id': 'hebrews_11_1',
        'reference': 'Hebrews 11:1',
        'kjv': 'Now faith is the substance of things hoped for, the evidence of things not seen.',
        'msg': 'The fundamental fact of existence is that this trust in God, this faith, is the firm foundation under everything that makes life worth living. It\'s our handle on what we can\'t see.',
        'amp': 'NOW FAITH is the assurance (the confirmation, the title deed) of the things [we] hope for, being the proof of things [we] do not see and the conviction of their reality [faith perceiving as real fact what is not revealed to the senses].',
        'image_url': null,
        'created_at': now.toIso8601String(),
      },

      // Proverbs 3:5-6 - Confidence/Trust
      {
        'id': 'proverbs_3_5_6',
        'reference': 'Proverbs 3:5-6',
        'kjv': 'Trust in the Lord with all thine heart; and lean not unto thine own understanding. In all thy ways acknowledge him, and he shall direct thy paths.',
        'msg': 'Trust God from the bottom of your heart; don\'t try to figure out everything on your own. Listen for God\'s voice in everything you do, everywhere you go; he\'s the one who will keep you on track.',
        'amp': 'Lean on, trust in, and be confident in the Lord with all your heart and mind and do not rely on your own insight or understanding. In all your ways know, recognize, and acknowledge Him, and He will direct and make straight and plain your paths.',
        'image_url': null,
        'created_at': now.toIso8601String(),
      },

      // 1 Peter 5:7 - Anxiety
      {
        'id': '1_peter_5_7',
        'reference': '1 Peter 5:7',
        'kjv': 'Casting all your care upon him; for he careth for you.',
        'msg': 'Live carefree before God; he is most careful with you.',
        'amp': 'Casting the whole of your care [all your anxieties, all your worries, all your concerns, once and for all] on Him, for He cares for you affectionately and cares about you watchfully.',
        'image_url': null,
        'created_at': now.toIso8601String(),
      },

      // Matthew 11:28-30 - Peace/Rest
      {
        'id': 'matthew_11_28_30',
        'reference': 'Matthew 11:28-30',
        'kjv': 'Come unto me, all ye that labour and are heavy laden, and I will give you rest. Take my yoke upon you, and learn of me; for I am meek and lowly in heart: and ye shall find rest unto your souls. For my yoke is easy, and my burden is light.',
        'msg': 'Are you tired? Worn out? Burned out on religion? Come to me. Get away with me and you\'ll recover your life. I\'ll show you how to take a real rest. Walk with me and work with me—watch how I do it. Learn the unforced rhythms of grace. I won\'t lay anything heavy or ill-fitting on you. Keep company with me and you\'ll learn to live freely and lightly.',
        'amp': 'Come to Me, all you who labor and are heavy-laden and overburdened, and I will cause you to rest. [I will ease and relieve and refresh your souls.] Take My yoke upon you and learn of Me, for I am gentle (meek) and humble (lowly) in heart, and you will find rest (relief and ease and refreshment and recreation and blessed quiet) for your souls. For My yoke is wholesome (useful, good—not harsh, hard, sharp, or pressing, but comfortable, gracious, and pleasant), and My burden is light and easy to be borne.',
        'image_url': null,
        'created_at': now.toIso8601String(),
      },

      // Romans 8:28 - Hope
      {
        'id': 'romans_8_28',
        'reference': 'Romans 8:28',
        'kjv': 'And we know that all things work together for good to them that love God, to them who are the called according to his purpose.',
        'msg': 'That\'s why we can be so sure that every detail in our lives of love for God is worked into something good.',
        'amp': 'We are assured and know that [God being a partner in their labor] all things work together and are [fitting into a plan] for good to and for those who love God and are called according to [His] design and purpose.',
        'image_url': null,
        'created_at': now.toIso8601String(),
      },

      // Psalm 23:1-4 - Peace/Comfort
      {
        'id': 'psalm_23_1_4',
        'reference': 'Psalm 23:1-4',
        'kjv': 'The Lord is my shepherd; I shall not want. He maketh me to lie down in green pastures: he leadeth me beside the still waters. He restoreth my soul: he leadeth me in the paths of righteousness for his name\'s sake. Yea, though I walk through the valley of the shadow of death, I will fear no evil: for thou art with me; thy rod and thy staff they comfort me.',
        'msg': 'God, my shepherd! I don\'t need a thing. You have bedded me down in lush meadows, you find me quiet pools to drink from. True to your word, you let me catch my breath and send me in the right direction. Even when the way goes through Death Valley, I\'m not afraid when you walk at my side. Your trusty shepherd\'s crook makes me feel secure.',
        'amp': 'The Lord is my Shepherd [to feed, guide, and shield me], I shall not lack. He makes me lie down in [fresh, tender] green pastures; He leads me beside the still and restful waters. He refreshes and restores my life (my self); He leads me in the paths of righteousness [uprightness and right standing with Him—not for my earning it, but] for His name\'s sake. Yes, though I walk through the [deep, sunless] valley of the shadow of death, I will fear or dread no evil, for You are with me; Your rod [to protect] and Your staff [to guide], they comfort me.',
        'image_url': null,
        'created_at': now.toIso8601String(),
      },

      // Isaiah 41:10 - Fear
      {
        'id': 'isaiah_41_10',
        'reference': 'Isaiah 41:10',
        'kjv': 'Fear thou not; for I am with thee: be not dismayed; for I am thy God: I will strengthen thee; yea, I will help thee; yea, I will uphold thee with the right hand of my righteousness.',
        'msg': 'Don\'t panic. I\'m with you. There\'s no need to fear for I\'m your God. I\'ll give you strength. I\'ll help you. I\'ll hold you steady, keep a firm grip on you.',
        'amp': 'Fear not [there is nothing to fear], for I am with you; do not look around you in terror and be dismayed, for I am your God. I will strengthen and harden you to difficulties, yes, I will help you; yes, I will hold you up and retain you with My [victorious] right hand of rightness and justice.',
        'image_url': null,
        'created_at': now.toIso8601String(),
      },
    ];
  }

  static List<Map<String, dynamic>> _getAllIssueVerseRelationships() {
    return [
      // Love
      {'issue_id': 'love', 'verse_id': 'john_3_16'},

      // Anxiety
      {'issue_id': 'anxiety', 'verse_id': 'philippians_4_6_7'},
      {'issue_id': 'anxiety', 'verse_id': '1_peter_5_7'},

      // Anger
      {'issue_id': 'anger', 'verse_id': 'ephesians_4_26_27'},

      // Faith
      {'issue_id': 'faith', 'verse_id': 'hebrews_11_1'},

      // Confidence
      {'issue_id': 'confidence', 'verse_id': 'proverbs_3_5_6'},

      // Peace
      {'issue_id': 'peace', 'verse_id': 'matthew_11_28_30'},
      {'issue_id': 'peace', 'verse_id': 'psalm_23_1_4'},

      // Hope
      {'issue_id': 'hope', 'verse_id': 'romans_8_28'},

      // Fear
      {'issue_id': 'fear', 'verse_id': 'isaiah_41_10'},
    ];
  }

  static List<Map<String, dynamic>> _getDailyVerses() {
    final now = DateTime.now();
    final today = DateTime(now.year, now.month, now.day);

    return [
      {
        'date': today.toIso8601String(),
        'verse_id': 'john_3_16',
        'inspirational_message': 'Remember that God\'s love for you is unconditional and eternal.',
      },
    ];
  }
}