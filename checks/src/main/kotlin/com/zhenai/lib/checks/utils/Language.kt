package com.zhenai.lib.checks.utils

/**
 * This enum is used only to distinguish default values for rule parameters. This should be the sole exception in otherwise
 * language agnostic module
 */
enum class Language {
    KOTLIN, RUBY, SCALA, GO;


    companion object {

        val RUBY_NAMING_DEFAULT = "^(@{0,2}[\\da-z_]+[!?=]?)|([*+-/%=!><~]+)|(\\[]=?)$"

        // scala constant starts with upper-case
        val SCALA_NAMING_DEFAULT = "^[_a-zA-Z][a-zA-Z0-9]*$"

        // support function name suffix '_=', '_+', '_!', ... and operators '+', '-', ...
        val SCALA_FUNCTION_OR_OPERATOR_NAMING_DEFAULT = "^([a-z][a-zA-Z0-9]*+(_[^a-zA-Z0-9]++)?+|[^a-zA-Z0-9]++)$"

        val GO_NAMING_DEFAULT = "^(_|[a-zA-Z0-9]+)$"

        val GO_NESTED_STATEMENT_MAX_DEPTH = 4
        val GO_MATCH_CASES_DEFAULT_MAX = 6
        val GO_DEFAULT_MAXIMUM_LINE_LENGTH = 120
        val GO_DEFAULT_FILE_LINE_MAX = 750
        val GO_DEFAULT_MAXIMUM_FUNCTION_LENGTH = 120
    }
}
