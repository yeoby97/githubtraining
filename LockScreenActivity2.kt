
val defaultAppList = listOf(
    AppItem("phone",    "전화",    Icons.Filled.Phone,                Color(0xFF4CAF50)),
    AppItem("message",  "메시지",   Icons.AutoMirrored.Filled.Message, Color(0xFF2196F3)),
    AppItem("camera",   "카메라",   Icons.Filled.CameraAlt,            Color(0xFF424242)),
    AppItem("gallery",  "갤러리",   Icons.Filled.PhotoLibrary,         Color(0xFFE91E63)),
    AppItem("internet", "인터넷",   Icons.Filled.Language,             Color(0xFF03A9F4)),
    AppItem("music",    "음악",    Icons.Filled.MusicNote,            Color(0xFF9C27B0)),
    AppItem("youtube",  "유튜브",   Icons.Filled.PlayCircle,           Color(0xFFF44336)),
    AppItem("kakao",    "카카오톡", Icons.AutoMirrored.Filled.Chat,    Color(0xFFFBC02D)),
    AppItem("calendar", "캘린더",   Icons.Filled.CalendarMonth,        Color(0xFF3F51B5)),
    AppItem("email",    "이메일",   Icons.Filled.Email,                Color(0xFFFF5722)),
    AppItem("maps",     "지도",    Icons.Filled.Map,                  Color(0xFF4CAF50)),
    AppItem("contacts", "연락처",   Icons.Filled.Contacts,             Color(0xFF009688)),
    AppItem("settings", "설정",    Icons.Filled.Settings,             Color(0xFF424242)),
)

// ============================================================
// 메인 화면
// ============================================================

