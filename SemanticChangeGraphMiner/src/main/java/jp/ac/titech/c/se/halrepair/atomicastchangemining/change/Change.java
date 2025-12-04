package jp.ac.titech.c.se.halrepair.atomicastchangemining.change;

public class Change {
    private int oldBegin;
    private int oldEnd;
    private String oldText;
    private String newText;

    public Change(int oldBegin, int oldEnd, String oldText, String newText){
        this.oldBegin = oldBegin;
        this.oldEnd = oldEnd;
        this.oldText = oldText;
        this.newText = newText;
    }

    public int getOldBegin(){
        return oldBegin;
    }

    public int getOldEnd(){
        return oldEnd;
    }

    public String getOldText(){
        return oldText;
    }

    public String getNewText(){
        return newText;
    }

    @Override
    public String toString(){
        return "$Change$(" + oldBegin + ", "+ oldEnd + ")\n" +
                "[" + oldText + "]\n" +
                "[" + newText + "]\n";
    }
}
