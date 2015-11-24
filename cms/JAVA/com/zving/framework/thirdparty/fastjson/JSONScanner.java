/*
 * Copyright 1999-2101 Alibaba Group.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.zving.framework.thirdparty.fastjson;

import static com.zving.framework.thirdparty.fastjson.JSONToken.COLON;
import static com.zving.framework.thirdparty.fastjson.JSONToken.COMMA;
import static com.zving.framework.thirdparty.fastjson.JSONToken.EOF;
import static com.zving.framework.thirdparty.fastjson.JSONToken.ERROR;
import static com.zving.framework.thirdparty.fastjson.JSONToken.LBRACE;
import static com.zving.framework.thirdparty.fastjson.JSONToken.LBRACKET;
import static com.zving.framework.thirdparty.fastjson.JSONToken.LITERAL_STRING;
import static com.zving.framework.thirdparty.fastjson.JSONToken.LPAREN;
import static com.zving.framework.thirdparty.fastjson.JSONToken.RBRACE;
import static com.zving.framework.thirdparty.fastjson.JSONToken.RBRACKET;
import static com.zving.framework.thirdparty.fastjson.JSONToken.RPAREN;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Calendar;

import com.zving.framework.json.JSONException;

//这个类，为了性能优化做了很多特别处理，一切都是为了性能！！！

/**
 * @author wenshao<szujobs@hotmail.com>
 */
public class JSONScanner implements JSONLexer {

	private final static int[] digits = new int['f' + 1];
	static {
		for (int i = '0'; i <= '9'; ++i) {
			digits[i] = i - '0';
		}

		for (int i = 'a'; i <= 'f'; ++i) {
			digits[i] = i - 'a' + 10;
		}
		for (int i = 'A'; i <= 'F'; ++i) {
			digits[i] = i - 'A' + 10;
		}
	}
	public final static byte EOI = 0x1A;
	private final char[] buf;

	private int bp;

	private final int buflen;

	private int eofPos;
	/**
	 * The current character.
	 */
	private char ch;

	/**
	 * The token's position, 0-based offset from beginning of text.
	 */
	private int pos;

	/**
	 * A character buffer for literals.
	 */
	private char[] sbuf;

	private int sp;

	/**
	 * number start position
	 */
	private int np;

	/**
	 * The token, set by nextToken().
	 */
	private int token;

	private Keywords keywods = Keywords.DEFAULT_KEYWORDS;

	private final static ThreadLocal<char[]> sbufRef = new ThreadLocal<char[]>();

	private int features = 0;

	private Calendar calendar = null;

	public JSONScanner(String input) {
		this(input, 0);
	}

	public JSONScanner(String input, int features) {
		this(input.toCharArray(), input.length(), features);
	}

	public JSONScanner(char[] input, int inputLength, int features) {
		this.features = features;

		sbuf = sbufRef.get(); // new char[1024];
		if (sbuf == null) {
			sbuf = new char[64];
			sbufRef.set(sbuf);
		}

		eofPos = inputLength;

		if (inputLength == input.length) {
			if (input.length > 0 && isWhitespace(input[input.length - 1])) {
				inputLength--;
			} else {
				char[] newInput = new char[inputLength + 1];
				System.arraycopy(input, 0, newInput, 0, input.length);
				input = newInput;
			}
		}
		buf = input;
		buflen = inputLength;
		buf[buflen] = EOI;
		bp = -1;

		ch = buf[++bp];
	}

	public final int getBufferPosition() {
		return bp;
	}

	@Override
	public boolean isBlankInput() {
		for (int i = 0; i < buflen; ++i) {
			if (!isWhitespace(buf[i])) {
				return false;
			}
		}

		return true;
	}

	public static final boolean isWhitespace(char ch) {
		// 专门调整了判断顺序
		return ch == ' ' || ch == '\n' || ch == '\r' || ch == '\t' || ch == '\f' || ch == '\b';
	}

	/**
	 * Report an error at the current token position using the provided arguments.
	 */
	private void lexError(String key, Object... args) {
		token = ERROR;
	}

	/**
	 * Return the current token, set by nextToken().
	 */
	@Override
	public final int token() {
		return token;
	}

	private static boolean[] whitespaceFlags = new boolean[256];

