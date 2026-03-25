package com.mdm.devicemanager.data.collector

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import com.mdm.devicemanager.data.model.AppDetail

/**
 * Collects installed application inventory using PackageManager APIs.
 * Classifies apps as system or user-installed and captures version information.
 */
class AppInventoryCollector(private val context: Context) {

    companion object {
        private const val TAG = "AppInventoryCollector"
    }

    fun collect(): List<AppDetail> {
        val pm = context.packageManager
        val packages = pm.getInstalledPackages(PackageManager.GET_META_DATA)

        Log.i(TAG, "Found ${packages.size} installed packages")

        return packages.mapNotNull { packageInfo ->
            try {
                mapToAppDetail(pm, packageInfo)
            } catch (e: Exception) {
                Log.w(TAG, "Error processing package ${packageInfo.packageName}: ${e.message}")
                null
            }
        }
    }

    private fun mapToAppDetail(pm: PackageManager, packageInfo: PackageInfo): AppDetail {
        val appName = packageInfo.applicationInfo?.loadLabel(pm)?.toString() ?: packageInfo.packageName
        val isSystemApp = isSystemApp(packageInfo)
        val installSource = getInstallSource(pm, packageInfo.packageName)
        val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packageInfo.longVersionCode
        } else {
            @Suppress("DEPRECATION")
            packageInfo.versionCode.toLong()
        }

        return AppDetail(
            appName = appName,
            packageName = packageInfo.packageName,
            versionName = packageInfo.versionName ?: "Unknown",
            versionCode = versionCode,
            installationSource = installSource,
            isSystemApp = isSystemApp,
            category = classifyApp(packageInfo.packageName)
        )
    }

    private fun classifyApp(packageName: String): String {
        val pkg = packageName.lowercase()
        return when {
            // Social Media
            pkg.containsAny("facebook", "instagram", "twitter", "tiktok", "snapchat",
                "linkedin", "pinterest", "reddit", "tumblr", "threads", "mastodon",
                "social", "wechat", "weibo") -> "Social Media"

            // Communication / Messaging
            pkg.containsAny("whatsapp", "telegram", "messenger", "signal", "viber",
                "discord", "skype", "zoom", "teams", "slack", "chat", "messaging",
                "sms", "dialer", "contacts", "call", "duo", "meet") -> "Communication"

            // Entertainment / Streaming
            pkg.containsAny("youtube", "netflix", "spotify", "amazon.music", "primevideo",
                "disney", "hotstar", "hulu", "twitch", "vimeo", "player", "video",
                "music", "media", "podcast", "audible", "jio.cinema", "jiotv",
                "sonyliv", "zee5", "mxplayer", "wynk") -> "Entertainment"

            // Games
            pkg.containsAny("game", "games", "play.mob", "supercell", "miniclip",
                "rovio", "gameloft", "pubg", "freefire", "fortnite", "roblox",
                "minecraft", "candy", "clash", "puzzle", "racing") -> "Games"

            // Productivity / Office
            pkg.containsAny("docs", "sheets", "slides", "drive", "office", "word",
                "excel", "powerpoint", "onenote", "notion", "evernote", "trello",
                "todoist", "calendar", "calculator", "notes", "editor", "pdf",
                "scanner", "document") -> "Productivity"

            // Shopping / E-Commerce
            pkg.containsAny("amazon.shopping", "flipkart", "myntra", "meesho",
                "ajio", "nykaa", "shop", "shopping", "ebay", "aliexpress",
                "walmart", "swiggy", "zomato", "blinkit", "zepto", "instamart",
                "bigbasket", "dunzo", "grocery") -> "Shopping"

            // Finance / Banking
            pkg.containsAny("paytm", "phonepe", "gpay", "payment", "upi", "bank",
                "banking", "finance", "money", "wallet", "cred", "groww",
                "zerodha", "upstox", "stock", "insurance", "loan", "tax",
                "bhim", "mobikwik") -> "Finance"

            // Travel / Navigation
            pkg.containsAny("maps", "uber", "ola", "rapido", "booking",
                "makemytrip", "goibibo", "irctc", "navigation", "compass",
                "travel", "flight", "hotel", "airbnb", "tripadvisor") -> "Travel"

            // Health / Fitness
            pkg.containsAny("health", "fitness", "workout", "yoga", "meditation",
                "step", "heart", "medical", "doctor", "pharma", "cure",
                "practo", "1mg", "netmeds", "healthify") -> "Health"

            // Education
            pkg.containsAny("education", "learn", "study", "school", "classroom",
                "university", "course", "byju", "unacademy", "vedantu",
                "duolingo", "khan", "udemy", "coursera") -> "Education"

            // News / Reading
            pkg.containsAny("news", "daily", "times", "read", "book", "kindle",
                "inshorts", "TOI", "ndtv", "bbc", "cnn", "magazine",
                "newspaper") -> "News"

            // Photography / Camera
            pkg.containsAny("camera", "photo", "gallery", "snapseed", "lightroom",
                "picsart", "canva", "img", "image", "pic") -> "Photography"

            // Utilities / Tools
            pkg.containsAny("clock", "alarm", "weather", "flashlight", "file",
                "cleaner", "battery", "vpn", "proxy", "launcher", "keyboard",
                "settings", "storage", "manager", "tool", "util", "wifi",
                "bluetooth", "nfc") -> "Utilities"

            // Browser
            pkg.containsAny("browser", "chrome", "firefox", "opera", "brave",
                "edge", "safari", "webview") -> "Browser"

            // System
            pkg.startsWith("com.android.") || pkg.startsWith("com.google.android.") ||
            pkg.startsWith("com.samsung.") || pkg.startsWith("com.sec.") ||
            pkg.startsWith("com.qualcomm.") || pkg.startsWith("android") -> "System"

            else -> "Other"
        }
    }

    private fun String.containsAny(vararg keywords: String): Boolean {
        return keywords.any { this.contains(it) }
    }

    private fun isSystemApp(packageInfo: PackageInfo): Boolean {
        val flags = packageInfo.applicationInfo?.flags ?: 0
        return (flags and ApplicationInfo.FLAG_SYSTEM) != 0
    }

    private fun getInstallSource(pm: PackageManager, packageName: String): String? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val installSourceInfo = pm.getInstallSourceInfo(packageName)
                installSourceInfo.installingPackageName
            } else {
                @Suppress("DEPRECATION")
                pm.getInstallerPackageName(packageName)
            }
        } catch (e: Exception) {
            null
        }
    }
}
