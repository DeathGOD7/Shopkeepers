package com.nisovin.shopkeepers.config.lib.value.types;

import com.nisovin.shopkeepers.config.lib.value.ValueLoadException;
import com.nisovin.shopkeepers.config.lib.value.ValueParseException;
import com.nisovin.shopkeepers.config.lib.value.ValueType;
import com.nisovin.shopkeepers.util.java.ConversionUtils;

public class IntegerValue extends ValueType<Integer> {

	public static final IntegerValue INSTANCE = new IntegerValue();

	public IntegerValue() {
	}

	@Override
	public Integer load(Object configValue) throws ValueLoadException {
		if (configValue == null) return null;
		Integer value = ConversionUtils.toInteger(configValue);
		if (value == null) {
			throw new ValueLoadException("Invalid integer value: " + configValue);
		}
		return value;
	}

	@Override
	public Object save(Integer value) {
		return value;
	}

	@Override
	public Integer parse(String input) throws ValueParseException {
		Integer value = ConversionUtils.parseInt(input);
		if (value == null) {
			throw new ValueParseException("Invalid integer value: " + input);
		}
		return value;
	}
}
