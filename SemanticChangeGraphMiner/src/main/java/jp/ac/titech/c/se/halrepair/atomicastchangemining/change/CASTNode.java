package jp.ac.titech.c.se.halrepair.atomicastchangemining.change;

import org.eclipse.jdt.core.dom.*;

import java.io.Serializable;
import java.util.*;

public class CASTNode implements Serializable {
    private static final long serialVersionUID = 1L;
    private int id = -1;
    private int pos = -1;
    private int length = -1;
    private List<CASTNode> children = new ArrayList<>();
    private CASTNode parent = null;
    private String rawText = "";
    private int type = -1;
    private String label = "";

    public int getStartPosition(){
        return pos;
    }

    public int getLength(){
        return length;
    }

    public List<CASTNode> getChildren(){
        return children;
    }

    public String getLabel(){
        return label;
    }

    private String normalizedLabel = "";
    public void setNormalizedLabel(String normalizedLabel) {
    	this.normalizedLabel = normalizedLabel;
    }

    public String getNormalizedLabel() {
    	return normalizedLabel;
    }

    public String toString(){
        return "CASTNode{id=" + id + ", type=" + type + ", label=" + label + ", pos=" + pos + ", length=" + length + "}";
    }

    public String printTree(){
        return printTree(0, false);
    }

    public String printNormalizedTree(){
        return printTree(0, true);
    }

    public String printTree(int depth, boolean isNormalized){
        StringBuilder sb = new StringBuilder();
        String label = isNormalized ? getNormalizedLabel() : getLabel();
        sb.append(String.format("%s%d %s \"%s\" [%d, %d]\n", "  ".repeat(depth), id, ASTNode.nodeClassForType(type).getSimpleName(), label, pos, length));
        for(CASTNode child : children){
            sb.append(child.printTree(depth + 1, isNormalized));
        }
        return sb.toString();
    }
}
