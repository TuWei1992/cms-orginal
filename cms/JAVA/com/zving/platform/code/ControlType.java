package com.zving.platform.code;

import com.zving.platform.FixedCodeType;

/**
 * @author 王育春
 * @email wyuch@zving.com
 * @date 2011-11-17
 */
public class ControlType extends FixedCodeType {
	public static final String Text = "Text";
	public static final String Password = "Password";
	public static final String Selector = "Select";
	public static final String TextArea = "TextArea";
	public static final String Radio = "Radio";
	public static final String Checkbox = "Checkbox";
	public static final String DateSelector = "Date";
	public static final String DateTimeSelector = "DateTime";
	public static final String TimeSelector = "Time";

	public ControlType() {
		super("ControlType", "@{Metadata.ControlType}", true, false);
		addFixedItem(ControlType.Checkbox, "@{Platform.CheckBox}", null);
		addFixedItem(ControlType.DateSelector, "@{Platform.DataSellection}", null);
		addFixedItem(ControlType.DateTimeSelector, "@{Platform.DateTimeSelection}", null);
		addFixedItem(ControlType.Radio, "@{Platform.Radio}", null);
		addFixedItem(ControlType.Selector, "@{Platform.Select}", null);
		addFixedItem(ControlType.Text, "@{Platform.Input}", null);
		addFixedItem(ControlType.Password, "@{Platform.PasswordInput}", null);
		addFixedItem(ControlType.TextArea, "@{Platform.Textarea}", null);
	}

	public boolean isText(String type) {
		return Text.equals(type);
	}

	public boolean isPassword(String type) {
		return Password.equals(type);
	}

	public boolean isSelector(String type) {
		return Selector.equals(type);
	}

	public boolean isTextArea(String type) {
		return TextArea.equals(type);
	}

	public boolean isRadio(String type) {
		return Radio.equals(type);
	}

	public boolean isCheckbox(String type) {
		return Checkbox.equals(type);
	}

	public boolean isDateSelector(String type) {
		return DateSelector.equals(type);
	}

	public boolean isDateTimeSelector(String type) {
		return DateTimeSelector.equals(type);
	}
}