@Composable
fun LockScreen(onUnlock: () -> Unit) {
    var isFloating by remember { mutableStateOf(false) }
    var showShortcutPopup by remember { mutableStateOf(false) }
    var showAppWidgetSheet by remember { mutableStateOf(false) }
    var showLockWidgetPicker by remember { mutableStateOf(false) }
    var addedApps by remember { mutableStateOf(listOf<AppItem>()) }
    var placedWidgets by remember { mutableStateOf<List<PlacedWidget>>(emptyList()) }
    var addCounter by remember { mutableStateOf(0) }

    var clockOffset by remember { mutableStateOf(Offset.Zero) }
    var greenBoxOffset by remember { mutableStateOf(Offset.Zero) }
    var savedClockOffset by remember { mutableStateOf(Offset.Zero) }

    BackHandler(enabled = isFloating) {
        clockOffset = savedClockOffset
        greenBoxOffset = Offset.Zero
        isFloating = false
    }

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp

    val scale by animateFloatAsState(
        targetValue = if (isFloating) 0.7f else 1f,
        animationSpec = tween(500, easing = FastOutSlowInEasing), label = "scale"
    )
    val cornerRadius by animateDpAsState(
        targetValue = if (isFloating) 30.dp else 0.dp,
        animationSpec = tween(500, easing = FastOutSlowInEasing), label = "corner"
    )
    val blurRadius by animateDpAsState(
        targetValue = if (isFloating) 20.dp else 0.dp,
        animationSpec = tween(500, easing = FastOutSlowInEasing), label = "blur"
    )

    // 슬롯 크기 계산 (4칸 기준, 간격 8dp * 3)
    val slotGap = 8.dp
    val slotSize = (screenWidth * 0.1f)

    Box(modifier = Modifier.fillMaxSize()) {

        // 바깥 배경 (floating 시 blur)
        Image(
            painter = painterResource(id = R.drawable.images),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
                .then(if (blurRadius > 0.dp) Modifier.blur(blurRadius) else Modifier)
        )

        // 안쪽 (정상 배경 + 컨텐츠)
        Box(
            modifier = Modifier.fillMaxSize()
                .graphicsLayer {
                    scaleX = scale; scaleY = scale
                    transformOrigin = TransformOrigin(0.5f, 0.2f)
                }
                .clip(RoundedCornerShape(cornerRadius))
                .pointerInput(Unit) {
                    detectTapGestures(onPress = {
                        val t0 = System.currentTimeMillis()
                        val released = try {
                            withTimeout(500) { tryAwaitRelease(); true }
                        } catch (e: TimeoutCancellationException) { isFloating = true; false }
                        if (!isFloating && released && System.currentTimeMillis() - t0 >= 500)
                            isFloating = true
                    })
                }
        ) {
            Image(
                painter = painterResource(id = R.drawable.images),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // 상단
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (isFloating) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Button(onClick = {}) { Text("배경화면") }
                            Button(onClick = {
                                savedClockOffset = clockOffset
                                greenBoxOffset = Offset.Zero
                                isFloating = false
                            }) { Text("확인") }
                        }
                    } else {
                        Spacer(modifier = Modifier.height(48.dp))
                    }

                    Spacer(modifier = Modifier.height(screenHeight * 0.05f))

                    // 시계 + 날짜 + 위젯 슬롯
                    Column(
                        modifier = Modifier
                            .offset { IntOffset(clockOffset.x.roundToInt(), clockOffset.y.roundToInt()) }
                            .pointerInput(isFloating) {
                                if (isFloating) detectDragGestures { change, drag ->
                                    change.consume(); clockOffset += drag
                                }
                            },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val timeFormat = SimpleDateFormat("hh:mm", Locale.getDefault())
                        Text(
                            text = timeFormat.format(Calendar.getInstance().time),
                            color = Color.White, fontSize = 64.sp,
                            fontWeight = FontWeight.Light, letterSpacing = (-2).sp
                        )
                        val dateFormat = SimpleDateFormat("M월 d일 EEEE", Locale.KOREAN)
                        Text(
                            text = dateFormat.format(Calendar.getInstance().time),
                            color = Color.White.copy(alpha = 0.9f), fontSize = 16.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )

                        // ─── 위젯 슬롯 Row ───
                        Spacer(modifier = Modifier.height(16.dp))
                        WidgetSlotRow(
                            placedWidgets = placedWidgets,
                            isFloating = isFloating,
                            slotSize = slotSize,
                            slotGap = slotGap,
                            onRemove = { uid -> placedWidgets = placedWidgets.filter { it.uid != uid } },
                            onAdd = { showLockWidgetPicker = true }
                        )
                    }

                    if (addedApps.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(screenHeight * 0.03f))
                        AddedAppsRow(apps = addedApps)
                    }
                }

                // 하단 초록 바
                Box(
                    modifier = Modifier
                        .padding(bottom = screenHeight * 0.05f)
                        .offset { IntOffset(greenBoxOffset.x.roundToInt(), greenBoxOffset.y.roundToInt()) }
                        .pointerInput(isFloating) {
                            if (isFloating) detectDragGestures { c, d -> c.consume(); greenBoxOffset += d }
                        }
                        .pointerInput(isFloating) {
                            if (isFloating) detectTapGestures(onTap = { showShortcutPopup = true })
                        }
                        .clip(RoundedCornerShape(20.dp))
                        .border(if (isFloating) 2.dp else 0.dp, Color.LightGray, RoundedCornerShape(20.dp))
                        .height(50.dp).width(200.dp)
                        .background(Color.Green)
                )
            }
        }

        // 항목 선택 팝업
        if (showShortcutPopup) {
            ShortcutPickerDialog(
                onDismiss = { showShortcutPopup = false },
                onSelect = { selected ->
                    showShortcutPopup = false
                    when (selected) {
                        "app_widget" -> showAppWidgetSheet = true
                        "favorite_app" -> {}
                        "text" -> {}
                    }
                }
            )
        }

        // 잠금화면 위젯 선택 (2단계 바텀시트)
        if (showLockWidgetPicker) {
            LockWidgetPickerSheet(
                onDismiss = { showLockWidgetPicker = false },
                onWidgetSelected = { widget ->
                    val needed = if (widget.size == WidgetSize.WIDE) 2 else 1
                    val used = placedWidgets.sumOf { if (it.widget.size == WidgetSize.WIDE) 2 else 1 }
                    if (used + needed <= 4) {
                        addCounter++
                        placedWidgets = placedWidgets + PlacedWidget("${widget.id}_$addCounter", widget)
                    }
                    showLockWidgetPicker = false
                }
            )
        }

        // 앱 위젯 바텀시트
        if (showAppWidgetSheet) {
            AppWidgetBottomSheet(
                apps = defaultAppList,
                onDismiss = { showAppWidgetSheet = false },
                onAppSelected = { app ->
                    if (addedApps.none { it.id == app.id }) addedApps = addedApps + app
                    showAppWidgetSheet = false
                }
            )
        }
    }
}

// ============================================================
// 위젯 슬롯 Row (4칸)
// ============================================================
