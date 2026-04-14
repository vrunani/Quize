package com.jfsd.quize.dto;

import java.math.BigDecimal;

/**
 * Body for PUT /answers/grade/{answerId}
 * Used by teacher to manually grade a subjective answer.
 */
public class GradeAnswerRequest {

    private BigDecimal marksAwarded;   // must be 0 ≤ value ≤ question.marks
    private String checkedBy;          // teacher's user id

    // ── getters & setters ─────────────────────────────────────
    public BigDecimal getMarksAwarded()                     { return marksAwarded; }
    public void       setMarksAwarded(BigDecimal marksAwarded) { this.marksAwarded = marksAwarded; }

    public String getCheckedBy()                { return checkedBy; }
    public void   setCheckedBy(String checkedBy) { this.checkedBy = checkedBy; }
}