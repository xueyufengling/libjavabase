package javacomm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javallo.ObjectManipulator;
import javallo.Reflection;
import javallo.VmBase;

import java.util.Set;

/**
 * 序列化和反序列化的自动托管工具
 */
public class AutoSerialization {
	public static final long DEFAULT_AUTOSERIALIZATION_TIME = 100;
	private String serializable_map_filepath;
	/**
	 * 储存了自动序列化的所有对象以及其索引。索引是用户自行指定的字符串，用于区分不同的序列化对象
	 */
	private HashMap<String, Serializable> serializable_map;
	private boolean running_flag = false;
	private Thread auto_serialize;

	String buf_filepath_str;
	Path original_filepath;
	Path tmp_filepath;
	Path buf_filepath;

	private static HashMap<String, AutoSerialization> auto_serialization_entries = new HashMap<>();

	/**
	 * 构建自动序列化，传入序列化文件存放目录，将自动开始反序列化已有的文件，并隔段时间就序列化写入文件一次
	 * 
	 * @param serializable_datafolder      序列化对象的存放目录，为本地的目录
	 * @param serializable_map_filename    序列化储存的文件名称
	 * @param auto_serialize_time_interval 间隔多长时间自动序列化并写入文件，单位ms
	 */
	public AutoSerialization(String serializable_datafolder, String serializable_map_filename, long auto_serialize_time_interval) {
		if (serializable_map_filename == "" || serializable_map_filename == null)
			serializable_map_filename = "serializable_map";
		String serializable_map_filefolder = ((serializable_datafolder == null || serializable_datafolder == "") ? File.separator : File.separator + serializable_datafolder + File.separator);
		new File(serializable_map_filefolder).mkdirs();// 如果序列化对象储存的目录不存在则建立文件夹
		serializable_map_filepath = serializable_map_filefolder + serializable_map_filename;
		buf_filepath_str = serializable_map_filepath + "_buf";
		// 由于FileOutputStream不会自动创建多级目录，因此先手动创建
		original_filepath = Path.of(serializable_map_filepath);
		tmp_filepath = Path.of(serializable_map_filepath + "_tmp");
		buf_filepath = Path.of(buf_filepath_str);
		File original = original_filepath.toFile();
		try {
			original.createNewFile();
		} catch (IOException ex) {
		}
		deserializeAllObjects();
		auto_serialize = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					while (running_flag) {
						Thread.sleep(auto_serialize_time_interval);
						serializeWithDoublebuffer();
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
	}

	public static AutoSerialization boot(String entry_key, String serializable_datafolder, String serializable_map_filename, long auto_serialize_time_in_tick) {
		AutoSerialization as = new AutoSerialization(serializable_datafolder, serializable_map_filename, auto_serialize_time_in_tick);
		auto_serialization_entries.put(entry_key, as);
		as.launch();
		return as;
	}

	public static AutoSerialization boot(String entry_key, String serializable_datafolder, String serializable_map_filename) {
		return boot(entry_key, serializable_datafolder, serializable_map_filename, DEFAULT_AUTOSERIALIZATION_TIME);
	}

	public static AutoSerialization boot(String entry_key, String serializable_datafolder) {
		return boot(entry_key, serializable_datafolder, null);
	}

	public static AutoSerialization boot(String entry_key) {
		return boot(entry_key, "serializable_objects");
	}

	/**
	 * 开启序列化线程，将定时序列化。
	 */
	public void launch() {
		if (!running_flag) {
			running_flag = true;
			auto_serialize.start();
		}
	}

	/**
	 * 终止序列化线程
	 */
	public void terminate() {
		running_flag = false;
		serializeWithDoublebuffer();// 结束后再完整地序列化一次，防止序列化一半时突然结束导致文件不完整
	}

	/**
	 * 注册序列化对象，被注册以后才可以托管给自动序列化。如果一个类需要将其成员托管给自动序列化，则需要在其构造函数注册要托管的对象
	 * 
	 * @param ref_name 该对象的引用名称，要通过该名称才可以在反序列化后查找回来，名称与序列化后的文件名一致
	 * @param obj      要托管的对象
	 */
	public AutoSerialization pushObject(String ref_name, Serializable obj) {
		if (obj instanceof AutoSerializable as)
			as.onPush(ref_name);
		serializable_map.put(ref_name, obj);
		return this;
	}

	/**
	 * 获取给定引用名称对应的对象，如果不存在则返回null
	 * 
	 * @param ref_name 对象的引用名称
	 * @return
	 */
	public Serializable pullObject(String ref_name) {
		Serializable obj = serializable_map.get(ref_name);
		if (obj instanceof AutoSerializable as)
			as.onPull(ref_name);
		return obj;
	}

	/**
	 * 获取给定引用名称对应的对象，如果不存在则将给定的obj存入ref_name并返回该对象
	 * 
	 * @param ref_name 对象的引用名称
	 * @param obj      如果目标对象不存在则新加的对象
	 * @return 获取到的对象引用
	 */
	@SuppressWarnings("unchecked")
	public <T extends Serializable> T pullObject(String ref_name, T obj) {
		T deserialized_obj = (T) pullObject(ref_name);
		if (deserialized_obj == null) {// 如果该引用名称对应的对象不在序列化列表里，则将obj存入该引用名称
			pushObject(ref_name, obj);
			deserialized_obj = obj;
		}
		return deserialized_obj;
	}

	public static AutoSerialization pushObject(String plugin_name, String ref_name, Serializable obj) {
		return auto_serialization_entries.get(plugin_name).pushObject(ref_name, obj);
	}

	public static Serializable pullObject(String plugin_name, String ref_name) {
		return auto_serialization_entries.get(plugin_name).pullObject(ref_name);
	}

	public static <T extends Serializable> T pullObject(String plugin_name, String ref_name, T obj) {
		return auto_serialization_entries.get(plugin_name).pullObject(ref_name, obj);
	}

	/**
	 * 执行自动序列化操作，本次序列化后的对象将存入另一个缓冲中，直到写入完成才将会将该缓冲的文件名称改为实际的serializable_map文件（即缓冲交换），防止序列化过程中程序异常终止导致序列化对象丢失。
	 * 
	 * @return
	 */
	protected synchronized AutoSerialization serializeWithDoublebuffer() {
		serialize(serializable_map, buf_filepath_str);// 序列化所有对象
		try {
			// 双缓冲
			Files.move(original_filepath, tmp_filepath, StandardCopyOption.REPLACE_EXISTING);
			Files.move(buf_filepath, original_filepath, StandardCopyOption.REPLACE_EXISTING);
			Files.move(tmp_filepath, buf_filepath, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException ex) {
			System.err.println("AutoSerialization cannot swap buffer file for " + serializable_map_filepath);
			ex.printStackTrace();
		}
		return this;
	}

	/**
	 * 自动反序列化操作，如果储存的对象还实现了Listener接口，就给它注册监听器。这个对象的Listener将只能注册到启动该AutoSerialization的插件
	 * 
	 * @param file_path 序列化对象的文件地址
	 * @return 反序列化的对象
	 */
	@SuppressWarnings("unchecked")
	public AutoSerialization deserializeAllObjects() {
		serializable_map = (HashMap<String, Serializable>) deserialize(serializable_map_filepath);// 存在序列化后的serializable_map则加载它，否则新建
		if (serializable_map == null)
			serializable_map = new HashMap<>();
		else {
			Set<Entry<String, Serializable>> serializable_set = serializable_map.entrySet();
			for (Map.Entry<String, Serializable> serializable_entry : serializable_set) {
				doDeserializeOperationWithMemberFieldRecursively(serializable_entry.getKey(), serializable_entry.getValue());
			}
		}
		return this;
	}

	protected static void doDeserializeOperationWithMemberFieldRecursively(String ref_name, Object obj) {
		if (obj == null || VmBase.isPrimitiveBoxingType(obj))
			return;
		if (obj instanceof AutoSerializable as)
			as.onDeserialize(ref_name);
		Field[] fields = Reflection.getDeclaredFields(obj.getClass());
		for (Field field : fields)
			try {
				Object field_obj = ObjectManipulator.removeAccessCheck(field).get(obj);
				if ((obj != field_obj) && (!Modifier.isStatic(field.getModifiers())))
					doDeserializeOperationWithMemberFieldRecursively(ref_name, field_obj);
				ObjectManipulator.recoveryAccessCheck(field);
			} catch (IllegalArgumentException | IllegalAccessException ex) {
				System.err.println("Recursively do on-deserialize operation on " + field + " failed");
				ex.printStackTrace();
			}
	}

	/**
	 * 反序列化一个文件
	 * 
	 * @param file_path 序列化对象的文件地址
	 * @return 反序列化的对象
	 */
	public static Serializable deserialize(String file_path) {
		Serializable obj = null;
		try {
			FileInputStream serializable_file = new FileInputStream(file_path);
			ObjectInputStream serializable_s = new ObjectInputStream(serializable_file);
			obj = (Serializable) serializable_s.readObject();
			serializable_s.close();
			serializable_file.close();
		} catch (IOException | ClassNotFoundException ex) {
			System.err.println("Dserialize " + file_path + " failed");
			ex.printStackTrace();
			obj = null;
		}
		return obj;
	}

	/**
	 * 序列化一个对象
	 * 
	 * @param obj       要序列化的对象
	 * @param file_path 序列化后储存的地址
	 */
	public static void serialize(Serializable obj, String file_path) {
		try {
			FileOutputStream serializable_files = new FileOutputStream(file_path);
			ObjectOutputStream serializable_s = new ObjectOutputStream(serializable_files);
			serializable_s.writeObject(obj);
			serializable_s.close();
			serializable_files.close();
		} catch (IOException ex) {
			System.err.println("Serialize " + file_path + " failed");
			ex.printStackTrace();
		}
	}

}
