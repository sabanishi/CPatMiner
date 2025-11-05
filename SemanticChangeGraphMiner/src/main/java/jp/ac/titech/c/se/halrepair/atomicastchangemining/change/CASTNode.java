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

    public CASTNode(CASTNode parent, ASTNode node, int id){
        this.parent = parent;
        this.id = id;
        this.pos = node.getStartPosition();
        this.length = node.getLength();
        this.type = node.getNodeType();

        switch(node.getNodeType()){
            case ASTNode.BOOLEAN_LITERAL:
                BooleanLiteral booleanLiteral = (BooleanLiteral)node;
                this.label = String.valueOf(booleanLiteral.booleanValue());
                break;
            case ASTNode.CHARACTER_LITERAL:
                CharacterLiteral characterLiteral = (CharacterLiteral)node;
                this.label = String.valueOf(characterLiteral.charValue());
                break;
            case ASTNode.STRING_LITERAL:
                StringLiteral stringLiteral = (StringLiteral)node;
                this.label = stringLiteral.getLiteralValue();
                break;
            case ASTNode.NUMBER_LITERAL:
                NumberLiteral numberLiteral = (NumberLiteral)node;
                this.label = numberLiteral.getToken();
                break;
            case ASTNode.SIMPLE_NAME:
                SimpleName simpleName = (SimpleName)node;
                this.label = simpleName.getIdentifier();
                break;
            case ASTNode.PREFIX_EXPRESSION:
                PrefixExpression prefixExpression = (PrefixExpression)node;
                this.label = prefixExpression.getOperator().toString();
                break;
            case ASTNode.POSTFIX_EXPRESSION:
                PostfixExpression postfixExpression = (PostfixExpression)node;
                this.label = postfixExpression.getOperator().toString();
                break;
            default:
                this.label = "";
                break;
        }

        @SuppressWarnings("unchecked")
        List<StructuralPropertyDescriptor> props = node.structuralPropertiesForType();
        for(StructuralPropertyDescriptor prop : props){
            Object value = node.getStructuralProperty(prop);
            if (prop instanceof ChildPropertyDescriptor) {
                // 単一の子ノード
                if (value instanceof ASTNode) {
                    ASTNode child = (ASTNode) value;
                    int childId = this.id;
                    this.children.add(new CASTNode(this, child, childId));
                }
            } else if (prop instanceof ChildListPropertyDescriptor) {
                // 複数子ノードのリスト
                @SuppressWarnings("unchecked")
                List<ASTNode> list = (List<ASTNode>) value;
                for (ASTNode child : list) {
                    int childId = this.id;
                    this.children.add(new CASTNode(this, child, childId));
                }
            }
        }
    }

    public String toString(){
        return "CASTNode{id=" + id + ", type=" + type + ", label=" + label + ", pos=" + pos + ", length=" + length + "}";
    }
}
