// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
@file:JvmName("DestructuringUtils")

package org.jetbrains.kotlin.idea.codeinsight.utils

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.symbols.KaNamedClassOrObjectSymbol
import org.jetbrains.kotlin.analysis.api.types.KaFlexibleType
import org.jetbrains.kotlin.analysis.api.types.KaClassType
import org.jetbrains.kotlin.analysis.api.types.KaType
import org.jetbrains.kotlin.analysis.api.types.KaTypeNullability
import org.jetbrains.kotlin.psi.KtDestructuringDeclaration
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtParameter

fun getParameterNames(declaration: KtDestructuringDeclaration): List<String>? {
    return analyze(declaration) {
        val type = getClassType(declaration) ?: return null
        getParameterNames(type)
    }
}

fun getParameterNames(expression: KtExpression): List<String>? {
    return analyze(expression) {
        val type = toNonErrorClassType(expression.expressionType) ?: return null
        getParameterNames(type)
    }
}

context(KaSession)
private fun getParameterNames(type: KaClassType): List<String>? {
    if (type.nullability != KaTypeNullability.NON_NULLABLE) return null
    val classSymbol = type.expandedSymbol

    return if (classSymbol is KaNamedClassOrObjectSymbol && classSymbol.isData) {
        val constructorSymbol = classSymbol.declaredMemberScope
            .constructors
            .find { it.isPrimary }
            ?: return null

        constructorSymbol.valueParameters.map { it.name.asString() }
    } else null
}

context(KaSession)
private fun getClassType(declaration: KtDestructuringDeclaration): KaClassType? {
    val initializer = declaration.initializer
    val type = if (initializer != null) {
        initializer.expressionType
    } else {
        val parentAsParameter = declaration.parent as? KtParameter
        parentAsParameter?.getParameterSymbol()?.returnType
    }
    return toNonErrorClassType(type)
}

private fun toNonErrorClassType(type: KaType?): KaClassType? {
    return when (type) {
        is KaClassType -> type
        is KaFlexibleType -> type.lowerBound as? KaClassType
        else -> null
    }
}