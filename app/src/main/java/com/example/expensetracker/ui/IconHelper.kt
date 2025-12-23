package com.example.expensetracker.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

object IconHelper {

    val allIcons = mapOf(
        "Default" to Icons.Default.ShoppingCart,
        "Food" to Icons.Default.Restaurant,
        "Fuel" to Icons.Default.LocalGasStation,
        "Rent" to Icons.Default.Home,
        "Shopping" to Icons.Default.ShoppingBag,
        "Health" to Icons.Default.MedicalServices,
        "Bills" to Icons.Default.Receipt,
        "Entertainment" to Icons.Default.Movie,
        "Transport" to Icons.Default.DirectionsBus,
        "Grocery" to Icons.Default.Storefront,
        "Salary" to Icons.Default.AttachMoney,
        "Personal" to Icons.Default.Person,
        "Education" to Icons.Default.School,
        "Investment" to Icons.Default.TrendingUp
    )

    val selectableIcons = allIcons.keys.toList().filter { it != "Default" }.sorted()

    fun getIcon(name: String?): ImageVector {
        return allIcons[name] ?: Icons.Default.ShoppingCart
    }

    fun suggestIcon(categoryName: String): String {
        val name = categoryName.lowercase()
        return when {
            name.contains("food") || name.contains("eat") || name.contains("din") -> "Food"
            name.contains("fuel") || name.contains("petrol") || name.contains("gas") -> "Fuel"
            name.contains("rent") || name.contains("house") -> "Rent"
            name.contains("shop") || name.contains("buy") -> "Shopping"
            name.contains("health") || name.contains("med") || name.contains("doc") -> "Health"
            name.contains("bill") || name.contains("recharge") -> "Bills"
            name.contains("movie") || name.contains("show") || name.contains("fun") -> "Entertainment"
            name.contains("bus") || name.contains("train") || name.contains("cab") || name.contains("auto") -> "Transport"
            name.contains("grocer") || name.contains("market") -> "Grocery"
            name.contains("salary") || name.contains("pay") -> "Salary"
            name.contains("learn") || name.contains("school") || name.contains("college") -> "Education"
            name.contains("invest") || name.contains("stock") -> "Investment"
            else -> "Default"
        }
    }
}
