package kr.ac.kaist.csrc.koala.gui.listener;

public interface NodeClickListener {
	void onNodeImageClicked();
	void onNodeDBTableClicked(String selecteDBTableName);
	void onNodeStructuredClicked();
	void onNodeEncImageClicked();
}
