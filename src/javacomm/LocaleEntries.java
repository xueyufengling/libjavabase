package javacomm;

import java.util.HashMap;
import java.util.Map;

public class LocaleEntries {
	protected Map<String, Locale> locale_entries = new HashMap<>();// 储存了多个语言的配置

	/**
	 * 加载语言文件，如果已经加载则重新加载一次
	 * 
	 * @param entry_clazz
	 * @param locale_entry_key
	 * @param locale
	 */
	public void loadLocale(Class<?> entry_clazz, String locale_folder, String file_type, String locale) {
		Locale l;
		if (!locale_entries.containsKey(locale)) {
			l = new Locale(entry_clazz, locale_folder, file_type);
			locale_entries.put(locale, l);
		} else
			l = locale_entries.get(locale);
		l.loadLocale(locale);
	}

	public void loadLocale(Class<?> entry_clazz, String locale) {
		loadLocale(entry_clazz, Locale.default_locale_folder, Locale.default_file_type, locale);
	}

	/**
	 * 获取某个语言的配置对象
	 * 
	 * @param locale 要获取的语言的名称，必须和实际的文件名称保持一致，即实际语言配置文件名称要为locale+file_type
	 * @return
	 */
	public Locale getLocale(String locale) {
		if (!locale_entries.containsKey(locale))
			return null;
		return locale_entries.get(locale);
	}

	/**
	 * 根据指定的目标语言，根据locale得到本地化文本
	 * 
	 * @param locale 目标语言
	 * @param key    文本对应的key值
	 * @return 本地化文本
	 */
	public String getLocalizedValue(String locale, String key) {
		return locale_entries.get(locale).getLocalizedValue(key);
	}
}
