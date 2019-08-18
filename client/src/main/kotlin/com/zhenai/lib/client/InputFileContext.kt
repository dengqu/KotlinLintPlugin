package com.zhenai.lib.client


import org.sonar.api.batch.fs.InputFile
import org.sonar.api.batch.fs.TextRange
import org.sonar.api.batch.sensor.SensorContext
import org.sonar.api.rule.RuleKey
import com.zhenai.lib.checks.api.SecondaryLocation
import com.zhenai.lib.core.slang.visitors.TreeContext

class InputFileContext(val sensorContext: SensorContext?, val inputFile: InputFile?) : TreeContext() {

    fun textRange(textRange: com.zhenai.lib.core.slang.api.TextRange): TextRange {
        return inputFile!!.newRange(
            textRange.start().line(),
            textRange.start().lineOffset(),
            textRange.end().line(),
            textRange.end().lineOffset()
        )
    }

    fun reportIssue(
        ruleKey: RuleKey,
        textRange: com.zhenai.lib.core.slang.api.TextRange?,
        message: String,
        secondaryLocations: List<SecondaryLocation>,
        gap: Double?
    ) {
        val issue = sensorContext!!.newIssue()
        val issueLocation = issue.newLocation()
            .on(inputFile)
            .message(message)

        if (textRange != null) {
            issueLocation.at(textRange(textRange))
        }

        issue
            .forRule(ruleKey)
            .at(issueLocation)
            .gap(gap)

        secondaryLocations.forEach { secondary ->
            issue.addLocation(
                issue.newLocation()
                    .on(inputFile)
                    .at(textRange(secondary.textRange))
                    .message(if (secondary.message == null) "" else secondary.message)
            )
        }

        issue.save()
    }

    fun reportAnalysisError(message: String, location: com.zhenai.lib.core.slang.api.TextPointer?) {
        val error = sensorContext!!.newAnalysisError()
        error
            .message(message)
            .onFile(inputFile)

        if (location != null) {
            val pointerLocation = inputFile!!.newPointer(location.line(), location.lineOffset())
            error.at(pointerLocation)
        }

        error.save()
    }

    companion object {

        private val PARSING_ERROR_RULE_KEY = "ParsingError"
    }

}