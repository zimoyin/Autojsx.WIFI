package lib.kotlin

import lib.module.Thread
import lib.module.UI

/**
 *
 * @author : zimo
 * @date : 2024/07/30
 */
fun thread(action: () -> Unit){
    Thread.start(action)
}

fun ui(action: () -> Unit){
    UI.run(action)
}