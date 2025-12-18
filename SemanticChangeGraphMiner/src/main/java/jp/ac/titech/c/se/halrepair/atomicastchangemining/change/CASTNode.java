package jp.ac.titech.c.se.halrepair.atomicastchangemining.change;

import jp.ac.titech.c.se.halrepair.atomicastchangemining.groum.GROUMNode;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.swt.internal.C;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Stream;

public class CASTNode implements Serializable {
    private static final long serialVersionUID = 1L;
    private int id = -1;
    private int pos = -1;
    private int length = -1;
    private List<CASTNode> children = new ArrayList<>();
    private CASTNode parent = null;
    private int type = -1;
    private String label = "";

    // 以下はAtomicASTChangeMiningには存在しない変数
    // 自身と紐づいているPDGノード
    private GROUMNode groumNode;

    public static CASTNode copyOf(CASTNode node){
        CASTNode newNode = new CASTNode();
        newNode.id = node.id;
        newNode.pos = node.pos;
        newNode.length = node.length;
        newNode.type = node.type;
        newNode.label = node.label;
        for(CASTNode child : node.children){
            CASTNode newChild = copyOf(child);
            newChild.parent = newNode;
            newNode.children.add(newChild);
        }
        return newNode;
    }

    public void setGroumNode(GROUMNode node){
        this.groumNode = node;
    }
    public int getId(){
        return id;
    }

    public int getType(){
        return type;
    }

    public GROUMNode getGroumNode(){
        return groumNode;
    }

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

    public Iterable<CASTNode> preOrder(){
        List<CASTNode> nodes = new ArrayList<>();
        return preOrderHelper(nodes);
    }

    private Iterable<CASTNode> preOrderHelper(List<CASTNode> list){
        list.add(this);
        for(CASTNode child : children){
            child.preOrderHelper(list);
        }
        return list;
    }

    public CASTNode getChild(int index){
        if(index == this.id){
            return this;
        }

        for(CASTNode child : children){
            CASTNode result = child.getChild(index);
            if(result != null){
                return result;
            }
        }

        return null;
    }

    public String makeNormalizeText(String rawText) {
        String baseText = "";
        if(this.parent == null){
            baseText = rawText;
        }else{
            // TODO: 間違っているかも
            baseText = rawText.substring(pos, pos + length);
        }
        List<Change> changes = new ArrayList<>();
        makeNormalizeLoop(changes,baseText,pos);

        return makeNormalized(changes,baseText);
    }

    private String makeNormalized(List<Change> changes,String baseText){
        //startが小さい順にソートする
        Stream<Change> sortedChanges = changes.stream().sorted(Comparator.comparingInt(a -> a.getOldBegin()));
        StringBuilder sb = new StringBuilder();
        final int[] pos = {0};
        sortedChanges.forEach(change->{
            sb.append(baseText, pos[0], change.getOldBegin());
            sb.append(change.getNewText());
            pos[0] = change.getOldEnd();
        });
        sb.append(baseText, pos[0], baseText.length());

        return sb.toString();
    }

    private void makeNormalizeLoop(List<Change> changes,String baseText,int startPos){
        String myText = baseText.substring(pos-startPos, pos + getLength()-startPos);
        String normalizedText = makeNormalizeTextInternal(myText);
        if(this.parent != null){
            if(!myText.equals(normalizedText)){
                Change change = new Change(
                        pos - startPos,
                        pos + length - startPos,
                        myText,
                        normalizedText);
                changes.add(change);
            }
        }
        for (CASTNode child : children) {
            child.makeNormalizeLoop(changes,baseText,startPos);
        }
    }

    /*
    public void makeNormalizeFlag(){
        this.isNormalized = false;
        this.normalizeType = NormalizeType.None;

        if(this.groumNode == null){
            return;
        }

        if(this.type == ASTNode.SIMPLE_NAME){
            if(this.parent.type == ASTNode.SINGLE_VARIABLE_DECLARATION
                    || this.parent.type == ASTNode.VARIABLE_DECLARATION_FRAGMENT){
                this.isNormalized = true;
                this.normalizeType = NormalizeType.Variable;
            }
            if(this.parent.type == ASTNode.METHOD_INVOCATION
                    && !this.label.equals(this.parent.label)){
                this.isNormalized = true;
                this.normalizeType = NormalizeType.Variable;
            }
        }

        if(this.type == ASTNode.STRING_LITERAL){
            // 「""」の有無を除いて同じなら元の文章を返す

            if (rawText != null && rawText.length() >= 2 &&
                    rawText.startsWith("\"") && rawText.endsWith("\"")) {

                // rawText の両端の「"」を除いた中身
                String inner = rawText.substring(1, rawText.length() - 1);

                if (inner.equals(normalizedLabel)) {
                    return rawText;
                }
            }

            this.isNormalized = true;
            this.normalizeType = NormalizeType.Literal;
        }
    }
    */

    private String makeNormalizeTextInternal(String rawText) {
        if(groumNode == null){
            return rawText;
        }
        if(!groumNode.getIsNormalized() || !groumNode.getIsNormalizeValid()){
            return rawText;
        }

        NormalizeType normalizeType = groumNode.getNormalizeType();

        switch(normalizeType){
            case Variable:
                return "$V"+ this.id;
            case Literal:
                return "$L"+ this.id;
            default:
                System.err.println("Unknown normalize type: " + normalizeType);
                return rawText;
        }
    }

    public String toString(){
        return "CASTNode{id=" + id + ", type=" + type + ", label=" + label + ", pos=" + pos + ", length=" + length + "}";
    }

    public String printTree(){
        return printTree(0);
    }

    public String printTree(int depth){
        StringBuilder sb = new StringBuilder();
        String label = getLabel();
        sb.append(String.format("%s%d %s \"%s\" [%d, %d]\n", "  ".repeat(depth), id, ASTNode.nodeClassForType(type).getSimpleName(), label, pos, length));
        for(CASTNode child : children){
            sb.append(child.printTree(depth + 1));
        }
        return sb.toString();
    }
}
