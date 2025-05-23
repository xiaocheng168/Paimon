package cc.mcyx.paimon.common;

import cc.mcyx.paimon.common.plugin.Paimon;
import cc.mcyx.paimon.util.Metrics;
import sun.misc.Unsafe;

import java.io.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * PaimonPlugin 插件类
 * 请继承这个类编写！
 */
public class PaimonPlugin extends Paimon {
    //处理Paimon相关核心依赖
    public static File rootFolder = new File("Paimon");
    //依赖文件夹
    public static File libFolder = new File(rootFolder, "lib");
    //依赖文件列表
    public static List<LibInfo> libs = new LinkedList<>();

    public static PaimonPlugin paimonPlugin;

    /**
     * 加载Jar
     *
     * @param jarFile 加载的jar文件
     */
    public static void loadJar(File jarFile) throws Throwable {
        Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
        theUnsafe.setAccessible(true);
        Unsafe unSafe = (Unsafe) theUnsafe.get(null);

        ClassLoader classLoader = Paimon.class.getClassLoader();

        Field ucp = scanUcp(Paimon.class.getClassLoader().getClass());
        assert ucp != null;
        Object object = unSafe.getObject(classLoader, unSafe.objectFieldOffset(ucp));

        Field implLookup = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
        MethodHandles.Lookup lookup = (MethodHandles.Lookup) unSafe.getObject(unSafe.staticFieldBase(implLookup), unSafe.staticFieldOffset(implLookup));
        MethodHandle addURL = lookup.findVirtual(object.getClass(), "addURL", MethodType.methodType(void.class, URL.class));
        addURL.invoke(object, jarFile.toURI().toURL());
    }

    /**
     * 递归扫描加载器中的ucp
     *
     * @param classes ClassLoader或者派生配
     * @return ucp字段
     */
    protected static Field scanUcp(Class<?> classes) {
        try {
            return classes.getDeclaredField("ucp");
        } catch (Exception e) {
            Class<?> superclass = classes.getSuperclass();
            if (superclass != Object.class) {
                return scanUcp(superclass);
            } else return null;
        }
    }

    public static final String ALIYUN_MAVEN = "https://maven.aliyun.com/repository/public/";
    public static final String IRD_MAVEN = "https://nexus.iridiumdevelopment.net/repository/maven-releases/";
    public static final String CODEMC_MAVEN = "https://repo.codemc.io/repository/maven-public/";

    /**
     * 增加依赖
     *
     * @param path  Maven 文件路径
     * @param maven Maven 仓库
     */
    public static void addLibURL(String path, String maven) {
        libs.add(new LibInfo(maven + path));
    }

