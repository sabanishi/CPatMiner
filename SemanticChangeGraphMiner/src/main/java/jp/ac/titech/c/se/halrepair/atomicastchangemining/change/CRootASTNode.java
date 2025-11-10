package jp.ac.titech.c.se.halrepair.atomicastchangemining.change;

public class CRootASTNode extends CASTNode{
    private static final long serialVersionUID = 1L;

    private String rawText;

    public String getRawText(){
        return rawText;
    }

    public String toString(){
        return rawText + "\n" + super.toString();
    }
}
