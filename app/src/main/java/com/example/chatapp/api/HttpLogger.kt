package com.foundationPractice.api


import okhttp3.logging.HttpLoggingInterceptor

class HttpLogger : HttpLoggingInterceptor.Logger {
    private val mMessage = StringBuilder()
    override fun log(message: String) {
        var msg = message;
        if (msg.startsWith("--> POST")) {
            mMessage.setLength(0);
        }
        if ((msg.startsWith("{") && msg.endsWith("}")) || (msg.startsWith("[") && msg.endsWith("]"))
        ) {
            msg = formatJson(decodeUnicode(msg));
        }
        mMessage.append(msg);
        if (msg.startsWith("--> END") || msg.startsWith("<-- END")) {
//            Logger.d("Http Response", mMessage.toString());
        }
    }

    private fun formatJson(jsonStr: String?): String {
        if (null == jsonStr || "" == jsonStr) return ""
        val sb = java.lang.StringBuilder()
        var current = '\u0000'
        var indent = 0
        for (element in jsonStr) {
            current = element
            when (current) {
                '{', '[' -> {
                    sb.append(current)
                    indent++
                }
                '}', ']' -> {
                    indent--
                    sb.append(current)
                }
                ',' -> {
                    sb.append(current)
                }
                else -> sb.append(current)
            }
        }
        return sb.toString()
    }

    private fun decodeUnicode(theString: String): String? {
        var aChar: Char
        val len = theString.length
        val outBuffer = StringBuffer(len)
        var x = 0
        while (x < len) {
            aChar = theString[x++]
            if (aChar == '\\') {
                aChar = theString[x++]
                if (aChar == 'u') {
                    var value = 0
                    for (i in 0..3) {
                        aChar = theString[x++]
                        value = when (aChar) {
                            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> (value shl 4) + aChar.toInt() - '0'.toInt()
                            'a', 'b', 'c', 'd', 'e', 'f' -> (value shl 4) + 10 + aChar.toInt() - 'a'.toInt()
                            'A', 'B', 'C', 'D', 'E', 'F' -> (value shl 4) + 10 + aChar.toInt() - 'A'.toInt()
                            else -> throw IllegalArgumentException(
                                "Malformed   \\uxxxx   encoding.")
                        }
                    }
                    outBuffer.append(value.toChar())
                } else {
                    when (aChar) {
                        't' -> aChar = '\t'
                        'r' -> aChar = '\r'
                        'n' -> aChar = '\n'
                        'f' -> aChar = '\u000C'
                    }
                    outBuffer.append(aChar)
                }
            } else outBuffer.append(aChar)
        }
        return outBuffer.toString()
    }
}
