package jp.ac.titech.c.se.halrepair.atomicastchangemining.change;

import jp.ac.titech.c.se.halrepair.atomicastchangemining.treed.TreedConstants;
import org.eclipse.jdt.core.dom.*;

import java.io.Serializable;
import java.util.*;

public class CASTNode implements Serializable {
    private static final long serialVersionUID = 1L;
    // NOTE: IDはコンストラクタでは振られず、変更前後のASTを構築したあとにマッピングされる
    private int id = -1;
    private int pos = -1;
    private int length = -1;
    private List<CASTNode> children = new ArrayList<>();
    private CASTNode parent = null;
    private int type = -1;
    private String label = "";
    private ASTNode originalNode = null;

    public void setId(int id){
        this.id = id;
    }
    public int getId(){
        return id;
    }
    public ASTNode getOriginalNode(){
        return originalNode;
    }

    public int getStartPosition(){
        return pos;
    }

    public int getLength(){
        return length;
    }

    public CASTNode(CASTNode parent, ASTNode node){
        this.parent = parent;
        this.pos = node.getStartPosition();
        this.length = node.getLength();
        this.type = node.getNodeType();
        this.originalNode = node;
        // 元のASTノードに自身の参照を追加しておく
        node.setProperty(TreedConstants.PROPERTY_C_NODE, this);

        switch(node.getNodeType()) {
            // ======== リテラル / 名前 ========
            case ASTNode.BOOLEAN_LITERAL: {
                BooleanLiteral n = (BooleanLiteral) node;
                this.label = String.valueOf(n.booleanValue());
                break;
            }
            case ASTNode.CHARACTER_LITERAL: {
                CharacterLiteral n = (CharacterLiteral) node;
                this.label = String.valueOf(n.charValue());
                break;
            }
            case ASTNode.STRING_LITERAL: {
                StringLiteral n = (StringLiteral) node;
                this.label = n.getLiteralValue();
                break;
            }
            case ASTNode.NUMBER_LITERAL: {
                NumberLiteral n = (NumberLiteral) node;
                this.label = n.getToken();
                break;
            }
            case ASTNode.NULL_LITERAL: {
                this.label = "null";
                break;
            }
            case ASTNode.SIMPLE_NAME: {
                SimpleName n = (SimpleName) node;
                this.label = n.getIdentifier();
                break;
            }
            case ASTNode.QUALIFIED_NAME: {
                QualifiedName n = (QualifiedName) node;
                this.label = n.getFullyQualifiedName();
                break;
            }
            case ASTNode.THIS_EXPRESSION: {
                ThisExpression n = (ThisExpression) node;
                this.label = n.getQualifier() == null ? "this" : n.getQualifier().getFullyQualifiedName() + ".this";
                break;
            }
            case ASTNode.SUPER_FIELD_ACCESS: {
                SuperFieldAccess n = (SuperFieldAccess) node;
                this.label = "super." + n.getName().getIdentifier();
                break;
            }
            case ASTNode.FIELD_ACCESS: {
                FieldAccess n = (FieldAccess) node;
                this.label = ".field";
                break;
            }
            case ASTNode.TYPE_LITERAL: {
                TypeLiteral n = (TypeLiteral) node;
                this.label = safeType(n.getType()) + ".class";
                break;
            }

            // ======== 型 ========
            case ASTNode.SIMPLE_TYPE:
            case ASTNode.QUALIFIED_TYPE:
            case ASTNode.NAME_QUALIFIED_TYPE:
            case ASTNode.PRIMITIVE_TYPE:
            case ASTNode.ARRAY_TYPE:
            case ASTNode.PARAMETERIZED_TYPE:
            case ASTNode.UNION_TYPE:
            case ASTNode.INTERSECTION_TYPE: {
                this.label = safeType((Type) node);
                break;
            }
            case ASTNode.WILDCARD_TYPE: {
                WildcardType n = (WildcardType) node;
                this.label = n.isUpperBound() ? "? extends" : (n.getBound() != null ? "? super" : "?");
                break;
            }

            // ======== 演算系式 ========
            case ASTNode.PREFIX_EXPRESSION: {
                PrefixExpression n = (PrefixExpression) node;
                this.label = n.getOperator().toString(); // ++, --, !, ~, +, -
                break;
            }
            case ASTNode.POSTFIX_EXPRESSION: {
                PostfixExpression n = (PostfixExpression) node;
                this.label = n.getOperator().toString(); // ++, --
                break;
            }
            case ASTNode.INFIX_EXPRESSION: {
                InfixExpression n = (InfixExpression) node;
                this.label = n.getOperator().toString(); // +, -, *, /, ==, &&, ||, ...
                break;
            }
            case ASTNode.ASSIGNMENT: {
                Assignment n = (Assignment) node;
                this.label = n.getOperator().toString(); // =, +=, -=, ...
                break;
            }
            case ASTNode.CONDITIONAL_EXPRESSION: {
                this.label = "?:"; // 三項演算子
                break;
            }
            case ASTNode.INSTANCEOF_EXPRESSION: {
                this.label = "instanceof";
                break;
            }
            case ASTNode.CAST_EXPRESSION: {
                this.label = "(cast)";
                break;
            }
            case ASTNode.PARENTHESIZED_EXPRESSION: {
                this.label = "( )";
                break;
            }

            // ======== メソッド呼び出し / 生成 ========
            case ASTNode.METHOD_INVOCATION: {
                MethodInvocation n = (MethodInvocation) node;
                this.label = n.getName().getIdentifier(); // メソッド名
                break;
            }
            case ASTNode.SUPER_METHOD_INVOCATION: {
                SuperMethodInvocation n = (SuperMethodInvocation) node;
                this.label = "super." + n.getName().getIdentifier();
                break;
            }
            case ASTNode.CLASS_INSTANCE_CREATION: {
                ClassInstanceCreation n = (ClassInstanceCreation) node;
                this.label = "new " + safeType(n.getType());
                break;
            }
            case ASTNode.ARRAY_CREATION: {
                ArrayCreation n = (ArrayCreation) node;
                this.label = "new " + safeType(n.getType());
                break;
            }
            case ASTNode.ARRAY_INITIALIZER: {
                this.label = "{...}";
                break;
            }
            case ASTNode.ARRAY_ACCESS: {
                this.label = "[]";
                break;
            }

            // ======== 文 ========
            case ASTNode.BLOCK: {
                this.label = "{block}";
                break;
            }
            case ASTNode.IF_STATEMENT: {
                this.label = "if";
                break;
            }
            case ASTNode.SWITCH_STATEMENT: {
                this.label = "switch";
                break;
            }
            case ASTNode.SWITCH_CASE: {
                SwitchCase n = (SwitchCase) node;
                this.label = n.isDefault() ? "default" : "case";
                break;
            }
            case ASTNode.FOR_STATEMENT: {
                this.label = "for";
                break;
            }
            case ASTNode.ENHANCED_FOR_STATEMENT: {
                this.label = "for-each";
                break;
            }
            case ASTNode.WHILE_STATEMENT: {
                this.label = "while";
                break;
            }
            case ASTNode.DO_STATEMENT: {
                this.label = "do-while";
                break;
            }
            case ASTNode.TRY_STATEMENT: {
                this.label = "try";
                break;
            }
            case ASTNode.CATCH_CLAUSE: {
                CatchClause n = (CatchClause) node;
                this.label = "catch(" + safeType(n.getException().getType()) + ")";
                break;
            }

            case ASTNode.SYNCHRONIZED_STATEMENT: {
                this.label = "synchronized";
                break;
            }
            case ASTNode.RETURN_STATEMENT: {
                this.label = "return";
                break;
            }
            case ASTNode.BREAK_STATEMENT: {
                this.label = "break";
                break;
            }
            case ASTNode.CONTINUE_STATEMENT: {
                this.label = "continue";
                break;
            }
            case ASTNode.THROW_STATEMENT: {
                this.label = "throw";
                break;
            }
            case ASTNode.ASSERT_STATEMENT: {
                this.label = "assert";
                break;
            }
            case ASTNode.LABELED_STATEMENT: {
                LabeledStatement n = (LabeledStatement) node;
                this.label = n.getLabel().getIdentifier() + ":";
                break;
            }
            case ASTNode.EMPTY_STATEMENT: {
                this.label = ";";
                break;
            }
            case ASTNode.VARIABLE_DECLARATION_STATEMENT: {
                VariableDeclarationStatement n = (VariableDeclarationStatement) node;
                this.label = safeType(n.getType()) + " var";
                break;
            }
            case ASTNode.VARIABLE_DECLARATION_EXPRESSION: {
                VariableDeclarationExpression n = (VariableDeclarationExpression) node;
                this.label = safeType(n.getType()) + " var";
                break;
            }
            case ASTNode.EXPRESSION_STATEMENT: {
                this.label = "expr;";
                break;
            }
            case ASTNode.CONSTRUCTOR_INVOCATION: {
                this.label = "this(...)";
                break;
            }
            case ASTNode.SUPER_CONSTRUCTOR_INVOCATION: {
                this.label = "super(...)";
                break;
            }

            // ======== 宣言 ========
            case ASTNode.TYPE_DECLARATION: {
                TypeDeclaration n = (TypeDeclaration) node;
                this.label = (n.isInterface() ? "interface " : "class ") + n.getName().getIdentifier();
                break;
            }
            case ASTNode.ENUM_DECLARATION: {
                EnumDeclaration n = (EnumDeclaration) node;
                this.label = "enum " + n.getName().getIdentifier();
                break;
            }
            case ASTNode.ANNOTATION_TYPE_DECLARATION: {
                AnnotationTypeDeclaration n = (AnnotationTypeDeclaration) node;
                this.label = "@interface " + n.getName().getIdentifier();
                break;
            }
            case ASTNode.FIELD_DECLARATION: {
                FieldDeclaration n = (FieldDeclaration) node;
                this.label = safeType(n.getType()) + " field";
                break;
            }
            case ASTNode.METHOD_DECLARATION: {
                MethodDeclaration n = (MethodDeclaration) node;
                String name = n.getName() != null ? n.getName().getIdentifier() : "<init>";
                this.label = name + "(" + n.parameters().size() + ")";
                break;
            }
            case ASTNode.SINGLE_VARIABLE_DECLARATION: {
                SingleVariableDeclaration n = (SingleVariableDeclaration) node;
                this.label = safeType(n.getType()) + " " + (n.getName() != null ? n.getName().getIdentifier() : "");
                break;
            }
            case ASTNode.VARIABLE_DECLARATION_FRAGMENT: {
                VariableDeclarationFragment n = (VariableDeclarationFragment) node;
                this.label = n.getName().getIdentifier();
                break;
            }
            case ASTNode.INITIALIZER: {
                this.label = "{static|instance init}";
                break;
            }
            case ASTNode.COMPILATION_UNIT: {
                this.label = "(compilationUnit)";
                break;
            }
            case ASTNode.PACKAGE_DECLARATION: {
                PackageDeclaration n = (PackageDeclaration) node;
                this.label = n.getName().getFullyQualifiedName();
                break;
            }
            case ASTNode.IMPORT_DECLARATION: {
                ImportDeclaration n = (ImportDeclaration) node;
                this.label = "import " + n.getName().getFullyQualifiedName() + (n.isOnDemand() ? ".*" : "");
                break;
            }
            case ASTNode.MODIFIER:{
                Modifier n = (Modifier) node;
                this.label = n.getKeyword().toString();
                break;
            }

            // ======== アノテーション ========
            case ASTNode.MARKER_ANNOTATION: {
                MarkerAnnotation n = (MarkerAnnotation) node;
                this.label = "@" + n.getTypeName().getFullyQualifiedName();
                break;
            }
            case ASTNode.NORMAL_ANNOTATION: {
                NormalAnnotation n = (NormalAnnotation) node;
                this.label = "@" + n.getTypeName().getFullyQualifiedName();
                break;
            }
            case ASTNode.SINGLE_MEMBER_ANNOTATION: {
                SingleMemberAnnotation n = (SingleMemberAnnotation) node;
                this.label = "@" + n.getTypeName().getFullyQualifiedName();
                break;
            }

            // ======== ラムダ / メソッド参照（JLS8+） ========
            case ASTNode.LAMBDA_EXPRESSION: {
                this.label = "lambda";
                break;
            }
            case ASTNode.EXPRESSION_METHOD_REFERENCE:
            case ASTNode.TYPE_METHOD_REFERENCE:
            case ASTNode.SUPER_METHOD_REFERENCE:
            case ASTNode.CREATION_REFERENCE: {
                this.label = "::";
                break;
            }

            // ======== その他(デフォルト) ========
            default: {
                this.label = node.getClass().getSimpleName();
                break;
            }
        }

        @SuppressWarnings("unchecked")
        List<StructuralPropertyDescriptor> props = node.structuralPropertiesForType();
        for(StructuralPropertyDescriptor prop : props){
            Object value = node.getStructuralProperty(prop);
            if (prop instanceof ChildPropertyDescriptor) {
                // 単一の子ノード
                if (value instanceof ASTNode) {
                    ASTNode child = (ASTNode) value;
                    this.children.add(new CASTNode(this, child));
                }
            } else if (prop instanceof ChildListPropertyDescriptor) {
                // 複数子ノードのリスト
                @SuppressWarnings("unchecked")
                List<ASTNode> list = (List<ASTNode>) value;
                for (ASTNode child : list) {
                    this.children.add(new CASTNode(this, child));
                }
            }
        }
    }

    private static String safeType(Type t) {
        if (t == null) return "<nullType>";
        try {
            if (t.isSimpleType()) {
                Name n = ((SimpleType) t).getName();
                return (n != null) ? n.getFullyQualifiedName() : t.toString();
            }
            return t.toString();
        } catch (Exception e) {
            return t.getClass().getSimpleName();
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
        sb.append(String.format("%s%d %s \"%s\" [%d, %d]\n", "  ".repeat(depth), id, ASTNode.nodeClassForType(type).getSimpleName(), label, pos, length));
        for(CASTNode child : children){
            sb.append(child.printTree(depth + 1));
        }
        return sb.toString();
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
}
