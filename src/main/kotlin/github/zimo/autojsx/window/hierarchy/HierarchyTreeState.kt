package github.zimo.autojsx.window.hierarchy

import github.zimo.autojsx.uiHierarchyAnalysis.UIHierarchy
import github.zimo.autojsx.uiHierarchyAnalysis.UINode
import java.awt.Component
import javax.swing.ImageIcon
import javax.swing.JTree
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeCellRenderer
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreePath


/**
 *
 * @author : zimo
 * @date : 2024/08/10
 */
data class HierarchyTreeState(
    val treeRootNode: DefaultMutableTreeNode = DefaultMutableTreeNode("Root"),
    val treeModel: DefaultTreeModel = DefaultTreeModel(treeRootNode),
    val treeNodeIdMap: MutableMap<String, UINode> = mutableMapOf<String, UINode>(),
    val tree: JTree = JTree(treeModel).apply {
        isRootVisible = true
        // 禁用 JTree 的图标
        cellRenderer = object : DefaultTreeCellRenderer() {
            override fun getTreeCellRendererComponent(
                tree: JTree?,
                value: Any?,
                sel: Boolean,
                expanded: Boolean,
                leaf: Boolean,
                row: Int,
                hasFocus: Boolean
            ): Component {
                val component = super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus)
                icon = null
                leafIcon = null
                openIcon = null
                closedIcon = null
                return component
            }
        }
    }
) {

    fun init(ImageState: HierarchyImageState, HierarchyState: HierarchyState, TableState: HierarchyTableState) {
        //添加监听
        tree.addTreeSelectionListener { e ->
            val selectedNode = tree.getLastSelectedPathComponent() as DefaultMutableTreeNode?
            if (selectedNode != null) {
                val id = selectedNode.userObject.toString()
                val uiNode = treeNodeIdMap[id]
                HierarchyState.selectNode = uiNode
                ImageState.update(ImageIcon(ImageState.resizeAndRedrawRectangleImage(HierarchyState)), HierarchyState, this, TableState)
            }
        }
    }

    fun updateTree(HierarchyState: HierarchyState) {
        for (i in 0 until treeRootNode.children().toList().size) {
            treeRootNode.remove(i)
        }
        treeModel.nodeStructureChanged(treeRootNode)
        treeNodeIdMap.clear()
        HierarchyState.hierarchy?.toTreeNode(treeRootNode)
    }

    private fun findNode(root: DefaultMutableTreeNode, nodeLabel: String): DefaultMutableTreeNode? {
        val enumeration = root.breadthFirstEnumeration()
        while (enumeration.hasMoreElements()) {
            val node = enumeration.nextElement() as DefaultMutableTreeNode
            if (node.userObject == nodeLabel) {
                return node
            }
        }
        return null
    }

    fun selectNodeInTree(tree: JTree, root: DefaultMutableTreeNode, nodeLabel: String) {
        val nodeToSelect = findNode(root, nodeLabel)
        if (nodeToSelect != null) {
            val treePath = TreePath(nodeToSelect.path)
            tree.selectionPath = treePath
            tree.scrollPathToVisible(treePath)  // 确保节点可见
        }
    }

    fun selectNodeInTree(node: UINode) {
        selectNodeInTree(tree, treeRootNode, node.asID())
    }


    private fun UIHierarchy.toTreeNode(treeRoot: DefaultMutableTreeNode): DefaultMutableTreeNode {
        val roots = this.hierarchy
        roots.forEach {
            treeRoot.add(it.toTreeNode())
        }
        return treeRoot
    }

    private fun UINode.toTreeNode(): DefaultMutableTreeNode {
        val treeNodeId = asID()
        val currentNode = DefaultMutableTreeNode(treeNodeId)
        treeNodeIdMap[treeNodeId] = this
        this.childNodes.forEach {
            currentNode.add(it.toTreeNode())
        }

        return currentNode
    }

    private fun UINode.asID(): String {
        val index = this.index
        val depth = this.depth
        val id = this.id.let {
            if (it.trim().isEmpty()) "" else " $$it"
        }
        val clazz = this.className.let {
            it.split(".").lastOrNull() ?: ""
        }
        val text = this.text.let {
            if (it.trim().isEmpty()) "" else ":$it"
        }
        val desc = this.desc.let {
            if (it.trim().isEmpty()) "" else " {$it}"
        }
        val bounds = this.bounds.let {
            "[${it.left},${it.top}][${it.right}, ${it.bottom}]"
        }
        return "($depth:$index) $clazz$text$desc$id $bounds"
    }
}
