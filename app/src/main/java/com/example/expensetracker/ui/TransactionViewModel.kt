package com.example.expensetracker.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.data.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import androidx.compose.ui.graphics.Color
import com.example.expensetracker.ui.charts.ChartData
import com.example.expensetracker.ui.charts.TrendPoint
import javax.inject.Inject

@HiltViewModel
class TransactionViewModel @Inject constructor(
    private val repository: TransactionRepository,
    private val budgetManager: com.example.expensetracker.utils.BudgetManager,
    private val backupManager: com.example.expensetracker.utils.BackupManager,
    private val notificationHelper: com.example.expensetracker.utils.NotificationHelper
) : ViewModel() {

    private val notifiedLimitIds = mutableSetOf<Int>()


    private val _selectedMonth = MutableStateFlow(Calendar.getInstance())
    val selectedMonth: StateFlow<Calendar> = _selectedMonth.asStateFlow()

    private val _filterCategoryId = MutableStateFlow<Int?>(null)
    val filterCategoryId: StateFlow<Int?> = _filterCategoryId.asStateFlow()

    private val _currentTheme = MutableStateFlow(com.example.expensetracker.ui.theme.AppTheme.System)
    val currentTheme: StateFlow<com.example.expensetracker.ui.theme.AppTheme> = _currentTheme.asStateFlow()

    val spendingLimits: StateFlow<List<SpendingLimit>> = repository.allSpendingLimits
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val limitStatuses: StateFlow<List<LimitStatus>> = combine(
        repository.allSpendingLimits,
        repository.allTransactions
    ) { limits, transactions ->
        val statuses = budgetManager.checkLimits(limits, transactions)

        // Check for breaches and notify
        statuses.forEach { status ->
            if (status.isBreached) {
                if (!notifiedLimitIds.contains(status.limit.id)) {
                    notificationHelper.sendLimitBreachNotification(
                        status.limit.id,
                        status.limit.name,
                        status.spentAmount,
                        status.limit.amount
                    )
                    notifiedLimitIds.add(status.limit.id)
                }
            } else {
                // Reset notification state if back within limit
                notifiedLimitIds.remove(status.limit.id)
            }
        }
        statuses
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addSpendingLimit(limit: SpendingLimit) {
        viewModelScope.launch {
            repository.addSpendingLimit(limit)
        }
    }

    fun updateSpendingLimit(limit: SpendingLimit) {
        viewModelScope.launch {
            repository.updateSpendingLimit(limit)
        }
    }

    fun deleteSpendingLimit(limit: SpendingLimit) {
        viewModelScope.launch {
            repository.deleteSpendingLimit(limit)
        }
    }

    fun setTheme(theme: com.example.expensetracker.ui.theme.AppTheme) {
        _currentTheme.value = theme
    }

    private val _customDateRange = MutableStateFlow<Pair<Long, Long>?>(null)
    val customDateRange: StateFlow<Pair<Long, Long>?> = _customDateRange.asStateFlow()

    fun setCustomDateRange(start: Long, end: Long) {
        _customDateRange.value = start to end
    }

    fun clearCustomDateRange() {
        _customDateRange.value = null
    }

    private val _merchantFilter = MutableStateFlow<String?>(null)
    val merchantFilter: StateFlow<String?> = _merchantFilter.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val transactions: StateFlow<List<Transaction>> = combine(
        _selectedMonth,
        _filterCategoryId,
        _customDateRange,
        _merchantFilter
    ) { calendar, filterId, dateRange, merchantName ->
        data class FilterParams(val calendar: Calendar, val filterId: Int?, val dateRange: Pair<Long, Long>?, val merchantName: String?)
        FilterParams(calendar, filterId, dateRange, merchantName)
    }
        .flatMapLatest { params ->
            val (start, end) = params.dateRange ?: getMonthRange(params.calendar)
            repository.getTransactionsForMonth(start, end).map { list ->
                var filtered = list
                if (params.filterId != null) {
                    filtered = filtered.filter { it.categoryId == params.filterId }
                }
                if (params.merchantName != null) {
                    filtered = filtered.filter { it.merchant == params.merchantName }
                }
                filtered
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val totalExpense: StateFlow<Double?> = combine(
        _selectedMonth,
        _customDateRange,
        _filterCategoryId // Include filterId to update total based on filter
    ) { calendar, dateRange, filterId ->
        Triple(calendar, dateRange, filterId)
    }
        .flatMapLatest { (calendar, dateRange, filterId) ->
            val (start, end) = dateRange ?: getMonthRange(calendar)
            // If we want total to reflect the filtered list, we should probably compute it from the list or use a specific query.
            // Existing repo method getTotalExpenseForMonth gets ALL expenses for time range.
            // If category filter is active, we should ideally sum only those.
            // For now, let's stick to the repo method if no category filter, or manual filter if there is one?
            // Actually, usually "Total Expense" on screen shows the total of what's visible.
            // Let's use the transactions flow to derive total to ensure consistency?
            // But transactions flow is List<Transaction>.
            // Let's filter manually if filterId is present.
            if (filterId != null) {
                repository.getTransactionsForMonth(start, end).map { list ->
                     list.filter { it.categoryId == filterId }.sumOf { it.amount }
                }
            } else {
                repository.getTotalExpenseForMonth(start, end)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    @OptIn(ExperimentalCoroutinesApi::class)
    val categoryExpenses: StateFlow<List<CategoryExpense>> = combine(
        _selectedMonth,
        _customDateRange
    ) { calendar, dateRange ->
        dateRange ?: getMonthRange(calendar)
    }
        .flatMapLatest { (start, end) ->
            repository.getExpensesByCategory(start, end)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories: StateFlow<List<Category>> = repository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun nextMonth() {
        val newCal = _selectedMonth.value.clone() as Calendar
        newCal.add(Calendar.MONTH, 1)
        _selectedMonth.value = newCal
        _customDateRange.value = null // Reset custom range on navigation
        // _filterCategoryId.value = null // REMOVED: Keep filter active
    }

    fun prevMonth() {
        val newCal = _selectedMonth.value.clone() as Calendar
        newCal.add(Calendar.MONTH, -1)
        _selectedMonth.value = newCal
        _customDateRange.value = null // Reset custom range on navigation
        // _filterCategoryId.value = null // REMOVED: Keep filter active
    }

    fun setCategoryFilter(categoryId: Int?) {
        _filterCategoryId.value = categoryId
    }

    fun setMerchantFilter(merchantName: String?) {
        _merchantFilter.value = merchantName
        // Reset category filter when merchant filter is set
        if (merchantName != null) {
            _filterCategoryId.value = null
        }
    }
    
    val uniqueMerchants: StateFlow<List<String>> = transactions
        .map { txns ->
            txns.map { it.merchant }.distinct().sorted()
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.updateTransaction(transaction)
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

    // Selection Mode Logic
    private val _isSelectionMode = MutableStateFlow(false)
    val isSelectionMode: StateFlow<Boolean> = _isSelectionMode.asStateFlow()

    private val _selectedTransactionIds = MutableStateFlow<Set<Int>>(emptySet())
    val selectedTransactionIds: StateFlow<Set<Int>> = _selectedTransactionIds.asStateFlow()

    fun toggleSelection(transactionId: Int) {
        val currentSelection = _selectedTransactionIds.value.toMutableSet()
        if (currentSelection.contains(transactionId)) {
            currentSelection.remove(transactionId)
        } else {
            currentSelection.add(transactionId)
        }
        _selectedTransactionIds.value = currentSelection
        
        // Ensure selection mode is active (triggered by long press or first selection)
        // User requested NOT to auto-exit if empty.
        _isSelectionMode.value = true
    }

    fun clearSelection() {
        _selectedTransactionIds.value = emptySet()
        _isSelectionMode.value = false
    }

    fun deleteSelectedTransactions() {
        viewModelScope.launch {
            val selectedIds = _selectedTransactionIds.value
            val currentTransactions = transactions.value
            
            // NOTE: If we are in "Global Selection Mode" (Filter), currentTransactions might not contain all selected IDs 
            // if we are only viewing a specific month.
            // However, our deleteTransactions DAO method takes a LIST of objects.
            // If we only have IDs, we need to be careful.
            // Actually, `selectedTransactionIds` tracks IDs.
            // We should probably just delete by IDs for robustness, OR ensure we have the objects.
            // Given the requirement "exclude some messages", the user likely did this on the Home Screen.
            // But if we selected items from *outside* the current month view?
            // The plan dictates we bring them into view.
            // So `transactions.value` should contain them if we updated the filters correctly.
            
            val toDelete = currentTransactions.filter { selectedIds.contains(it.id) }
            
            // Fallback: If we selected items but they are somehow not in current list (edge case),
            // we might miss them. But since we force filters to match, it should be fine.
            // Ideally Repo should support deleteByIds, but let's stick to this for now.
            
            if (toDelete.isNotEmpty()) {
                repository.deleteTransactions(toDelete)
            }
            clearSelection()
        }
    }

    fun selectTransactionsByFilter(minDate: Long?, maxDate: Long?, categoryId: Int?, merchant: String?) {
        viewModelScope.launch {
            val matches = repository.getTransactionsByFilter(minDate, maxDate, categoryId, merchant)
            if (matches.isNotEmpty()) {
                val ids = matches.map { it.id }.toSet()
                _selectedTransactionIds.value = ids
                _isSelectionMode.value = true
                
                // FORCE UPDATE VIEW FILTERS TO SHOW THESE ITEMS
                // 1. Date Range: If provided, use it. If not, use min/max of results.
                if (minDate != null && maxDate != null) {
                    _customDateRange.value = minDate to maxDate
                } else {
                    // Find min/max from results to ensure visibility
                    val min = matches.minOf { it.date }
                    val max = matches.maxOf { it.date }
                    // Expand slightly to cover full days? 
                    // Actually, let's just use the exact range or just clear date filter if it was limited?
                    // Safe bet: Set custom range to cover the results.
                    _customDateRange.value = min to max
                }
                
                // 2. Category: If provided, use it.
                if (categoryId != null) {
                    _filterCategoryId.value = categoryId
                } else {
                    _filterCategoryId.value = null // Show all categories
                }
                
                // 3. Merchant: If provided, use it? 
                // Actually, if we filter by LIKE merchant, we might filter the view by exact merchant name?
                // `merchantFilter` in VM filters by Exact Match currently: `it.merchant == params.merchantName`.
                // Our DAO query was `LIKE`.
                // If we set `merchantFilter` to the search term, and there are multiple merchants matching (e.g. "Uber" matches "Uber One" and "Uber Eats"),
                // the UI filter logic `it.merchant == "Uber"` will HIDE "Uber Eats"!
                // So we must NOT set `merchantFilter` if it's a partial match.
                // Instead, we should rely on the Date/Category filters + the fact that we selected them.
                // BUT, if we don't filter the view, other non-matching items for that date range will show up (Unselected).
                // This is actually GOOD behavior: User sees "Selected matches" mixed with "Unselected non-matches", providing context.
                // So let's CLEAR merchant filter to ensure we see all potential matches.
                _merchantFilter.value = null 
            }
        }
    }

    fun addCategory(name: String, iconName: String, onResult: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            if (repository.isCategoryNameDuplicate(name)) {
                onResult(false)
            } else {
                repository.addCategory(Category(name = name, iconName = iconName))
                onResult(true)
            }
        }
    }

    fun updateCategory(category: Category, onResult: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            // Unique check if name changed
            repository.updateCategory(category)
            onResult(true)
        }
    }

    fun deleteCategory(category: Category, unlinkTransactions: Boolean) {
        viewModelScope.launch {
            repository.deleteCategoryWithStrategy(category, unlinkTransactions)
        }
    }

    fun addManualTransaction(amount: Double, merchant: String, date: Long, categoryId: Int?, tags: String? = null) {
        viewModelScope.launch {
            repository.addTransaction(
                Transaction(
                    amount = amount,
                    merchant = merchant,
                    date = date,
                    sender = "Manual",
                    fullMessage = "Manually entered",
                    categoryId = categoryId,
                    isEdited = true,
                    tags = tags
                )
            )
        }
    }

    fun importFromSms(messages: List<com.example.expensetracker.data.SmsMessage>) {
        viewModelScope.launch {
            val transactions = messages.mapNotNull { msg ->
                // Reuse existing parser
                com.example.expensetracker.core.SmsParser.parseSms(msg.sender, msg.body, msg.date)
            }
            
            // Apply auto-categorization
            transactions.forEach { transaction ->
                val mapping = repository.getMappingForMerchant(transaction.merchant)
                if (mapping != null) {
                    transaction.categoryId = mapping.categoryId
                    transaction.tags = mapping.tags
                }
                repository.addTransaction(transaction)
            }
        }
    }

    fun saveMerchantMapping(merchantName: String, categoryId: Int, tags: String? = null) {
        viewModelScope.launch {
            repository.addMapping(MerchantMapping(merchantName, categoryId, tags))
        }
    }

    // Analytics Data

    private val colors = listOf(
        Color(0xFFEF5350), // Red
        Color(0xFF42A5F5), // Blue
        Color(0xFF66BB6A), // Green
        Color(0xFFFFA726), // Orange
        Color(0xFFAB47BC), // Purple
        Color(0xFF26C6DA), // Cyan
        Color(0xFFFF7043)  // Deep Orange
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val pieChartData: StateFlow<List<com.example.expensetracker.ui.charts.ChartData>> = combine(
        transactions,
        categories,
        _filterCategoryId
    ) { txns, cats, filterId ->
        Triple(txns, cats, filterId)
    }
        .map { (txns, cats, filterId) ->
            if (txns.isEmpty()) return@map emptyList()

            val total = txns.sumOf { it.amount }
            if (total == 0.0) return@map emptyList()

            val grouped = if (filterId == null) {
                // Group by Category
                txns.groupBy { it.categoryId }
                    .map { (catId, list) ->
                        val cat = cats.find { it.id == catId }
                        val name = cat?.name ?: "Uncategorized"
                        name to list.sumOf { it.amount }
                    }
            } else {
                // Group by Merchant/Tag (Drill-down)
                // Priority: Tag -> Merchant
                txns.groupBy { txn ->
                    val tag = txn.tags?.split(",")?.firstOrNull { it.isNotBlank() }
                    tag ?: txn.merchant
                }.map { (name, list) ->
                    name to list.sumOf { it.amount }
                }
            }

            // Sort descending
            val sorted = grouped.sortedByDescending { it.second }

            // Take top 5, group rest as "Others"
            val top5 = sorted.take(5)
            val others = sorted.drop(5)
            
            val result = top5.mapIndexed { index, (label, value) ->
                com.example.expensetracker.ui.charts.ChartData(
                    label = label,
                    value = value,
                    color = colors.getOrElse(index) { Color.Gray },
                    percentage = (value / total).toFloat()
                )
            }.toMutableList()

            if (others.isNotEmpty()) {
                val othersTotal = others.sumOf { it.second }
                result.add(
                    com.example.expensetracker.ui.charts.ChartData(
                        label = "Others",
                        value = othersTotal,
                        color = Color.LightGray,
                        percentage = (othersTotal / total).toFloat()
                    )
                )
            }
            result
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val trendLineData: StateFlow<List<com.example.expensetracker.ui.charts.TrendPoint>> = transactions
        .map { txns ->
            if (txns.isEmpty()) return@map emptyList()

            // Group by Day
            val grouped = txns.groupBy { txn ->
                val cal = Calendar.getInstance()
                cal.timeInMillis = txn.date
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                cal.timeInMillis
            }

            grouped.map { (date, list) ->
                com.example.expensetracker.ui.charts.TrendPoint(
                    dateMillis = date,
                    amount = list.sumOf { it.amount }
                )
            }.sortedBy { it.dateMillis }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun backupData(context: android.content.Context, uri: android.net.Uri, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = backupManager.exportData(context, uri)
            onResult(success)
        }
    }

    fun restoreData(context: android.content.Context, uri: android.net.Uri, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = backupManager.importData(context, uri)
            onResult(success)
        }
    }

    private fun getMonthRange(calendar: Calendar): Pair<Long, Long> {
        val start = calendar.clone() as Calendar
        start.set(Calendar.DAY_OF_MONTH, 1)
        start.set(Calendar.HOUR_OF_DAY, 0)
        start.set(Calendar.MINUTE, 0)
        start.set(Calendar.SECOND, 0)
        start.set(Calendar.MILLISECOND, 0)

        val end = calendar.clone() as Calendar
        end.set(Calendar.DAY_OF_MONTH, end.getActualMaximum(Calendar.DAY_OF_MONTH))
        end.set(Calendar.HOUR_OF_DAY, 23)
        end.set(Calendar.MINUTE, 59)
        end.set(Calendar.SECOND, 59)
        end.set(Calendar.MILLISECOND, 999)

        return start.timeInMillis to end.timeInMillis
    }
}
