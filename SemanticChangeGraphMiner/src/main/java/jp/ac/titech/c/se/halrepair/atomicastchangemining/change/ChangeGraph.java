package jp.ac.titech.c.se.halrepair.atomicastchangemining.change;

import java.io.Serializable;
import java.util.HashSet;

public class ChangeGraph implements Serializable {
	private static final long serialVersionUID = -974502848659906533L;
	
	private HashSet<ChangeNode> nodes = new HashSet<>();
	private CASTNode beforeAST = null;
	private CASTNode afterAST = null;

	public HashSet<ChangeNode> getNodes() {
		return nodes;
	}
	public CASTNode getBeforeAST() {
		return beforeAST;
	}

	public CASTNode getAfterAST() {
		return afterAST;
	}
}
