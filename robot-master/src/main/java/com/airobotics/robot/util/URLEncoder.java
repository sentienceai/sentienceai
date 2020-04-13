package com.airobotics.robot.util;

public class URLEncoder {
	public static String encode(String s) {
		StringBuffer retVal = new StringBuffer();

		char[] chars = s.toCharArray();
		for (char c : chars) {
			switch (c) {
			case ' ':
				retVal.append('%');
				retVal.append('2');
				retVal.append('0');
				break;
			case '!':
				retVal.append('%');
				retVal.append('2');
				retVal.append('1');
				break;
			case '"':
				retVal.append('%');
				retVal.append('2');
				retVal.append('2');
				break;
			case '#':
				retVal.append('%');
				retVal.append('2');
				retVal.append('3');
				break;
			case '$':
				retVal.append('%');
				retVal.append('2');
				retVal.append('4');
				break;
			case '%':
				retVal.append('%');
				retVal.append('2');
				retVal.append('5');
				break;
			case '&':
				retVal.append('%');
				retVal.append('2');
				retVal.append('6');
				break;
			case '\'':
				retVal.append('%');
				retVal.append('2');
				retVal.append('7');
				break;
			case '(':
				retVal.append('%');
				retVal.append('2');
				retVal.append('8');
				break;
			case ')':
				retVal.append('%');
				retVal.append('2');
				retVal.append('9');
				break;
			case '*':
				retVal.append('%');
				retVal.append('2');
				retVal.append('A');
				break;
			case '+':
				retVal.append('%');
				retVal.append('2');
				retVal.append('B');
				break;
			case ',':
				retVal.append('%');
				retVal.append('2');
				retVal.append('C');
				break;
			case '-':
				retVal.append('%');
				retVal.append('2');
				retVal.append('D');
				break;
			case '/':
				retVal.append('%');
				retVal.append('2');
				retVal.append('F');
				break;
			case ':':
				retVal.append('%');
				retVal.append('3');
				retVal.append('A');
				break;
			case ';':
				retVal.append('%');
				retVal.append('3');
				retVal.append('B');
				break;
			case '<':
				retVal.append('%');
				retVal.append('3');
				retVal.append('C');
				break;
			case '=':
				retVal.append('%');
				retVal.append('3');
				retVal.append('D');
				break;
			case '>':
				retVal.append('%');
				retVal.append('3');
				retVal.append('E');
				break;
			case '?':
				retVal.append('%');
				retVal.append('3');
				retVal.append('F');
				break;
			case '@':
				retVal.append('%');
				retVal.append('4');
				retVal.append('0');
				break;
			case '[':
				retVal.append('%');
				retVal.append('5');
				retVal.append('B');
				break;
			case '\\':
				retVal.append('%');
				retVal.append('5');
				retVal.append('C');
				break;
			case ']':
				retVal.append('%');
				retVal.append('5');
				retVal.append('D');
				break;
			case '^':
				retVal.append('%');
				retVal.append('5');
				retVal.append('E');
				break;
			case '_':
				retVal.append('%');
				retVal.append('5');
				retVal.append('F');
				break;
			case '`':
				retVal.append('%');
				retVal.append('5');
				retVal.append('F');
				break;
			case '{':
				retVal.append('%');
				retVal.append('7');
				retVal.append('B');
				break;
			case '|':
				retVal.append('%');
				retVal.append('7');
				retVal.append('C');
				break;
			case '}':
				retVal.append('%');
				retVal.append('7');
				retVal.append('D');
				break;
			case '~':
				retVal.append('%');
				retVal.append('7');
				retVal.append('E');
				break;
			default:
				retVal.append(c);
				break;
			}
		}

		return retVal.toString();
	}
}
