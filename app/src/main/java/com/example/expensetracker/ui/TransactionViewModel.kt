package com.example.expensetracker.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.data.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class TransactionViewModel @Inject constructor(
    private val repository: TransactionRepository
) : ViewModel() {

    private val _selectedMonth = MutableStateFlow(Calendar.getInstance())
    val selectedMonth: StateFlow<Calendar> = _selectedMonth.asStateFlow()

    private val _filterCategoryId = MutableStateFlow<Int?>(null)
    val filterCategoryId: StateFlow<Int?> = _filterCategoryId.asStateFlow()

    private val _currentTheme = MutableStateFlow(com.example.expensetracker.ui.theme.AppTheme.System)
    val currentTheme: StateFlow<com.example.expensetracker.ui.theme.AppTheme> = _currentTheme.asStateFlow()

    fun setTheme(theme: com.example.expensetracker.ui.theme.AppTheme) {
        _currentTheme.value = theme
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val transactions: StateFlow<List<Transaction>> = combine(
        _selectedMonth,
        _filterCategoryId
    ) { calendar, filterId ->
        calendar to filterId
    }
        .flatMapLatest { (calendar, filterId) ->
            val (start, end) = getMonthRange(calendar)
            repository.getTransactionsForMonth(start, end).map { list ->
                if (filterId != null) {
                    list.filter { it.categoryId == filterId }
                } else {
                    list
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val totalExpense: StateFlow<Double?> = _selectedMonth
        .flatMapLatest { calendar ->
            val (start, end) = getMonthRange(calendar)
            repository.getTotalExpenseForMonth(start, end)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    @OptIn(ExperimentalCoroutinesApi::class)
    val categoryExpenses: StateFlow<List<CategoryExpense>> = _selectedMonth
        .flatMapLatest { calendar ->
            val (start, end) = getMonthRange(calendar)
            repository.getExpensesByCategory(start, end)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories: StateFlow<List<Category>> = repository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun nextMonth() {
        val newCal = _selectedMonth.value.clone() as Calendar
        newCal.add(Calendar.MONTH, 1)
        _selectedMonth.value = newCal
        _filterCategoryId.value = null // Reset filter on month change
    }

    fun prevMonth() {
        val newCal = _selectedMonth.value.clone() as Calendar
        newCal.add(Calendar.MONTH, -1)
        _selectedMonth.value = newCal
        _filterCategoryId.value = null // Reset filter on month change
    }

    fun setCategoryFilter(categoryId: Int?) {
        _filterCategoryId.value = categoryId
    }

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

    fun saveMerchantMapping(merchantName: String, categoryId: Int, tags: String? = null) {
        viewModelScope.launch {
            repository.addMapping(MerchantMapping(merchantName, categoryId, tags))
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
