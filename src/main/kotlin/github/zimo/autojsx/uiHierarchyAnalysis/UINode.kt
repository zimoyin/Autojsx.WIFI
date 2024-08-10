package github.zimo.autojsx.uiHierarchyAnalysis

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import github.zimo.autojsx.util.logI
import github.zimo.autojsx.util.logW

/**
 * UINode class annotated for Jackson
 */
//@JsonInclude(JsonInclude.Include.NON_NULL)
data class UINode @JsonCreator constructor(
    @JsonProperty("index") val index: Int,
    @JsonProperty("depth") val depth: Int,
    @JsonProperty("text") val text: String,
    @JsonProperty("resourceId") val id: String,
    @JsonProperty("className") val className: String,
    @JsonProperty("packageName") val packageName: String,
    @JsonProperty("applicationPackageName") val applicationPackageName: String,
    @JsonProperty("contentDesc") val desc: String,
    @JsonProperty("bounds") val bounds: UIBounds = UIBounds(),
    @JsonProperty("checkable") val checkable: Boolean,
    @JsonProperty("checked") val checked: Boolean,
    @JsonProperty("clickable") val clickable: Boolean,
    @JsonProperty("enabled") val enabled: Boolean,
    @JsonProperty("focusable") val focusable: Boolean,
    @JsonProperty("focused") val focused: Boolean,
    @JsonProperty("scrollable") val scrollable: Boolean,
    @JsonProperty("longClickable") val longClickable: Boolean,
    @JsonProperty("password") val password: Boolean,
    @JsonProperty("selected") val selected: Boolean,
    @JsonProperty("childNodes") val childNodes: Collection<UINode> = emptyList()
) {

    var parent: UINode? = null

    /**
     * 判断是否是主要的节点
     */
    fun isMainNode(checkChildNode: Boolean = true): Boolean {
        return enabled && !bounds.isEmpty() && (clickable || focusable || text.isNotBlank() || (childNodes.isNotEmpty() && checkChildNode )|| id.isNotBlank() || desc.isNotBlank())
    }

    fun buildParent() {
        childNodes.forEach {
            it.parent = this
            it.buildParent()
        }
    }

    override fun toString(): String {
        return "UINode(parent=#${
            parent?.hashCode()?.toString()?.replace("-", "$")
        },index=$index, depth=$depth, text='$text', id='$id', className=$className, packageName='$packageName', applicationPackageName='$applicationPackageName', desc='$desc', bounds=$bounds, checkable=$checkable, checked=$checked, clickable=$clickable, enabled=$enabled, focusable=$focusable, focused=$focused, scrollable=$scrollable, longClickable=$longClickable, password=$password, selected=$selected, childNodes.size=${childNodes.size})"
    }


}

@JsonInclude(JsonInclude.Include.NON_NULL)
data class UIBounds(
    @JsonProperty("str") val str: String = "",
    @JsonProperty("left") val left: Int = -1,
    @JsonProperty("top") val top: Int = -1,
    @JsonProperty("right") val right: Int = -1,
    @JsonProperty("bottom") val bottom: Int = -1,
    @JsonProperty("width") val width: Int = -1,
    @JsonProperty("height") val height: Int = -1,
) {
    fun isEmpty(): Boolean {
        return left == -1 || top == -1 || right == -1 || bottom == -1
    }

    fun toRectangle(): Rectangle {
        return Rectangle(left, top, right, bottom)
    }
}

/**
 * 解析获取到的层次布局文件 json(通过 VertxCommand.getNodesAsJson 方法获取)
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
class UIHierarchy @Deprecated("use UIHierarchy.parse(json)") @JsonCreator constructor(
    @JsonProperty("hierarchy") @JsonInclude(JsonInclude.Include.NON_EMPTY) val hierarchy: List<UINode> = emptyList(),
) {

    var json: String? = null

    companion object {
        fun parse(json: String): UIHierarchy? {
            return try {
                val mapper = com.fasterxml.jackson.databind.ObjectMapper()
                val hierarchy = mapper.readValue(json, UIHierarchy::class.java)
                hierarchy.json = json
                hierarchy.init()
            } catch (e: Exception) {
                logW("parse json error: $e")
                null
            }
        }
    }

    fun init(): UIHierarchy {
        hierarchy.forEach {
            it.buildParent()
        }
        return this
    }

    /**
     * 通过坐标找到节点,例如点击某个了解密上的某个坐标，找到该坐标所在的节点
     */
    fun findNode(point: Point): UINode? {
        var result: UINode? = null
        hierarchy.map {
            findNode(point, it, HashSet()).let {nodes->
                if (nodes.isEmpty()) findNode(point, it, nodes,false)
                else nodes
            }
        }.forEach {
            if (it.size == 1) result = it.last()
            if (it.size > 1) {
                result = it.maxBy { it.depth }
            }
        }
        return result
    }

    private fun findNode(point: Point, node: UINode, results: HashSet<UINode>,ignoreEmptyChildNode: Boolean = true): HashSet<UINode> {
        val rectangle = node.bounds.toRectangle()
        if (rectangle.isPointInRectangle(point)) {
            if (!ignoreEmptyChildNode) results.add(node)
            if (node.childNodes.isEmpty()) results.add(node)
            node.childNodes.forEach {
                findNode(point, it, results)
            }
        }
        return results
    }

    /**
     * 获取所有节点
     */
    fun getAllNode(): List<UINode> {
        return hierarchy.map {
            getChildren(it, ArrayList()).toList()
        }.flatten()
    }

    private fun getChildren(node: UINode, list: ArrayList<UINode>): ArrayList<UINode> {
        list.add(node)
        node.childNodes.forEach {
            getChildren(it, list)
        }
        return list
    }
}
