package com.mumuzi.sutan.backend.agent;

import com.mumuzi.sutan.backend.rag.Citation;

import java.util.ArrayList;
import java.util.List;

/**
 * Agent 一次运行的完整结果：最终答案 + 全部步骤 + 聚合溯源引用。
 */
public class AgentResult {

    private String answer;
    private final List<AgentStep> steps = new ArrayList<>();
    private final List<Citation> citations = new ArrayList<>();
    private boolean grounded = true;
    private String groundNote;

    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }

    public List<AgentStep> getSteps() { return steps; }

    public List<Citation> getCitations() { return citations; }

    public void addCitations(List<Citation> c) {
        if (c != null) citations.addAll(c);
    }

    public boolean isGrounded() { return grounded; }
    public void setGrounded(boolean grounded) { this.grounded = grounded; }

    public String getGroundNote() { return groundNote; }
    public void setGroundNote(String groundNote) { this.groundNote = groundNote; }
}
