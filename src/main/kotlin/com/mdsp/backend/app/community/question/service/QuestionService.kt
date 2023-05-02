package com.mdsp.backend.app.community.question.service

import com.mathworks.engine.MatlabEngine
import com.mdsp.backend.app.community.Matlab
import com.mdsp.backend.app.community.question.model.Questions
import com.mdsp.backend.app.community.question.payload.Variable
import com.mdsp.backend.app.community.question.payload.Variant
import kotlin.collections.ArrayList
import kotlin.math.*
import kotlin.random.Random

class QuestionService {

    companion object {
        private fun randomVariable(variable: Variable) {
            val range: ArrayList<Long> = variable.getRange()

            if(range[1] < range[0]) {
                throw Exception("Variable condition incorrect.${variable.getName()}'s second range is less than first range")
            }
            when (variable.getType()) {
                "int" -> randomInt(variable)
                "float" -> randomFloat(variable)
            }
        }

        private fun randomInt(variable: Variable){
            val range: ArrayList<Long> = variable.getRange()
            val randomInt: Long = Random.nextLong(range[0], range[1] +1)
            variable.setResult(randomInt.toString())
            variable.setLatexFormatResult(randomInt.toString())
        }

        private fun randomFloat(variable: Variable){
            val range: ArrayList<Long> = variable.getRange()
            val del = 10.0.pow(variable.getDelimiter().toDouble()).toLong()
            val randomFloat: Double = Random.nextLong(range[0],range[1]+1) + round(Random.nextDouble() * del) / del
            variable.setResult(randomFloat.toString())
//            TODO need cast double to latex format with matlab
//            variable.setLatexFormatResult(randomFloat.toString())
        }


        fun generateVariables(variables: ArrayList<Variable>) {
//            TODO implement variable condition
            for (item in variables) {
                if (item.getName().isEmpty()
                        || item.getName() == "result") continue
                if (item.getIsAssign()) { //assign
                    var assignText = item.getAssignText()
                    when (item.getType()) {
                        "function" -> {
                            assignText = reconstructStatement(assignText, variables)
                            item.setResult(solveExpression(assignText))
                            item.setLatexFormatResult(solveExpressionLF(assignText))
                        }
                    }
                }
                else { //random variable
                    when (item.getType()) {
                        "int", "float" -> {
                            randomVariable(item)
                        }
                    }
                }

            }
        }

        fun getResultAnswer(variables: ArrayList<Variable>): String {
            val result: Variable = variables.find { variable -> variable.getName() == "result" }!!
            val expression: String = reconstructStatement(result.getAssignText(), variables)
            val matlabEngine: MatlabEngine = Matlab.getMatlabEngine()
            if(result.getType() == "expression"){
                result.setResult(solveExpression(expression))
                result.setLatexFormatResult(solveExpressionLF(expression))
            }else{
//                TODO need implement correctly
                matlabEngine.eval("ans=string(solve(${expression}));")
                matlabEngine.eval("lat=string(latex(solve(${expression})));")
                result.setLatexFormatResult(matlabEngine.getVariable("lat"))
                result.setResult(matlabEngine.getVariable("ans"))
                return result.getResult()
            }
            return solveExpression(expression)
        }

        fun reconstructStatement(statement: String, variables: ArrayList<Variable>): String {
            var expression: String = statement
            for (variable in variables) {
                expression = expression.replace("{${variable.getName()}}", variable.getResult())
            }
            return expression
        }

        fun getAnswerVariants(variables: ArrayList<Variable>, customVariants: ArrayList<Variant>): ArrayList<Variant> {
            var answerVariants = arrayListOf<Variant>()
            for (variant in customVariants) {
                val text = reconstructStatement(variant.text!!, variables)
                val textRu = reconstructStatement(variant.textRu!!, variables)
                val textEn = reconstructStatement(variant.textEn!!, variables)
                variant.text = (generateVariant(text))
                variant.textRu = (generateVariant(textRu))
                variant.textEn = (generateVariant(textEn))
                answerVariants.add(variant)
            }
            return answerVariants
        }

        private fun generateVariant(variantText: String): String{
            var result: String = variantText
            val map: MutableMap<Int, String> = mutableMapOf()
            var l = 0; var r: Int; var index = 0
            while(l < result.length){
                if(result[l] == '~'){
                    r = l+1
                    while(r < result.length && result[r] != '~') r++
                    if(r < result.length){
                        val statement: String = result.substring(l+1, r)
                        map[index] = statement
                        result = result.replace("~${statement}~", "@${index}@")
                        index++
                    }
                }
                l++
            }
            for((key, item) in map) result = result.replace("@${key}@", solveExpressionLF(item))
            return result
        }

        private fun solveExpression(statement: String): String {
            val matlabEngine: MatlabEngine = Matlab.getMatlabEngine()
            matlabEngine.eval("answer=string(sym(${statement}));")
            return matlabEngine.getVariable("answer")
        }

//         Latex Format
        private fun solveExpressionLF(statement: String): String {
            val matlabEngine: MatlabEngine = Matlab.getMatlabEngine()
            matlabEngine.eval("latexFormat=string(latex(sym(${statement})));")
            return matlabEngine.getVariable("latexFormat")
        }

        fun generateQuestion(question: Questions): Questions {
            val variants = question.variants
            val variables = question.variables
            generateVariables(variables)
            getResultAnswer(variables)
            val answerVariants = getAnswerVariants(variables, variants)
            question.description = (reconstructStatement(question.description!!, variables))
            question.descriptionEn = (reconstructStatement(question.descriptionEn!!, variables))
            question.descriptionRu = (reconstructStatement(question.descriptionRu!!, variables))
            question.variants = (answerVariants)
            return question
        }
    }
}
