package com.akira.island.core

/** Что делает свайп вниз по свёрнутой капсуле. */
enum class SwipeDownAction { NONE, EXPAND, QUICK_MENU }

/**
 * Чистое решение по свайпу вниз — без Android, чтобы его можно было проверить Unit-тестами.
 *
 * [menuEnabled] включён: по капсуле с активностью открывается быстрое меню, а пустая капсула,
 * как и раньше, раскрывает свою панель (она и есть то же меню). Настройка [swipeExpand] в этом
 * режиме не участвует: меню имеет приоритет.
 *
 * [menuEnabled] выключен: прежнее поведение, полностью под управлением [swipeExpand] и [idleExpand].
 *
 * [cancelled] — жест оборвала система. Тогда меню открывается по короткому движению вниз, пока
 * системный жест ещё не перехватил касание; при выключенном меню отменённый жест ничего не делает.
 */
fun swipeDownAction(
    expanded: Boolean,
    vertical: Boolean,
    dy: Float,
    threshold: Float,
    transient: Boolean,
    idle: Boolean,
    swipeExpand: Boolean,
    idleExpand: Boolean,
    menuEnabled: Boolean,
    cancelled: Boolean = false,
): SwipeDownAction {
    if (cancelled && !menuEnabled) return SwipeDownAction.NONE
    if (expanded || transient || !vertical || dy <= threshold) return SwipeDownAction.NONE
    if (menuEnabled) return if (idle && !idleExpand) SwipeDownAction.NONE else if (idle) SwipeDownAction.EXPAND else SwipeDownAction.QUICK_MENU
    if (!swipeExpand) return SwipeDownAction.NONE
    if (idle && !idleExpand) return SwipeDownAction.NONE
    return SwipeDownAction.EXPAND
}

/**
 * Быстрое меню поверх текущей активности. Помнит, какая капсула была на экране при открытии:
 * при закрытии активность не останавливается, и остров возвращается к ней же.
 */
data class QuickMenu(val activityId: String?) {
    /** Капсула, которую показываем после закрытия меню. */
    val capsuleOnClose: String? get() = activityId
}

/** Переходы быстрого меню без Android: их проверяют Unit-тесты, а Controls их применяет. */
object QuickMenuLogic {
    /** Открытие не трогает активность, только запоминает капсулу для возврата. */
    fun open(activityId: String?): QuickMenu = QuickMenu(activityId)

    /** Закрытие убирает меню; активность осталась на месте, поэтому возвращается её капсула. */
    fun close(): QuickMenu? = null
}
