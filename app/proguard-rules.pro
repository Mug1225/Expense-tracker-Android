# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Users\Mugesh\.android\avd\proguard-android-optimize.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.kts.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any custom rules here that might be necessary for your project.

# Hilt specific rules are usually bundled with the library.
# Room specific rules are usually bundled with the library.
# Glance specific rules are usually bundled with the library.

# Example: keep domain model classes if they are used for JSON serialization/deserialization
-keep class com.optimisticbyte.expensetracker.data.** { *; }
