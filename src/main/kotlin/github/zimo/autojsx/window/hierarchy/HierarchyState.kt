package github.zimo.autojsx.window.hierarchy

import github.zimo.autojsx.uiHierarchyAnalysis.Point
import github.zimo.autojsx.uiHierarchyAnalysis.UIHierarchy
import github.zimo.autojsx.uiHierarchyAnalysis.UINode
import github.zimo.autojsx.util.logW

data class HierarchyState(
    var hierarchy: UIHierarchy? = null,
    var selectImagePoint: Point? = null,
    var selectOriginalImagePoint: Point? = null,
    var selectNode: UINode? = null
) {
    fun updateSelectNode() {
        if (hierarchy ==null) logW("无法找到层次节点信息，请重新分析层次布局")
        selectNode = selectOriginalImagePoint?.let { hierarchy?.findNode(it) }
    }
}