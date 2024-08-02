package lib.module

/**
 *
 * @author : zimo
 * @date : 2024/07/30
 */
// 声明 Rect 接口
external interface Rect {
    val left: Int
    val right: Int
    val top: Int
    val bottom: Int
    fun centerX(): Int
    fun centerY(): Int
    fun width(): Int
    fun height(): Int
    fun contains(r: Rect): Boolean
    fun intersect(r: Rect): Boolean
}

// 声明 UiSelector 接口
external interface UiSelector {
    fun text(str: String): UiSelector
    fun textContains(str: String): UiSelector
    fun textStartsWith(prefix: String): UiSelector
    fun textEndsWith(suffix: String): UiSelector
    fun textMatches(reg: dynamic): UiSelector
    fun desc(str: String): UiSelector
    fun descContains(str: String): UiSelector
    fun descStartsWith(prefix: String): UiSelector
    fun descEndsWith(suffix: String): UiSelector
    fun descMatches(reg: dynamic): UiSelector
    fun id(resId: String): UiSelector
    fun idContains(str: String): UiSelector
    fun idStartsWith(prefix: String): UiSelector
    fun idEndsWith(suffix: String): UiSelector
    fun idMatches(reg: dynamic): UiSelector
    fun className(str: String): UiSelector
    fun classNameContains(str: String): UiSelector
    fun classNameStartsWith(prefix: String): UiSelector
    fun classNameEndsWith(suffix: String): UiSelector
    fun classNameMatches(reg: dynamic): UiSelector
    fun packageName(str: String): UiSelector
    fun packageNameContains(str: String): UiSelector
    fun packageNameStartsWith(prefix: String): UiSelector
    fun packageNameEndsWith(suffix: String): UiSelector
    fun packageNameMatches(reg: dynamic): UiSelector
    fun bounds(left: Int, top: Int, right: Int, bottom: Int): UiSelector
    fun boundsInside(left: Int, top: Int, right: Int, bottom: Int): UiSelector
    fun boundsContains(left: Int, top: Int, right: Int, bottom: Int): UiSelector
    fun drawingOrder(order: Int): UiSelector
    fun clickable(b: Boolean): UiSelector
    fun longClickable(b: Boolean): UiSelector
    fun checkable(b: Boolean): UiSelector
    fun selected(b: Boolean): UiSelector
    fun enabled(b: Boolean): UiSelector
    fun scrollable(b: Boolean): UiSelector
    fun editable(b: Boolean): UiSelector
    fun multiLine(b: Boolean): UiSelector
    fun findOne(): UiObject
    fun findOne(timeout: Int): UiObject
    fun findOnce(): UiObject
    fun findOnce(i: Int): UiObject
    fun find(): UiCollection
    fun untilFind(): UiCollection
    fun exists(): Boolean
    fun waitFor()
    fun filter(filter: (obj: UiObject) -> Boolean): UiSelector
}

// 声明 UiObject 接口
external interface UiObject {
    fun click(): Boolean
    fun longClick(): Boolean
    fun setText(text: String): Boolean
    fun copy(): Boolean
    fun cut(): Boolean
    fun paste(): Boolean
    fun setSelection(start: Int, end: Int): Boolean
    fun scrollForward(): Boolean
    fun scrollBackward(): Boolean
    fun select(): Boolean
    fun collapse(): Boolean
    fun expand(): Boolean
    fun show(): Boolean
    fun scrollUp(): Boolean
    fun scrollDown(): Boolean
    fun scrollLeft(): Boolean
    fun scrollRight(): Boolean
    fun children(): UiCollection
    fun childCount(): Int
    fun child(i: Int): UiObject
    fun parent(): UiObject
    fun bounds(): Rect
    fun boundsInParent(): Rect
    fun drawingOrder(): Int
    fun id(): String
    fun text(): String
    fun findByText(str: String): UiCollection
    fun findOne(selector: dynamic): UiObject
    fun find(selector: dynamic): UiCollection
}

// 声明 UiCollection 接口
external interface UiCollection {
    fun size(): Int
    fun get(i: Int): UiObject
    fun each(func: (obj: UiObject) -> Unit)
    fun empty(): Boolean
    fun nonEmpty(): Boolean
    fun find(selector: dynamic): UiCollection
    fun findOne(selector: dynamic): UiObject
}

// Define `UILike` interface with a method `toString` returning a `String`
external interface UILike {
    override fun toString(): String
}

// Define functions for UI selectors
external fun text(str: String): UiSelector
external fun textContains(str: String): UiSelector
external fun textStartsWith(prefix: String): UiSelector
external fun textEndsWith(suffix: String): UiSelector
external fun textMatches(reg: dynamic): UiSelector // `reg` can be either `String` or `RegExp` in JavaScript
external fun desc(str: String): UiSelector
external fun descContains(str: String): UiSelector
external fun descStartsWith(prefix: String): UiSelector
external fun descEndsWith(suffix: String): UiSelector
external fun descMatches(reg: dynamic): UiSelector // `reg` can be either `String` or `RegExp`
external fun id(resId: String): UiSelector
external fun idContains(str: String): UiSelector
external fun idStartsWith(prefix: String): UiSelector
external fun idEndsWith(suffix: String): UiSelector
external fun idMatches(reg: dynamic): UiSelector // `reg` can be either `String` or `RegExp`
external fun className(str: String): UiSelector
external fun classNameContains(str: String): UiSelector
external fun classNameStartsWith(prefix: String): UiSelector
external fun classNameEndsWith(suffix: String): UiSelector
external fun classNameMatches(reg: dynamic): UiSelector // `reg` can be either `String` or `RegExp`
external fun packageName(str: String): UiSelector
external fun packageNameContains(str: String): UiSelector
external fun packageNameStartsWith(prefix: String): UiSelector
external fun packageNameEndsWith(suffix: String): UiSelector
external fun packageNameMatches(reg: dynamic): UiSelector // `reg` can be either `String` or `RegExp`
external fun bounds(left: Int, top: Int, right: Int, bottom: Int): UiSelector
external fun boundsInside(left: Int, top: Int, right: Int, bottom: Int): UiSelector
external fun boundsContains(left: Int, top: Int, right: Int, bottom: Int): UiSelector
external fun drawingOrder(order: dynamic): UiSelector // Adjust type based on usage
external fun clickable(b: Boolean): UiSelector
external fun longClickable(b: Boolean): UiSelector
external fun checkable(b: Boolean): UiSelector
external fun selected(b: Boolean): UiSelector
external fun enabled(b: Boolean): UiSelector
external fun scrollable(b: Boolean): UiSelector
external fun editable(b: Boolean): UiSelector
external fun multiLine(b: Boolean): UiSelector

// Define functions for finding UI objects and collections
external fun findOne(): UiObject
external fun findOne(timeout: Int): UiObject
external fun findOnce(): UiObject
external fun findOnce(i: Int): UiObject

external fun find(): UiCollection
external fun untilFind(): UiCollection

// Define functions for existence and waiting
external fun exists(): Boolean
external fun waitFor()
external fun filter(filter: (obj: UiObject) -> Boolean)
