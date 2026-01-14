package javacomm;

import java.io.File;

import javallo.JavaLang;

/**
 * 专用于配置文件的Yaml结构体，优先加载文件系统的本地配置，没有才加载jar包内的默认配置
 */
public class YamlConfig {
	protected Class<?> entry_clazz;// jar包内的任意一个类，用于读取yml文件
	protected String config_path = null;// 要优先加载的本地yml配置文件
	protected YamlStruct yml_entries = null;

	public YamlConfig(Class<?> entry_clazz) {
		this.entry_clazz = entry_clazz;
	}

	public YamlConfig() {
		this(JavaLang.getOuterCallerClassAsParam());
	}

	/**
	 * 构造yml配置，如果config_path不为null则加载
	 * 
	 * @param entry_clazz
	 * @param config_path
	 */
	public YamlConfig(Class<?> entry_clazz, String config_path) {
		this.entry_clazz = entry_clazz;
		this.config_path = config_path;
		if (config_path != null)
			loadConfigFile(entry_clazz, config_path);
	}

	public YamlConfig(String config_path) {
		this(JavaLang.getOuterCallerClassAsParam(), config_path);
	}

	/**
	 * 加载配置文件，优先加载本地文件夹的配置文件，如果没有则加载jar包内置本地化文件jar:/config_path.yml
	 * 
	 * @return 返回是否加载成功
	 */
	public boolean loadConfigFile(Class<?> entry_clazz, String config_path) {
		boolean load_complete = false;
		if (config_path == null)
			return false;
		File locale_file = new File(config_path);
		// 本地目录下如果存在配置文件则直接加载
		if (locale_file.exists()) {
			try {
				yml_entries = YamlStruct.fromFile(config_path);
				load_complete = true;
			} catch (Exception ex) {
				System.err.println("Cannot load local config file " + config_path);
				ex.printStackTrace();
			}
		} else {
			try {
				if (entry_clazz != null) {// 指定了jar中的类
					yml_entries = YamlStruct.fromJarFile(entry_clazz, config_path);
					load_complete = true;
				} else
					System.err.println("No config file exists in local filesystem, please specify jar entry clazz for finding embeded config file " + config_path);
			} catch (Exception ex) {// jar包内置配置加载失败
				System.err.println("No config file exists in local filesystem. Cannot load embeded config file " + config_path + " in jar of entry class " + entry_clazz.getName());
				ex.printStackTrace();
			}
		}
		return load_complete;
	}

	/**
	 * 加载指定路径的配置文件
	 * 
	 * @param config_path
	 * @return
	 */
	public boolean loadConfigFile(String config_path) {
		return loadConfigFile(entry_clazz, config_path);
	}

	/**
	 * 加载默认的配置文件
	 * 
	 * @return
	 */
	public boolean loadConfigFile() {
		return loadConfigFile(entry_clazz, config_path);
	}

	/**
	 * 获取字符串值
	 * 
	 * @param key
	 * @return
	 */
	public String getString(String key) {
		return yml_entries.getString(key);
	}

	/**
	 * 重新加载配置文件
	 * 
	 * @return
	 */
	public boolean reload() {
		return loadConfigFile();
	}

	/**
	 * 获取加载的配置列表
	 * 
	 * @return
	 */
	public YamlStruct entries() {
		return yml_entries;
	}
}
