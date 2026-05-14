class LockScreenActivity : ComponentActivity() {
    private lateinit var keyguardManager: KeyguardManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        keyguardManager = getSystemService(KEYGUARD_SERVICE) as KeyguardManager
        setShowWhenLocked(true)
        setTurnScreenOn(true)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O_MR1) {
            keyguardManager.requestDismissKeyguard(this, null)
        }
        setContent {
            LockScreenTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    LockScreen(onUnlock = { unlockDevice() })
                }
            }
        }
    }
    private fun unlockDevice() {
        Toast.makeText(this, "Device unlocked!", Toast.LENGTH_SHORT).show()
    }
}

// ============================================================
// 데이터 모델
// ============================================================

enum class WidgetSize { SMALL, WIDE }  // 1x1, 2x1

data class LockWidget(
    val id: String,
    val appId: String,
    val name: String,
    val size: WidgetSize,
    val icon: ImageVector? = null,
    val iconTint: Color = Color.White,
    val mainValue: String = "",
    val subValue: String = ""
)

data class PlacedWidget(
    val uid: String,
    val widget: LockWidget
)

data class WidgetApp(
    val id: String,
    val name: String,
    val icon: ImageVector,
    val iconBg: Color,
    val widgets: List<LockWidget>
)

data class AppItem(
    val id: String,
    val name: String,
    val icon: ImageVector,
    val tint: Color
)

// ============================================================
// 위젯 앱 데이터 (스크린샷 기반)
// ============================================================

val lockWidgetApps = listOf(
    WidgetApp("weather", "날씨", Icons.Filled.WbSunny, Color(0xFF4DAAED), listOf(
        LockWidget("w_rain",  "weather", "강수 확률",       WidgetSize.SMALL, Icons.Filled.WaterDrop, Color.White,           "88%"),
        LockWidget("w_temp",  "weather", "현재 온도와 날씨", WidgetSize.SMALL, Icons.Filled.CloudQueue, Color.White,          "23°"),
        LockWidget("w_uv",    "weather", "자외선",          WidgetSize.SMALL, Icons.Filled.WbSunny,   Color(0xFFFFB300),    "7"),
        LockWidget("w_sun",   "weather", "일출과 일몰",      WidgetSize.SMALL, null,                  Color.White,           "오전\n7:45"),
        LockWidget("w_temp2", "weather", "현재 온도와 날씨", WidgetSize.WIDE,  Icons.Filled.CloudQueue, Color.White,         "23°", "도시 이름"),
        LockWidget("w_dust",  "weather", "미세/초미세먼지",  WidgetSize.WIDE,  Icons.Filled.Air,       Color.White,           "15 / 38"),
    )),
    WidgetApp("clock", "시계", Icons.Filled.AccessTime, Color(0xFF636366), listOf(
        LockWidget("c_alarm", "clock", "곧 울릴 알람", WidgetSize.SMALL, Icons.Filled.Alarm,      Color.White, "오전\n6:00"),
        LockWidget("c_world", "clock", "세계시각",     WidgetSize.SMALL, Icons.Filled.AccessTime, Color.White, "오전\n6:30", "런던"),
    )),
    WidgetApp("calendar", "캘린더", Icons.Filled.CalendarMonth, Color(0xFF30B080), listOf(
        LockWidget("cal_today",  "calendar", "오늘",     WidgetSize.SMALL, null,                       Color.White, "목\n14"),
        LockWidget("cal_dday",   "calendar", "디데이",   WidgetSize.SMALL, null,                       Color.White, "19",   "일 남음"),
        LockWidget("cal_next",   "calendar", "다음 일정", WidgetSize.SMALL, Icons.Filled.CalendarMonth, Color.White),
        LockWidget("cal_next2",  "calendar", "다음 일정", WidgetSize.WIDE,  Icons.Filled.CalendarMonth, Color.White, "내일", "스승의날"),
        LockWidget("cal_dday2",  "calendar", "디데이",   WidgetSize.WIDE,  null,                       Color.White, "내 생일", "19일 남음"),
        LockWidget("cal_today2", "calendar", "오늘",     WidgetSize.WIDE,  null,                       Color.White, "14",   "5월 목요일"),
    )),
    WidgetApp("battery", "배터리", Icons.Filled.BatteryFull, Color(0xFF30B0C7), listOf(
        LockWidget("bat1", "battery", "배터리 상태(원형)", WidgetSize.SMALL, Icons.Filled.BatteryFull, Color.White, "53"),
        LockWidget("bat2", "battery", "배터리 상태(원형)", WidgetSize.WIDE,  Icons.Filled.BatteryFull, Color.White, "92", "53"),
    )),
    WidgetApp("health", "삼성 헬스", Icons.Filled.Favorite, Color(0xFF32D74B), listOf(
        LockWidget("h1", "health", "일일 활동", WidgetSize.SMALL, Icons.Filled.Favorite, Color(0xFF32D74B)),
        LockWidget("h2", "health", "일일 활동", WidgetSize.WIDE,  Icons.Filled.Favorite, Color(0xFF32D74B), "4,350", "76 / 458"),
    )),
    WidgetApp("reminder", "리마인더", Icons.Filled.CheckCircle, Color(0xFF7C3AED), listOf(
        LockWidget("r1", "reminder", "카테고리", WidgetSize.SMALL, Icons.Filled.CheckCircle, Color(0xFF7C3AED), "3"),
        LockWidget("r2", "reminder", "카테고리", WidgetSize.WIDE,  null,                    Color.White,        "숙제 / 운동", "티켓 구매"),
    )),
    WidgetApp("routines", "모드 및 루틴", Icons.Filled.Schedule, Color(0xFF5B5EA6), listOf(
        LockWidget("rt1", "routines", "루틴", WidgetSize.SMALL, Icons.Filled.Schedule, Color(0xFF5B5EA6)),
        LockWidget("rt2", "routines", "루틴", WidgetSize.WIDE,  Icons.Filled.Schedule, Color(0xFF5B5EA6), "회의"),
    )),
)
