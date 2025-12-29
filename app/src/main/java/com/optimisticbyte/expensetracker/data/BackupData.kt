package com.optimisticbyte.expensetracker.data

import com.google.gson.annotations.SerializedName

data class BackupData(
    @SerializedName("transactions") val transactions: List<Transaction>,
    @SerializedName("categories") val categories: List<Category>,
    @SerializedName("spending_limits") val spendingLimits: List<SpendingLimit>,
    @SerializedName("merchant_mappings") val merchantMappings: List<MerchantMapping>
)