	@Override
	public final void skipWhitespace() {
		for (;;) {
			if (whitespaceFlags[ch]) {
				ch = buf[++bp];
				continue;
			} else {
				break;
			}
		}
	}

	@Override
	public final char getCurrent() {
		return ch;
	}

	@Override
	public final void incrementBufferPosition() {
		ch = buf[++bp];
	}

	@Override
	public final void resetStringPosition() {
		sp = 0;
	}

	@Override
	public void nextToken(int expect) {
		for (;;) {
			switch (expect) {
			case JSONToken.LBRACE:
				if (ch == '{') {
					token = JSONToken.LBRACE;
					ch = buf[++bp];
					return;
				}
				if (ch == '[') {
					token = JSONToken.LBRACKET;
					ch = buf[++bp];
					return;
				}
				break;
			case JSONToken.COMMA:
				if (ch == ',') {
					token = JSONToken.COMMA;
					ch = buf[++bp];
					return;
				}

				if (ch == '}') {
					token = JSONToken.RBRACE;
					ch = buf[++bp];
					return;
				}

				if (ch == ']') {
					token = JSONToken.RBRACKET;
					ch = buf[++bp];
					return;
				}

				if (ch == EOI) {
					token = JSONToken.EOF;
					return;
				}
				break;
			case JSONToken.LITERAL_INT:
				if (ch >= '0' && ch <= '9') {
					sp = 0;
					pos = bp;
					scanNumber();
					return;
				}

				if (ch == '"') {
					sp = 0;
					pos = bp;
					scanString();
					return;
				}

				if (ch == '[') {
					token = JSONToken.LBRACKET;
					ch = buf[++bp];
					return;
				}

				if (ch == '{') {
					token = JSONToken.LBRACE;
					ch = buf[++bp];
					return;
				}

				break;
			case JSONToken.LITERAL_STRING:
				if (ch == '"') {
					sp = 0;
					pos = bp;
					scanString();
					return;
				}

				if (ch >= '0' && ch <= '9') {
					sp = 0;
					pos = bp;
					scanNumber();
					return;
				}

				if (ch == '[') {
					token = JSONToken.LBRACKET;
					ch = buf[++bp];
					return;
				}

				if (ch == '{') {
					token = JSONToken.LBRACE;
					ch = buf[++bp];
					return;
				}
				break;
			case JSONToken.LBRACKET:
				if (ch == '[') {
					token = JSONToken.LBRACKET;
					ch = buf[++bp];
					return;
				}

				if (ch == '{') {
					token = JSONToken.LBRACE;
					ch = buf[++bp];
					return;
				}
				break;
			case JSONToken.RBRACKET:
				if (ch == ']') {
					token = JSONToken.RBRACKET;
					ch = buf[++bp];
					return;
				}
			case JSONToken.EOF:
				if (ch == EOI) {
					token = JSONToken.EOF;
					return;
				}
				break;
			default:
				break;
			}

			if (ch == ' ' || ch == '\n' || ch == '\r' || ch == '\t' || ch == '\f' || ch == '\b') {
				ch = buf[++bp];
				continue;
			}

			nextToken();
			break;
		}
	}

	@Override
	public final void nextToken() {
		sp = 0;

		for (;;) {
			pos = bp;

			if (ch == '"') {
				scanString();
				return;
			}

			if (ch == ',') {
				ch = buf[++bp];
				token = COMMA;
				return;
			}

			if (ch >= '0' && ch <= '9') {
				scanNumber();
				return;
			}

			if (ch == '-') {
				scanNumber();
				return;
			}

			switch (ch) {
			case '\'':
				if (!isEnabled(Feature.AllowSingleQuotes)) {
					throw new JSONException("Feature.AllowSingleQuotes is false");
				}
				scanStringSingleQuote();
				return;
			case ' ':
			case '\t':
			case '\b':
			case '\f':
			case '\n':
			case '\r':
				ch = buf[++bp];
				break;
			case 't': // true
				scanTrue();
				return;
			case 'f': // false
				scanFalse();
				return;
			case 'n': // new,null
				scanNullOrNew();
				return;
			case 'D': // Date
				scanIdent();
				return;
			case '(':
				ch = buf[++bp];
				token = LPAREN;
				return;
			case ')':
				ch = buf[++bp];
				token = RPAREN;
				return;
			case '[':
				ch = buf[++bp];
				token = LBRACKET;
				return;
			case ']':
				ch = buf[++bp];
				token = RBRACKET;
				return;
			case '{':
				ch = buf[++bp];
				token = LBRACE;
				return;
			case '}':
				ch = buf[++bp];
				token = RBRACE;
				return;
			case ':':
				ch = buf[++bp];
				token = COLON;
				return;
			default:
				if (bp == buflen || ch == EOI && bp + 1 == buflen) { // JLS
					if (token == EOF) {
						throw new JSONException("EOF error");
					}

					token = EOF;
					pos = bp = eofPos;
				} else {
					lexError("illegal.char", String.valueOf((int) ch));
					ch = buf[++bp];
				}

				return;
			}
		}

	}

