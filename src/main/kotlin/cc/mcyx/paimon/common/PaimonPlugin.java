package cc.mcyx.paimon.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.MessageDigest;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

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

    static {
        //基础的 Kotlin 依赖
        libs.add(new LibInfo("https://repo1.maven.org/maven2/org/jetbrains/kotlin/kotlin-stdlib-common/1.9.10/kotlin-stdlib-common-1.9.10.jar"));

        libs.add(new LibInfo("https://repo1.maven.org/maven2/org/jetbrains/kotlin/kotlin-stdlib/1.9.10/kotlin-stdlib-1.9.10.jar"));


        //建立所有子文件夹
        if (libFolder.mkdirs()) {
            System.out.println("init libFolder");
        }
        try {
            //遍历素有需要加载的依赖
            libs.forEach((lib) -> {
                try {
                    //检查依赖是否存在且可用
                    if (!lib.check()) {
                        //下载依赖包
                        downloadJar(new URL(lib.getUrl()));
                        //日志提示
                        System.out.println("[Paimon] Downloading " + lib.url);
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

            //加载所有依赖
            for (File jar : (Objects.requireNonNull(libFolder.listFiles()))) {
                loadJar(jar);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * 请求简单的HTTP页面
     *
     * @param url 路径
     * @return 页面内容
     */
    public static String getHttpContent(String url) {

        try {
            HttpURLConnection urlConnection = (HttpURLConnection) new URL(url).openConnection();
            int ready;
            byte[] bytes = new byte[10240];
            StringBuilder stringBuilder = new StringBuilder();
            //获取页面内容
            while ((ready = urlConnection.getInputStream().read(bytes)) != -1) {
                stringBuilder.append(new String(bytes, 0, ready));
            }
            //返回页面内容
            return stringBuilder.toString();
        } catch (IOException e) {
            return "";
        }
    }

    /**
     * 加载Jar
     *
     * @param jarFile 加载的jar文件
     */
    public static void loadJar(File jarFile) throws Exception {
        Method addURL = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
        addURL.setAccessible(true);
        addURL.invoke(ClassLoader.getSystemClassLoader(), jarFile.toURI().toURL());
        addURL.setAccessible(false);
    }

    /**
     * 下载某个Jar路径到Paimon/lib
     *
     * @param url 下载URL
     * @throws IOException IO的异常
     */
    public static void downloadJar(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
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
     * 依赖信息类
     */
    static class LibInfo {
        //下载地址
        private final String url;
        //校验数据
        private final String md5;

        /**
         * 依赖信息类
         *
         * @param url 下载地址
         */
        public LibInfo(String url) {
            this.url = url;
            this.md5 = getHttpContent(this.url + ".md5");
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
         * @throws Exception 逃逸
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