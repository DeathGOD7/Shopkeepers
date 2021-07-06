package com.nisovin.shopkeepers.config.lib.value.types;

import com.nisovin.shopkeepers.config.lib.value.ValueLoadException;
import com.nisovin.shopkeepers.config.lib.value.ValueParseException;
import com.nisovin.shopkeepers.config.lib.value.ValueType;
import com.nisovin.shopkeepers.util.java.Validate;

public class StringValue extends ValueType<String> {

	public static final StringValue INSTANCE = new StringValue();

	public StringValue() {
	}

	@Override
	public String load(Object configValue) throws ValueLoadException {
		if (configValue == null) return null;
		return configValue.toString();
	}

	@Override
	public Object save(String value) {
		return value;
	}

	@Override
	public String parse(String input) throws ValueParseException {
		Validate.notNull(input, "input is null");
		return input;
	}
}
