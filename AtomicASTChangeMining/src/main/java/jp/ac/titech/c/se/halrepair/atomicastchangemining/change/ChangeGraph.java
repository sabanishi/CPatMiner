package jp.ac.titech.c.se.halrepair.atomicastchangemining.change;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;

import jp.ac.titech.c.se.halrepair.atomicastchangemining.pdg.graph.PDGEdge;
import jp.ac.titech.c.se.halrepair.atomicastchangemining.pdg.graph.PDGGraph;
import jp.ac.titech.c.se.halrepair.atomicastchangemining.pdg.graph.PDGNode;
import org.eclipse.jdt.core.dom.ASTNode;

public class ChangeGraph implements Serializable {
    private static final long serialVersionUID = -974502848659906533L;

    private final HashSet<ChangeNode> nodes = new HashSet<>();

    private String beforeRawText = "";
    private String afterRawText = "";
    private CASTNode beforeAST = null;
    private CASTNode afterAST = null;

    public ChangeGraph(PDGGraph pdg, CASTNode beforeAST, CASTNode afterAST, String beforeRawText, String afterRawText) {
        HashSet<PDGNode> changedNodes = pdg.getChangedNodes();
        if (changedNodes.isEmpty()) return;
        HashMap<PDGNode, ChangeNode> map = new HashMap<>();
        for (PDGNode node : changedNodes) {
            ChangeNode cn = new ChangeNode(node);
            map.put(node, cn);
            nodes.add(cn);
        }

        for (PDGNode node : changedNodes) {
            ChangeNode cn = map.get(node);
            for (PDGEdge e : node.getInEdges()) {
                if (changedNodes.contains(e.getSource())) {
                    new ChangeEdge(map.get(e.getSource()), cn, e);
                }
            }
        }

        this.beforeAST = beforeAST;
        this.afterAST = afterAST;
        this.beforeRawText = beforeRawText;
        this.afterRawText = afterRawText;

        // PDGノードとASTノードの対応付けを行う
        for(ChangeNode pdgNode : nodes){
            CASTNode root = null;
            if(pdgNode.getVersion() == 0){
                root = this.beforeAST;
            }else if(pdgNode.getVersion() == 1){
                root = this.afterAST;
            }
            //CASTNode cAstNode = searchCASTNode(root, pdgNode.getStartPos(), pdgNode.getLength());

            for(CASTNode cAstNode : root.preOrder()){
                if(pdgNode.getStartPos().contains(cAstNode.getStartPosition())
                && pdgNode.getLength().contains(cAstNode.getLength())){
                    pdgNode.setAstId(cAstNode.getId());
                }
            }
        }
    }

    private CASTNode searchCASTNode(CASTNode node, int startPos, int length){
        for(CASTNode child  : node.preOrder()){
            if(child.getStartPosition() == startPos && child.getLength() == length){
                return child;
            }
        }

        return null;
    }

    public HashSet<ChangeNode> getNodes() {
        return nodes;
    }
    public CASTNode getBeforeAST() {
        return beforeAST;
    }

    public CASTNode getAfterAST() {
        return afterAST;
    }

    public boolean isMultiGraph() {
        for (ChangeNode node : nodes) {
            HashSet<ChangeNode> s = new HashSet<>();
            for (ChangeEdge e : node.getOutEdges()) {
                if (s.contains(e.target))
                    return false;
                s.add(e.target);
            }
        }
        return true;
    }

    public boolean hasCycle() {
        HashSet<ChangeNode> sinks = new HashSet<>();
        for (ChangeNode n : nodes)
            if (n.getInEdges().isEmpty()) {
                sinks.add(n);
            }
        if (sinks.isEmpty())
            return true;
        for (ChangeNode sink : sinks) {
            Stack<ChangeNode> stk = new Stack<>();
            stk.push(sink);
            HashSet<ChangeNode> visitedNodes = new HashSet<>();
            if (dfs(stk, visitedNodes))
                return true;
        }
        return false;
    }

    private boolean dfs(Stack<ChangeNode> stk, HashSet<ChangeNode> visitedNodes) {
        ChangeNode n = stk.peek();
        if (visitedNodes.contains(n)) {
            stk.pop();
            return false;
        }
        for (ChangeEdge e : n.getOutEdges()) {
            if (e.target == n)
                return true;
            if (stk.contains(e.target))
                return true;
            stk.push(e.target);
            if (dfs(stk, visitedNodes))
                return true;
        }
        stk.pop();
        visitedNodes.add(n);
        return false;
    }

    public boolean hasMethods() {
        for (ChangeNode node : nodes) {
            if (node.getAstNodeType() == ASTNode.METHOD_INVOCATION
                    || node.getAstNodeType() == ASTNode.SUPER_METHOD_INVOCATION
                    || node.getAstNodeType() == ASTNode.CLASS_INSTANCE_CREATION
                    || node.getAstNodeType() == ASTNode.CONSTRUCTOR_INVOCATION
                    || node.getAstNodeType() == ASTNode.SUPER_CONSTRUCTOR_INVOCATION)
                return true;
        }
        return false;
    }

    public String summarize() {
        int numOfEdges = 0;
        HashMap<String, Integer> edges = new HashMap<>(), nodes = new HashMap<>();
        for (ChangeNode node : this.nodes) {
            int c = 1;
            if (nodes.containsKey(node.getType())) {
                c += nodes.get(node.getType());
            }
            nodes.put(node.getType(), c);
            numOfEdges += node.getInEdges().size();
            for (ChangeEdge e : node.getInEdges()) {
                String label = e.getLabel();
                c = 1;
                if (edges.containsKey(label)) {
                    c += edges.get(label);
                }
                edges.put(label, c);
            }
        }
        String sum = "";
        sum += this.nodes.size();
        String[] types = {"a", "c", "d"};
        for (String type : types) {
            sum += ":" + (nodes.containsKey(type) ? nodes.get(type) : 0);
        }
        sum += ":" + numOfEdges;
        types = new String[]{"_cond_", "_control_", "_def_", "_dep_",
                "_para_", "_qual_", "_recv_", "_ref_"};
        for (String type : types) {
            sum += ":" + (edges.containsKey(type) ? edges.get(type) : 0);
        }
        return sum;
    }

    public int[] getChangeSizes() {
        int[] sizes = new int[]{0, 0};
        for (ChangeNode node : nodes) {
            sizes[node.getVersion()]++;
        }
        return sizes;
    }
}