    static {
        try {
            System.out.println("LoadPaimonLibrary...");
            //基础的 Kotlin 依赖
            addLibURL("org/jetbrains/kotlin/kotlin-stdlib-common/1.9.10/kotlin-stdlib-common-1.9.10.jar", ALIYUN_MAVEN);
            addLibURL("org/jetbrains/kotlin/kotlin-stdlib/1.9.10/kotlin-stdlib-1.9.10.jar", ALIYUN_MAVEN);
            addLibURL("org/jetbrains/kotlin/kotlin-reflect/1.9.10/kotlin-reflect-1.9.10.jar", ALIYUN_MAVEN);
            addLibURL("cn/hutool/hutool-all/5.8.25/hutool-all-5.8.25.jar", ALIYUN_MAVEN);
            addLibURL("mysql/mysql-connector-java/8.0.28/mysql-connector-java-8.0.28.jar", ALIYUN_MAVEN);
            addLibURL("org/xerial/sqlite-jdbc/3.43.0.0/sqlite-jdbc-3.43.0.0.jar", ALIYUN_MAVEN);
            addLibURL("org/ktorm/ktorm-core/3.6.0/ktorm-core-3.6.0.jar", ALIYUN_MAVEN);
            addLibURL("com/google/zxing/core/3.3.3/core-3.3.3.jar", ALIYUN_MAVEN);
            addLibURL("com/iridium/IridiumColorAPI/1.0.9/IridiumColorAPI-1.0.9.jar", IRD_MAVEN);
            addLibURL("de/tr7zw/item-nbt-api-plugin/2.12.2/item-nbt-api-plugin-2.12.2.jar", CODEMC_MAVEN);

            //建立所有子文件夹
            if (libFolder.mkdirs()) {
                System.out.println("[Paimon] create libFolder ok!");
            }

            //遍历素有需要加载的依赖
            for (LibInfo lib : libs) {
                //检查依赖是否存在且可用
                if (!lib.check()) {

                    try {
                        //下载依赖包
                        downloadJar(new URL(lib.getUrl()));
                        downloadJar(new URL(lib.getUrl() + ".md5"));
                        //日志提示
                        System.out.println("[Paimon] Downloading " + lib.url);
                    } catch (Exception e) {
                        System.err.println("[Paimon] Downloading Error " + lib.url);
                    }

                }
            }

            //加载所有依赖
            for (File jar : (Objects.requireNonNull(libFolder.listFiles()))) {
                loadJar(jar);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    {
        paimonPlugin = this;
    }


    /**
     * 得到 PaimonPlugin 实例
     *
     * @return PaimonPlugin
     */
    public static Paimon getPaimonPlugin() {
        return getPlugin(PaimonPlugin.class);
    }

    /**
     * 获取插件名
     *
     * @return 插件名
     */

    public static String getThisPluginName() {
        return getPluginYmlConfig().getString("name");
    }

    /**
     * 获取插件Yaml配置
     *
     * @return 返回Bukkit Yaml配置处理类
     */
    public static org.bukkit.configuration.file.YamlConfiguration getPluginYmlConfig() {
        URL location = getPluginJarFile();
        try {
            JarFile jarFile = new JarFile(location.getFile());
            JarEntry pluginYml = jarFile.getJarEntry("plugin.yml");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(jarFile.getInputStream(pluginYml)));
            org.bukkit.configuration.file.YamlConfiguration yamlConfiguration = org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(bufferedReader);
            bufferedReader.close();
            return yamlConfiguration;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取插件jar文件
     *
     * @return jar路径
     */
    public static URL getPluginJarFile() {
        return PaimonPlugin.class.getProtectionDomain().getCodeSource().getLocation();
    }

    /**
     * 下载某个Jar路径到Paimon/lib
     *
     * @param url 下载URL
     * @throws IOException IO的异常
     */
    public static void downloadJar(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setConnectTimeout(3000);
        int ready;
        byte[] bytes = new byte[10240];
        //建立文件输出流
        FileOutputStream fileOutputStream = new FileOutputStream(new File(libFolder, urlToFileName(url)));
        //写出文件字节流
        while ((ready = urlConnection.getInputStream().read(bytes)) != -1) {
            fileOutputStream.write(bytes, 0, ready);
        }
        //关闭文件输出流
        fileOutputStream.close();
    }

    /**
     * URL路径取尾巴文件名
     *
     * @param url url 路径
     * @return 文件名
     */
    public static String urlToFileName(URL url) {
        //路径文件名
        String[] fileSplit = url.getFile().split("/");
        return fileSplit[fileSplit.length - 1];
    }

    /**
     * URL路径取尾巴文件名
     *
     * @param url url 路径
     * @return 文件名
     */
    public static String urlToFileName(String url) {
        //路径文件名
        String[] fileSplit = url.split("/");
        return fileSplit[fileSplit.length - 1];
    }

    /**
     * 服务端是否为 Forge 类型
     *
     * @return 是否为 Forge
     */
    public static boolean isForge() {
        try {
            Class.forName("net.minecraftforge.client.MinecraftForgeClient");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * bStats 统计API
     *
     * @param pluginId bStats Plugin Id
     * @return 返回BStatus统计Api
     */
    public Metrics callBStats(Integer pluginId) {
        return new Metrics(this, pluginId);
    }

    /**
     * 依赖信息类
     */
    public static class LibInfo {
        //下载地址
        private final String url;
        //校验数据
        private String md5 = "";

        /**
         * 依赖信息类
         *
         * @param url 下载地址
         */
        public LibInfo(String url) {
            this.url = url;
            byte[] bytes = new byte[32];
            try {
                FileInputStream fileInputStream = new FileInputStream(new File(libFolder, urlToFileName(this.url)) + ".md5");
                int readLine = fileInputStream.read(bytes);
                this.md5 = new String(bytes, 0, readLine);
                fileInputStream.close();
            } catch (Exception ignored) {
            }
        }

        public String getUrl() {
            return url;
        }

        public String getMd5() {
            return md5;
        }

        /**
         * 文件校验
         *
         * @return 文件是否正确
         */

        public boolean check() throws Exception {
            File file = new File(libFolder, urlToFileName(this.url));
            //验证文件是否存在 且 md5是否正确
            return (file.isFile() && calculateMD5(file.getAbsolutePath()).equals(this.getMd5()));
        }

        /**
         * @param filePath 文件路径
         * @return 返回计算出来的md5
         * @throws Exception 逃逸
         */
        public static String calculateMD5(String filePath) throws Exception {

            // 创建MessageDigest对象并指定使用MD5算法
            MessageDigest md = MessageDigest.getInstance("MD5");

            // 使用FileInputStream来读取文件内容
            FileInputStream fis = new FileInputStream(filePath);
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                md.update(buffer, 0, bytesRead);
            }
            fis.close();

            // 计算哈希值
            byte[] hashBytes = md.digest();

            // 将哈希值转换为十六进制字符串
            StringBuilder result = new StringBuilder();
            for (byte b : hashBytes) {
                result.append(String.format("%02x", b));
            }
            return result.toString();
        }

    }
}