	static {
		whitespaceFlags[' '] = true;
		whitespaceFlags['\n'] = true;
		whitespaceFlags['\r'] = true;
		whitespaceFlags['\t'] = true;
		whitespaceFlags['\f'] = true;
		whitespaceFlags['\b'] = true;
	}

	boolean hasSpecial;

	public final void scanStringSingleQuote() {
		np = bp;
		hasSpecial = false;
		char ch;
		for (;;) {
			ch = buf[++bp];

			if (ch == '\'') {
				break;
			}

			if (ch == EOI) {
				throw new JSONException("unclosed single-quote string");
			}

			if (ch == '\\') {
				if (!hasSpecial) {
					hasSpecial = true;

					if (sp > sbuf.length) {
						char[] newsbuf = new char[sp * 2];
						System.arraycopy(sbuf, 0, newsbuf, 0, sbuf.length);
						sbuf = newsbuf;
					}

					System.arraycopy(buf, np + 1, sbuf, 0, sp);
				}

				ch = buf[++bp];

				switch (ch) {
				case '"':
					putChar('"');
					break;
				case '\\':
					putChar('\\');
					break;
				case '/':
					putChar('/');
					break;
				case '\'':
					putChar('\'');
					break;
				case 'b':
					putChar('\b');
					break;
				case 'f':
				case 'F':
					putChar('\f');
					break;
				case 'n':
					putChar('\n');
					break;
				case 'r':
					putChar('\r');
					break;
				case 't':
					putChar('\t');
					break;
				case 'u':
					char c1 = ch = buf[++bp];
					char c2 = ch = buf[++bp];
					char c3 = ch = buf[++bp];
					char c4 = ch = buf[++bp];
					int val = Integer.parseInt(new String(new char[] { c1, c2, c3, c4 }), 16);
					putChar((char) val);
					break;
				default:
					this.ch = ch;
					throw new JSONException("unclosed single-quote string");
				}
				continue;
			}

			if (!hasSpecial) {
				sp++;
				continue;
			}

			if (sp == sbuf.length) {
				putChar(ch);
			} else {
				sbuf[sp++] = ch;
			}
		}

		token = LITERAL_STRING;
		this.ch = buf[++bp];
	}

	@Override
	public final void scanString() {
		np = bp;
		hasSpecial = false;
		char ch;
		for (;;) {
			ch = buf[++bp];

			if (ch == '\"') {
				break;
			}

			if (ch == '\\') {
				if (!hasSpecial) {
					hasSpecial = true;

					if (sp >= sbuf.length) {
						int newCapcity = sbuf.length * 2;
						if (sp > newCapcity) {
							newCapcity = sp;
						}
						char[] newsbuf = new char[newCapcity];
						System.arraycopy(sbuf, 0, newsbuf, 0, sbuf.length);
						sbuf = newsbuf;
					}

					System.arraycopy(buf, np + 1, sbuf, 0, sp);
				}

				ch = buf[++bp];

				switch (ch) {
				case '"':
					putChar('"');
					break;
				case '\\':
					putChar('\\');
					break;
				case '/':
					putChar('/');
					break;
				case 'b':
					putChar('\b');
					break;
				case 'f':
				case 'F':
					putChar('\f');
					break;
				case 'n':
					putChar('\n');
					break;
				case 'r':
					putChar('\r');
					break;
				case 't':
					putChar('\t');
					break;
				case 'x':
					char x1 = ch = buf[++bp];
					char x2 = ch = buf[++bp];

					int x_val = digits[x1] * 16 + digits[x2];
					char x_char = (char) x_val;
					putChar(x_char);
					break;
				case 'u':
					char u1 = ch = buf[++bp];
					char u2 = ch = buf[++bp];
					char u3 = ch = buf[++bp];
					char u4 = ch = buf[++bp];
					int val = Integer.parseInt(new String(new char[] { u1, u2, u3, u4 }), 16);
					putChar((char) val);
					break;
				default:
					this.ch = ch;
					throw new JSONException("unclosed string : " + ch);
				}
				continue;
			}

			if (!hasSpecial) {
				sp++;
				continue;
			}

			if (sp == sbuf.length) {
				putChar(ch);
			} else {
				sbuf[sp++] = ch;
			}
		}

		token = LITERAL_STRING;
		this.ch = buf[++bp];
	}

