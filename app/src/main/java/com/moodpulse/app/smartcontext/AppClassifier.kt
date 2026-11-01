package com.moodpulse.app.smartcontext

enum class AppCategory {
    SOCIAL, SHOPPING, ENTERTAINMENT, PRODUCTIVITY, FINANCE,
    EDUCATION, GAMING, NEWS, COMMUNICATION, HEALTH, OTHER
}

object AppClassifier {

    private val packageMap: Map<String, AppCategory> = mapOf(
        // Social
        "com.instagram.android" to AppCategory.SOCIAL,
        "com.facebook.katana" to AppCategory.SOCIAL,
        "com.twitter.android" to AppCategory.SOCIAL,
        "com.zhiliaoapp.musically" to AppCategory.SOCIAL,  // TikTok
        "com.snapchat.android" to AppCategory.SOCIAL,
        "com.pinterest" to AppCategory.SOCIAL,
        "com.linkedin.android" to AppCategory.SOCIAL,
        "com.reddit.frontpage" to AppCategory.SOCIAL,
        "com.tumblr" to AppCategory.SOCIAL,
        "me.sharechat.app" to AppCategory.SOCIAL,
        "com.koo.app" to AppCategory.SOCIAL,
        "com.moj.app" to AppCategory.SOCIAL,

        // Shopping
        "in.amazon.mShop.android.shopping" to AppCategory.SHOPPING,
        "com.flipkart.android" to AppCategory.SHOPPING,
        "com.myntra.android" to AppCategory.SHOPPING,
        "com.ajio.mobile" to AppCategory.SHOPPING,
        "com.meesho.supply" to AppCategory.SHOPPING,
        "com.snapdeal.main" to AppCategory.SHOPPING,
        "com.nykaa.user" to AppCategory.SHOPPING,
        "com.ebay.mobile" to AppCategory.SHOPPING,
        "com.zomato.android" to AppCategory.SHOPPING,
        "in.swiggy.android" to AppCategory.SHOPPING,
        "com.blinkit.consumer" to AppCategory.SHOPPING,
        "com.grofers.customerapp" to AppCategory.SHOPPING,
        "com.bigbasket.mobileapp" to AppCategory.SHOPPING,
        "com.dunzo.user" to AppCategory.SHOPPING,
        "com.zepto.app" to AppCategory.SHOPPING,

        // Entertainment
        "com.netflix.mediaclient" to AppCategory.ENTERTAINMENT,
        "com.amazon.avod.thirdpartyclient" to AppCategory.ENTERTAINMENT,  // Prime Video
        "com.hotstar.android" to AppCategory.ENTERTAINMENT,
        "com.sony.liv" to AppCategory.ENTERTAINMENT,
        "com.zee5.android" to AppCategory.ENTERTAINMENT,
        "tv.mxplayer.pro" to AppCategory.ENTERTAINMENT,
        "com.voot.mobile" to AppCategory.ENTERTAINMENT,
        "com.jio.jiocinema" to AppCategory.ENTERTAINMENT,
        "com.spotify.music" to AppCategory.ENTERTAINMENT,
        "com.google.android.youtube" to AppCategory.ENTERTAINMENT,
        "com.gaana" to AppCategory.ENTERTAINMENT,
        "com.wynk.music" to AppCategory.ENTERTAINMENT,
        "in.jiomusic" to AppCategory.ENTERTAINMENT,

        // Communication
        "com.whatsapp" to AppCategory.COMMUNICATION,
        "org.telegram.messenger" to AppCategory.COMMUNICATION,
        "com.google.android.gm" to AppCategory.COMMUNICATION,
        "com.microsoft.teams" to AppCategory.COMMUNICATION,
        "com.slack" to AppCategory.COMMUNICATION,
        "com.skype.raider" to AppCategory.COMMUNICATION,
        "com.discord" to AppCategory.COMMUNICATION,
        "com.viber.voip" to AppCategory.COMMUNICATION,
        "com.google.android.talk" to AppCategory.COMMUNICATION,
        "com.hike.sticker.status" to AppCategory.COMMUNICATION,

        // Finance
        "com.phonepe.app" to AppCategory.FINANCE,
        "net.one97.paytm" to AppCategory.FINANCE,
        "com.google.android.apps.nbu.paisa.user" to AppCategory.FINANCE,  // Google Pay
        "com.csam.icici.bank.imobile" to AppCategory.FINANCE,
        "com.sbi.SBIFreedomPlus" to AppCategory.FINANCE,
        "com.axis.mobile" to AppCategory.FINANCE,
        "com.hdfc.ergo.gomotion" to AppCategory.FINANCE,
        "com.groww.app" to AppCategory.FINANCE,
        "com.zerodha.kite3" to AppCategory.FINANCE,
        "com.upstox.app" to AppCategory.FINANCE,
        "in.credavenue.consumer" to AppCategory.FINANCE,  // CRED
        "com.mobikwik_new" to AppCategory.FINANCE,
        "com.lendingkart.borrower" to AppCategory.FINANCE,

        // Productivity
        "com.google.android.apps.docs" to AppCategory.PRODUCTIVITY,
        "com.google.android.apps.spreadsheets" to AppCategory.PRODUCTIVITY,
        "com.microsoft.office.word" to AppCategory.PRODUCTIVITY,
        "com.microsoft.office.excel" to AppCategory.PRODUCTIVITY,
        "com.microsoft.office.powerpoint" to AppCategory.PRODUCTIVITY,
        "com.microsoft.office.outlook" to AppCategory.PRODUCTIVITY,
        "com.todoist" to AppCategory.PRODUCTIVITY,
        "com.ticktick.task" to AppCategory.PRODUCTIVITY,
        "com.notion.id" to AppCategory.PRODUCTIVITY,
        "com.evernote" to AppCategory.PRODUCTIVITY,
        "com.google.android.keep" to AppCategory.PRODUCTIVITY,

        // News
        "com.google.android.apps.magazines" to AppCategory.NEWS,
        "com.inshorts.newsbytes" to AppCategory.NEWS,
        "in.dailyhunt" to AppCategory.NEWS,
        "com.etmoney.fund" to AppCategory.NEWS,
        "com.thehindu.mobileapp" to AppCategory.NEWS,
        "com.timesinternet.ndtv" to AppCategory.NEWS,
        "com.india.today" to AppCategory.NEWS,

        // Gaming
        "com.garena.game.freefire" to AppCategory.GAMING,
        "com.pubg.krmobile" to AppCategory.GAMING,
        "com.activision.callofduty.shooter" to AppCategory.GAMING,
        "com.king.candycrushsaga" to AppCategory.GAMING,
        "com.supercell.clashofclans" to AppCategory.GAMING,
        "com.supercell.clashroyale" to AppCategory.GAMING,
        "com.roblox.client" to AppCategory.GAMING,
        "com.dena.a12026418" to AppCategory.GAMING,
        "com.dream11.games" to AppCategory.GAMING,
        "com.mpl.sport" to AppCategory.GAMING,

        // Education
        "com.byjus.thelearningapp" to AppCategory.EDUCATION,
        "com.unacademy.learnerapp" to AppCategory.EDUCATION,
        "com.duolingo" to AppCategory.EDUCATION,
        "in.toppr.student" to AppCategory.EDUCATION,
        "com.coursera.android" to AppCategory.EDUCATION,
        "com.udemy.android" to AppCategory.EDUCATION,
        "com.khan.academy" to AppCategory.EDUCATION,

        // Health
        "com.google.android.apps.fitness" to AppCategory.HEALTH,
        "com.samsung.android.shealth" to AppCategory.HEALTH,
        "com.fitbit.FitbitMobile" to AppCategory.HEALTH,
        "com.nianticproject.ingress" to AppCategory.HEALTH,
        "com.headspace.android" to AppCategory.HEALTH,
        "com.calm.android" to AppCategory.HEALTH,
        "com.practo.user" to AppCategory.HEALTH,
        "com.apollomindfulness.apolloapp" to AppCategory.HEALTH,
    )

