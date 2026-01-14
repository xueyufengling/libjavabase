package javacomm;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;

import javallo.JarFiles;

/**
 * 通用的Yaml结构体，支持从文件系统、jar文件、流中解析。
 */
public class YamlStruct {
	protected ArrayList<LinkedHashMap<String, Object>> configs = new ArrayList<>();
	protected char delim = '.';// path分隔符，用于分割出yaml的多级结构的路径
	public char[] end_path_identifier = { '(', ')', '[', ']', '{', '}', ',', ';' };// 遇到这些字符时停止分隔命名空间，并且从上一个分隔符开始到path结尾均视作key

	/**
	 * 获取YamlLoader对象，该对象用于加载Yaml
	 * 
	 * @return
	 */
	public static Yaml getYamlLoader() {
		LoaderOptions yamlLoaderOptions = new LoaderOptions();
		yamlLoaderOptions.setMaxAliasesForCollections(Integer.MAX_VALUE);
		yamlLoaderOptions.setCodePointLimit(Integer.MAX_VALUE);// 设置Yaml文件的最大能解析的字符数
		yamlLoaderOptions.setNestingDepthLimit(Integer.MAX_VALUE);
		return new Yaml(yamlLoaderOptions);
	}

	/**
	 * 从jar文件中加载指定路径的yml文件
	 * 
	 * @param any_class_in_jar 要加载的jar中的任意一个class
	 * @param path             所要加载的yml文件在jar包中的路径
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static YamlStruct fromJarFile(Class<?> any_class_in_jar, String path) {
		YamlStruct struct = new YamlStruct();
		Yaml yaml = getYamlLoader();
		Iterable<LinkedHashMap> all_configs = (Iterable<LinkedHashMap>) (Object) yaml.loadAll(new String(JarFiles.getResourceAsBytes(any_class_in_jar, path)));
		for (LinkedHashMap map : all_configs)
			struct.configs.add(map);
		return struct;
	}

	/**
	 * 直接从文件系统中加载yml文件
	 * 
	 * @param path 文件系统中的yml文件路径
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static YamlStruct fromFile(String path) {
		YamlStruct struct = new YamlStruct();
		Yaml yaml = getYamlLoader();
		Iterable<LinkedHashMap> all_configs = null;
		try (FileInputStream stream = new FileInputStream(path)) {
			all_configs = (Iterable<LinkedHashMap>) (Object) yaml.loadAll(stream);
		} catch (IOException e) {
			System.err.println("Read yaml file " + path + " failed.");
			e.printStackTrace();
		}
		if (all_configs != null)
			for (LinkedHashMap map : all_configs)
				struct.configs.add(map);
		return struct;
	}

	/**
	 * 从流中加载yml文件
	 * 
	 * @param stream 流对象
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static YamlStruct fromStream(InputStream stream) {
		YamlStruct struct = new YamlStruct();
		Yaml yaml = getYamlLoader();
		Iterable<LinkedHashMap> all_configs = null;
		all_configs = (Iterable<LinkedHashMap>) (Object) yaml.loadAll(stream);
		for (LinkedHashMap map : all_configs)
			struct.configs.add(map);
		return struct;
	}

	/**
	 * 转换为字符串
	 */
	@Override
	public String toString() {
		return configs.toString();
	}

	/**
	 * 获取当前采用的yml结构路径分隔符
	 * 
	 * @return 当前采用的yml结构路径分隔符
	 */
	public char getDelim() {
		return delim;
	}

	/**
	 * 设置当前采用的yml结构路径分隔符
	 * 
	 * @param new_delim 当前采用的yml结构路径分隔符
	 * @return
	 */
	public YamlStruct setDelim(char new_delim) {
		delim = new_delim;
		return this;
	}

	/**
	 * 解析一个yaml结构路径并获取结果，结果是以字符串序列化的yml对象，它可能是一个值，也可能是一个数组或者map等。<br>
	 * 例如map中的key对应的值，它的结构路径就是map.key
	 * 
	 * @param key 结构路径
	 * @return
	 */
	public String[] parsePath(String key) {
		ArrayList<String> result = new ArrayList<>();
		int last_namespace_start_idx = 0;
		int end_idx = key.length();
		FIND_END_IDX: for (int i = 0; i < end_idx; ++i) {
			char ch = key.charAt(i);
			if (ch == delim) {
				result.add(key.substring(last_namespace_start_idx, i));
				last_namespace_start_idx = i + 1;
			} else
				for (int j = 0; j < end_path_identifier.length; ++j)
					if (ch == end_path_identifier[j]) {
						end_idx = i;
						break FIND_END_IDX;
					}
		}
		result.add(key.substring(last_namespace_start_idx));
		return result.toArray(new String[result.size()]);
	}

	/**
	 * 直接将结构路径的值视作对象返回
	 * 
	 * @param key        目标对象的结构路径
	 * @param config_idx 配置的索引，同一个yml文件中是可以有多个独立yml配置的，具体自行查阅yaml文档
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public Object getObject(String key, int config_idx) {
		Object value = null;
		String[] namespaces = parsePath(key);
		LinkedHashMap map = (LinkedHashMap) configs.get(config_idx);
		for (int i = 0; i < namespaces.length; ++i) {
			if (i != namespaces.length - 1)
				map = (LinkedHashMap) map.get(namespaces[i]);
			else
				value = map.get(namespaces[i]);
		}
		return value;
	}

	/**
	 * 直接将结构路径的值视作字符串返回
	 * 
	 * @param key        目标字符串的结构路径
	 * @param config_idx 配置的索引，同一个yml文件中是可以有多个独立yml配置的，具体自行查阅yaml文档
	 * @return
	 */
	public String getString(String key, int config_idx) {
		return getObject(key, config_idx).toString();
	}

	/**
	 * 返回第一个配置中的key指向的对象。绝大多数情况下一个yml文件就一个配置，所以config_idx直接设置为0
	 * 
	 * @param key 目标对象的结构路径
	 * @return
	 */
	public Object getObject(String key) {
		return getObject(key, 0);
	}

	/**
	 * 返回第一个配置中的key指向的字符串。绝大多数情况下一个yml文件就一个配置，所以config_idx直接设置为0
	 * 
	 * @param key 目标字符串的结构路径
	 * @return
	 */
	public String getString(String key) {
		return getString(key, 0);
	}

	/**
	 * 直接将结构路径的值视作数组返回
	 * 
	 * @param key        目标数组的结构路径
	 * @param config_idx 配置的索引，同一个yml文件中是可以有多个独立yml配置的，具体自行查阅yaml文档
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public ArrayList<Object> getArray(String key, int config_idx) {
		Object obj = getObject(key, config_idx);
		if (obj instanceof ArrayList list)
			return list;
		else {
			ArrayList<Object> list = new ArrayList<>();
			list.add(obj);
			return list;
		}
	}

	/**
	 * 返回第一个配置中的key指向的数组。绝大多数情况下一个yml文件就一个配置，所以config_idx直接设置为0
	 * 
	 * @param key 目标数组的结构路径
	 * @return
	 */
	public ArrayList<Object> getArray(String key) {
		return getArray(key, 0);
	}
}