	@Override
	public final String scanSymbolUnQuoted(final SymbolTable symbolTable) {
		final boolean[] firstIdentifierFlags = CharTypes.firstIdentifierFlags;
		final char first = ch;

		final boolean firstFlag = ch >= firstIdentifierFlags.length || firstIdentifierFlags[first];
		if (!firstFlag) {
			throw new JSONException("illegal identifier : " + ch);
		}

		final boolean[] identifierFlags = CharTypes.identifierFlags;

		int hash = first;

		np = bp;
		sp = 1;
		char ch;
		for (;;) {
			ch = buf[++bp];

			if (ch < identifierFlags.length) {
				if (!identifierFlags[ch]) {
					break;
				}
			}

			hash = 31 * hash + ch;

			sp++;
			continue;
		}

		this.ch = buf[bp];
		token = JSONToken.IDENTIFIER;

		return symbolTable.addSymbol(buf, np, sp, hash);
	}

	@Override
	public final String scanSymbol(final SymbolTable symbolTable, final char quote) {
		int hash = 0;

		np = bp;
		sp = 0;
		boolean hasSpecial = false;
		char ch;
		for (;;) {
			ch = buf[++bp];

			if (ch == quote) {
				break;
			}

			if (ch == EOI) {
				throw new JSONException("unclosed.str");
			}

			if (ch == '\\') {
				if (!hasSpecial) {
					hasSpecial = true;

					if (sp >= sbuf.length) {
						int newCapcity = sbuf.length * 2;
						if (sp > newCapcity) {
							newCapcity = sp;
						}
						char[] newsbuf = new char[newCapcity];
						System.arraycopy(sbuf, 0, newsbuf, 0, sbuf.length);
						sbuf = newsbuf;
					}

					System.arraycopy(buf, np + 1, sbuf, 0, sp);
				}

				ch = buf[++bp];

				switch (ch) {
				case '"':
					hash = 31 * hash + '"';
					putChar('"');
					break;
				case '\\':
					hash = 31 * hash + '\\';
					putChar('\\');
					break;
				case '/':
					hash = 31 * hash + '/';
					putChar('/');
					break;
				case 'b':
					hash = 31 * hash + '\b';
					putChar('\b');
					break;
				case 'f':
				case 'F':
					hash = 31 * hash + '\f';
					putChar('\f');
					break;
				case 'n':
					hash = 31 * hash + '\n';
					putChar('\n');
					break;
				case 'r':
					hash = 31 * hash + '\r';
					putChar('\r');
					break;
				case 't':
					hash = 31 * hash + '\t';
					putChar('\t');
					break;
				case 'u':
					char c1 = ch = buf[++bp];
					char c2 = ch = buf[++bp];
					char c3 = ch = buf[++bp];
					char c4 = ch = buf[++bp];
					int val = Integer.parseInt(new String(new char[] { c1, c2, c3, c4 }), 16);
					hash = 31 * hash + val;
					putChar((char) val);
					break;
				default:
					this.ch = ch;
					throw new JSONException("unclosed.str.lit");
				}
				continue;
			}

			hash = 31 * hash + ch;

			if (!hasSpecial) {
				sp++;
				continue;
			}

			if (sp == sbuf.length) {
				putChar(ch);
			} else {
				sbuf[sp++] = ch;
			}
		}

		token = LITERAL_STRING;
		this.ch = buf[++bp];

		if (!hasSpecial) {
			return symbolTable.addSymbol(buf, np + 1, sp, hash);
		} else {
			return symbolTable.addSymbol(sbuf, 0, sp, hash);
		}
	}

