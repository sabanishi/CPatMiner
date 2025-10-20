package jp.ac.titech.c.se.halrepair.atomicastchangemining.change;

import java.io.Serializable;
import java.util.List;

public class CRevision implements Serializable {
    private static final long serialVersionUID = 8519380253593912513L;

    long id;
    int numOfFiles = 0;
    List<CSourceFile> files;
    List<CMethod> methods;
}
