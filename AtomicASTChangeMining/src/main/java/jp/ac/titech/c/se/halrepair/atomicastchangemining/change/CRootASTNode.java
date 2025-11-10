package jp.ac.titech.c.se.halrepair.atomicastchangemining.change;

import org.eclipse.jdt.core.dom.ASTNode;

public class CRootASTNode extends CASTNode{
    private static final long serialVersionUID = 1L;

    private String rawText;

    public CRootASTNode(String rawText, ASTNode node){
        super(null, node);
        this.rawText = rawText;
    }

    public String getRawText(){
        return rawText;
    }
}