	public void scanTrue() {
		if (buf[bp++] != 't') {
			throw new JSONException("error parse true");
		}
		if (buf[bp++] != 'r') {
			throw new JSONException("error parse true");
		}
		if (buf[bp++] != 'u') {
			throw new JSONException("error parse true");
		}
		if (buf[bp++] != 'e') {
			throw new JSONException("error parse true");
		}

		ch = buf[bp];

		if (ch == ' ' || ch == ',' || ch == '}' || ch == ']' || ch == '\n' || ch == '\r' || ch == '\t' || ch == EOI || ch == '\f'
				|| ch == '\b') {
			token = JSONToken.TRUE;
		} else {
			throw new JSONException("scan true error");
		}
	}

	public void scanNullOrNew() {
		if (buf[bp++] != 'n') {
			throw new JSONException("error parse null or new");
		}

		if (buf[bp] == 'u') {
			bp++;
			if (buf[bp++] != 'l') {
				throw new JSONException("error parse true");
			}
			if (buf[bp++] != 'l') {
				throw new JSONException("error parse true");
			}
			ch = buf[bp];

			if (ch == ' ' || ch == ',' || ch == '}' || ch == ']' || ch == '\n' || ch == '\r' || ch == '\t' || ch == EOI || ch == '\f'
					|| ch == '\b') {
				token = JSONToken.NULL;
			} else {
				throw new JSONException("scan true error");
			}
			return;
		}

		if (buf[bp] != 'e') {
			throw new JSONException("error parse e");
		}

		bp++;
		if (buf[bp++] != 'w') {
			throw new JSONException("error parse w");
		}
		ch = buf[bp];

		if (ch == ' ' || ch == ',' || ch == '}' || ch == ']' || ch == '\n' || ch == '\r' || ch == '\t' || ch == EOI || ch == '\f'
				|| ch == '\b') {
			token = JSONToken.NEW;
		} else {
			throw new JSONException("scan true error");
		}
	}

	public void scanFalse() {
		if (buf[bp++] != 'f') {
			throw new JSONException("error parse false");
		}
		if (buf[bp++] != 'a') {
			throw new JSONException("error parse false");
		}
		if (buf[bp++] != 'l') {
			throw new JSONException("error parse false");
		}
		if (buf[bp++] != 's') {
			throw new JSONException("error parse false");
		}
		if (buf[bp++] != 'e') {
			throw new JSONException("error parse false");
		}

		ch = buf[bp];

		if (ch == ' ' || ch == ',' || ch == '}' || ch == ']' || ch == '\n' || ch == '\r' || ch == '\t' || ch == EOI || ch == '\f'
				|| ch == '\b') {
			token = JSONToken.FALSE;
		} else {
			throw new JSONException("scan false error");
		}
	}

	public void scanIdent() {
		np = bp - 1;
		hasSpecial = false;

		for (;;) {
			sp++;

			ch = buf[++bp];
			if (Character.isLetterOrDigit(ch)) {
				continue;
			}

			String ident = stringVal();

			Integer tok = keywods.getKeyword(ident);
			if (tok != null) {
				token = tok;
			} else {
				token = JSONToken.IDENTIFIER;
			}
			return;
		}
	}

	@Override
	public void scanNumber() {
		np = bp;

		if (ch == '-') {
			sp++;
			ch = buf[++bp];
		}

		for (;;) {
			if (ch >= '0' && ch <= '9') {
				sp++;
			} else {
				break;
			}
			ch = buf[++bp];
		}

		boolean isDouble = false;

		if (ch == '.') {
			sp++;
			ch = buf[++bp];
			isDouble = true;

			for (;;) {
				if (ch >= '0' && ch <= '9') {
					sp++;
				} else {
					break;
				}
				ch = buf[++bp];
			}
		}

		if (ch == 'e' || ch == 'E') {
			sp++;
			ch = buf[++bp];

			if (ch == '+' || ch == '-') {
				sp++;
				ch = buf[++bp];
			}

			for (;;) {
				if (ch >= '0' && ch <= '9') {
					sp++;
				} else {
					break;
				}
				ch = buf[++bp];
			}

			isDouble = true;
		}

		if (isDouble) {
			token = JSONToken.LITERAL_FLOAT;
		} else {
			token = JSONToken.LITERAL_INT;
		}
	}

