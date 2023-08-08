package github.zimo.autojsx.server

import com.intellij.openapi.ui.ComboBox
import java.util.function.Predicate
import java.util.function.UnaryOperator

/**
 * 设备列表
 */
object Devices : ArrayList<String>() {
    var component: ComboBox<String>? = null
    var currentDevice: String? = null

    init {
        add("所有设备")
    }

    override fun add(element: String): Boolean {
        val add = super.add(element)
        component?.apply {
            removeAllItems()
            Devices.forEach {
                addItem(it)
            }
        }
        return add
    }

    override fun add(index: Int, element: String) {
        super.add(index, element)
        updateComponent()
    }


    override fun addAll(elements: Collection<String>): Boolean {
        val addAll = super.addAll(elements)
        updateComponent()
        return addAll
    }

    override fun addAll(index: Int, elements: Collection<String>): Boolean {
        val result = super.addAll(index, elements)
        updateComponent()
        return result
    }


    override fun remove(element: String): Boolean {
        val remove = super.remove(element)
        updateComponent()
        return remove
    }

    override fun removeAll(elements: Collection<String>): Boolean {
        val result = super.removeAll(elements)
        add("所有设备")
        updateComponent()
        return result
    }

    override fun removeAt(index: Int): String {
        val result = super.removeAt(index)
        updateComponent()
        return result
    }

    override fun removeIf(filter: Predicate<in String>): Boolean {
        val result = super.removeIf(filter)
        updateComponent()
        return result
    }

    override fun replaceAll(operator: UnaryOperator<String>) {
        super.replaceAll(operator)
        updateComponent()
    }

    override fun retainAll(elements: Collection<String>): Boolean {
        val result = super.retainAll(elements)
        updateComponent()
        return result
    }

    override fun removeRange(fromIndex: Int, toIndex: Int) {
        super.removeRange(fromIndex, toIndex)
        updateComponent()
    }

    override fun clear() {
        super.clear()
        add("所有设备")
        updateComponent()
    }


    private fun updateComponent() {
        component?.apply {
            removeAllItems()
            Devices.forEach {
                addItem(it)
            }
        }
    }
}