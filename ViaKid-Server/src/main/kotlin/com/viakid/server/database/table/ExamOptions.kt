package com.viakid.server.database.table

import org.jetbrains.exposed.sql.Table

object ExamOptions : Table("exam_options") {
    val id = varchar("id", 50)
    val questionId = reference("question_id", ExamQuestions.id)
    val optionKey = varchar("option_key", 10)
    val content = text("content")
    val isCorrect = bool("is_correct").default(false)
    val sort = integer("sort").default(0)

    override val primaryKey = PrimaryKey(id)
}
