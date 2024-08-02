package lib.module


// 声明 app 模块的外部函数和接口
@JsName("app")
external object App {
    // SendEmailOptions 接口
    interface SendEmailOptions {
        var email: dynamic // dynamic 可以是 String 或 String[]
        var cc: dynamic // dynamic 可以是 String 或 String[]
        var bcc: dynamic // dynamic 可以是 String 或 String[]
        var subject: String?
        var text: String?
        var attachment: String?
    }

    // Intent 接口
    interface Intent

    // IntentOptions 接口
    interface IntentOptions {
        var action: String?
        var type: String?
        var data: String?
        var category: Array<String>?
        var packageName: String?
        var className: String?
        var extras: dynamic // dynamic 可以是任意对象
    }

    fun launchApp(appName: String): Boolean
    fun launch(packageName: String): Boolean
    fun launchPackage(packageName: String): Boolean
    fun getPackageName(appName: String): String
    fun getAppName(packageName: String): String
    fun openAppSetting(packageName: String): Boolean
    fun viewFile(path: String)
    fun editFile(path: String)
    fun uninstall(packageName: String)
    fun openUrl(url: String)

    fun sendEmail(options: SendEmailOptions)

    fun intent(options: IntentOptions): Intent
    fun startActivity(intent: Intent)
    fun sendBroadcast(intent: Intent)

    // 特定界面启动
    fun startActivity(name: String)
}

fun App.versionCode(): Int{
    return js("app.autojs.versionCode()")
}

fun App.versionName(): String{
    return js("app.autojs.versionName()")
}



