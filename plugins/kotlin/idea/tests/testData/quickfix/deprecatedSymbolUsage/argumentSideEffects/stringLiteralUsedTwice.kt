// "Replace with 'newFun(p, p)'" "true"

@Deprecated("", ReplaceWith("newFun(p, p)"))
fun oldFun(p: String) {
    newFun(p, p)
}

fun newFun(p1: String, p2: String){}

fun foo() {
    <caret>oldFun("x")
}

// FUS_QUICKFIX_NAME: org.jetbrains.kotlin.idea.quickfix.replaceWith.DeprecatedSymbolUsageFix
// FUS_K2_QUICKFIX_NAME: org.jetbrains.kotlin.idea.k2.codeinsight.fixes.replaceWith.DeprecatedSymbolUsageFix