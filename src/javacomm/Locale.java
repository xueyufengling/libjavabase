package javacomm;

import javallo.JavaLang;

/**
 * 语言系统支持，基于YamlConfig。语言文件仅内置于jar包中
 */
public class Locale extends YamlConfig {
	/**
	 * 语言文件存放路径，
	 */
	public String locale_folder = default_locale_folder;

	/**
	 * 语言文件的拓展名
	 */
	public String file_type = default_file_type;

	public static String default_locale_folder = "/assets/lang";
	public static String default_file_type = ".yml";

	/**
	 * 该语言的名称，必须和实际的文件名称保持一致，即实际语言配置文件名称要为locale+file_type
	 */
	protected String locale;

	public Locale(Class<?> entry_clazz, String locale_folder, String file_type, String locale) {
		super(entry_clazz);
		this.locale_folder = locale_folder;
		this.file_type = file_type;
		this.locale = locale;
		loadLocale(locale);
	}

	public Locale(Class<?> entry_clazz, String locale_folder, String file_type) {
		super(entry_clazz);
		this.locale_folder = locale_folder;
		this.file_type = file_type;
	}

	public Locale(Class<?> entry_clazz, String locale) {
		super(entry_clazz);
		this.locale = locale;
	}

	public Locale(Class<?> entry_clazz) {
		super(entry_clazz);
	}

	public Locale(String locale_folder, String file_type, String locale) {
		this(JavaLang.getOuterCallerClassAsParam(), locale_folder, file_type, locale);
	}

	public Locale(String locale_folder, String file_type) {
		this(JavaLang.getOuterCallerClassAsParam(), locale_folder, file_type);
	}

	public Locale(String locale) {
		this(JavaLang.getOuterCallerClassAsParam(), locale);
	}

	public Locale() {
		this(JavaLang.getOuterCallerClassAsParam());
	}

	/**
	 * 获取实际的语言文件完整路径
	 * 
	 * @return
	 */
	protected String getLocalePath() {
		return locale_folder + '/' + locale + file_type;
	}

	/**
	 * 加载语言文件
	 * 
	 * @param locale 要加载的语言的名称，必须和实际的文件名称保持一致，即实际语言配置文件名称要为locale+file_type
	 * @return
	 */
	public boolean loadLocale(String locale) {
		super.config_path = getLocalePath();
		return super.loadConfigFile(entry_clazz, config_path);
	}

	public String getLocale() {
		return locale;
	}

	/**
	 * 根据locale得到本地化文本，不存在则返回key值
	 * 
	 * @param key 文本对应的key值
	 * @return 本地化文本
	 */
	public String getLocalizedValue(String key) {
		String value = super.getString(key);
		return value == null ? key : value;
	}
}