    fun classify(packageName: String): AppCategory {
        packageMap[packageName]?.let { return it }
        val lower = packageName.lowercase()
        return when {
            lower.contains("amazon") || lower.contains("flipkart") || lower.contains("myntra") ||
            lower.contains("meesho") || lower.contains("ajio") || lower.contains("zomato") ||
            lower.contains("swiggy") || lower.contains("zepto") || lower.contains("blinkit") ||
            lower.contains("ebay") || lower.contains("shop") || lower.contains("store") ||
            lower.contains("cart") || lower.contains("retail") || lower.contains("buy") -> AppCategory.SHOPPING

            lower.contains("instagram") || lower.contains("facebook") || lower.contains("katana") ||
            lower.contains("twitter") || lower.contains("threads") || lower.contains("snapchat") ||
            lower.contains("reddit") || lower.contains("tiktok") || lower.contains("social") ||
            lower.contains("chat") || lower.contains("wechat") || lower.contains("pinterest") -> AppCategory.SOCIAL

            lower.contains("youtube") || lower.contains("netflix") || lower.contains("spotify") ||
            lower.contains("hotstar") || lower.contains("primevideo") || lower.contains("music") ||
            lower.contains("video") || lower.contains("stream") || lower.contains("tv") -> AppCategory.ENTERTAINMENT

            lower.contains("game") || lower.contains("play") -> AppCategory.GAMING
            lower.contains("news") || lower.contains("daily") -> AppCategory.NEWS
            lower.contains("bank") || lower.contains("pay") || lower.contains("finance") || lower.contains("upi") -> AppCategory.FINANCE
            lower.contains("doc") || lower.contains("sheet") || lower.contains("office") || lower.contains("note") || lower.contains("task") -> AppCategory.PRODUCTIVITY
            lower.contains("learn") || lower.contains("edu") || lower.contains("study") -> AppCategory.EDUCATION
            lower.contains("health") || lower.contains("fitness") || lower.contains("medic") || lower.contains("step") -> AppCategory.HEALTH
            else -> AppCategory.OTHER
        }
    }
}