	/**
	 * Append a character to sbuf.
	 */
	private final void putChar(char ch) {
		if (sp == sbuf.length) {
			char[] newsbuf = new char[sbuf.length * 2];
			System.arraycopy(sbuf, 0, newsbuf, 0, sbuf.length);
			sbuf = newsbuf;
		}
		sbuf[sp++] = ch;
	}

	/**
	 * Return the current token's position: a 0-based offset from beginning of the raw input stream (before unicode
	 * translation)
	 */
	@Override
	public final int pos() {
		return pos;
	}

	/**
	 * The value of a literal token, recorded as a string. For integers, leading 0x and 'l' suffixes are suppressed.
	 */
	@Override
	public final String stringVal() {
		if (!hasSpecial) {
			return new String(buf, np + 1, sp);
		} else {
			return new String(sbuf, 0, sp);
		}
	}

	private static final long MULTMIN_RADIX_TEN = Long.MIN_VALUE / 10;

	private static final long N_MULTMAX_RADIX_TEN = -Long.MAX_VALUE / 10;

	@Override
	public Number integerValue() throws NumberFormatException {
		long result = 0;
		boolean negative = false;
		int i = np, max = np + sp;
		long limit;
		long multmin;
		int digit;

		if (buf[np] == '-') {
			negative = true;
			limit = Long.MIN_VALUE;
			i++;
		} else {
			limit = -Long.MAX_VALUE;
		}
		multmin = negative ? MULTMIN_RADIX_TEN : N_MULTMAX_RADIX_TEN;
		if (i < max) {
			digit = digits[buf[i++]];
			result = -digit;
		}
		while (i < max) {
			// Accumulating negatively avoids surprises near MAX_VALUE
			digit = digits[buf[i++]];
			if (result < multmin) {
				return new BigInteger(numberString());
			}
			result *= 10;
			if (result < limit + digit) {
				return new BigInteger(numberString());
			}
			result -= digit;
		}

		if (negative) {
			if (i > np + 1) {
				if (result >= Integer.MIN_VALUE) {
					return (int) result;
				}
				return result;
			} else { /* Only got "-" */
				throw new NumberFormatException(numberString());
			}
		} else {
			result = -result;
			if (result <= Integer.MAX_VALUE) {
				return (int) result;
			}
			return result;
		}
	}

	@Override
	public final String numberString() {
		return new String(buf, np, sp);
	}

	@Override
	public double doubleValue() {
		return Double.parseDouble(numberString());
	}

	@Override
	public BigDecimal decimalValue() {
		return new BigDecimal(buf, np, sp);
	}

	@Override
	public boolean isEnabled(Feature feature) {
		return Feature.isEnabled(features, feature);
	}

	public final int ISO8601_LEN_0 = "0000-00-00".length();

	public final int ISO8601_LEN_1 = "0000-00-00T00:00:00".length();

	public final int ISO8601_LEN_2 = "0000-00-00T00:00:00.000".length();

