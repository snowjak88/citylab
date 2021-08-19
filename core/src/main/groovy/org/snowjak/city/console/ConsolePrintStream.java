/**
 * 
 */
package org.snowjak.city.console;

import java.io.PrintStream;
import java.util.LinkedList;

/**
 * @author snowjak88
 *
 */
public class ConsolePrintStream extends PrintStream {
	
	private final Console console;
	private LinkedList<Object> values;
	private final StringBuffer buffer;
	
	public ConsolePrintStream(final Console console) {
		
		super(System.out);
		
		this.console = console;
		this.values = new LinkedList<>();
		this.buffer = new StringBuffer();
	}
	
	@Override
	public PrintStream append(char ch) {
		
		buffer.append(ch);
		return this;
	}
	
	@Override
	public PrintStream append(CharSequence sequence, int start, int end) {
		
		for (int i = start; i < end; i++)
			if (i < sequence.length())
				append(sequence.charAt(i));
			
		return this;
	}
	
	@Override
	public PrintStream append(CharSequence sequence) {
		
		for (int i = 0; i < sequence.length(); i++)
			append(sequence.charAt(i));
		
		return this;
	}
	
	@Override
	public void close() {
		
		if (buffer.length() > 0 || !values.isEmpty())
			flush();
	}
	
	@Override
	public void flush() {
		
		values.add(buffer.toString());
		buffer.setLength(0);
		
		console.print(values.toArray());
		values.clear();
	}
	
	@Override
	public void print(boolean value) {
		
		append(Boolean.toString(value));
		flush();
	}
	
	@Override
	public void print(char value) {
		
		append(value);
		flush();
	}
	
	@Override
	public void print(char[] value) {
		
		for (int i = 0; i < value.length; i++)
			append(value[i]);
		flush();
	}
	
	@Override
	public void print(double value) {
		
		append(Double.toString(value));
		flush();
	}
	
	@Override
	public void print(float value) {
		
		append(Float.toString(value));
		flush();
	}
	
	@Override
	public void print(int value) {
		
		append(Integer.toString(value));
		flush();
	}
	
	@Override
	public void print(long value) {
		
		append(Long.toString(value));
		flush();
	}
	
	@Override
	public void print(Object value) {
		
		values.add(buffer.toString());
		buffer.setLength(0);
		
		values.add(value);
		
		flush();
	}
	
	@Override
	public void print(String value) {
		
		append(value);
		flush();
	}
	
	@Override
	public void println() {
		
		append('\n');
		flush();
	}
	
	@Override
	public void println(boolean value) {
		
		append(Boolean.toString(value));
		println();
	}
	
	@Override
	public void println(char value) {
		
		append(value);
		println();
	}
	
	@Override
	public void println(char[] value) {
		
		for (char ch : value)
			append(ch);
		println();
	}
	
	@Override
	public void println(double value) {
		
		append(Double.toString(value));
		println();
	}
	
	@Override
	public void println(float value) {
		
		append(Float.toString(value));
		println();
	}
	
	@Override
	public void println(int value) {
		
		append(Integer.toString(value));
		println();
	}
	
	@Override
	public void println(long value) {
		
		append(Long.toString(value));
		println();
	}
	
	@Override
	public void println(Object value) {
		
		values.add(buffer.toString());
		buffer.setLength(0);
		
		values.add(value);
		
		println();
	}
	
	@Override
	public void println(String value) {
		
		append(value);
		println();
	}
}