	@Override
	public boolean scanISO8601DateIfMatch() {
		int rest = buflen - bp;

		if (rest < ISO8601_LEN_0) {
			return false;
		}

		char y0 = buf[bp];
		char y1 = buf[bp + 1];
		char y2 = buf[bp + 2];
		char y3 = buf[bp + 3];
		if (y0 != '1' && y0 != '2') {
			return false;
		}
		if (y1 < '0' || y1 > '9') {
			return false;
		}
		if (y2 < '0' || y2 > '9') {
			return false;
		}
		if (y3 < '0' || y3 > '9') {
			return false;
		}

		if (buf[bp + 4] != '-') {
			return false;
		}

		char M0 = buf[bp + 5];
		char M1 = buf[bp + 6];
		if (M0 == '0') {
			if (M1 < '1' || M1 > '9') {
				return false;
			}
		} else if (M0 == '1') {
			if (M1 != '0' && M1 != '1' && M1 != '2') {
				return false;
			}
		} else {
			return false;
		}

		if (buf[bp + 7] != '-') {
			return false;
		}

		char d0 = buf[bp + 8];
		char d1 = buf[bp + 9];
		if (d0 == '0') {
			if (d1 < '1' || d1 > '9') {
				return false;
			}
		} else if (d0 == '1' || d0 == '2') {
			if (d1 < '0' || d1 > '9') {
				return false;
			}
		} else if (d0 == '3') {
			if (d1 != '0' && d1 != '1') {
				return false;
			}
		} else {
			return false;
		}

		calendar = Calendar.getInstance();
		int year = digits[y0] * 1000 + digits[y1] * 100 + digits[y2] * 10 + digits[y3];
		int month = digits[M0] * 10 + digits[M1] - 1;
		int day = digits[d0] * 10 + digits[d1];
		calendar.set(Calendar.YEAR, year);
		calendar.set(Calendar.MONTH, month);
		calendar.set(Calendar.DAY_OF_MONTH, day);

		char t = buf[bp + 10];
		if (t == 'T') {
			if (rest < ISO8601_LEN_1) {
				return false;
			}
		} else {
			calendar.set(Calendar.HOUR_OF_DAY, 0);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);

			ch = buf[bp += 10];

			token = JSONToken.LITERAL_ISO8601_DATE;
			return true;
		}

		char h0 = buf[bp + 11];
		char h1 = buf[bp + 12];
		if (h0 == '0') {
			if (h1 < '0' || h1 > '9') {
				return false;
			}
		} else if (h0 == '1') {
			if (h1 < '0' || h1 > '9') {
				return false;
			}
		} else if (h0 == '2') {
			if (h1 < '0' || h1 > '4') {
				return false;
			}
		} else {
			return false;
		}

		if (buf[bp + 13] != ':') {
			return false;
		}

		char m0 = buf[bp + 14];
		char m1 = buf[bp + 15];
		if (m0 >= '0' && m0 <= '5') {
			if (m1 < '0' || m1 > '9') {
				return false;
			}
		} else if (m0 == '6') {
			if (m1 != '0') {
				return false;
			}
		} else {
			return false;
		}

		if (buf[bp + 16] != ':') {
			return false;
		}

		char s0 = buf[bp + 17];
		char s1 = buf[bp + 18];
		if (s0 >= '0' && s0 <= '5') {
			if (s1 < '0' || s1 > '9') {
				return false;
			}
		} else if (s0 == '6') {
			if (s1 != '0') {
				return false;
			}
		} else {
			return false;
		}

		int hour = digits[h0] * 10 + digits[h1];
		int minute = digits[m0] * 10 + digits[m1];
		int seconds = digits[s0] * 10 + digits[s1];
		calendar.set(Calendar.HOUR_OF_DAY, hour);
		calendar.set(Calendar.MINUTE, minute);
		calendar.set(Calendar.SECOND, seconds);

		char dot = buf[bp + 19];
		if (dot == '.') {
			if (rest < ISO8601_LEN_2) {
				return false;
			}
		} else {
			calendar.set(Calendar.MILLISECOND, 0);

			ch = buf[bp += 19];

			token = JSONToken.LITERAL_ISO8601_DATE;
			return true;
		}

		char S0 = buf[bp + 20];
		char S1 = buf[bp + 21];
		char S2 = buf[bp + 22];
		if (S0 < '0' || S0 > '9') {
			return false;
		}
		if (S1 < '0' || S1 > '9') {
			return false;
		}
		if (S2 < '0' || S2 > '9') {
			return false;
		}

		int millis = digits[S0] * 100 + digits[S1] * 10 + digits[S2];
		calendar.set(Calendar.MILLISECOND, millis);

		ch = buf[bp += 23];

		token = JSONToken.LITERAL_ISO8601_DATE;
		return true;
	}

	@Override
	public Calendar getCalendar() {
		return calendar;
	}

	@Override
	public boolean isEOF() {
		switch (token) {
		case JSONToken.EOF:
			return true;
		case JSONToken.ERROR:
			return false;
		case JSONToken.RBRACE:
			return false;
		default:
			return false;
		}
	}
}
